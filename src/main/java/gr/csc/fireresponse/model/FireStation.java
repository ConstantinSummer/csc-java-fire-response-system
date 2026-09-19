package gr.csc.fireresponse.model;

import gr.csc.fireresponse.exception.CapacityExceededException;
import gr.csc.fireresponse.util.Validate;

/**
 * A fire station or air base that owns resources. The resources are kept in a fixed-size array,
 * so a station has a hard capacity that {@link #addResource(Resource)} enforces.
 */
public class FireStation {

    private final int id;
    private final String name;
    private final Location location;
    private final Resource[] resources;
    private int resourceCount;

    public FireStation(int id, String name, Location location, int capacity) {
        this.id = Validate.inRange(id, 1, 999, "id");
        this.name = Validate.notBlank(name, "name");
        Validate.notNull(location, "location");
        this.location = location;
        this.resources = new Resource[Validate.inRange(capacity, 1, 50, "capacity")];
    }

    public void addResource(Resource resource) throws CapacityExceededException {
        Validate.notNull(resource, "resource");
        if (resourceCount == resources.length) {
            throw new CapacityExceededException("Station " + name, resources.length);
        }
        resources[resourceCount] = resource;
        resourceCount++;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public int getCapacity() {
        return resources.length;
    }

    public int getResourceCount() {
        return resourceCount;
    }

    public Resource getResource(int index) {
        Validate.inRange(index, 0, resourceCount - 1, "index");
        return resources[index];
    }

    public int countWithStatus(ResourceStatus status) {
        int count = 0;
        for (int i = 0; i < resourceCount; i++) {
            if (resources[i].getStatus() == status) {
                count++;
            }
        }
        return count;
    }

    @Override
    public String toString() {
        return name;
    }
}
