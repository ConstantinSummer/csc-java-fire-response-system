package gr.csc.fireresponse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gr.csc.fireresponse.Scenario;
import gr.csc.fireresponse.exception.CapacityExceededException;
import gr.csc.fireresponse.exception.FireResponseException;
import gr.csc.fireresponse.model.FireStation;
import gr.csc.fireresponse.model.Firefighter;
import gr.csc.fireresponse.model.Incident;
import gr.csc.fireresponse.model.IncidentStatus;
import gr.csc.fireresponse.model.Location;
import gr.csc.fireresponse.model.Region;
import gr.csc.fireresponse.model.ResourceStatus;
import gr.csc.fireresponse.model.ResponseTeam;
import gr.csc.fireresponse.model.Severity;
import gr.csc.fireresponse.model.WildlandEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CapacityTest {

    private Scenario s;

    @BeforeEach
    void setUp() throws FireResponseException {
        s = new Scenario();
    }

    @Test
    void anIncidentAcceptsAtMostMaxAssignmentsAndTheOverflowIsNotDeployed() throws FireResponseException {
        Incident incident = s.report(Severity.LOW);
        WildlandEngine[] engines = new WildlandEngine[Incident.MAX_ASSIGNMENTS + 1];
        for (int i = 0; i < engines.length; i++) {
            engines[i] = new WildlandEngine(100 + i, "Extra-" + i, 2000, false);
            s.station.addResource(engines[i]);
        }

        for (int i = 0; i < Incident.MAX_ASSIGNMENTS; i++) {
            s.dispatch.assign(incident, engines[i]);
        }
        CapacityExceededException error = assertThrows(CapacityExceededException.class,
                () -> s.dispatch.assign(incident, engines[Incident.MAX_ASSIGNMENTS]));

        assertTrue(error.getMessage().contains("full"));
        assertEquals(Incident.MAX_ASSIGNMENTS, incident.getAssignmentCount());
        assertEquals(ResourceStatus.AVAILABLE, engines[Incident.MAX_ASSIGNMENTS].getStatus());
        assertEquals(0, engines[Incident.MAX_ASSIGNMENTS].getDeploymentCount());
    }

    @Test
    void releasedAssignmentsStillOccupyTheirSlots() throws FireResponseException {
        // The assignment array keeps history, which is a deliberate limitation to discuss in lessons.
        Incident incident = s.report(Severity.LOW);
        s.dispatch.assign(incident, s.engine1);
        s.dispatch.changeStatus(incident, IncidentStatus.FALSE_ALARM);

        assertEquals(0, incident.getActiveAssignmentCount());
        assertEquals(1, incident.getAssignmentCount());
    }

    @Test
    void aStationRejectsResourcesBeyondItsCapacity() throws FireResponseException {
        FireStation tiny = new FireStation(2, "Tiny", new Location(Region.CRETE, "Chania", 35.5, 24.0), 2);
        tiny.addResource(new WildlandEngine(50, "A", 1000, false));
        tiny.addResource(new WildlandEngine(51, "B", 1000, false));

        assertThrows(CapacityExceededException.class, () -> tiny.addResource(new WildlandEngine(52, "C", 1000, false)));

        assertEquals(2, tiny.getResourceCount());
        assertEquals(2, tiny.getCapacity());
    }

    @Test
    void aTeamRejectsAMemberBeyondMaxMembers() throws FireResponseException {
        ResponseTeam crew = new ResponseTeam(60, "Full crew");
        for (int i = 0; i < ResponseTeam.MAX_MEMBERS; i++) {
            crew.addMember(new Firefighter("Member " + i));
        }

        assertThrows(CapacityExceededException.class, () -> crew.addMember(new Firefighter("One too many")));

        assertEquals(ResponseTeam.MAX_MEMBERS, crew.getMemberCount());
    }

    @Test
    void theIncidentRegistryRejectsAReportWhenFull() throws FireResponseException {
        IncidentRegistry small = new IncidentRegistry(2);
        DispatchService service = new DispatchService(small, s.fleet, Scenario.fixedClock());
        Location place = Scenario.location(Region.ATTICA);
        service.reportIncident("First", place, Severity.LOW);
        service.reportIncident("Second", place, Severity.LOW);

        assertThrows(CapacityExceededException.class, () -> service.reportIncident("Third", place, Severity.LOW));

        assertEquals(2, small.getCount());
        assertEquals(3, small.nextId());
    }

    @Test
    void theFleetRegistryRejectsAStationBeyondItsCapacity() throws FireResponseException {
        FleetRegistry small = new FleetRegistry(1);
        small.addStation(new FireStation(1, "One", Scenario.location(Region.ATTICA), 3));

        assertThrows(CapacityExceededException.class,
                () -> small.addStation(new FireStation(2, "Two", Scenario.location(Region.CRETE), 3)));

        assertEquals(1, small.getStationCount());
    }

    @Test
    void theRegistryKeepsIncidentIdsInIncreasingOrder() {
        Incident outOfOrder = new Incident(5, "Wrong id", Scenario.location(Region.ATTICA), Severity.LOW, Scenario.T0);

        assertThrows(IllegalArgumentException.class, () -> s.incidents.add(outOfOrder));

        assertEquals(0, s.incidents.getCount());
        assertEquals(1, s.incidents.nextId());
    }
}
