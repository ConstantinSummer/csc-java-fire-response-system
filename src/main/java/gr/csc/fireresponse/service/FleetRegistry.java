package gr.csc.fireresponse.service;

import gr.csc.fireresponse.exception.CapacityExceededException;
import gr.csc.fireresponse.exception.NotFoundException;
import gr.csc.fireresponse.model.FireStation;
import gr.csc.fireresponse.model.Incident;
import gr.csc.fireresponse.model.Location;
import gr.csc.fireresponse.model.Resource;
import gr.csc.fireresponse.model.ResourceStatus;
import gr.csc.fireresponse.model.ResourceType;
import gr.csc.fireresponse.model.Severity;
import gr.csc.fireresponse.util.Validate;

/**
 * Keeps track of every station and, through the stations, every resource.
 * Resources live inside stations (an array of arrays), so most queries walk two levels.
 */
public class FleetRegistry {

    private final FireStation[] stations;
    private int stationCount;

    public FleetRegistry(int stationCapacity) {
        this.stations = new FireStation[Validate.inRange(stationCapacity, 1, 500, "stationCapacity")];
    }

    public void addStation(FireStation station) throws CapacityExceededException {
        Validate.notNull(station, "station");
        if (stationCount == stations.length) {
            throw new CapacityExceededException("Fleet registry", stations.length);
        }
        stations[stationCount] = station;
        stationCount++;
    }

    public int getStationCount() {
        return stationCount;
    }

    public FireStation getStation(int index) {
        Validate.inRange(index, 0, stationCount - 1, "index");
        return stations[index];
    }

    public int countResources() {
        int total = 0;
        for (int i = 0; i < stationCount; i++) {
            total += stations[i].getResourceCount();
        }
        return total;
    }

    /** The id for the next resource to register: one more than the highest id in use. */
    public int nextResourceId() {
        int highest = 0;
        Resource[] all = allResources();
        for (int i = 0; i < all.length; i++) {
            if (all[i].getId() > highest) {
                highest = all[i].getId();
            }
        }
        return highest + 1;
    }

    /** Flattens the stations' arrays into one exact-length array. */
    public Resource[] allResources() {
        Resource[] all = new Resource[countResources()];
        int next = 0;
        for (int s = 0; s < stationCount; s++) {
            for (int r = 0; r < stations[s].getResourceCount(); r++) {
                all[next] = stations[s].getResource(r);
                next++;
            }
        }
        return all;
    }

    public Resource[] resourcesWithStatus(ResourceStatus status) {
        Validate.notNull(status, "status");
        Resource[] all = allResources();
        int matches = 0;
        for (int i = 0; i < all.length; i++) {
            if (all[i].getStatus() == status) {
                matches++;
            }
        }
        Resource[] result = new Resource[matches];
        int next = 0;
        for (int i = 0; i < all.length; i++) {
            if (all[i].getStatus() == status) {
                result[next] = all[i];
                next++;
            }
        }
        return result;
    }

    public Resource findResourceById(int id) throws NotFoundException {
        for (int s = 0; s < stationCount; s++) {
            for (int r = 0; r < stations[s].getResourceCount(); r++) {
                Resource resource = stations[s].getResource(r);
                if (resource.getId() == id) {
                    return resource;
                }
            }
        }
        throw new NotFoundException("Resource", id);
    }

    /** Returns the station that owns the resource, or null if none does. */
    public FireStation findStationOf(Resource resource) {
        for (int s = 0; s < stationCount; s++) {
            for (int r = 0; r < stations[s].getResourceCount(); r++) {
                if (stations[s].getResource(r) == resource) {
                    return stations[s];
                }
            }
        }
        return null;
    }

    /** The available resource of the given type whose station is closest to the target, or null. */
    public Resource findNearestAvailable(Location target, ResourceType type) {
        return findNearest(target, type, null);
    }

    /**
     * Overloaded variant for an incident: the target is the incident's location and the resource
     * must also be suitable for the incident's severity.
     */
    public Resource findNearestAvailable(Incident incident, ResourceType type) {
        Validate.notNull(incident, "incident");
        return findNearest(incident.getLocation(), type, incident.getSeverity());
    }

    private Resource findNearest(Location target, ResourceType type, Severity severityOrNull) {
        Validate.notNull(target, "target");
        Validate.notNull(type, "type");
        Resource best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int s = 0; s < stationCount; s++) {
            double distance = stations[s].getLocation().distanceKmTo(target);
            if (distance >= bestDistance) {
                continue;
            }
            for (int r = 0; r < stations[s].getResourceCount(); r++) {
                Resource candidate = stations[s].getResource(r);
                boolean suitable = severityOrNull == null || candidate.canServe(severityOrNull);
                if (candidate.getType() == type && candidate.isAvailable() && suitable) {
                    best = candidate;
                    bestDistance = distance;
                    break;
                }
            }
        }
        return best;
    }
}
