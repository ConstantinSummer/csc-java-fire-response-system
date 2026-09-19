package gr.csc.fireresponse.model;

import gr.csc.fireresponse.util.Validate;

/** A mobile command post for coordinating large operations: only useful for HIGH or CRITICAL incidents. */
public class CommandVehicle extends FireVehicle {

    private final int radioChannels;

    public CommandVehicle(int id, String name, int radioChannels) {
        super(id, name, ResourceType.COMMAND_VEHICLE, 5);
        this.radioChannels = Validate.inRange(radioChannels, 1, 8, "radioChannels");
    }

    public int getRadioChannels() {
        return radioChannels;
    }

    @Override
    public boolean canServe(Severity severity) {
        return severity.isAtLeast(Severity.HIGH);
    }

    @Override
    public int suppressionPoints() {
        return radioChannels * 3;
    }

    @Override
    public String describe() {
        return radioChannels + " radio channels, coordination";
    }
}
