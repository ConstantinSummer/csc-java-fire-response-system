package gr.csc.fireresponse.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gr.csc.fireresponse.Scenario;
import gr.csc.fireresponse.exception.FireResponseException;
import gr.csc.fireresponse.model.Incident;
import gr.csc.fireresponse.model.IncidentStatus;
import gr.csc.fireresponse.model.Region;
import gr.csc.fireresponse.model.Resource;
import gr.csc.fireresponse.model.ResourceStatus;
import gr.csc.fireresponse.service.DispatchService;
import gr.csc.fireresponse.service.FleetRegistry;
import gr.csc.fireresponse.service.IncidentRegistry;
import gr.csc.fireresponse.service.StatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SampleDataTest {

    private IncidentRegistry incidents;
    private FleetRegistry fleet;
    private StatisticsService statistics;

    @BeforeEach
    void setUp() throws FireResponseException {
        incidents = new IncidentRegistry(60);
        fleet = new FleetRegistry(12);
        DispatchService dispatch = new DispatchService(incidents, fleet, Scenario.fixedClock());
        statistics = new StatisticsService(incidents, fleet);
        SampleData.populate(fleet, dispatch);
    }

    @Test
    void theSampleDataLoadsTheDocumentedNumberOfObjects() {
        assertEquals(14, incidents.getCount());
        assertEquals(10, fleet.getStationCount());
        assertEquals(38, fleet.countResources());
        assertEquals(9, statistics.countActiveIncidents());
    }

    @Test
    void everyRegionHasAtLeastOneIncident() {
        int[] byRegion = statistics.countByRegion();
        for (Region region : Region.values()) {
            assertTrue(byRegion[region.ordinal()] >= 1, region.getDisplayName());
        }
    }

    @Test
    void resourceStatesAreConsistentWithTheIncidentStates() {
        Resource[] all = fleet.allResources();
        int deployed = 0;
        for (int i = 0; i < all.length; i++) {
            if (all[i].getStatus() == ResourceStatus.DEPLOYED) {
                deployed++;
            }
        }
        assertEquals(21, deployed);
        assertEquals(2, fleet.resourcesWithStatus(ResourceStatus.OUT_OF_SERVICE).length);

        // Every deployed resource must be actively assigned to exactly one active incident.
        for (int r = 0; r < all.length; r++) {
            int activeAssignments = 0;
            for (int i = 0; i < incidents.getCount(); i++) {
                Incident incident = incidents.getByIndex(i);
                if (incident.hasActiveResource(all[r])) {
                    activeAssignments++;
                    assertTrue(incident.isActive());
                }
            }
            assertEquals(all[r].getStatus() == ResourceStatus.DEPLOYED ? 1 : 0, activeAssignments, all[r].getName());
        }
    }

    @Test
    void closedIncidentsHaveNoActiveAssignments() {
        for (int i = 0; i < incidents.getCount(); i++) {
            Incident incident = incidents.getByIndex(i);
            if (incident.getStatus() == IncidentStatus.EXTINGUISHED || incident.getStatus() == IncidentStatus.FALSE_ALARM) {
                assertEquals(0, incident.getActiveAssignmentCount(), "Incident " + incident.getId());
            }
        }
    }
}
