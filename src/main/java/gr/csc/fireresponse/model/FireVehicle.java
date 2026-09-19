package gr.csc.fireresponse.model;

import gr.csc.fireresponse.util.Validate;

/**
 * Common base for all vehicles. It adds what only vehicles have (crew seats) on top of {@link Resource};
 * the concrete vehicle classes add their own specialised data and rules.
 */
public abstract class FireVehicle extends Resource {

    private final int crewSeats;

    protected FireVehicle(int id, String name, ResourceType type, int crewSeats) {
        super(id, name, type);
        this.crewSeats = Validate.inRange(crewSeats, 1, 12, "crewSeats");
    }

    public int getCrewSeats() {
        return crewSeats;
    }
}
