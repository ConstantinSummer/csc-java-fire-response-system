package gr.csc.fireresponse.model;

import gr.csc.fireresponse.util.Validate;
import java.time.LocalDateTime;

/** Records that one resource was assigned to one incident, and when it was released again. */
public class Assignment {

    private final Resource resource;
    private final LocalDateTime assignedAt;
    private LocalDateTime releasedAt;

    public Assignment(Resource resource, LocalDateTime assignedAt) {
        Validate.notNull(resource, "resource");
        Validate.notNull(assignedAt, "assignedAt");
        this.resource = resource;
        this.assignedAt = assignedAt;
    }

    public Resource getResource() {
        return resource;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public LocalDateTime getReleasedAt() {
        return releasedAt;
    }

    public boolean isActive() {
        return releasedAt == null;
    }

    /** Stamps the release time and returns the resource to the available pool. Releasing twice has no effect. */
    void release(LocalDateTime when) {
        if (releasedAt == null) {
            releasedAt = when;
            resource.release();
        }
    }
}
