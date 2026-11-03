package zm.gearlog.model;

public class SoundEquipment extends Equipment {
    public SoundEquipment(int id, String name) { super(id, name); }

    @Override public String getType() { return "Sound"; }
    @Override public int getLoanDays() { return 2; }
    @Override public double getLateFeePerDay() { return 20.0; }
}
