import java.io.Serializable;

public class Bed implements Serializable {
    private static final long serialVersionUID = 1L;

    private String bedId;
    private String department;
    private boolean available;

    public Bed(String bedId, String department, boolean available) {
        this.bedId = bedId;
        this.department = department;
        this.available = available;
    }

    public String getBedId() { return bedId; }
    public String getDepartment() { return department; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
