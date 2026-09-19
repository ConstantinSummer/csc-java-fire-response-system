package gr.csc.fireresponse;

import gr.csc.fireresponse.exception.FireResponseException;
import gr.csc.fireresponse.model.AerialUnit;
import gr.csc.fireresponse.model.CommandVehicle;
import gr.csc.fireresponse.model.FireStation;
import gr.csc.fireresponse.model.Firefighter;
import gr.csc.fireresponse.model.Incident;
import gr.csc.fireresponse.model.Location;
import gr.csc.fireresponse.model.Rank;
import gr.csc.fireresponse.model.Region;
import gr.csc.fireresponse.model.ResourceType;
import gr.csc.fireresponse.model.ResponseTeam;
import gr.csc.fireresponse.model.Severity;
import gr.csc.fireresponse.model.WaterTender;
import gr.csc.fireresponse.model.WildlandEngine;
import gr.csc.fireresponse.service.DispatchService;
import gr.csc.fireresponse.service.FleetRegistry;
import gr.csc.fireresponse.service.IncidentRegistry;
import gr.csc.fireresponse.service.StatisticsService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * A small, fully predictable world for tests: one station with one resource of each kind, empty
 * registries, and a clock frozen at {@link #T0} so that every timestamp is known in advance.
 *
 * <p>Suppression points: engine1 = 35, engine2 = 25, tender = 20, command = 12, helicopter = 30,
 * bomber = 60, team = 18. {@code smallTeam} has only two members, so it can never serve.</p>
 */
public final class Scenario {

    public static final LocalDateTime T0 = LocalDateTime.of(2026, 8, 1, 10, 0);
    public static final ZoneId ZONE = ZoneId.of("Europe/Athens");

    public final IncidentRegistry incidents = new IncidentRegistry(10);
    public final FleetRegistry fleet = new FleetRegistry(5);
    public final DispatchService dispatch;
    public final StatisticsService statistics;
    public final FireStation station;
    public final WildlandEngine engine1;
    public final WildlandEngine engine2;
    public final WaterTender tender;
    public final CommandVehicle command;
    public final AerialUnit helicopter;
    public final AerialUnit bomber;
    public final ResponseTeam team;
    public final ResponseTeam smallTeam;

    public Scenario() throws FireResponseException {
        dispatch = new DispatchService(incidents, fleet, fixedClock());
        statistics = new StatisticsService(incidents, fleet);

        station = new FireStation(1, "Test Station", location(Region.ATTICA), 30);
        fleet.addStation(station);

        engine1 = new WildlandEngine(1, "E1", 3000, true);
        engine2 = new WildlandEngine(2, "E2", 2500, false);
        tender = new WaterTender(3, "WT1", 10000);
        command = new CommandVehicle(4, "CMD1", 4);
        helicopter = new AerialUnit(5, "H1", ResourceType.HELICOPTER, 3000);
        bomber = new AerialUnit(6, "WB1", ResourceType.WATER_BOMBER, 6000);
        team = new ResponseTeam(7, "T1");
        team.addMember(new Firefighter("Lead", Rank.OFFICER, 10));
        team.addMember(new Firefighter("Senior", Rank.SENIOR_FIREFIGHTER, 5));
        team.addMember(new Firefighter("Junior", Rank.FIREFIGHTER, 1));
        smallTeam = new ResponseTeam(8, "T2");
        smallTeam.addMember(new Firefighter("Alone", Rank.OFFICER, 10));
        smallTeam.addMember(new Firefighter("Pair"));

        station.addResource(engine1);
        station.addResource(engine2);
        station.addResource(tender);
        station.addResource(command);
        station.addResource(helicopter);
        station.addResource(bomber);
        station.addResource(team);
        station.addResource(smallTeam);
    }

    public static Clock fixedClock() {
        return Clock.fixed(T0.atZone(ZONE).toInstant(), ZONE);
    }

    public static Location location(Region region) {
        return new Location(region, "Test area", 38.0, 23.7);
    }

    /** Reports an Attica incident of the given severity at {@link #T0}. */
    public Incident report(Severity severity) throws FireResponseException {
        return report(Region.ATTICA, severity);
    }

    public Incident report(Region region, Severity severity) throws FireResponseException {
        return dispatch.reportIncident("Test fire", location(region), severity, T0);
    }
}
