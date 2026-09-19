package gr.csc.fireresponse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gr.csc.fireresponse.Scenario;
import gr.csc.fireresponse.exception.FireResponseException;
import gr.csc.fireresponse.exception.NotFoundException;
import gr.csc.fireresponse.model.Incident;
import gr.csc.fireresponse.model.IncidentStatus;
import gr.csc.fireresponse.model.Location;
import gr.csc.fireresponse.model.Region;
import gr.csc.fireresponse.model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IncidentSearchTest {

    /** Backing array with capacity 10 but only 8 slots in use, like the registry's internal array. */
    private Incident[] raw;
    private static final int COUNT = 8;

    @BeforeEach
    void setUp() throws FireResponseException {
        raw = new Incident[10];
        raw[0] = make(1, Region.ATTICA, "Athens hills", "Brush fire", Severity.LOW, 1);
        raw[1] = make(2, Region.CRETE, "Chania", "Olive grove fire", Severity.HIGH, 2);
        raw[2] = make(3, Region.ATTICA, "Prokopi", "Evia forest fire", Severity.CRITICAL, 3);
        raw[3] = make(4, Region.CRETE, "Heraklion", "Smoke reported", Severity.LOW, 4);
        raw[4] = make(5, Region.ATTICA, "Marathon", "Pine forest fire", Severity.HIGH, 5);
        raw[5] = make(6, Region.EPIRUS, "Ioannina", "Lakeside fire", Severity.MODERATE, 6);
        raw[6] = make(7, Region.CRETE, "Rethymno", "Hillside fire", Severity.MODERATE, 7);
        raw[7] = make(8, Region.ATTICA, "Penteli", "Slope fire", Severity.HIGH, 8);
        raw[3].changeStatus(IncidentStatus.FALSE_ALARM, Scenario.T0.plusHours(9));
        raw[5].changeStatus(IncidentStatus.RESPONDING, Scenario.T0.plusHours(9));
    }

    private Incident make(int id, Region region, String area, String description, Severity severity, int hours) {
        return new Incident(id, description, new Location(region, area, 38.0, 23.7), severity,
                Scenario.T0.plusHours(hours));
    }

    private int[] ids(Incident[] incidents) {
        int[] result = new int[incidents.length];
        for (int i = 0; i < incidents.length; i++) {
            result[i] = incidents[i].getId();
        }
        return result;
    }

    private void assertIds(int[] expected, Incident[] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i].getId());
        }
    }

    // ---- searching by id -------------------------------------------------------------

    @Test
    void linearSearchChecksElementsInOrderAndCountsComparisons() {
        SearchResult first = IncidentSearch.linearSearchById(raw, COUNT, 1);
        SearchResult third = IncidentSearch.linearSearchById(raw, COUNT, 3);
        SearchResult last = IncidentSearch.linearSearchById(raw, COUNT, 8);
        SearchResult missing = IncidentSearch.linearSearchById(raw, COUNT, 99);

        assertEquals(0, first.getIndex());
        assertEquals(1, first.getComparisons());
        assertEquals(2, third.getIndex());
        assertEquals(3, third.getComparisons());
        assertEquals(7, last.getIndex());
        assertEquals(8, last.getComparisons());
        assertFalse(missing.isFound());
        assertEquals(-1, missing.getIndex());
        assertEquals(8, missing.getComparisons());
    }

    @Test
    void binarySearchHalvesTheRangeAndNeedsFewerComparisons() {
        SearchResult middle = IncidentSearch.binarySearchById(raw, COUNT, 4);
        SearchResult last = IncidentSearch.binarySearchById(raw, COUNT, 8);
        SearchResult tooHigh = IncidentSearch.binarySearchById(raw, COUNT, 99);
        SearchResult tooLow = IncidentSearch.binarySearchById(raw, COUNT, 0);

        assertEquals(3, middle.getIndex());
        assertEquals(1, middle.getComparisons());
        assertEquals(7, last.getIndex());
        assertEquals(4, last.getComparisons());
        assertTrue(last.getComparisons() < IncidentSearch.linearSearchById(raw, COUNT, 8).getComparisons());
        assertFalse(tooHigh.isFound());
        assertEquals(4, tooHigh.getComparisons());
        assertFalse(tooLow.isFound());
        assertEquals(3, tooLow.getComparisons());
    }

    @Test
    void bothSearchesIgnoreTheUnusedPartOfTheArray() {
        // Slots 8 and 9 are null. Neither algorithm may look at them.
        assertFalse(IncidentSearch.linearSearchById(raw, COUNT, 99).isFound());
        assertFalse(IncidentSearch.binarySearchById(raw, COUNT, 99).isFound());
        assertFalse(IncidentSearch.linearSearchById(raw, 0, 1).isFound());
        assertEquals(0, IncidentSearch.linearSearchById(raw, 0, 1).getComparisons());
        assertEquals(0, IncidentSearch.binarySearchById(raw, 0, 1).getComparisons());
    }

    @Test
    void theRegistryFindsIncidentsWithBinarySearchAndReportsMissingOnes() throws FireResponseException {
        Scenario scenario = new Scenario();
        scenario.report(Severity.LOW);
        Incident second = scenario.report(Severity.HIGH);

        assertSame(second, scenario.incidents.findById(2));
        assertThrows(NotFoundException.class, () -> scenario.incidents.findById(3));
        assertEquals(2, scenario.incidents.toArray().length);   // exact length, not the capacity of 10
        assertEquals(10, scenario.incidents.getCapacity());
    }

    // ---- filtering -------------------------------------------------------------------

    @Test
    void filtersReturnExactlyTheMatchingIncidentsInOriginalOrder() {
        assertIds(new int[] {1, 3, 5, 8}, IncidentSearch.filterByRegion(raw, COUNT, Region.ATTICA));
        assertIds(new int[] {2, 3, 5, 8}, IncidentSearch.filterByMinSeverity(raw, COUNT, Severity.HIGH));
        assertIds(new int[] {2, 3, 5, 6, 7, 8}, IncidentSearch.filterByMinSeverity(raw, COUNT, Severity.MODERATE));
        assertIds(new int[] {4}, IncidentSearch.filterByStatus(raw, COUNT, IncidentStatus.FALSE_ALARM));
        assertIds(new int[] {6}, IncidentSearch.filterByStatus(raw, COUNT, IncidentStatus.RESPONDING));
        assertIds(new int[] {1, 2, 3, 5, 6, 7, 8}, IncidentSearch.filterActive(raw, COUNT));
    }

    @Test
    void aFilterResultHasExactlyAsManySlotsAsMatches() {
        assertEquals(0, IncidentSearch.filterByRegion(raw, COUNT, Region.THESSALY).length);
        assertEquals(COUNT, IncidentSearch.filterByMinSeverity(raw, COUNT, Severity.LOW).length);
    }

    @Test
    void textSearchIsCaseInsensitiveAndCoversDescriptionAreaAndRegion() {
        assertIds(new int[] {3}, IncidentSearch.searchText(raw, COUNT, "EVIA"));
        assertIds(new int[] {8}, IncidentSearch.searchText(raw, COUNT, "  penteli "));
        assertIds(new int[] {2, 4, 7}, IncidentSearch.searchText(raw, COUNT, "crete"));
        assertEquals(0, IncidentSearch.searchText(raw, COUNT, "volcano").length);
        assertThrows(IllegalArgumentException.class, () -> IncidentSearch.searchText(raw, COUNT, "   "));
    }

    // ---- sorting ---------------------------------------------------------------------

    @Test
    void insertionSortOrdersBySeverityThenByEarliestReport() {
        Incident[] exact = IncidentSearch.filter(raw, COUNT, incident -> true);

        Incident[] sorted = IncidentSearch.sortBySeverityDescending(exact);

        assertIds(new int[] {3, 2, 5, 8, 6, 7, 1, 4}, sorted);
    }

    @Test
    void sortingLeavesTheOriginalArrayUntouched() {
        Incident[] exact = IncidentSearch.filter(raw, COUNT, incident -> true);
        int[] before = ids(exact);

        Incident[] sorted = IncidentSearch.sortBySeverityDescending(exact);

        assertIds(before, exact);
        assertFalse(sorted == exact);
    }

    @Test
    void sortingHandlesEmptyAndSingleElementArrays() {
        assertEquals(0, IncidentSearch.sortBySeverityDescending(new Incident[0]).length);
        Incident[] one = {raw[0]};
        assertSame(raw[0], IncidentSearch.sortBySeverityDescending(one)[0]);
    }

    @Test
    void theRegistryReturnsActiveIncidentsMostSevereFirst() throws FireResponseException {
        Scenario scenario = new Scenario();
        scenario.report(Severity.LOW);                        // id 1
        scenario.report(Severity.CRITICAL);                   // id 2
        scenario.report(Severity.HIGH);                       // id 3
        Incident closed = scenario.report(Severity.CRITICAL); // id 4
        scenario.dispatch.changeStatus(closed, IncidentStatus.FALSE_ALARM);

        assertIds(new int[] {2, 3, 1}, scenario.incidents.activeIncidents());
        assertIds(new int[] {2, 4}, scenario.incidents.filterByMinSeverity(Severity.CRITICAL));
        assertIds(new int[] {2, 4, 3, 1}, scenario.incidents.filterByRegion(Region.ATTICA));
    }
}
