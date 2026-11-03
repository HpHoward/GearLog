package zm.gearlog.model;

public class Furniture extends Equipment {
    public Furniture(int id, String name) { super(id, name); }

    @Override public String getType() { return "Furniture"; }
    @Override public int getLoanDays() { return 5; }
    @Override public double getLateFeePerDay() { return 5.0; }
}
