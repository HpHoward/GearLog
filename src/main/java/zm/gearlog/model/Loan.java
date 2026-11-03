package zm.gearlog.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** One borrowing of one item by one borrower. */
public class Loan {
    private final int id;
    private final Equipment equipment;
    private final Borrower borrower;
    private final LocalDate dateOut;
    private final LocalDate dueDate;
    private LocalDate dateReturned;   // null while the item is still out

    public Loan(int id, Equipment equipment, Borrower borrower, LocalDate dateOut, LocalDate dateReturned) {
        this.id = id;
        this.equipment = equipment;
        this.borrower = borrower;
        this.dateOut = dateOut;
        this.dueDate = equipment.getDueDate(dateOut);   // depends on the equipment type
        this.dateReturned = dateReturned;
    }

    public int getId() { return id; }
    public Equipment getEquipment() { return equipment; }
    public Borrower getBorrower() { return borrower; }

    // Dates are sent to the front end as plain text like 2026-11-10
    public String getDateOut() { return dateOut.toString(); }
    public String getDueDate() { return dueDate.toString(); }
    public String getDateReturned() { return dateReturned == null ? null : dateReturned.toString(); }

    public boolean isReturned() { return dateReturned != null; }

    public boolean isOverdue() { return daysLate() > 0 && !isReturned(); }

    public double getLateFee() { return equipment.calculateLateFee(daysLate()); }

    public void closeLoan(LocalDate date) { this.dateReturned = date; }

    /** Days past the due date, up to the return date (or today if still out). */
    private long daysLate() {
        LocalDate end = dateReturned != null ? dateReturned : LocalDate.now();
        return Math.max(0, ChronoUnit.DAYS.between(dueDate, end));
    }
}
