import java.io.Serializable;

public class Patient implements Serializable {
    private static final long serialVersionUID = 1L;

    private int patientId;
    private String name;
    private int age;
    private String gender;
    private String disease;
    private String department;
    private String priority;

    public Patient(int patientId, String name, int age, String gender,
                   String disease, String department, String priority) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.disease = disease;
        this.department = department;
        this.priority = priority;
    }

    public int getPatientId() { return patientId; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getDisease() { return disease; }
    public String getDepartment() { return department; }
    public String getPriority() { return priority; }
}
