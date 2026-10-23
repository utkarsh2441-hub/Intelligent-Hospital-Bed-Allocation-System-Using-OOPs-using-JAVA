import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HospitalSystem implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String DATA_FILE = "hospital_data.ser";

    private ArrayList<Patient> patients;
    private ArrayList<Bed> beds;
    private HashMap<Integer, String> allocations;

    public HospitalSystem() {
        patients = new ArrayList<>();
        beds = new ArrayList<>();
        allocations = new HashMap<>();

        if (!loadData()) {
            createInitialBeds();
            saveData();
        }
    }

    private void createInitialBeds() {
        beds.clear();
        beds.add(new Bed("B-101", "General", true));
        beds.add(new Bed("B-102", "Cardiology", true));
        beds.add(new Bed("B-103", "Neurology", false));
        beds.add(new Bed("B-104", "Emergency", true));
        beds.add(new Bed("B-105", "Orthopedics", false));
        beds.add(new Bed("B-106", "General", true));
    }

    private boolean loadData() {
        File file = new File(DATA_FILE);

        if (!file.exists()) {
            return false;
        }

        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(file))) {

            HospitalSystem saved =
                    (HospitalSystem) in.readObject();

            if (saved == null
                    || saved.patients == null
                    || saved.beds == null
                    || saved.allocations == null) {
                System.err.println("Saved data is incomplete.");
                return false;
            }

            patients = saved.patients;
            beds = saved.beds;
            allocations = saved.allocations;

            return true;

        } catch (IOException | ClassNotFoundException
                | ClassCastException ex) {

            System.err.println(
                    "Could not load saved data: " + ex.getMessage());
            return false;
        }
    }

    // Save into a temporary file first, then replace the data file.
    private boolean saveData() {
        Path target = Paths.get(DATA_FILE);
        Path temp = Paths.get(DATA_FILE + ".tmp");

        try {
            try (ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(temp.toFile()))) {

                out.writeObject(this);
                out.flush();
            }

            try {
                Files.move(
                        temp,
                        target,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );

            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(
                        temp,
                        target,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            return true;

        } catch (IOException ex) {
            System.err.println(
                    "Could not save hospital data: " + ex.getMessage());
            return false;

        } finally {
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ex) {
                System.err.println(
                        "Could not remove temporary file: "
                                + ex.getMessage());
            }
        }
    }

    public boolean addPatient(Patient patient) {
        if (patient == null
                || getPatientById(patient.getPatientId()) != null) {
            return false;
        }

        patients.add(patient);

        if (saveData()) {
            return true;
        }

        // Roll back if the record could not be saved.
        patients.remove(patient);
        return false;
    }

    public ArrayList<Patient> getPatients() {
        return new ArrayList<>(patients);
    }

    public ArrayList<Bed> getBeds() {
        return new ArrayList<>(beds);
    }

    public Patient getPatientById(int patientId) {
        for (Patient patient : patients) {
            if (patient.getPatientId() == patientId) {
                return patient;
            }
        }
        return null;
    }

    public Bed findAvailableBed(String department) {
        if (department == null) {
            return null;
        }

        for (Bed bed : beds) {
            if (bed.isAvailable()
                    && bed.getDepartment().equalsIgnoreCase(department)) {
                return bed;
            }
        }

        return null;
    }

    public String getAllocatedBedId(int patientId) {
        return allocations.get(patientId);
    }

    public boolean isPatientAllocated(int patientId) {
        return allocations.containsKey(patientId);
    }

    public String allocateBed(int patientId, String department) {
        Patient patient = getPatientById(patientId);

        if (patient == null) {
            return "Patient ID not found. Please register the patient first.";
        }

        if (isPatientAllocated(patientId)) {
            return "This patient already has bed "
                    + allocations.get(patientId) + ".";
        }

        if (department == null
                || !patient.getDepartment().equalsIgnoreCase(department)) {
            return "Department mismatch. This patient is registered under "
                    + patient.getDepartment() + ".";
        }

        Bed bed = findAvailableBed(department);

        if (bed == null) {
            return "No bed is currently available in "
                    + department + " department.";
        }

        bed.setAvailable(false);
        allocations.put(patientId, bed.getBedId());

        if (!saveData()) {
            // Undo allocation if saving fails.
            bed.setAvailable(true);
            allocations.remove(patientId);

            return "Allocation cancelled because data could not be saved.";
        }

        return "SUCCESS: Bed " + bed.getBedId()
                + " allocated to " + patient.getName()
                + " (Priority: " + patient.getPriority() + ").";
    }

    public String allocateNextPriorityPatient(String department) {
        if (department == null) {
            return "Please select a department.";
        }

        if (findAvailableBed(department) == null) {
            return "No bed is currently available in "
                    + department + " department.";
        }

        Patient best = null;

        for (Patient patient : patients) {
            if (!patient.getDepartment().equalsIgnoreCase(department)
                    || isPatientAllocated(patient.getPatientId())) {
                continue;
            }

            if (best == null
                    || priorityRank(patient.getPriority())
                    < priorityRank(best.getPriority())) {
                best = patient;
            }
        }

        if (best == null) {
            return "No waiting patient found in "
                    + department + " department.";
        }

        return allocateBed(best.getPatientId(), department);
    }

    private int priorityRank(String priority) {
        if (priority == null) {
            return 3;
        }

        switch (priority.trim().toLowerCase()) {
            case "high":
                return 1;
            case "medium":
                return 2;
            default:
                return 3;
        }
    }

    public String dischargePatient(int patientId) {
        String bedId = allocations.get(patientId);

        if (bedId == null) {
            return "No active bed allocation was found for Patient ID "
                    + patientId + ".";
        }

        for (Bed bed : beds) {
            if (bed.getBedId().equals(bedId)) {
                Patient patient = getPatientById(patientId);
                String name = patient == null
                        ? "Patient " + patientId
                        : patient.getName();

                bed.setAvailable(true);
                allocations.remove(patientId);

                if (!saveData()) {
                    // Restore the allocation if saving fails.
                    bed.setAvailable(false);
                    allocations.put(patientId, bedId);

                    return "Discharge update could not be saved. "
                            + "The allocation has been restored.";
                }

                return name + " discharged successfully. Bed "
                        + bedId + " is now Available.";
            }
        }

        return "The allocated bed record could not be found. "
                + "Please check the data file.";
    }

    public String getBedStatusText(Bed bed) {
        for (Map.Entry<Integer, String> entry
                : allocations.entrySet()) {

            if (entry.getValue().equals(bed.getBedId())) {
                Patient patient = getPatientById(entry.getKey());

                return "Occupied - "
                        + (patient == null
                        ? "Patient " + entry.getKey()
                        : patient.getName());
            }
        }

        return bed.isAvailable()
                ? "Available"
                : "Occupied (existing/demo)";
    }

    public String getPatientStatusText(int patientId) {
        String bedId = allocations.get(patientId);

        return bedId == null
                ? "Waiting / Not allocated"
                : "Allocated: " + bedId;
    }
}