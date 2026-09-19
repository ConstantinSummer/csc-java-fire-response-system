package gr.csc.fireresponse.exception;

import gr.csc.fireresponse.model.IncidentStatus;

/** Thrown when an incident status change is not allowed by the status state machine or a dispatch rule. */
public class InvalidStatusTransitionException extends FireResponseException {

    private static final long serialVersionUID = 1L;

    private final IncidentStatus from;
    private final IncidentStatus to;

    public InvalidStatusTransitionException(IncidentStatus from, IncidentStatus to) {
        super("Cannot change status from " + from.getLabel() + " to " + to.getLabel());
        this.from = from;
        this.to = to;
    }

    // Overloaded constructor: same transition, but with an explanation of the violated rule.
    public InvalidStatusTransitionException(IncidentStatus from, IncidentStatus to, String reason) {
        super("Cannot change status from " + from.getLabel() + " to " + to.getLabel() + ": " + reason);
        this.from = from;
        this.to = to;
    }

    public IncidentStatus getFrom() {
        return from;
    }

    public IncidentStatus getTo() {
        return to;
    }
}
