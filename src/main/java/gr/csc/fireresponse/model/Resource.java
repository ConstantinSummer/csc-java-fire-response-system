package gr.csc.fireresponse.model;

import gr.csc.fireresponse.exception.InvalidAssignmentException;
import gr.csc.fireresponse.exception.InvalidResourceStateException;
import gr.csc.fireresponse.util.Validate;

/**
 * Anything that can be assigned to an incident: a vehicle or a response team.
 *
 * <p>This abstract class holds what every resource shares (identity, availability, deployment
 * history) and declares what every concrete resource must answer in its own way. Code that
 * works with a {@code Resource} reference does not need to know which subclass it holds:
 * that is polymorphism.</p>
 */
public abstract class Resource {

    private final int id;
    private final String name;
    private final ResourceType type;
    private ResourceStatus status = ResourceStatus.AVAILABLE;
    private int deploymentCount;

    protected Resource(int id, String name, ResourceType type) {
        this.id = Validate.inRange(id, 1, 9999, "id");
        this.name = Validate.notBlank(name, "name");
        Validate.notNull(type, "type");
        this.type = type;
    }

    /** Can this resource usefully serve an incident of the given severity? Each subclass decides. */
    public abstract boolean canServe(Severity severity);

    /** Suppression capability of this resource in "points" (an educational scale). */
    public abstract int suppressionPoints();

    /** One-line description of the resource's capability, shown in the console. */
    public abstract String describe();

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ResourceType getType() {
        return type;
    }

    public ResourceStatus getStatus() {
        return status;
    }

    public int getDeploymentCount() {
        return deploymentCount;
    }

    public boolean isAvailable() {
        return status == ResourceStatus.AVAILABLE;
    }

    /** Marks the resource as deployed. Only an available resource can be deployed. */
    public void deploy() throws InvalidAssignmentException {
        if (status != ResourceStatus.AVAILABLE) {
            throw new InvalidAssignmentException(name + " is not available (status: " + status.getLabel() + ")");
        }
        status = ResourceStatus.DEPLOYED;
        deploymentCount++;
    }

    /** Returns a deployed resource to the available pool. Other statuses are left unchanged. */
    public void release() {
        if (status == ResourceStatus.DEPLOYED) {
            status = ResourceStatus.AVAILABLE;
        }
    }

    public void markOutOfService() throws InvalidResourceStateException {
        if (status == ResourceStatus.DEPLOYED) {
            throw new InvalidResourceStateException(name + " is deployed and cannot be taken out of service");
        }
        status = ResourceStatus.OUT_OF_SERVICE;
    }

    public void returnToService() throws InvalidResourceStateException {
        if (status != ResourceStatus.OUT_OF_SERVICE) {
            throw new InvalidResourceStateException(name + " is not out of service");
        }
        status = ResourceStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return name;
    }
}
