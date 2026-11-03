package zm.gearlog.model;

public class Borrower {
    private final int id;
    private final String name;
    private final String phone;
    private final String group;

    public Borrower(int id, String name, String phone, String group) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Borrower name is required");
        }
        this.id = id;
        this.name = name.trim();
        this.phone = phone == null ? "" : phone.trim();
        this.group = group == null ? "" : group.trim();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getGroup() { return group; }
}
