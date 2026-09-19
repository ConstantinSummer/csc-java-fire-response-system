package gr.csc.fireresponse.model;

import gr.csc.fireresponse.util.Validate;

/** A large tanker that resupplies water. It is only worth sending to incidents of at least MODERATE severity. */
public class WaterTender extends FireVehicle {

    private final int tankLitres;

    public WaterTender(int id, String name, int tankLitres) {
        super(id, name, ResourceType.WATER_TENDER, 2);
        this.tankLitres = Validate.inRange(tankLitres, 5000, 30000, "tankLitres");
    }

    public int getTankLitres() {
        return tankLitres;
    }

    @Override
    public boolean canServe(Severity severity) {
        return severity.isAtLeast(Severity.MODERATE);
    }

    @Override
    public int suppressionPoints() {
        return tankLitres / 500;
    }

    @Override
    public String describe() {
        return tankLitres + " L bulk water supply";
    }
}
