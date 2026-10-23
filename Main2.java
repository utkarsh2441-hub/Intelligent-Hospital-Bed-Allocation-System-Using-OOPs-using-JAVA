import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Main2 {
    static HospitalSystem hospital = new HospitalSystem();
    private static final String[] DEPARTMENTS = {
        "General", "Cardiology", "Neurology", "Orthopedics", "Emergency"
    };

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main2::showDashboard);
    }

    private static void showDashboard() {
        JFrame frame = new JFrame("Intelligent Hospital Bed Allocation System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(620, 440);
        frame.setLocationRelativeTo(null);

        JLabel title = new JLabel("INTELLIGENT HOSPITAL BED ALLOCATION SYSTEM", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 19));
        JLabel note = new JLabel("Patient records and bed status are saved automatically.", SwingConstants.CENTER);

        JButton registerButton = new JButton("Register Patient");
        JButton viewPatientsButton = new JButton("View Patients");
        JButton viewBedsButton = new JButton("View Beds");
        JButton allocateButton = new JButton("Allocate Bed");
        JButton dischargeButton = new JButton("Discharge Patient / Release Bed");

        JPanel buttons = new JPanel(new GridLayout(5, 1, 10, 10));
        buttons.add(registerButton);
        buttons.add(viewPatientsButton);
        buttons.add(viewBedsButton);
        buttons.add(allocateButton);
        buttons.add(dischargeButton);

        JPanel top = new JPanel(new GridLayout(2, 1, 5, 5));
        top.add(title);
        top.add(note);
        JPanel content = new JPanel(new BorderLayout(15, 20));
        content.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        content.add(top, BorderLayout.NORTH);
        content.add(buttons, BorderLayout.CENTER);
        frame.setContentPane(content);

        registerButton.addActionListener(e -> registerPatient(frame));
        viewPatientsButton.addActionListener(e -> viewPatients(frame));
        viewBedsButton.addActionListener(e -> viewBeds(frame));
        allocateButton.addActionListener(e -> allocateBed(frame));
        dischargeButton.addActionListener(e -> dischargePatient(frame));
        frame.setVisible(true);
    }

    public static void registerPatient(JFrame parent) {
        JFrame form = new JFrame("Register Patient");
        form.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel(new GridLayout(8, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField id = new JTextField();
        JTextField name = new JTextField();
        JTextField age = new JTextField();
        JTextField disease = new JTextField();
        JComboBox<String> gender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        JComboBox<String> department = new JComboBox<>(DEPARTMENTS);
        JComboBox<String> priority = new JComboBox<>(new String[]{"High", "Medium", "Low"});
        JButton save = new JButton("Register Patient");

        panel.add(new JLabel("Patient ID:")); panel.add(id);
        panel.add(new JLabel("Patient Name:")); panel.add(name);
        panel.add(new JLabel("Age:")); panel.add(age);
        panel.add(new JLabel("Gender:")); panel.add(gender);
        panel.add(new JLabel("Disease:")); panel.add(disease);
        panel.add(new JLabel("Department:")); panel.add(department);
        panel.add(new JLabel("Priority:")); panel.add(priority);
        panel.add(new JLabel("")); panel.add(save);
        form.setContentPane(panel);

        save.addActionListener(e -> {
            try {
                if (id.getText().trim().isEmpty() || name.getText().trim().isEmpty()
                        || age.getText().trim().isEmpty() || disease.getText().trim().isEmpty()) {
                    showMessage(form, "Please fill all required fields.");
                    return;
                }
                int patientId = Integer.parseInt(id.getText().trim());
                int patientAge = Integer.parseInt(age.getText().trim());
                if (patientId <= 0) { showMessage(form, "Patient ID must be a positive number."); return; }
                if (patientAge <= 0 || patientAge > 120) { showMessage(form, "Please enter a valid age (1-120)."); return; }

                Patient patient = new Patient(patientId, name.getText().trim(), patientAge,
                        gender.getSelectedItem().toString(), disease.getText().trim(),
                        department.getSelectedItem().toString(), priority.getSelectedItem().toString());
                if (!hospital.addPatient(patient)) {
                    showMessage(form, "This Patient ID already exists. Please use a unique ID.");
                    return;
                }
                showMessage(form, "Patient registered successfully. Record saved permanently.");
                id.setText(""); name.setText(""); age.setText(""); disease.setText("");
            } catch (NumberFormatException ex) {
                showMessage(form, "Patient ID and Age must be whole numbers.");
            }
        });
        form.setSize(500, 390);
        form.setLocationRelativeTo(parent);
        form.setVisible(true);
    }

    public static void viewPatients(JFrame parent) {
        String[] columns = {"Patient ID", "Name", "Age", "Gender", "Disease", "Department", "Priority", "Bed / Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        for (Patient p : hospital.getPatients()) {
            model.addRow(new Object[]{p.getPatientId(), p.getName(), p.getAge(), p.getGender(),
                    p.getDisease(), p.getDepartment(), p.getPriority(), hospital.getPatientStatusText(p.getPatientId())});
        }
        showTable(parent, "Registered Patients", model, 1000, 350);
    }

    public static void viewBeds(JFrame parent) {
        String[] columns = {"Bed No.", "Department", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        for (Bed bed : hospital.getBeds()) {
            model.addRow(new Object[]{bed.getBedId(), bed.getDepartment(), hospital.getBedStatusText(bed)});
        }
        showTable(parent, "Hospital Bed Status", model, 650, 320);
    }

    private static void showTable(JFrame parent, String title, DefaultTableModel model, int width, int height) {
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(new JScrollPane(table));
        frame.setSize(width, height);
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
    }

    public static void allocateBed(JFrame parent) {
        JFrame frame = new JFrame("Allocate Hospital Bed");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JTextField id = new JTextField();
        JComboBox<String> department = new JComboBox<>(DEPARTMENTS);
        JButton allocateSelected = new JButton("Allocate to Patient ID");
        JButton allocatePriority = new JButton("Allocate Next Highest Priority");
        panel.add(new JLabel("Patient ID:")); panel.add(id);
        panel.add(new JLabel("Department:")); panel.add(department);
        panel.add(new JLabel("Selected patient:")); panel.add(allocateSelected);
        panel.add(new JLabel("Priority queue:")); panel.add(allocatePriority);
        frame.setContentPane(panel);

        allocateSelected.addActionListener(e -> {
            try {
                if (id.getText().trim().isEmpty()) { showMessage(frame, "Enter a registered Patient ID."); return; }
                int patientId = Integer.parseInt(id.getText().trim());
                String result = hospital.allocateBed(patientId, department.getSelectedItem().toString());
                showMessage(frame, result);
            } catch (NumberFormatException ex) {
                showMessage(frame, "Patient ID must be a number.");
            }
        });
        allocatePriority.addActionListener(e -> {
            String result = hospital.allocateNextPriorityPatient(department.getSelectedItem().toString());
            showMessage(frame, result);
        });
        frame.setSize(580, 230);
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
    }

    public static void dischargePatient(JFrame parent) {
        String value = JOptionPane.showInputDialog(parent, "Enter Patient ID to discharge:", "Discharge Patient", JOptionPane.QUESTION_MESSAGE);
        if (value == null) return;
        if (value.trim().isEmpty()) { showMessage(parent, "Patient ID cannot be empty."); return; }
        try {
            int patientId = Integer.parseInt(value.trim());
            showMessage(parent, hospital.dischargePatient(patientId));
        } catch (NumberFormatException ex) {
            showMessage(parent, "Patient ID must be a number.");
        }
    }

    private static void showMessage(java.awt.Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message,
                message.startsWith("SUCCESS:") ? "Success" : "Hospital System",
                message.startsWith("SUCCESS:") ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }
}
