package gr.csc.fireresponse.model;

/** Whether a fire service resource can currently be assigned. */
public enum ResourceStatus {
    AVAILABLE("Available"),
    DEPLOYED("Deployed"),
    OUT_OF_SERVICE("Out of service");

    private final String label;

    ResourceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
