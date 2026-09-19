package gr.csc.fireresponse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gr.csc.fireresponse.Scenario;
import gr.csc.fireresponse.exception.FireResponseException;
import gr.csc.fireresponse.exception.InvalidAssignmentException;
import gr.csc.fireresponse.exception.NotFoundException;
import gr.csc.fireresponse.model.Assignment;
import gr.csc.fireresponse.model.Incident;
import gr.csc.fireresponse.model.IncidentStatus;
import gr.csc.fireresponse.model.ResourceStatus;
import gr.csc.fireresponse.model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AssignmentTest {

    private Scenario s;

    @BeforeEach
    void setUp() throws FireResponseException {
        s = new Scenario();
    }

    // ---- valid assignments ---------------------------------------------------------

    @Test
    void validAssignmentDeploysTheResourceAndRecordsTheAssignment() throws FireResponseException {
        Incident incident = s.report(Severity.MODERATE);

        Assignment assignment = s.dispatch.assign(incident, s.engine1);

        assertSame(s.engine1, assignment.getResource());
        assertTrue(assignment.isActive());
        assertEquals(Scenario.T0, assignment.getAssignedAt());
        assertEquals(ResourceStatus.DEPLOYED, s.engine1.getStatus());
        assertEquals(1, s.engine1.getDeploymentCount());
        assertEquals(1, incident.getAssignmentCount());
        assertEquals(1, incident.getActiveAssignmentCount());
        assertTrue(incident.hasActiveResource(s.engine1));
        assertEquals(35, incident.getAssignedPoints());
    }

    @Test
    void assignmentByIdsFindsBothObjects() throws FireResponseException {
        Incident incident = s.report(Severity.LOW);

        s.dispatch.assign(incident.getId(), s.engine2.getId());

        assertTrue(incident.hasActiveResource(s.engine2));
        assertEquals(ResourceStatus.DEPLOYED, s.engine2.getStatus());
    }

    @Test
    void pointsAreSummedPolymorphicallyAcrossDifferentResourceTypes() throws FireResponseException {
        Incident incident = s.report(Severity.MODERATE);

        s.dispatch.assign(incident, s.engine1);    // 35
        s.dispatch.assign(incident, s.tender);     // 20
        assertFalse(incident.isAdequatelyResourced());   // 55 of 60
        s.dispatch.assign(incident, s.team);       // 18

        assertEquals(73, incident.getAssignedPoints());
        assertTrue(incident.isAdequatelyResourced());
    }

    // ---- invalid assignments ---------------------------------------------------------

    @Test
    void aResourceAlreadyDeployedCannotBeAssignedAgain() throws FireResponseException {
        Incident first = s.report(Severity.HIGH);
        Incident second = s.report(Severity.HIGH);
        s.dispatch.assign(first, s.engine1);

        InvalidAssignmentException error = assertThrows(InvalidAssignmentException.class,
                () -> s.dispatch.assign(second, s.engine1));

        assertTrue(error.getMessage().contains("not available"));
        assertEquals(0, second.getAssignmentCount());
        assertEquals(1, s.engine1.getDeploymentCount());
    }

    @Test
    void aClosedIncidentRejectsAssignments() throws FireResponseException {
        Incident incident = s.report(Severity.LOW);
        s.dispatch.changeStatus(incident, IncidentStatus.FALSE_ALARM);

        InvalidAssignmentException error = assertThrows(InvalidAssignmentException.class,
                () -> s.dispatch.assign(incident, s.engine1));

        assertTrue(error.getMessage().contains("closed"));
        assertEquals(ResourceStatus.AVAILABLE, s.engine1.getStatus());
    }

    @Test
    void anOutOfServiceResourceCannotBeAssigned() throws FireResponseException {
        Incident incident = s.report(Severity.LOW);
        s.engine1.markOutOfService();

        assertThrows(InvalidAssignmentException.class, () -> s.dispatch.assign(incident, s.engine1));

        assertEquals(ResourceStatus.OUT_OF_SERVICE, s.engine1.getStatus());
        assertEquals(0, incident.getAssignmentCount());
    }

    @Test
    void resourcesUnsuitableForTheSeverityAreRejected() throws FireResponseException {
        Incident low = s.report(Severity.LOW);
        Incident moderate = s.report(Severity.MODERATE);

        assertThrows(InvalidAssignmentException.class, () -> s.dispatch.assign(low, s.tender));
        assertThrows(InvalidAssignmentException.class, () -> s.dispatch.assign(low, s.command));
        assertThrows(InvalidAssignmentException.class, () -> s.dispatch.assign(low, s.helicopter));
        assertThrows(InvalidAssignmentException.class, () -> s.dispatch.assign(moderate, s.bomber));
        assertThrows(InvalidAssignmentException.class, () -> s.dispatch.assign(moderate, s.command));

        // Nothing was deployed by the failed attempts.
        assertEquals(0, low.getAssignmentCount());
        assertEquals(0, moderate.getAssignmentCount());
        assertEquals(ResourceStatus.AVAILABLE, s.bomber.getStatus());

        // The same resources are fine where they are suitable.
        s.dispatch.assign(moderate, s.helicopter);
        s.dispatch.assign(moderate, s.tender);
        assertEquals(2, moderate.getAssignmentCount());
    }

    @Test
    void anUnderstaffedTeamCannotServeAnySeverity() throws FireResponseException {
        Incident critical = s.report(Severity.CRITICAL);

        assertThrows(InvalidAssignmentException.class, () -> s.dispatch.assign(critical, s.smallTeam));

        s.dispatch.assign(critical, s.team);
        assertTrue(critical.hasActiveResource(s.team));
    }

    @Test
    void unknownIdsAreReportedAsNotFound() throws FireResponseException {
        Incident incident = s.report(Severity.LOW);

        assertThrows(NotFoundException.class, () -> s.dispatch.assign(99, s.engine1.getId()));
        assertThrows(NotFoundException.class, () -> s.dispatch.assign(incident.getId(), 999));
        assertEquals(0, incident.getAssignmentCount());
    }

    @Test
    void aFailedAssignmentLeavesTheResourceAvailableForAnotherIncident() throws FireResponseException {
        Incident low = s.report(Severity.LOW);
        Incident high = s.report(Severity.HIGH);

        assertThrows(InvalidAssignmentException.class, () -> s.dispatch.assign(low, s.bomber));
        s.dispatch.assign(high, s.bomber);

        assertTrue(high.hasActiveResource(s.bomber));
        assertEquals(1, s.bomber.getDeploymentCount());
    }
}
