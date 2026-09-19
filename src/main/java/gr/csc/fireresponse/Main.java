package gr.csc.fireresponse;

import gr.csc.fireresponse.data.SampleData;
import gr.csc.fireresponse.exception.FireResponseException;
import gr.csc.fireresponse.service.DispatchService;
import gr.csc.fireresponse.service.FleetRegistry;
import gr.csc.fireresponse.service.IncidentRegistry;
import gr.csc.fireresponse.service.StatisticsService;
import gr.csc.fireresponse.ui.ConsoleInput;
import gr.csc.fireresponse.ui.ConsoleMenu;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

/**
 * Entry point. It only wires the objects together (who needs whom) and starts the menu.
 *
 * <p>Optional argument {@code --now=2026-08-14T16:00} freezes the clock. Because the services receive
 * a {@link Clock} instead of calling the system time themselves, the same input then always produces
 * the same output, which is how the transcript in the README was captured.</p>
 */
public class Main {

    private static final int INCIDENT_CAPACITY = 60;
    private static final int STATION_CAPACITY = 12;
    private static final String NOW_OPTION = "--now=";

    public static void main(String[] args) {
        Clock clock;
        try {
            clock = createClock(args);
        } catch (DateTimeParseException e) {
            System.err.println("Invalid --now value. Use the ISO format, for example --now=2026-08-14T16:00");
            System.exit(2);
            return;
        }

        IncidentRegistry incidents = new IncidentRegistry(INCIDENT_CAPACITY);
        FleetRegistry fleet = new FleetRegistry(STATION_CAPACITY);
        DispatchService dispatch = new DispatchService(incidents, fleet, clock);
        StatisticsService statistics = new StatisticsService(incidents, fleet);

        try {
            SampleData.populate(fleet, dispatch);
        } catch (FireResponseException e) {
            System.err.println("Could not load the sample data: " + e.getMessage());
            System.exit(1);
        }

        ConsoleInput input = new ConsoleInput(System.in, System.out);
        new ConsoleMenu(input, System.out, incidents, fleet, dispatch, statistics).run();
    }

    private static Clock createClock(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith(NOW_OPTION)) {
                LocalDateTime fixed = LocalDateTime.parse(args[i].substring(NOW_OPTION.length()));
                ZoneId zone = ZoneId.systemDefault();
                return Clock.fixed(fixed.atZone(zone).toInstant(), zone);
            }
        }
        return Clock.systemDefaultZone();
    }
}
