package gr.csc.fireresponse.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gr.csc.fireresponse.Scenario;
import gr.csc.fireresponse.data.SampleData;
import gr.csc.fireresponse.exception.FireResponseException;
import gr.csc.fireresponse.service.DispatchService;
import gr.csc.fireresponse.service.FleetRegistry;
import gr.csc.fireresponse.service.IncidentRegistry;
import gr.csc.fireresponse.service.StatisticsService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Test;

/** Drives the console menu with scripted input. These tests prove that wrong input never ends the program. */
class ConsoleMenuTest {

    private String run(String... lines) throws FireResponseException {
        StringBuilder script = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            script.append(lines[i]).append('\n');
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true);

        IncidentRegistry incidents = new IncidentRegistry(60);
        FleetRegistry fleet = new FleetRegistry(12);
        DispatchService dispatch = new DispatchService(incidents, fleet, Scenario.fixedClock());
        SampleData.populate(fleet, dispatch);
        ConsoleInput input = new ConsoleInput(new ByteArrayInputStream(script.toString().getBytes()), out);
        new ConsoleMenu(input, out, incidents, fleet, dispatch, new StatisticsService(incidents, fleet)).run();
        return bytes.toString();
    }

    @Test
    void invalidMenuInputIsRejectedAndTheMenuKeepsRunning() throws FireResponseException {
        String output = run("abc", "99", "", "-1", "0");

        assertTrue(output.contains("Please enter a whole number between 0 and 11."));
        assertTrue(output.contains("Goodbye."));
        assertFalse(output.contains("Input ended"));
    }

    @Test
    void endOfInputEndsTheProgramGracefully() throws FireResponseException {
        String output = run();

        assertTrue(output.contains("Input ended. Goodbye."));
    }

    @Test
    void anInvalidAssignmentShowsTheReasonAndReturnsToTheMenu() throws FireResponseException {
        // Incident 5 is a LOW incident in Crete; resource 1 (ATT-E1) is already deployed elsewhere.
        String output = run("4", "5", "1", "0");

        assertTrue(output.contains("! ATT-E1 is not available (status: Deployed)"));
        assertTrue(output.contains("Goodbye."));
    }

    @Test
    void anUnknownIncidentIdIsReportedNotThrown() throws FireResponseException {
        String output = run("3", "500", "0");

        assertTrue(output.contains("! Incident with id 500 was not found"));
        assertTrue(output.contains("Goodbye."));
    }

    @Test
    void anInvalidStatusChangeIsReportedAndTheStatusIsKept() throws FireResponseException {
        // Incident 5 is REPORTED. Menu item 3 in the status list is "Active", which cannot follow REPORTED.
        String output = run("5", "5", "3", "3", "5", "0");

        assertTrue(output.contains("! Cannot change status from Reported to Active"));
        assertTrue(output.contains("Goodbye."));
    }

    @Test
    void reportingAnIncidentValidatesEveryFieldBeforeSavingIt() throws FireResponseException {
        String output = run(
                "1",
                "Smoke above the test hills",   // description
                "1",                            // region: Attica
                "Test hills",                   // area
                "abc", "38.1",                  // latitude: invalid, then valid
                "99", "23.7",                   // longitude: out of range, then valid
                "2",                            // severity: Moderate
                "yesterday", "2026-08-20 10:15",// date/time: invalid, then valid
                "3", "15",                      // show the new incident
                "0");

        assertTrue(output.contains("Please enter a number between 34.5 and 42.0."));
        assertTrue(output.contains("Please enter a number between 19.0 and 29.9."));
        assertTrue(output.contains("Use the format yyyy-MM-dd HH:mm"));
        assertTrue(output.contains("Incident #15 reported (Moderate, Test hills, Attica). Status: Reported."));
        assertTrue(output.contains("Incident #15: Smoke above the test hills"));
        assertTrue(output.contains("2026-08-20 10:15"));
    }

    @Test
    void aValidAssignmentThroughTheMenuIsAppliedAndSummarised() throws FireResponseException {
        // Incident 11 is HIGH and REPORTED. Resource 30 is THE-WT1, an available water tender (ids follow SampleData).
        String output = run("4", "11", "30", "0");

        assertTrue(output.contains("THE-WT1 assigned to incident #11. Resourcing is now 40 of 120 recommended points."));
        assertFalse(output.contains("  ! "));
    }

    @Test
    void searchingByIdShowsTheComparisonCountOfBothAlgorithms() throws FireResponseException {
        String output = run("8", "1", "14", "0");

        assertTrue(output.contains("Linear search: found at index 13 after 14 comparisons"));
        assertTrue(output.contains("Binary search: found at index 13 after 4 comparisons"));
    }

    @Test
    void theTextTableRejectsRowsBeyondItsCapacityAndWrongCellCounts() {
        TextTable table = new TextTable(new String[] {"A", "B"}, 1);
        table.addRow("1", "2");

        assertThrows(IllegalStateException.class, () -> table.addRow("3", "4"));
        assertThrows(IllegalArgumentException.class, () -> new TextTable(new String[] {"A", "B"}, 2).addRow("only one"));
        assertEquals(5, table.render().split("\n").length);
    }
}
