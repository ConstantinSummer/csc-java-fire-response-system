package gr.csc.fireresponse.model;

import gr.csc.fireresponse.exception.CapacityExceededException;
import gr.csc.fireresponse.exception.InvalidStatusTransitionException;
import gr.csc.fireresponse.util.Validate;
import java.time.LocalDateTime;

/**
 * A wildfire incident. It owns its assignments in a fixed-size array (capacity {@link #MAX_ASSIGNMENTS}).
 *
 * <p>Fields that never change after creation are {@code final}; only the status and the
 * assignments can change, and only through methods that check the rules (encapsulation).</p>
 */
public class Incident {

    public static final int MAX_ASSIGNMENTS = 8;

    private final int id;
    private final String description;
    private final Location location;
    private final Severity severity;
    private final LocalDateTime reportedAt;
    private IncidentStatus status = IncidentStatus.REPORTED;
    private LocalDateTime lastUpdated;
    private final Assignment[] assignments = new Assignment[MAX_ASSIGNMENTS];
    private int assignmentCount;

    public Incident(int id, String description, Location location, Severity severity, LocalDateTime reportedAt) {
        this.id = Validate.inRange(id, 1, 99999, "id");
        this.description = Validate.notBlank(description, "description");
        Validate.notNull(location, "location");
        Validate.notNull(severity, "severity");
        Validate.notNull(reportedAt, "reportedAt");
        this.location = location;
        this.severity = severity;
        this.reportedAt = reportedAt;
        this.lastUpdated = reportedAt;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Location getLocation() {
        return location;
    }

    public Region getRegion() {
        return location.getRegion();
    }

    public Severity getSeverity() {
        return severity;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    /** An incident is active until it reaches a terminal status. */
    public boolean isActive() {
        return !status.isTerminal();
    }

    /** Changes the status if the state machine allows it; otherwise throws and leaves the incident unchanged. */
    public void changeStatus(IncidentStatus next, LocalDateTime when) throws InvalidStatusTransitionException {
        Validate.notNull(next, "next");
        Validate.notNull(when, "when");
        if (!status.canTransitionTo(next)) {
            throw new InvalidStatusTransitionException(status, next);
        }
        status = next;
        lastUpdated = when;
    }

    public void addAssignment(Assignment assignment) throws CapacityExceededException {
        Validate.notNull(assignment, "assignment");
        if (assignmentCount == MAX_ASSIGNMENTS) {
            throw new CapacityExceededException("Incident " + id, MAX_ASSIGNMENTS);
        }
        assignments[assignmentCount] = assignment;
        assignmentCount++;
    }

    /** Total number of assignments ever made, including released ones. */
    public int getAssignmentCount() {
        return assignmentCount;
    }

    public Assignment getAssignment(int index) {
        Validate.inRange(index, 0, assignmentCount - 1, "index");
        return assignments[index];
    }

    public int getActiveAssignmentCount() {
        int count = 0;
        for (int i = 0; i < assignmentCount; i++) {
            if (assignments[i].isActive()) {
                count++;
            }
        }
        return count;
    }

    /** Linear search: is this resource currently assigned to this incident? */
    public boolean hasActiveResource(Resource resource) {
        for (int i = 0; i < assignmentCount; i++) {
            if (assignments[i].isActive() && assignments[i].getResource() == resource) {
                return true;
            }
        }
        return false;
    }

    /** Sum of the suppression points of every active assignment: a polymorphic call on each resource. */
    public int getAssignedPoints() {
        int points = 0;
        for (int i = 0; i < assignmentCount; i++) {
            if (assignments[i].isActive()) {
                points += assignments[i].getResource().suppressionPoints();
            }
        }
        return points;
    }

    public boolean isAdequatelyResourced() {
        return getAssignedPoints() >= severity.getRecommendedPoints();
    }

    /** Releases every active assignment, returning the resources to the available pool. */
    public void releaseAllResources(LocalDateTime when) {
        Validate.notNull(when, "when");
        for (int i = 0; i < assignmentCount; i++) {
            assignments[i].release(when);
        }
    }
}
