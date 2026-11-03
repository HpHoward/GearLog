package zm.gearlog.model;

import java.time.LocalDate;

/** Anything the group can lend out. Each type decides its own loan period and late fee. */
public abstract class Equipment {
    private final int id;
    private final String name;
    private boolean available = true;

    protected Equipment(int id, String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Equipment name is required");
        }
        this.id = id;
        this.name = name.trim();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public boolean isAvailable() { return available; }

    public void markLent() { this.available = false; }
    public void markReturned() { this.available = true; }

    /** Each subclass answers differently: this is the polymorphism. */
    public abstract String getType();
    public abstract int getLoanDays();
    public abstract double getLateFeePerDay();

    public LocalDate getDueDate(LocalDate dateOut) {
        return dateOut.plusDays(getLoanDays());
    }

    public double calculateLateFee(long daysLate) {
        return daysLate <= 0 ? 0 : daysLate * getLateFeePerDay();
    }
}
