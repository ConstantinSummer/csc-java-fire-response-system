package gr.csc.fireresponse.model;

/** Classification of resources, used for grouping in statistics and for searching. */
public enum ResourceType {
    WILDLAND_ENGINE("Wildland engine"),
    WATER_TENDER("Water tender"),
    COMMAND_VEHICLE("Command vehicle"),
    HELICOPTER("Helicopter"),
    WATER_BOMBER("Water bomber"),
    RESPONSE_TEAM("Response team");

    private final String label;

    ResourceType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
