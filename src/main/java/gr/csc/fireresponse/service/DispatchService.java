package gr.csc.fireresponse.service;

import gr.csc.fireresponse.exception.CapacityExceededException;
import gr.csc.fireresponse.exception.InvalidAssignmentException;
import gr.csc.fireresponse.exception.InvalidResourceStateException;
import gr.csc.fireresponse.exception.InvalidStatusTransitionException;
import gr.csc.fireresponse.exception.NotFoundException;
import gr.csc.fireresponse.model.Assignment;
import gr.csc.fireresponse.model.Incident;
import gr.csc.fireresponse.model.IncidentStatus;
import gr.csc.fireresponse.model.Location;
import gr.csc.fireresponse.model.Resource;
import gr.csc.fireresponse.model.Severity;
import gr.csc.fireresponse.util.Validate;
import java.time.Clock;
import java.time.LocalDateTime;

/**
 * The business rules that involve more than one object: reporting incidents, assigning resources
 * and changing incident status. The console never applies these rules itself; it only calls this class.
 *
 * <p>The {@link Clock} is injected so tests can use a fixed time and get predictable results.</p>
 */
public class DispatchService {

    private final IncidentRegistry incidents;
    private final FleetRegistry fleet;
    private final Clock clock;

    public DispatchService(IncidentRegistry incidents, FleetRegistry fleet, Clock clock) {
        Validate.notNull(incidents, "incidents");
        Validate.notNull(fleet, "fleet");
        Validate.notNull(clock, "clock");
        this.incidents = incidents;
        this.fleet = fleet;
        this.clock = clock;
    }

    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    // ---- reporting --------------------------------------------------------------

    public Incident reportIncident(String description, Location location, Severity severity, LocalDateTime reportedAt)
            throws CapacityExceededException {
        Incident incident = new Incident(incidents.nextId(), description, location, severity, reportedAt);
        incidents.add(incident);
        return incident;
    }

    /** Overload: the incident is reported "now" according to the service's clock. */
    public Incident reportIncident(String description, Location location, Severity severity)
            throws CapacityExceededException {
        return reportIncident(description, location, severity, now());
    }

    // ---- assignment -------------------------------------------------------------

    /**
     * Assigns a resource to an incident after checking every rule. Nothing changes unless all checks
     * pass: the resource is deployed only once we know the incident can accept it.
     */
    public Assignment assign(Incident incident, Resource resource, LocalDateTime when)
            throws InvalidAssignmentException, CapacityExceededException {
        Validate.notNull(incident, "incident");
        Validate.notNull(resource, "resource");
        Validate.notNull(when, "when");

        if (!incident.isActive()) {
            throw new InvalidAssignmentException("Incident " + incident.getId() + " is closed ("
                    + incident.getStatus().getLabel() + ") and cannot receive resources");
        }
        if (!resource.isAvailable()) {
            throw new InvalidAssignmentException(resource.getName() + " is not available (status: "
                    + resource.getStatus().getLabel() + ")");
        }
        if (!resource.canServe(incident.getSeverity())) {
            throw new InvalidAssignmentException(resource.getName() + " (" + resource.getType().getLabel()
                    + ") is not suitable for a " + incident.getSeverity().getLabel() + " incident");
        }
        if (incident.getAssignmentCount() == Incident.MAX_ASSIGNMENTS) {
            throw new CapacityExceededException("Incident " + incident.getId(), Incident.MAX_ASSIGNMENTS);
        }

        resource.deploy();
        Assignment assignment = new Assignment(resource, when);
        incident.addAssignment(assignment);
        return assignment;
    }

    /** Overload: assign "now" according to the service's clock. */
    public Assignment assign(Incident incident, Resource resource)
            throws InvalidAssignmentException, CapacityExceededException {
        return assign(incident, resource, now());
    }

    /** Overload: look up both objects by id first. */
    public Assignment assign(int incidentId, int resourceId)
            throws NotFoundException, InvalidAssignmentException, CapacityExceededException {
        Incident incident = incidents.findById(incidentId);
        Resource resource = fleet.findResourceById(resourceId);
        return assign(incident, resource);
    }

    // ---- status -----------------------------------------------------------------

    /**
     * Changes an incident's status. On top of the state machine, RESPONDING needs at least one assigned
     * resource. Reaching a terminal status releases every resource of the incident.
     */
    public void changeStatus(Incident incident, IncidentStatus next, LocalDateTime when)
            throws InvalidStatusTransitionException {
        Validate.notNull(incident, "incident");
        Validate.notNull(next, "next");
        Validate.notNull(when, "when");

        IncidentStatus current = incident.getStatus();
        if (next == IncidentStatus.RESPONDING && current.canTransitionTo(next)
                && incident.getActiveAssignmentCount() == 0) {
            throw new InvalidStatusTransitionException(current, next,
                    "assign at least one resource before the incident can be responded to");
        }
        incident.changeStatus(next, when);
        if (next.isTerminal()) {
            incident.releaseAllResources(when);
        }
    }

    /** Overload: change status "now" according to the service's clock. */
    public void changeStatus(Incident incident, IncidentStatus next) throws InvalidStatusTransitionException {
        changeStatus(incident, next, now());
    }

    // ---- resource availability ----------------------------------------------------

    public void setOutOfService(int resourceId) throws NotFoundException, InvalidResourceStateException {
        fleet.findResourceById(resourceId).markOutOfService();
    }

    public void returnToService(int resourceId) throws NotFoundException, InvalidResourceStateException {
        fleet.findResourceById(resourceId).returnToService();
    }
}
