package gr.csc.fireresponse.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gr.csc.fireresponse.Scenario;
import gr.csc.fireresponse.exception.CapacityExceededException;
import gr.csc.fireresponse.exception.InvalidAssignmentException;
import gr.csc.fireresponse.exception.InvalidResourceStateException;
import org.junit.jupiter.api.Test;

class DomainModelTest {

    // ---- Location --------------------------------------------------------------------

    @Test
    void locationRejectsInvalidValuesAndAcceptsTheBoundaries() {
        assertThrows(IllegalArgumentException.class, () -> new Location(Region.ATTICA, "X", 10.0, 23.0));
        assertThrows(IllegalArgumentException.class, () -> new Location(Region.ATTICA, "X", 38.0, 50.0));
        assertThrows(IllegalArgumentException.class, () -> new Location(Region.ATTICA, "X", Double.NaN, 23.0));
        assertThrows(IllegalArgumentException.class, () -> new Location(Region.ATTICA, "   ", 38.0, 23.0));
        assertThrows(IllegalArgumentException.class, () -> new Location(null, "X", 38.0, 23.0));

        Location corner = new Location(Region.CRETE, "Edge", Location.MIN_LATITUDE, Location.MIN_LONGITUDE);
        assertEquals(Location.MIN_LATITUDE, corner.getLatitude(), 0.0);
    }

    @Test
    void theTwoDistanceOverloadsAgreeAndGiveARealisticDistance() {
        Location athens = new Location(Region.ATTICA, "Athens", 37.98, 23.73);
        Location thessaloniki = new Location(Region.CENTRAL_MACEDONIA, "Thessaloniki", 40.64, 22.94);

        double viaLocation = athens.distanceKmTo(thessaloniki);
        double viaCoordinates = athens.distanceKmTo(40.64, 22.94);

        assertEquals(viaLocation, viaCoordinates, 1e-9);
        assertEquals(303.0, viaLocation, 10.0);
        assertEquals(viaLocation, thessaloniki.distanceKmTo(athens), 1e-9);
        assertEquals(0.0, athens.distanceKmTo(athens), 1e-9);
    }

    // ---- resources: polymorphism ---------------------------------------------------------

    @Test
    void everyResourceAnswersCanServePolymorphically() throws Exception {
        Scenario s = new Scenario();
        Resource[] resources = {s.engine1, s.tender, s.command, s.helicopter, s.bomber, s.team};
        // Rows follow the array above. Columns: LOW, MODERATE, HIGH, CRITICAL.
        boolean[][] expected = {
            {true, true, true, true},      // wildland engine: any severity
            {false, true, true, true},     // water tender: MODERATE and above
            {false, false, true, true},    // command vehicle: HIGH and above
            {false, true, true, true},     // helicopter: MODERATE and above
            {false, false, true, true},    // water bomber: HIGH and above
            {true, true, true, true},      // team with three members: any severity
        };

        Severity[] severities = Severity.values();
        for (int r = 0; r < resources.length; r++) {
            for (int c = 0; c < severities.length; c++) {
                assertEquals(expected[r][c], resources[r].canServe(severities[c]),
                        resources[r].getName() + " for " + severities[c]);
            }
        }
    }

    @Test
    void suppressionPointsFollowEachResourcesOwnFormula() throws Exception {
        Scenario s = new Scenario();

        assertEquals(35, s.engine1.suppressionPoints());   // 3000 L / 100 + 5 for 4x4
        assertEquals(25, s.engine2.suppressionPoints());   // 2500 L / 100
        assertEquals(20, s.tender.suppressionPoints());    // 10000 L / 500
        assertEquals(12, s.command.suppressionPoints());   // 4 channels * 3
        assertEquals(30, s.helicopter.suppressionPoints());// 3000 L / 100
        assertEquals(60, s.bomber.suppressionPoints());    // 6000 L / 100
        assertEquals(18, s.team.suppressionPoints());      // 3 * (3 + 2 + 1)
    }

    @Test
    void aTeamLeaderIsTheHighestRankedMember() throws Exception {
        ResponseTeam crew = new ResponseTeam(20, "Crew");
        assertNull(crew.getLeader());
        assertFalse(crew.canServe(Severity.LOW));

        Firefighter junior = new Firefighter("Junior");
        Firefighter officer = new Firefighter("Boss", Rank.OFFICER, 12);
        Firefighter senior = new Firefighter("Senior", Rank.SENIOR_FIREFIGHTER, 6);
        crew.addMember(junior);
        crew.addMember(officer);
        crew.addMember(senior);

        assertSame(officer, crew.getLeader());
        assertEquals(3, crew.getMemberCount());
        assertTrue(crew.canServe(Severity.LOW));
        assertSame(senior, crew.getMember(2));
        assertThrows(IllegalArgumentException.class, () -> crew.getMember(3));
    }

    @Test
    void firefighterConstructorsChainToTheFullConstructor() {
        Firefighter recruit = new Firefighter("Recruit");
        Firefighter ranked = new Firefighter("Ranked", Rank.OFFICER);

        assertEquals(Rank.FIREFIGHTER, recruit.getRank());
        assertEquals(0, recruit.getYearsOfService());
        assertEquals(Rank.OFFICER, ranked.getRank());
        assertEquals(0, ranked.getYearsOfService());
        assertThrows(IllegalArgumentException.class, () -> new Firefighter("Bad", Rank.OFFICER, -1));
        assertThrows(IllegalArgumentException.class, () -> new Firefighter("  "));
        assertThrows(IllegalArgumentException.class, () -> new Firefighter("Bad", null, 1));
    }

    @Test
    void anAerialUnitMustBeAHelicopterOrAWaterBomber() {
        assertThrows(IllegalArgumentException.class,
                () -> new AerialUnit(1, "Nope", ResourceType.WILDLAND_ENGINE, 3000));
        assertThrows(IllegalArgumentException.class, () -> new AerialUnit(1, "Nope", null, 3000));
        assertThrows(IllegalArgumentException.class,
                () -> new AerialUnit(1, "Nope", ResourceType.HELICOPTER, 10));
        assertEquals(ResourceType.WATER_BOMBER, new AerialUnit(2, "OK", ResourceType.WATER_BOMBER, 6000).getType());
    }

    @Test
    void vehicleConstructorsValidateTheirArguments() {
        assertThrows(IllegalArgumentException.class, () -> new WildlandEngine(0, "Bad id", 3000, true));
        assertThrows(IllegalArgumentException.class, () -> new WildlandEngine(1, " ", 3000, true));
        assertThrows(IllegalArgumentException.class, () -> new WildlandEngine(1, "Tiny", 100, true));
        assertThrows(IllegalArgumentException.class, () -> new WaterTender(1, "Tiny", 100));
        assertThrows(IllegalArgumentException.class, () -> new CommandVehicle(1, "No radio", 0));
    }

    // ---- resource availability ---------------------------------------------------------

    @Test
    void aResourceMovesBetweenAvailableDeployedAndOutOfServiceOnlyThroughItsMethods() throws Exception {
        WildlandEngine engine = new WildlandEngine(9, "Engine", 3000, true);
        assertTrue(engine.isAvailable());

        engine.deploy();
        assertEquals(ResourceStatus.DEPLOYED, engine.getStatus());
        assertThrows(InvalidAssignmentException.class, engine::deploy);
        assertThrows(InvalidResourceStateException.class, engine::markOutOfService);
        assertThrows(InvalidResourceStateException.class, engine::returnToService);

        engine.release();
        assertTrue(engine.isAvailable());
        engine.markOutOfService();
        assertEquals(ResourceStatus.OUT_OF_SERVICE, engine.getStatus());
        engine.release();   // has no effect on a resource that is not deployed
        assertEquals(ResourceStatus.OUT_OF_SERVICE, engine.getStatus());
        engine.returnToService();
        assertTrue(engine.isAvailable());
    }

    // ---- incident ----------------------------------------------------------------------

    @Test
    void aNewIncidentStartsReportedWithNoResources() {
        Incident incident = new Incident(1, "  Smoke over the hill  ", Scenario.location(Region.ATTICA),
                Severity.MODERATE, Scenario.T0);

        assertEquals("Smoke over the hill", incident.getDescription());
        assertEquals(IncidentStatus.REPORTED, incident.getStatus());
        assertEquals(Scenario.T0, incident.getLastUpdated());
        assertEquals(0, incident.getAssignmentCount());
        assertEquals(0, incident.getAssignedPoints());
        assertFalse(incident.isAdequatelyResourced());
        assertTrue(incident.isActive());
        assertEquals(Region.ATTICA, incident.getRegion());
    }

    @Test
    void incidentConstructorRejectsInvalidArguments() {
        Location place = Scenario.location(Region.ATTICA);
        assertThrows(IllegalArgumentException.class, () -> new Incident(0, "x", place, Severity.LOW, Scenario.T0));
        assertThrows(IllegalArgumentException.class, () -> new Incident(1, " ", place, Severity.LOW, Scenario.T0));
        assertThrows(IllegalArgumentException.class, () -> new Incident(1, "x", null, Severity.LOW, Scenario.T0));
        assertThrows(IllegalArgumentException.class, () -> new Incident(1, "x", place, null, Scenario.T0));
        assertThrows(IllegalArgumentException.class, () -> new Incident(1, "x", place, Severity.LOW, null));
    }

    @Test
    void enumHelpersBehaveAsDocumented() {
        assertTrue(Severity.CRITICAL.isAtLeast(Severity.HIGH));
        assertTrue(Severity.HIGH.isAtLeast(Severity.HIGH));
        assertFalse(Severity.LOW.isAtLeast(Severity.MODERATE));
        assertEquals(13, Region.values().length);
        assertTrue(IncidentStatus.EXTINGUISHED.isTerminal());
        assertFalse(IncidentStatus.CONTAINED.isTerminal());
    }

    @Test
    void aStationReportsItsCountsByStatus() throws Exception {
        Scenario s = new Scenario();
        s.engine1.deploy();
        s.engine2.markOutOfService();

        assertEquals(1, s.station.countWithStatus(ResourceStatus.DEPLOYED));
        assertEquals(1, s.station.countWithStatus(ResourceStatus.OUT_OF_SERVICE));
        assertEquals(6, s.station.countWithStatus(ResourceStatus.AVAILABLE));
        assertEquals(8, s.station.getResourceCount());
        assertThrows(CapacityExceededException.class, () -> {
            for (int i = 0; i < 40; i++) {
                s.station.addResource(new WildlandEngine(100 + i, "Filler " + i, 1000, false));
            }
        });
        assertEquals(30, s.station.getResourceCount());
    }
}
