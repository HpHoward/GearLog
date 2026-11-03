package zm.gearlog.model;

public class Tent extends Equipment {
    public Tent(int id, String name) { super(id, name); }

    @Override public String getType() { return "Tent"; }
    @Override public int getLoanDays() { return 3; }
    @Override public double getLateFeePerDay() { return 30.0; }
}
