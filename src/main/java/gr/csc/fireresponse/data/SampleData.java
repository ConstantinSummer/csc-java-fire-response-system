package gr.csc.fireresponse.data;

import gr.csc.fireresponse.exception.FireResponseException;
import gr.csc.fireresponse.model.AerialUnit;
import gr.csc.fireresponse.model.CommandVehicle;
import gr.csc.fireresponse.model.FireStation;
import gr.csc.fireresponse.model.Firefighter;
import gr.csc.fireresponse.model.Incident;
import gr.csc.fireresponse.model.IncidentStatus;
import gr.csc.fireresponse.model.Location;
import gr.csc.fireresponse.model.Rank;
import gr.csc.fireresponse.model.Region;
import gr.csc.fireresponse.model.Resource;
import gr.csc.fireresponse.model.ResourceType;
import gr.csc.fireresponse.model.ResponseTeam;
import gr.csc.fireresponse.model.Severity;
import gr.csc.fireresponse.model.WaterTender;
import gr.csc.fireresponse.model.WildlandEngine;
import gr.csc.fireresponse.service.DispatchService;
import gr.csc.fireresponse.service.FleetRegistry;
import java.time.LocalDateTime;

/**
 * Preloaded educational data so the application is interesting from the first run.
 *
 * <p>All incidents, stations, vehicles and people are fictional. Place names and coordinates are
 * approximate and only serve to make the scenario recognisable; this is not real Fire Service data.
 * The incidents are created through {@link DispatchService}, so every assignment and status change
 * passes the same validation as input typed in the console.</p>
 */
public final class SampleData {

    private static final int STATION_CAPACITY = 8;

    private SampleData() {
        // Loader: not meant to be instantiated.
    }

    public static void populate(FleetRegistry fleet, DispatchService dispatch) throws FireResponseException {
        loadFleet(fleet);
        loadIncidents(fleet, dispatch);
    }

    // ---- stations and resources --------------------------------------------------------

    private static void loadFleet(FleetRegistry fleet) throws FireResponseException {
        FireStation athensNorth = station(fleet, 1, "Athens North Station", Region.ATTICA, "Acharnes", 38.084, 23.735);
        engine(fleet, athensNorth, "ATT-E1", 3000, true);
        engine(fleet, athensNorth, "ATT-E2", 2500, false);
        athensNorth.addResource(new WaterTender(fleet.nextResourceId(), "ATT-WT1", 12000));
        athensNorth.addResource(new CommandVehicle(fleet.nextResourceId(), "ATT-CMD1", 4));
        team(fleet, athensNorth, "ATT-T1",
                new Firefighter("K. Ioannou", Rank.OFFICER, 18), new Firefighter("P. Nikolaou", Rank.SENIOR_FIREFIGHTER, 11),
                new Firefighter("S. Georgiou", Rank.SENIOR_FIREFIGHTER, 9), new Firefighter("D. Vlachos", Rank.FIREFIGHTER, 3),
                new Firefighter("E. Markou", Rank.FIREFIGHTER, 2));

        FireStation marathon = station(fleet, 2, "Marathon Station", Region.ATTICA, "Marathon", 38.153, 23.958);
        engine(fleet, marathon, "MAR-E1", 3500, true);
        engine(fleet, marathon, "MAR-E2", 2000, true);
        team(fleet, marathon, "MAR-T1",
                new Firefighter("A. Papas", Rank.OFFICER, 15), new Firefighter("T. Lambrou", Rank.SENIOR_FIREFIGHTER, 8),
                new Firefighter("M. Sotiriou", Rank.FIREFIGHTER, 4), new Firefighter("L. Zervas", Rank.FIREFIGHTER));
        // Deliberately understaffed: a team below ResponseTeam.MIN_MEMBERS_TO_SERVE cannot be assigned.
        team(fleet, marathon, "MAR-T2", new Firefighter("N. Kalos", Rank.SENIOR_FIREFIGHTER, 6),
                new Firefighter("R. Dimas"));

        FireStation elefsina = station(fleet, 3, "Elefsina Air Base", Region.ATTICA, "Elefsina", 38.065, 23.556);
        elefsina.addResource(new AerialUnit(fleet.nextResourceId(), "ELE-WB1", ResourceType.WATER_BOMBER, 6000));
        elefsina.addResource(new AerialUnit(fleet.nextResourceId(), "ELE-WB2", ResourceType.WATER_BOMBER, 6000));
        elefsina.addResource(new AerialUnit(fleet.nextResourceId(), "ELE-H1", ResourceType.HELICOPTER, 3000));

        FireStation chalkida = station(fleet, 4, "Chalkida Station", Region.CENTRAL_GREECE, "Chalkida", 38.464, 23.600);
        engine(fleet, chalkida, "CHA-E1", 3000, true);
        engine(fleet, chalkida, "CHA-E2", 2500, true);
        chalkida.addResource(new WaterTender(fleet.nextResourceId(), "CHA-WT1", 10000));
        team(fleet, chalkida, "CHA-T1",
                new Firefighter("V. Anagnostou", Rank.OFFICER, 20), new Firefighter("I. Fotiou", Rank.SENIOR_FIREFIGHTER, 10),
                new Firefighter("G. Karras", Rank.FIREFIGHTER, 5), new Firefighter("O. Pappas", Rank.FIREFIGHTER, 1));

        FireStation patras = station(fleet, 5, "Patras Station", Region.WESTERN_GREECE, "Patras", 38.246, 21.735);
        engine(fleet, patras, "PAT-E1", 3000, true);
        engine(fleet, patras, "PAT-E2", 2500, true);
        patras.addResource(new CommandVehicle(fleet.nextResourceId(), "PAT-CMD1", 3));
        team(fleet, patras, "PAT-T1",
                new Firefighter("H. Stathis", Rank.OFFICER, 22), new Firefighter("C. Rallis", Rank.SENIOR_FIREFIGHTER, 12),
                new Firefighter("F. Mylonas", Rank.SENIOR_FIREFIGHTER, 9), new Firefighter("B. Tsakas", Rank.FIREFIGHTER, 4),
                new Firefighter("J. Antonis", Rank.FIREFIGHTER, 2));

        FireStation kalamata = station(fleet, 6, "Kalamata Station", Region.PELOPONNESE, "Kalamata", 37.039, 22.114);
        engine(fleet, kalamata, "KAL-E1", 3200, true);
        kalamata.addResource(new WaterTender(fleet.nextResourceId(), "KAL-WT1", 15000));
        team(fleet, kalamata, "KAL-T1",
                new Firefighter("Y. Manolis", Rank.OFFICER, 14), new Firefighter("W. Petrou", Rank.SENIOR_FIREFIGHTER, 7),
                new Firefighter("U. Diamanti", Rank.FIREFIGHTER, 3), new Firefighter("Z. Roussos", Rank.FIREFIGHTER, 1));

        FireStation volos = station(fleet, 7, "Volos Station", Region.THESSALY, "Volos", 39.361, 22.942);
        engine(fleet, volos, "VOL-E1", 3000, true);
        engine(fleet, volos, "VOL-E2", 2000, false);
        volos.addResource(new AerialUnit(fleet.nextResourceId(), "VOL-H1", ResourceType.HELICOPTER, 2500));
        team(fleet, volos, "VOL-T1",
                new Firefighter("Q. Efthymiou", Rank.OFFICER, 16), new Firefighter("X. Kanaris", Rank.SENIOR_FIREFIGHTER, 8),
                new Firefighter("A. Balis", Rank.FIREFIGHTER, 3), new Firefighter("T. Orfanos", Rank.FIREFIGHTER, 2));

        FireStation thessaloniki = station(fleet, 8, "Thessaloniki East Station", Region.CENTRAL_MACEDONIA,
                "Thessaloniki", 40.640, 23.030);
        engine(fleet, thessaloniki, "THE-E1", 3500, true);
        engine(fleet, thessaloniki, "THE-E2", 2500, true);
        thessaloniki.addResource(new WaterTender(fleet.nextResourceId(), "THE-WT1", 20000));
        thessaloniki.addResource(new CommandVehicle(fleet.nextResourceId(), "THE-CMD1", 5));
        team(fleet, thessaloniki, "THE-T1",
                new Firefighter("D. Kourtis", Rank.OFFICER, 19), new Firefighter("E. Panou", Rank.SENIOR_FIREFIGHTER, 13),
                new Firefighter("K. Lianos", Rank.SENIOR_FIREFIGHTER, 10), new Firefighter("S. Argyris", Rank.FIREFIGHTER, 6),
                new Firefighter("M. Vasos", Rank.FIREFIGHTER, 3), new Firefighter("P. Xenos", Rank.FIREFIGHTER, 1));

        FireStation chania = station(fleet, 9, "Chania Station", Region.CRETE, "Chania", 35.514, 24.018);
        engine(fleet, chania, "CHN-E1", 3000, true);
        engine(fleet, chania, "CHN-E2", 2000, true);
        team(fleet, chania, "CHN-T1",
                new Firefighter("G. Frangos", Rank.OFFICER, 12), new Firefighter("N. Kritikos", Rank.SENIOR_FIREFIGHTER, 7),
                new Firefighter("L. Sfakianos", Rank.FIREFIGHTER, 2), new Firefighter("I. Tzanakis", Rank.FIREFIGHTER, 1));

        FireStation rhodes = station(fleet, 10, "Rhodes Station", Region.SOUTH_AEGEAN, "Rhodes", 36.434, 28.217);
        engine(fleet, rhodes, "RHO-E1", 2500, true);
        rhodes.addResource(new WaterTender(fleet.nextResourceId(), "RHO-WT1", 8000));
        team(fleet, rhodes, "RHO-T1",
                new Firefighter("O. Kyriakos", Rank.OFFICER, 10), new Firefighter("A. Lindios", Rank.FIREFIGHTER, 3),
                new Firefighter("T. Kamiros", Rank.FIREFIGHTER, 1));

        // Two vehicles start out of service (maintenance), so utilisation excludes them.
        find(fleet, "CHN-E2").markOutOfService();
        find(fleet, "VOL-E2").markOutOfService();
    }

    private static FireStation station(FleetRegistry fleet, int id, String name, Region region, String area,
            double latitude, double longitude) throws FireResponseException {
        FireStation station = new FireStation(id, name, new Location(region, area, latitude, longitude), STATION_CAPACITY);
        fleet.addStation(station);
        return station;
    }

    private static void engine(FleetRegistry fleet, FireStation station, String name, int litres, boolean fourWheelDrive)
            throws FireResponseException {
        station.addResource(new WildlandEngine(fleet.nextResourceId(), name, litres, fourWheelDrive));
    }

    private static void team(FleetRegistry fleet, FireStation station, String name, Firefighter... members)
            throws FireResponseException {
        ResponseTeam team = new ResponseTeam(fleet.nextResourceId(), name);
        for (int i = 0; i < members.length; i++) {
            team.addMember(members[i]);
        }
        station.addResource(team);
    }

    /** Finds a resource by name. Sample data is fixed, so a missing name is a programming error. */
    private static Resource find(FleetRegistry fleet, String name) {
        Resource[] all = fleet.allResources();
        for (int i = 0; i < all.length; i++) {
            if (all[i].getName().equals(name)) {
                return all[i];
            }
        }
        throw new IllegalStateException("Sample data refers to an unknown resource: " + name);
    }

    // ---- incidents ------------------------------------------------------------------------

    private static void loadIncidents(FleetRegistry fleet, DispatchService dispatch) throws FireResponseException {
        // 1. Marathon foothills: HIGH, active, still under-resourced.
        Incident marathon = report(dispatch, "Fire in pine forest above Marathon", Region.ATTICA, "Marathon foothills",
                38.180, 23.980, Severity.HIGH, at(7, 15, 13, 20));
        assign(dispatch, fleet, marathon, at(7, 15, 13, 35), "MAR-E1", "MAR-E2", "MAR-T1");
        advance(dispatch, marathon, at(7, 15, 13, 36), IncidentStatus.RESPONDING);
        advance(dispatch, marathon, at(7, 15, 14, 5), IncidentStatus.ACTIVE);

        // 2. Penteli: MODERATE, contained, adequately resourced.
        Incident penteli = report(dispatch, "Brush fire on the slopes of Penteli", Region.ATTICA, "Penteli",
                38.050, 23.870, Severity.MODERATE, at(7, 15, 16, 5));
        assign(dispatch, fleet, penteli, at(7, 15, 16, 20), "ATT-E1", "ATT-E2", "ATT-WT1");
        advance(dispatch, penteli, at(7, 15, 16, 21), IncidentStatus.RESPONDING);
        advance(dispatch, penteli, at(7, 15, 16, 40), IncidentStatus.ACTIVE);
        advance(dispatch, penteli, at(7, 15, 19, 30), IncidentStatus.CONTAINED);

        // 3. Northern Evia: CRITICAL, active, large multi-resource operation.
        Incident evia = report(dispatch, "Large forest fire spreading through northern Evia", Region.CENTRAL_GREECE,
                "Prokopi, Evia", 38.680, 23.420, Severity.CRITICAL, at(7, 18, 9, 40));
        assign(dispatch, fleet, evia, at(7, 18, 10, 0), "CHA-E1", "CHA-E2", "CHA-WT1", "ELE-WB1", "ELE-H1", "ATT-CMD1");
        advance(dispatch, evia, at(7, 18, 10, 1), IncidentStatus.RESPONDING);
        advance(dispatch, evia, at(7, 18, 10, 30), IncidentStatus.ACTIVE);

        // 4. Taygetos foothills: HIGH, responding.
        Incident taygetos = report(dispatch, "Fire on the Taygetos foothills near villages", Region.PELOPONNESE,
                "Taygetos foothills", 37.060, 22.170, Severity.HIGH, at(7, 20, 14, 10));
        assign(dispatch, fleet, taygetos, at(7, 20, 14, 25), "KAL-E1", "KAL-T1");
        advance(dispatch, taygetos, at(7, 20, 14, 26), IncidentStatus.RESPONDING);

        // 5. Apokoronas: LOW, just reported.
        report(dispatch, "Small fire in an olive grove", Region.CRETE, "Apokoronas", 35.440, 24.140,
                Severity.LOW, at(7, 22, 11, 30));

        // 6. Rhodes Laerma: MODERATE, active.
        Incident laerma = report(dispatch, "Forest fire near Laerma", Region.SOUTH_AEGEAN, "Laerma", 36.300, 28.030,
                Severity.MODERATE, at(7, 24, 15, 45));
        assign(dispatch, fleet, laerma, at(7, 24, 16, 0), "RHO-E1", "RHO-T1");
        advance(dispatch, laerma, at(7, 24, 16, 1), IncidentStatus.RESPONDING);
        advance(dispatch, laerma, at(7, 24, 16, 30), IncidentStatus.ACTIVE);

        // 7. Pelion: HIGH, fully closed: resources were released again.
        Incident pelion = report(dispatch, "Wildfire on the Pelion mountain slopes", Region.THESSALY, "Pelion",
                39.430, 23.050, Severity.HIGH, at(7, 10, 10, 15));
        assign(dispatch, fleet, pelion, at(7, 10, 10, 30), "VOL-E1", "VOL-H1", "VOL-T1");
        advance(dispatch, pelion, at(7, 10, 10, 31), IncidentStatus.RESPONDING);
        advance(dispatch, pelion, at(7, 10, 11, 0), IncidentStatus.ACTIVE);
        advance(dispatch, pelion, at(7, 11, 2, 0), IncidentStatus.CONTAINED);
        advance(dispatch, pelion, at(7, 11, 9, 30), IncidentStatus.EXTINGUISHED);

        // 8. Seich Sou: MODERATE, closed.
        Incident seichSou = report(dispatch, "Fire in the Seich Sou forest", Region.CENTRAL_MACEDONIA, "Seich Sou",
                40.620, 23.020, Severity.MODERATE, at(7, 5, 17, 40));
        assign(dispatch, fleet, seichSou, at(7, 5, 17, 55), "THE-E1", "THE-E2", "THE-WT1");
        advance(dispatch, seichSou, at(7, 5, 17, 56), IncidentStatus.RESPONDING);
        advance(dispatch, seichSou, at(7, 5, 18, 20), IncidentStatus.ACTIVE);
        advance(dispatch, seichSou, at(7, 5, 20, 0), IncidentStatus.CONTAINED);
        advance(dispatch, seichSou, at(7, 5, 22, 10), IncidentStatus.EXTINGUISHED);

        // 9. Ioannina: LOW, false alarm.
        Incident ioannina = report(dispatch, "Smoke reported near the lake shore", Region.EPIRUS, "Lake Pamvotida",
                39.660, 20.870, Severity.LOW, at(7, 26, 8, 5));
        advance(dispatch, ioannina, at(7, 26, 8, 40), IncidentStatus.FALSE_ALARM);

        // 10. Corfu: MODERATE, just reported.
        report(dispatch, "Fire on the slopes of Pantokrator", Region.IONIAN_ISLANDS, "Pantokrator", 39.750, 19.870,
                Severity.MODERATE, at(8, 2, 12, 30));

        // 11. Lesbos: HIGH, just reported.
        report(dispatch, "Fire spreading in olive groves above Mytilene", Region.NORTH_AEGEAN, "Mytilene hills",
                39.110, 26.550, Severity.HIGH, at(8, 3, 16, 20));

        // 12. Ancient Olympia: CRITICAL, active, under-resourced.
        Incident olympia = report(dispatch, "Fire approaching the archaeological area", Region.WESTERN_GREECE,
                "Ancient Olympia", 37.640, 21.630, Severity.CRITICAL, at(8, 5, 13, 0));
        assign(dispatch, fleet, olympia, at(8, 5, 13, 20), "PAT-E1", "PAT-E2", "PAT-CMD1", "PAT-T1", "ELE-WB2");
        advance(dispatch, olympia, at(8, 5, 13, 21), IncidentStatus.RESPONDING);
        advance(dispatch, olympia, at(8, 5, 13, 50), IncidentStatus.ACTIVE);

        // 13. Nestos delta: LOW, closed.
        Incident nestos = report(dispatch, "Reed fire in the Nestos delta", Region.EASTERN_MACEDONIA_THRACE,
                "Nestos delta", 40.850, 24.800, Severity.LOW, at(7, 30, 19, 10));
        assign(dispatch, fleet, nestos, at(7, 30, 19, 20), "THE-E2");
        advance(dispatch, nestos, at(7, 30, 19, 21), IncidentStatus.RESPONDING);
        advance(dispatch, nestos, at(7, 30, 19, 50), IncidentStatus.ACTIVE);
        advance(dispatch, nestos, at(7, 30, 21, 0), IncidentStatus.CONTAINED);
        advance(dispatch, nestos, at(7, 30, 22, 30), IncidentStatus.EXTINGUISHED);

        // 14. Kozani: MODERATE, false alarm.
        Incident kozani = report(dispatch, "Smoke column reported over farmland", Region.WESTERN_MACEDONIA,
                "Kozani plain", 40.300, 21.790, Severity.MODERATE, at(8, 6, 9, 10));
        advance(dispatch, kozani, at(8, 6, 9, 45), IncidentStatus.FALSE_ALARM);
    }

    private static LocalDateTime at(int month, int day, int hour, int minute) {
        return LocalDateTime.of(2026, month, day, hour, minute);
    }

    private static Incident report(DispatchService dispatch, String description, Region region, String area,
            double latitude, double longitude, Severity severity, LocalDateTime reportedAt)
            throws FireResponseException {
        return dispatch.reportIncident(description, new Location(region, area, latitude, longitude), severity, reportedAt);
    }

    private static void assign(DispatchService dispatch, FleetRegistry fleet, Incident incident, LocalDateTime when,
            String... resourceNames) throws FireResponseException {
        for (int i = 0; i < resourceNames.length; i++) {
            dispatch.assign(incident, find(fleet, resourceNames[i]), when);
        }
    }

    private static void advance(DispatchService dispatch, Incident incident, LocalDateTime when, IncidentStatus next)
            throws FireResponseException {
        dispatch.changeStatus(incident, next, when);
    }
}
