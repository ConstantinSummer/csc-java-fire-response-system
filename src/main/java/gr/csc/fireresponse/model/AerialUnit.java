package gr.csc.fireresponse.model;

import gr.csc.fireresponse.util.Validate;

/**
 * An aircraft that drops water. Two of the {@link ResourceType} values are aerial, so the
 * constructor validates the type it is given: one class, two behaviours chosen by that type.
 */
public class AerialUnit extends FireVehicle {

    private final int dropCapacityLitres;

    public AerialUnit(int id, String name, ResourceType type, int dropCapacityLitres) {
        super(id, name, requireAerial(type), 2);
        this.dropCapacityLitres = Validate.inRange(dropCapacityLitres, 500, 12000, "dropCapacityLitres");
    }

    private static ResourceType requireAerial(ResourceType type) {
        Validate.notNull(type, "type");
        if (type != ResourceType.HELICOPTER && type != ResourceType.WATER_BOMBER) {
            throw new IllegalArgumentException("type must be HELICOPTER or WATER_BOMBER but was " + type);
        }
        return type;
    }

    public int getDropCapacityLitres() {
        return dropCapacityLitres;
    }

    /** Helicopters serve MODERATE and above; water bombers are reserved for HIGH and CRITICAL incidents. */
    @Override
    public boolean canServe(Severity severity) {
        if (getType() == ResourceType.WATER_BOMBER) {
            return severity.isAtLeast(Severity.HIGH);
        }
        return severity.isAtLeast(Severity.MODERATE);
    }

    @Override
    public int suppressionPoints() {
        return dropCapacityLitres / 100;
    }

    @Override
    public String describe() {
        return dropCapacityLitres + " L per drop";
    }
}
