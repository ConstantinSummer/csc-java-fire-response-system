package gr.csc.fireresponse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gr.csc.fireresponse.Scenario;
import gr.csc.fireresponse.exception.FireResponseException;
import gr.csc.fireresponse.exception.InvalidStatusTransitionException;
import gr.csc.fireresponse.model.Assignment;
import gr.csc.fireresponse.model.Incident;
import gr.csc.fireresponse.model.IncidentStatus;
import gr.csc.fireresponse.model.ResourceStatus;
import gr.csc.fireresponse.model.Severity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StatusTransitionTest {

    private Scenario s;

    @BeforeEach
    void setUp() throws FireResponseException {
        s = new Scenario();
    }

    @Test
    void aNewIncidentIsReportedAndTheFullLifeCycleIsAllowed() throws FireResponseException {
        Incident incident = s.report(Severity.HIGH);
        assertEquals(IncidentStatus.REPORTED, incident.getStatus());
        s.dispatch.assign(incident, s.engine1);

        LocalDateTime later = Scenario.T0.plusHours(2);
        s.dispatch.changeStatus(incident, IncidentStatus.RESPONDING, later);
        s.dispatch.changeStatus(incident, IncidentStatus.ACTIVE, later.plusHours(1));
        s.dispatch.changeStatus(incident, IncidentStatus.CONTAINED, later.plusHours(2));
        s.dispatch.changeStatus(incident, IncidentStatus.EXTINGUISHED, later.plusHours(3));

        assertEquals(IncidentStatus.EXTINGUISHED, incident.getStatus());
        assertEquals(later.plusHours(3), incident.getLastUpdated());
        assertFalse(incident.isActive());
    }

    @Test
    void aContainedFireCanFlareUpAndBecomeActiveAgain() throws FireResponseException {
        Incident incident = s.report(Severity.MODERATE);
        s.dispatch.assign(incident, s.engine1);
        s.dispatch.changeStatus(incident, IncidentStatus.RESPONDING);
        s.dispatch.changeStatus(incident, IncidentStatus.ACTIVE);
        s.dispatch.changeStatus(incident, IncidentStatus.CONTAINED);

        s.dispatch.changeStatus(incident, IncidentStatus.ACTIVE);

        assertEquals(IncidentStatus.ACTIVE, incident.getStatus());
        assertEquals(ResourceStatus.DEPLOYED, s.engine1.getStatus());
    }

    @Test
    void skippingOrReversingStatusesIsRejectedAndKeepsTheOldStatus() throws FireResponseException {
        Incident incident = s.report(Severity.MODERATE);

        InvalidStatusTransitionException error = assertThrows(InvalidStatusTransitionException.class,
                () -> s.dispatch.changeStatus(incident, IncidentStatus.ACTIVE));
        assertEquals(IncidentStatus.REPORTED, error.getFrom());
        assertEquals(IncidentStatus.ACTIVE, error.getTo());
        assertThrows(InvalidStatusTransitionException.class,
                () -> s.dispatch.changeStatus(incident, IncidentStatus.CONTAINED));
        assertThrows(InvalidStatusTransitionException.class,
                () -> s.dispatch.changeStatus(incident, IncidentStatus.EXTINGUISHED));

        assertEquals(IncidentStatus.REPORTED, incident.getStatus());
        assertEquals(Scenario.T0, incident.getLastUpdated());
    }

    @Test
    void terminalStatusesCannotBeLeft() throws FireResponseException {
        Incident falseAlarm = s.report(Severity.LOW);
        s.dispatch.changeStatus(falseAlarm, IncidentStatus.FALSE_ALARM);

        assertEquals(0, IncidentStatus.FALSE_ALARM.allowedNext().length);
        assertEquals(0, IncidentStatus.EXTINGUISHED.allowedNext().length);
        for (IncidentStatus next : IncidentStatus.values()) {
            assertFalse(IncidentStatus.FALSE_ALARM.canTransitionTo(next));
            assertFalse(IncidentStatus.EXTINGUISHED.canTransitionTo(next));
        }
        assertThrows(InvalidStatusTransitionException.class,
                () -> s.dispatch.changeStatus(falseAlarm, IncidentStatus.ACTIVE));
    }

    @Test
    void theStateMachineMatchesTheDocumentedTransitions() {
        assertTrue(IncidentStatus.REPORTED.canTransitionTo(IncidentStatus.RESPONDING));
        assertTrue(IncidentStatus.REPORTED.canTransitionTo(IncidentStatus.FALSE_ALARM));
        assertTrue(IncidentStatus.RESPONDING.canTransitionTo(IncidentStatus.ACTIVE));
        assertTrue(IncidentStatus.RESPONDING.canTransitionTo(IncidentStatus.FALSE_ALARM));
        assertTrue(IncidentStatus.ACTIVE.canTransitionTo(IncidentStatus.CONTAINED));
        assertTrue(IncidentStatus.CONTAINED.canTransitionTo(IncidentStatus.EXTINGUISHED));
        assertTrue(IncidentStatus.CONTAINED.canTransitionTo(IncidentStatus.ACTIVE));
        assertFalse(IncidentStatus.ACTIVE.canTransitionTo(IncidentStatus.EXTINGUISHED));
        assertFalse(IncidentStatus.ACTIVE.canTransitionTo(IncidentStatus.FALSE_ALARM));
        assertEquals(2, IncidentStatus.REPORTED.allowedNext().length);
        assertEquals(1, IncidentStatus.ACTIVE.allowedNext().length);
    }

    @Test
    void respondingRequiresAtLeastOneAssignedResource() throws FireResponseException {
        Incident incident = s.report(Severity.HIGH);

        InvalidStatusTransitionException error = assertThrows(InvalidStatusTransitionException.class,
                () -> s.dispatch.changeStatus(incident, IncidentStatus.RESPONDING));
        assertTrue(error.getMessage().contains("assign at least one resource"));
        assertEquals(IncidentStatus.REPORTED, incident.getStatus());

        s.dispatch.assign(incident, s.engine1);
        s.dispatch.changeStatus(incident, IncidentStatus.RESPONDING);
        assertEquals(IncidentStatus.RESPONDING, incident.getStatus());
    }

    @Test
    void closingAnIncidentReleasesEveryResourceButKeepsTheHistory() throws FireResponseException {
        Incident incident = s.report(Severity.HIGH);
        s.dispatch.assign(incident, s.engine1);
        s.dispatch.assign(incident, s.engine2);
        s.dispatch.changeStatus(incident, IncidentStatus.RESPONDING);
        s.dispatch.changeStatus(incident, IncidentStatus.ACTIVE);
        s.dispatch.changeStatus(incident, IncidentStatus.CONTAINED);
        LocalDateTime closedAt = Scenario.T0.plusDays(1);

        s.dispatch.changeStatus(incident, IncidentStatus.EXTINGUISHED, closedAt);

        assertEquals(ResourceStatus.AVAILABLE, s.engine1.getStatus());
        assertEquals(ResourceStatus.AVAILABLE, s.engine2.getStatus());
        assertEquals(2, incident.getAssignmentCount());
        assertEquals(0, incident.getActiveAssignmentCount());
        Assignment first = incident.getAssignment(0);
        assertFalse(first.isActive());
        assertNotNull(first.getReleasedAt());
        assertEquals(closedAt, first.getReleasedAt());
        assertEquals(1, s.engine1.getDeploymentCount());
    }

    @Test
    void aFalseAlarmAlsoReleasesResourcesAndTheyCanBeReassigned() throws FireResponseException {
        Incident first = s.report(Severity.LOW);
        s.dispatch.assign(first, s.engine1);
        s.dispatch.changeStatus(first, IncidentStatus.RESPONDING);
        s.dispatch.changeStatus(first, IncidentStatus.FALSE_ALARM);
        assertEquals(ResourceStatus.AVAILABLE, s.engine1.getStatus());

        Incident second = s.report(Severity.HIGH);
        s.dispatch.assign(second, s.engine1);

        assertTrue(second.hasActiveResource(s.engine1));
        assertEquals(2, s.engine1.getDeploymentCount());
    }
}
