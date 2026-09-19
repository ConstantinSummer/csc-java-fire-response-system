package gr.csc.fireresponse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import gr.csc.fireresponse.Scenario;
import gr.csc.fireresponse.exception.FireResponseException;
import gr.csc.fireresponse.model.Incident;
import gr.csc.fireresponse.model.IncidentStatus;
import gr.csc.fireresponse.model.Region;
import gr.csc.fireresponse.model.Resource;
import gr.csc.fireresponse.model.ResourceType;
import gr.csc.fireresponse.model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StatisticsTest {

    private Scenario s;

    @BeforeEach
    void setUp() throws FireResponseException {
        s = new Scenario();
        s.report(Region.ATTICA, Severity.LOW);         // id 1
        Incident second = s.report(Region.ATTICA, Severity.HIGH);       // id 2
        s.report(Region.CRETE, Severity.HIGH);         // id 3
        s.report(Region.CRETE, Severity.CRITICAL);     // id 4

        s.dispatch.changeStatus(s.incidents.findById(1), IncidentStatus.FALSE_ALARM);
        s.dispatch.assign(second, s.engine1);
        s.dispatch.changeStatus(second, IncidentStatus.RESPONDING);
    }

    @Test
    void incidentsAreCountedBySeverity() {
        int[] counts = s.statistics.countBySeverity();

        assertEquals(Severity.values().length, counts.length);
        assertEquals(1, counts[Severity.LOW.ordinal()]);
        assertEquals(0, counts[Severity.MODERATE.ordinal()]);
        assertEquals(2, counts[Severity.HIGH.ordinal()]);
        assertEquals(1, counts[Severity.CRITICAL.ordinal()]);
    }

    @Test
    void incidentsAreCountedByStatus() {
        int[] counts = s.statistics.countByStatus();

        assertEquals(2, counts[IncidentStatus.REPORTED.ordinal()]);
        assertEquals(1, counts[IncidentStatus.RESPONDING.ordinal()]);
        assertEquals(1, counts[IncidentStatus.FALSE_ALARM.ordinal()]);
        assertEquals(0, counts[IncidentStatus.ACTIVE.ordinal()]);
        assertEquals(3, s.statistics.countActiveIncidents());
    }

    @Test
    void incidentsAreCountedByRegion() {
        int[] counts = s.statistics.countByRegion();

        assertEquals(2, counts[Region.ATTICA.ordinal()]);
        assertEquals(2, counts[Region.CRETE.ordinal()]);
        assertEquals(0, counts[Region.EPIRUS.ordinal()]);
    }

    @Test
    void theRegionBySeverityMatrixHasOneRowPerRegionAndOneColumnPerSeverity() {
        int[][] matrix = s.statistics.countByRegionAndSeverity();

        assertEquals(Region.values().length, matrix.length);
        assertEquals(Severity.values().length, matrix[0].length);
        assertEquals(1, matrix[Region.ATTICA.ordinal()][Severity.LOW.ordinal()]);
        assertEquals(1, matrix[Region.ATTICA.ordinal()][Severity.HIGH.ordinal()]);
        assertEquals(1, matrix[Region.CRETE.ordinal()][Severity.HIGH.ordinal()]);
        assertEquals(1, matrix[Region.CRETE.ordinal()][Severity.CRITICAL.ordinal()]);
        int total = 0;
        for (int r = 0; r < matrix.length; r++) {
            for (int c = 0; c < matrix[r].length; c++) {
                total += matrix[r][c];
            }
        }
        assertEquals(4, total);
    }

    @Test
    void utilisationIsMeasuredAgainstInServiceResources() throws FireResponseException {
        Utilisation engines = s.statistics.utilisationByType()[ResourceType.WILDLAND_ENGINE.ordinal()];
        assertEquals(2, engines.getTotal());
        assertEquals(1, engines.getDeployed());
        assertEquals(1, engines.getAvailable());
        assertEquals(0, engines.getOutOfService());
        assertEquals(50.0, engines.getPercentDeployed(), 0.0001);

        s.engine2.markOutOfService();
        engines = s.statistics.utilisationByType()[ResourceType.WILDLAND_ENGINE.ordinal()];
        assertEquals(1, engines.getOutOfService());
        assertEquals(1, engines.getInService());
        assertEquals(100.0, engines.getPercentDeployed(), 0.0001);
    }

    @Test
    void overallAndPerStationUtilisationAddUp() {
        Utilisation overall = s.statistics.overallUtilisation();
        assertEquals(8, overall.getTotal());
        assertEquals(1, overall.getDeployed());
        assertEquals(7, overall.getAvailable());
        assertEquals(12.5, overall.getPercentDeployed(), 0.0001);

        Utilisation[] stations = s.statistics.utilisationByStation();
        assertEquals(1, stations.length);
        assertEquals("Test Station", stations[0].getLabel());
        assertEquals(8, stations[0].getTotal());
        assertEquals(1, stations[0].getDeployed());
    }

    @Test
    void utilisationOfAGroupWithNoInServiceResourcesIsZeroNotADivisionError() {
        Utilisation none = new Utilisation("Nothing in service", 2, 0, 0, 2);

        assertEquals(0, none.getInService());
        assertEquals(0.0, none.getPercentDeployed(), 0.0001);
    }

    @Test
    void theBusiestResourcesAreRankedByDeploymentsThenById() throws FireResponseException {
        // engine1 is already deployed once (setUp). Deploy engine2 twice and engine1 once more.
        cycle(s.engine2);
        cycle(s.engine2);
        s.dispatch.changeStatus(s.incidents.findById(2), IncidentStatus.FALSE_ALARM);
        cycle(s.engine1);

        Resource[] top = s.statistics.busiestResources(3);

        assertEquals(3, top.length);
        assertSame(s.engine1, top[0]);   // 2 deployments, lower id wins the tie
        assertSame(s.engine2, top[1]);   // 2 deployments
        assertSame(s.tender, top[2]);    // 0 deployments, lowest id among the rest
        assertEquals(8, s.statistics.busiestResources(50).length);
    }

    /** Assigns the resource to a fresh incident and closes it again, adding one deployment. */
    private void cycle(Resource resource) throws FireResponseException {
        Incident incident = s.report(Severity.LOW);
        s.dispatch.assign(incident, resource);
        s.dispatch.changeStatus(incident, IncidentStatus.FALSE_ALARM);
    }
}
