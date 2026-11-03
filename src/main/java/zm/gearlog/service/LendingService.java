package zm.gearlog.service;

import zm.gearlog.exception.ItemAlreadyLentException;
import zm.gearlog.model.*;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

/** Holds all equipment, borrowers and loans, applies the rules, and saves to a file. */
public class LendingService {
    private final Map<Integer, Equipment> equipment = new LinkedHashMap<>();
    private final Map<Integer, Borrower> borrowers = new LinkedHashMap<>();
    private final List<Loan> loans = new ArrayList<>();
    private final Path file;
    private int nextEquipmentId = 1;
    private int nextBorrowerId = 1;
    private int nextLoanId = 1;

    public LendingService(Path file) {
        this.file = file;
        load();
    }

    // ---------- Equipment and borrowers ----------
    public Equipment addEquipment(String type, String name) {
        Equipment item = createEquipment(type, nextEquipmentId, name);
        equipment.put(item.getId(), item);
        nextEquipmentId++;
        save();
        return item;
    }

    public List<Equipment> getEquipment() {
        return new ArrayList<>(equipment.values());
    }

    public Borrower registerBorrower(String name, String phone, String group) {
        Borrower b = new Borrower(nextBorrowerId, name, phone, group);
        nextBorrowerId++;
        borrowers.put(b.getId(), b);
        save();
        return b;
    }

    public List<Borrower> getBorrowers() {
        return new ArrayList<>(borrowers.values());
    }

    // ---------- Loans ----------
    public Loan lendItem(int equipmentId, int borrowerId) {
        Equipment item = findEquipment(equipmentId);
        Borrower borrower = findBorrower(borrowerId);
        if (!item.isAvailable()) throw new ItemAlreadyLentException(item.getName());
        item.markLent();
        Loan loan = new Loan(nextLoanId++, item, borrower, LocalDate.now(), null);
        loans.add(loan);
        save();
        return loan;
    }

    public Loan returnItem(int loanId) {
        Loan loan = findLoan(loanId);
        if (loan.isReturned()) throw new IllegalStateException("This loan was already returned");
        loan.closeLoan(LocalDate.now());
        loan.getEquipment().markReturned();
        save();
        return loan;
    }

    public List<Loan> getLoans() {
        return new ArrayList<>(loans);
    }

    public List<Loan> getOverdue() {
        List<Loan> overdue = new ArrayList<>();
        for (Loan l : loans) {
            if (l.isOverdue()) overdue.add(l);
        }
        return overdue;
    }

    // ---------- Helpers ----------
    private Equipment createEquipment(String type, int id, String name) {
        if (type == null) throw new IllegalArgumentException("Equipment type is required");
        switch (type.trim().toLowerCase()) {
            case "sound":     return new SoundEquipment(id, name);
            case "furniture": return new Furniture(id, name);
            case "tent":      return new Tent(id, name);
            default: throw new IllegalArgumentException("Unknown equipment type: " + type);
        }
    }

    private Equipment findEquipment(int id) {
        Equipment e = equipment.get(id);
        if (e == null) throw new NoSuchElementException("Equipment " + id + " not found");
        return e;
    }

    private Borrower findBorrower(int id) {
        Borrower b = borrowers.get(id);
        if (b == null) throw new NoSuchElementException("Borrower " + id + " not found");
        return b;
    }

    private Loan findLoan(int id) {
        for (Loan l : loans) {
            if (l.getId() == id) return l;
        }
        throw new NoSuchElementException("Loan " + id + " not found");
    }

    // ---------- Saving (simple text file, one record per line) ----------
    // E|id|type|name|available
    // R|id|name|phone|group
    // L|id|equipmentId|borrowerId|dateOut|dateReturned (empty if still out)
    private void save() {
        List<String> lines = new ArrayList<>();
        for (Equipment e : equipment.values()) {
            lines.add("E|" + e.getId() + "|" + e.getType() + "|" + clean(e.getName()) + "|" + e.isAvailable());
        }
        for (Borrower b : borrowers.values()) {
            lines.add("R|" + b.getId() + "|" + clean(b.getName()) + "|" + clean(b.getPhone()) + "|" + clean(b.getGroup()));
        }
        for (Loan l : loans) {
            lines.add("L|" + l.getId() + "|" + l.getEquipment().getId() + "|" + l.getBorrower().getId()
                    + "|" + l.getDateOut() + "|" + (l.getDateReturned() == null ? "" : l.getDateReturned()));
        }
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            Files.write(file, lines);
        } catch (IOException e) {
            System.err.println("Could not save data: " + e.getMessage());
        }
    }

    private void load() {
        if (!Files.exists(file)) return;
        try {
            for (String line : Files.readAllLines(file)) {
                String[] p = line.split("\\|", -1);
                if (p[0].equals("E")) {
                    int id = Integer.parseInt(p[1]);
                    Equipment e = createEquipment(p[2], id, p[3]);
                    if (!Boolean.parseBoolean(p[4])) e.markLent();
                    equipment.put(id, e);
                    nextEquipmentId = Math.max(nextEquipmentId, id + 1);
                } else if (p[0].equals("R")) {
                    int id = Integer.parseInt(p[1]);
                    borrowers.put(id, new Borrower(id, p[2], p[3], p[4]));
                    nextBorrowerId = Math.max(nextBorrowerId, id + 1);
                } else if (p[0].equals("L")) {
                    int id = Integer.parseInt(p[1]);
                    LocalDate returned = p[5].isEmpty() ? null : LocalDate.parse(p[5]);
                    loans.add(new Loan(id, findEquipment(Integer.parseInt(p[2])),
                            findBorrower(Integer.parseInt(p[3])), LocalDate.parse(p[4]), returned));
                    nextLoanId = Math.max(nextLoanId, id + 1);
                }
            }
        } catch (IOException | RuntimeException e) {
            System.err.println("Could not read saved data: " + e.getMessage());
        }
    }

    private String clean(String s) {
        return s.replace("|", " ");
    }
}
