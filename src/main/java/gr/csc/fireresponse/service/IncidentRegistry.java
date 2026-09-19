package gr.csc.fireresponse.service;

import gr.csc.fireresponse.exception.CapacityExceededException;
import gr.csc.fireresponse.exception.NotFoundException;
import gr.csc.fireresponse.model.Incident;
import gr.csc.fireresponse.model.IncidentStatus;
import gr.csc.fireresponse.model.Region;
import gr.csc.fireresponse.model.Severity;
import gr.csc.fireresponse.util.Validate;

/**
 * Stores incidents in a fixed-size array and answers queries about them.
 *
 * <p>Invariant: the used part of the array is always sorted by id, because ids only grow and
 * new incidents are appended. That invariant is what makes binary search valid.</p>
 */
public class IncidentRegistry {

    private final Incident[] incidents;
    private int count;

    public IncidentRegistry(int capacity) {
        this.incidents = new Incident[Validate.inRange(capacity, 1, 10000, "capacity")];
    }

    public int getCapacity() {
        return incidents.length;
    }

    public int getCount() {
        return count;
    }

    /** The id the next incident must use: one more than the last, or 1 for an empty registry. */
    public int nextId() {
        return count == 0 ? 1 : incidents[count - 1].getId() + 1;
    }

    public void add(Incident incident) throws CapacityExceededException {
        Validate.notNull(incident, "incident");
        if (count == incidents.length) {
            throw new CapacityExceededException("Incident registry", incidents.length);
        }
        if (incident.getId() != nextId()) {
            throw new IllegalArgumentException("Incident id must be " + nextId() + " but was " + incident.getId());
        }
        incidents[count] = incident;
        count++;
    }

    public Incident getByIndex(int index) {
        Validate.inRange(index, 0, count - 1, "index");
        return incidents[index];
    }

    public SearchResult searchByIdLinear(int id) {
        return IncidentSearch.linearSearchById(incidents, count, id);
    }

    public SearchResult searchByIdBinary(int id) {
        return IncidentSearch.binarySearchById(incidents, count, id);
    }

    public Incident findById(int id) throws NotFoundException {
        SearchResult result = searchByIdBinary(id);
        if (!result.isFound()) {
            throw new NotFoundException("Incident", id);
        }
        return incidents[result.getIndex()];
    }

    /** Exact-length copy of the used slots, so callers never see the unused part of the array. */
    public Incident[] toArray() {
        Incident[] copy = new Incident[count];
        System.arraycopy(incidents, 0, copy, 0, count);
        return copy;
    }

    /** Active incidents, most severe first. */
    public Incident[] activeIncidents() {
        return IncidentSearch.sortBySeverityDescending(IncidentSearch.filterActive(incidents, count));
    }

    public Incident[] filterByRegion(Region region) {
        return IncidentSearch.sortBySeverityDescending(IncidentSearch.filterByRegion(incidents, count, region));
    }

    public Incident[] filterByMinSeverity(Severity minimum) {
        return IncidentSearch.sortBySeverityDescending(IncidentSearch.filterByMinSeverity(incidents, count, minimum));
    }

    public Incident[] filterByStatus(IncidentStatus status) {
        return IncidentSearch.sortBySeverityDescending(IncidentSearch.filterByStatus(incidents, count, status));
    }

    public Incident[] searchText(String text) {
        return IncidentSearch.sortBySeverityDescending(IncidentSearch.searchText(incidents, count, text));
    }
}
