package gr.csc.fireresponse.model;

import gr.csc.fireresponse.util.Validate;

/** A light-to-medium engine built for forest terrain. It can serve any severity. */
public class WildlandEngine extends FireVehicle {

    private final int tankLitres;
    private final boolean fourWheelDrive;

    public WildlandEngine(int id, String name, int tankLitres, boolean fourWheelDrive) {
        super(id, name, ResourceType.WILDLAND_ENGINE, 4);
        this.tankLitres = Validate.inRange(tankLitres, 500, 6000, "tankLitres");
        this.fourWheelDrive = fourWheelDrive;
    }

    public int getTankLitres() {
        return tankLitres;
    }

    public boolean isFourWheelDrive() {
        return fourWheelDrive;
    }

    @Override
    public boolean canServe(Severity severity) {
        return true;
    }

    @Override
    public int suppressionPoints() {
        int points = tankLitres / 100;
        if (fourWheelDrive) {
            points += 5;
        }
        return points;
    }

    @Override
    public String describe() {
        return tankLitres + " L tank" + (fourWheelDrive ? ", 4x4" : "");
    }
}
