package gr.csc.fireresponse.service;

import gr.csc.fireresponse.model.Incident;
import gr.csc.fireresponse.model.IncidentStatus;
import gr.csc.fireresponse.model.Region;
import gr.csc.fireresponse.model.Severity;
import gr.csc.fireresponse.util.Validate;
import java.util.Locale;

/**
 * The searching, filtering and sorting algorithms, written out explicitly on arrays.
 *
 * <p>Every method is {@code static} because it is a pure function of its arguments and keeps no
 * state. An array has a <em>capacity</em> (its length) and a <em>count</em> (how many slots are in
 * use), so the search and filter methods take both.</p>
 */
public final class IncidentSearch {

    private IncidentSearch() {
        // Algorithm library: not meant to be instantiated.
    }

    /** A rule that decides whether an incident belongs in a filtered result. */
    public interface Criterion {
        boolean matches(Incident incident);
    }

    /** Linear search: check every element in turn. Works on any order. Cost grows with the count. */
    public static SearchResult linearSearchById(Incident[] incidents, int count, int id) {
        int comparisons = 0;
        for (int i = 0; i < count; i++) {
            comparisons++;
            if (incidents[i].getId() == id) {
                return new SearchResult(i, comparisons);
            }
        }
        return new SearchResult(-1, comparisons);
    }

    /**
     * Binary search: repeatedly halve the range. Requires the array to be sorted by id, which the
     * registry guarantees because ids only ever increase. Cost grows with log2 of the count.
     */
    public static SearchResult binarySearchById(Incident[] incidents, int count, int id) {
        int low = 0;
        int high = count - 1;
        int comparisons = 0;
        while (low <= high) {
            int middle = low + (high - low) / 2;
            comparisons++;
            int middleId = incidents[middle].getId();
            if (middleId == id) {
                return new SearchResult(middle, comparisons);
            }
            if (middleId < id) {
                low = middle + 1;
            } else {
                high = middle - 1;
            }
        }
        return new SearchResult(-1, comparisons);
    }

    /**
     * Generic filter in two passes: first count the matches so the result array can be created with
     * exactly the right size, then copy the matches into it.
     */
    public static Incident[] filter(Incident[] incidents, int count, Criterion criterion) {
        Validate.notNull(criterion, "criterion");
        int matches = 0;
        for (int i = 0; i < count; i++) {
            if (criterion.matches(incidents[i])) {
                matches++;
            }
        }
        Incident[] result = new Incident[matches];
        int next = 0;
        for (int i = 0; i < count; i++) {
            if (criterion.matches(incidents[i])) {
                result[next] = incidents[i];
                next++;
            }
        }
        return result;
    }

    public static Incident[] filterByRegion(Incident[] incidents, int count, Region region) {
        Validate.notNull(region, "region");
        return filter(incidents, count, incident -> incident.getRegion() == region);
    }

    public static Incident[] filterByMinSeverity(Incident[] incidents, int count, Severity minimum) {
        Validate.notNull(minimum, "minimum");
        return filter(incidents, count, incident -> incident.getSeverity().isAtLeast(minimum));
    }

    public static Incident[] filterByStatus(Incident[] incidents, int count, IncidentStatus status) {
        Validate.notNull(status, "status");
        return filter(incidents, count, incident -> incident.getStatus() == status);
    }

    public static Incident[] filterActive(Incident[] incidents, int count) {
        return filter(incidents, count, incident -> incident.isActive());
    }

    /** Case-insensitive text search over the description, area name and region name. */
    public static Incident[] searchText(Incident[] incidents, int count, String text) {
        String needle = Validate.notBlank(text, "text").toLowerCase(Locale.ROOT);
        return filter(incidents, count, incident -> {
            String haystack = (incident.getDescription() + " " + incident.getLocation().getAreaName()
                    + " " + incident.getRegion().getDisplayName()).toLowerCase(Locale.ROOT);
            return haystack.contains(needle);
        });
    }

    /**
     * Insertion sort on a copy: highest severity first; equal severities keep the earliest report first.
     * The original array is left untouched.
     */
    public static Incident[] sortBySeverityDescending(Incident[] incidents) {
        Incident[] sorted = new Incident[incidents.length];
        System.arraycopy(incidents, 0, sorted, 0, incidents.length);
        for (int i = 1; i < sorted.length; i++) {
            Incident current = sorted[i];
            int j = i - 1;
            while (j >= 0 && comesAfter(sorted[j], current)) {
                sorted[j + 1] = sorted[j];
                j--;
            }
            sorted[j + 1] = current;
        }
        return sorted;
    }

    /** True when {@code a} should be placed after {@code b} in the display order. */
    private static boolean comesAfter(Incident a, Incident b) {
        if (a.getSeverity() != b.getSeverity()) {
            return a.getSeverity().getLevel() < b.getSeverity().getLevel();
        }
        return a.getReportedAt().isAfter(b.getReportedAt());
    }
}
