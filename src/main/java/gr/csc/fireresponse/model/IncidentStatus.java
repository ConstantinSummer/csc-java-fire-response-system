package gr.csc.fireresponse.model;

/**
 * The life cycle of an incident, modelled as a small state machine.
 *
 * <pre>
 * REPORTED -> RESPONDING -> ACTIVE -> CONTAINED -> EXTINGUISHED
 *     |            |          ^          |
 *     +------------+-> FALSE_ALARM       +--(flare-up)--+
 * </pre>
 */
public enum IncidentStatus {
    REPORTED("Reported", false),
    RESPONDING("Responding", false),
    ACTIVE("Active", false),
    CONTAINED("Contained", false),
    EXTINGUISHED("Extinguished", true),
    FALSE_ALARM("False alarm", true);

    private final String label;
    private final boolean terminal;

    IncidentStatus(String label, boolean terminal) {
        this.label = label;
        this.terminal = terminal;
    }

    public String getLabel() {
        return label;
    }

    /** A terminal status can never be left: the incident is closed. */
    public boolean isTerminal() {
        return terminal;
    }

    /** The statuses that may directly follow this one. Terminal statuses have none. */
    public IncidentStatus[] allowedNext() {
        switch (this) {
            case REPORTED:
                return new IncidentStatus[] {RESPONDING, FALSE_ALARM};
            case RESPONDING:
                return new IncidentStatus[] {ACTIVE, FALSE_ALARM};
            case ACTIVE:
                return new IncidentStatus[] {CONTAINED};
            case CONTAINED:
                return new IncidentStatus[] {EXTINGUISHED, ACTIVE};
            default:
                return new IncidentStatus[0];
        }
    }

    public boolean canTransitionTo(IncidentStatus next) {
        IncidentStatus[] allowed = allowedNext();
        for (int i = 0; i < allowed.length; i++) {
            if (allowed[i] == next) {
                return true;
            }
        }
        return false;
    }
}
