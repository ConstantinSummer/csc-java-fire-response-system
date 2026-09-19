package gr.csc.fireresponse.service;

import gr.csc.fireresponse.model.FireStation;
import gr.csc.fireresponse.model.Incident;
import gr.csc.fireresponse.model.IncidentStatus;
import gr.csc.fireresponse.model.Region;
import gr.csc.fireresponse.model.Resource;
import gr.csc.fireresponse.model.ResourceStatus;
import gr.csc.fireresponse.model.ResourceType;
import gr.csc.fireresponse.model.Severity;
import gr.csc.fireresponse.util.Validate;

/**
 * Read-only calculations over the registries. Counts are stored in plain arrays indexed by the
 * enum's {@code ordinal()}, so {@code countBySeverity()[Severity.HIGH.ordinal()]} is the number of HIGH incidents.
 */
public class StatisticsService {

    private final IncidentRegistry incidents;
    private final FleetRegistry fleet;

    public StatisticsService(IncidentRegistry incidents, FleetRegistry fleet) {
        Validate.notNull(incidents, "incidents");
        Validate.notNull(fleet, "fleet");
        this.incidents = incidents;
        this.fleet = fleet;
    }

    public int[] countBySeverity() {
        int[] counts = new int[Severity.values().length];
        for (int i = 0; i < incidents.getCount(); i++) {
            counts[incidents.getByIndex(i).getSeverity().ordinal()]++;
        }
        return counts;
    }

    public int[] countByStatus() {
        int[] counts = new int[IncidentStatus.values().length];
        for (int i = 0; i < incidents.getCount(); i++) {
            counts[incidents.getByIndex(i).getStatus().ordinal()]++;
        }
        return counts;
    }

    public int[] countByRegion() {
        int[] counts = new int[Region.values().length];
        for (int i = 0; i < incidents.getCount(); i++) {
            counts[incidents.getByIndex(i).getRegion().ordinal()]++;
        }
        return counts;
    }

    /** Two-dimensional table: row = region, column = severity. */
    public int[][] countByRegionAndSeverity() {
        int[][] matrix = new int[Region.values().length][Severity.values().length];
        for (int i = 0; i < incidents.getCount(); i++) {
            Incident incident = incidents.getByIndex(i);
            matrix[incident.getRegion().ordinal()][incident.getSeverity().ordinal()]++;
        }
        return matrix;
    }

    public int countActiveIncidents() {
        int active = 0;
        for (int i = 0; i < incidents.getCount(); i++) {
            if (incidents.getByIndex(i).isActive()) {
                active++;
            }
        }
        return active;
    }

    // ---- resource utilisation -------------------------------------------------------

    public Utilisation overallUtilisation() {
        return summarise("All resources", fleet.allResources(), null);
    }

    /** One entry per {@link ResourceType}, in enum order; types with no resources have a total of 0. */
    public Utilisation[] utilisationByType() {
        Resource[] all = fleet.allResources();
        ResourceType[] types = ResourceType.values();
        Utilisation[] result = new Utilisation[types.length];
        for (int i = 0; i < types.length; i++) {
            result[i] = summarise(types[i].getLabel(), all, types[i]);
        }
        return result;
    }

    /** One entry per station, in registration order. */
    public Utilisation[] utilisationByStation() {
        Utilisation[] result = new Utilisation[fleet.getStationCount()];
        for (int i = 0; i < result.length; i++) {
            FireStation station = fleet.getStation(i);
            int deployed = station.countWithStatus(ResourceStatus.DEPLOYED);
            int available = station.countWithStatus(ResourceStatus.AVAILABLE);
            int outOfService = station.countWithStatus(ResourceStatus.OUT_OF_SERVICE);
            result[i] = new Utilisation(station.getName(), station.getResourceCount(), deployed, available, outOfService);
        }
        return result;
    }

    private Utilisation summarise(String label, Resource[] resources, ResourceType typeOrNull) {
        int total = 0;
        int deployed = 0;
        int available = 0;
        int outOfService = 0;
        for (int i = 0; i < resources.length; i++) {
            if (typeOrNull != null && resources[i].getType() != typeOrNull) {
                continue;
            }
            total++;
            switch (resources[i].getStatus()) {
                case DEPLOYED:
                    deployed++;
                    break;
                case AVAILABLE:
                    available++;
                    break;
                default:
                    outOfService++;
                    break;
            }
        }
        return new Utilisation(label, total, deployed, available, outOfService);
    }

    /**
     * The resources deployed most often, busiest first (ties: lower id first).
     * Uses insertion sort on a copy of the resource array.
     */
    public Resource[] busiestResources(int limit) {
        Validate.inRange(limit, 1, 1000, "limit");
        Resource[] sorted = fleet.allResources();
        for (int i = 1; i < sorted.length; i++) {
            Resource current = sorted[i];
            int j = i - 1;
            while (j >= 0 && isBusier(current, sorted[j])) {
                sorted[j + 1] = sorted[j];
                j--;
            }
            sorted[j + 1] = current;
        }
        int size = Math.min(limit, sorted.length);
        Resource[] top = new Resource[size];
        System.arraycopy(sorted, 0, top, 0, size);
        return top;
    }

    private boolean isBusier(Resource a, Resource b) {
        if (a.getDeploymentCount() != b.getDeploymentCount()) {
            return a.getDeploymentCount() > b.getDeploymentCount();
        }
        return a.getId() < b.getId();
    }
}
