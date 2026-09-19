package gr.csc.fireresponse.ui;

import gr.csc.fireresponse.exception.FireResponseException;
import gr.csc.fireresponse.model.FireStation;
import gr.csc.fireresponse.model.Incident;
import gr.csc.fireresponse.model.IncidentStatus;
import gr.csc.fireresponse.model.Location;
import gr.csc.fireresponse.model.Region;
import gr.csc.fireresponse.model.Resource;
import gr.csc.fireresponse.model.ResourceStatus;
import gr.csc.fireresponse.model.ResourceType;
import gr.csc.fireresponse.model.Severity;
import gr.csc.fireresponse.service.DispatchService;
import gr.csc.fireresponse.service.FleetRegistry;
import gr.csc.fireresponse.service.IncidentRegistry;
import gr.csc.fireresponse.service.SearchResult;
import gr.csc.fireresponse.service.StatisticsService;
import gr.csc.fireresponse.service.Utilisation;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * The numbered console menu. This class only asks questions, calls the services and prints results:
 * every business rule lives in the model and service packages, so the rules can be tested without a console.
 */
public class ConsoleMenu {

    private static final int EXIT = 0;
    private static final int MAX_MENU_OPTION = 11;
    private static final int TOP_RESOURCES = 5;

    private final ConsoleInput input;
    private final PrintStream out;
    private final IncidentRegistry incidents;
    private final FleetRegistry fleet;
    private final DispatchService dispatch;
    private final StatisticsService statistics;
    private final ConsoleFormatter formatter = new ConsoleFormatter();

    public ConsoleMenu(ConsoleInput input, PrintStream out, IncidentRegistry incidents, FleetRegistry fleet,
            DispatchService dispatch, StatisticsService statistics) {
        this.input = input;
        this.out = out;
        this.incidents = incidents;
        this.fleet = fleet;
        this.dispatch = dispatch;
        this.statistics = statistics;
    }

    /** Runs the menu loop until the user chooses Exit or the input ends. */
    public void run() {
        out.println(formatter.title("Greek Fire Response Coordination System"));
        out.println("CSC - Computer Science Center | https://csc.gr | Educational sample data, not real incidents");
        try {
            boolean running = true;
            while (running) {
                printMenu();
                int choice = input.readInt("Choose an option (0-" + MAX_MENU_OPTION + ")", EXIT, MAX_MENU_OPTION);
                out.println();
                running = handle(choice);
            }
            out.println("Goodbye.");
        } catch (InputEndedException e) {
            out.println();
            out.println("Input ended. Goodbye.");
        }
    }

    private void printMenu() {
        out.println();
        out.println("MAIN MENU");
        out.println("   1. Report a new wildfire incident");
        out.println("   2. List active incidents");
        out.println("   3. Show incident details");
        out.println("   4. Assign a resource to an incident");
        out.println("   5. Change incident status");
        out.println("   6. Show fire service resources");
        out.println("   7. Set a resource out of service / return it to service");
        out.println("   8. Search and filter incidents");
        out.println("   9. Suggest the nearest available resource");
        out.println("  10. Statistics (severity / status / region)");
        out.println("  11. Resource utilisation");
        out.println("   0. Exit");
    }

    /** Runs one menu option. Returns false when the user asked to exit. */
    private boolean handle(int choice) {
        try {
            switch (choice) {
                case 1:
                    reportIncident();
                    break;
                case 2:
                    listActiveIncidents();
                    break;
                case 3:
                    showIncidentDetails();
                    break;
                case 4:
                    assignResource();
                    break;
                case 5:
                    changeStatus();
                    break;
                case 6:
                    showResources();
                    break;
                case 7:
                    changeAvailability();
                    break;
                case 8:
                    searchIncidents();
                    break;
                case 9:
                    suggestNearest();
                    break;
                case 10:
                    showStatistics();
                    break;
                case 11:
                    showUtilisation();
                    break;
                default:
                    return false;
            }
        } catch (FireResponseException | IllegalArgumentException e) {
            // A rule violation or invalid value: report it and return to the menu, never crash.
            out.println("  ! " + e.getMessage());
        }
        return true;
    }

    // ---- menu actions ----------------------------------------------------------------

    private void reportIncident() throws FireResponseException {
        out.println(formatter.title("Report a new wildfire incident"));
        String description = input.readNonBlank("Description");
        Region region = Region.values()[input.chooseIndex("Region", formatter.regionLabels())];
        String area = input.readNonBlank("Area / locality");
        double latitude = input.readDouble("Latitude (" + Location.MIN_LATITUDE + " to " + Location.MAX_LATITUDE + ")",
                Location.MIN_LATITUDE, Location.MAX_LATITUDE);
        double longitude = input.readDouble(
                "Longitude (" + Location.MIN_LONGITUDE + " to " + Location.MAX_LONGITUDE + ")",
                Location.MIN_LONGITUDE, Location.MAX_LONGITUDE);
        Severity severity = Severity.values()[input.chooseIndex("Severity", severityLabels())];
        LocalDateTime reportedAt = input.readDateTimeOrDefault(
                "Reported at (yyyy-MM-dd HH:mm, Enter = now)", dispatch.now());

        Incident incident = dispatch.reportIncident(description, new Location(region, area, latitude, longitude),
                severity, reportedAt);
        out.println("Incident #" + incident.getId() + " reported (" + incident.getSeverity().getLabel()
                + ", " + incident.getLocation() + "). Status: " + incident.getStatus().getLabel() + ".");
    }

    private void listActiveIncidents() {
        out.println(formatter.title("Active incidents (most severe first)"));
        Incident[] active = incidents.activeIncidents();
        out.println(formatter.incidentTable(active));
        out.println(active.length + " active of " + incidents.getCount() + " recorded incidents"
                + " (registry capacity " + incidents.getCapacity() + "). Points = assigned / recommended.");
    }

    private void showIncidentDetails() throws FireResponseException {
        Incident incident = askForIncident();
        out.println(formatter.incidentDetails(incident, fleet));
    }

    private void assignResource() throws FireResponseException {
        out.println(formatter.title("Assign a resource to an incident"));
        Incident incident = askForIncident();
        Resource[] available = fleet.resourcesWithStatus(ResourceStatus.AVAILABLE);
        out.println("Available resources:");
        out.println(formatter.resourceTable(available, fleet));
        int resourceId = input.readInt("Resource id to assign", 1, 9999);
        Resource resource = fleet.findResourceById(resourceId);
        dispatch.assign(incident, resource);
        out.println(resource.getName() + " assigned to incident #" + incident.getId() + ". Resourcing is now "
                + incident.getAssignedPoints() + " of " + incident.getSeverity().getRecommendedPoints()
                + " recommended points.");
    }

    private void changeStatus() throws FireResponseException {
        out.println(formatter.title("Change incident status"));
        Incident incident = askForIncident();
        out.println("Current status: " + incident.getStatus().getLabel());
        IncidentStatus[] allowed = incident.getStatus().allowedNext();
        out.print("Allowed next statuses:");
        if (allowed.length == 0) {
            out.print(" none (the incident is closed)");
        }
        for (int i = 0; i < allowed.length; i++) {
            out.print(" " + allowed[i].getLabel() + (i < allowed.length - 1 ? "," : ""));
        }
        out.println();
        IncidentStatus[] all = IncidentStatus.values();
        String[] labels = new String[all.length];
        for (int i = 0; i < all.length; i++) {
            labels[i] = all[i].getLabel();
        }
        IncidentStatus next = all[input.chooseIndex("New status", labels)];
        dispatch.changeStatus(incident, next);
        out.println("Incident #" + incident.getId() + " is now " + incident.getStatus().getLabel() + ".");
        if (next.isTerminal()) {
            out.println("The incident is closed and its resources were released.");
        }
    }

    private void showResources() {
        out.println(formatter.title("Fire service resources"));
        String[] filters = {"All resources", "Available", "Deployed", "Out of service"};
        int choice = input.chooseIndex("Show", filters);
        Resource[] shown;
        if (choice == 0) {
            shown = fleet.allResources();
        } else {
            ResourceStatus[] statuses = {null, ResourceStatus.AVAILABLE, ResourceStatus.DEPLOYED,
                ResourceStatus.OUT_OF_SERVICE};
            shown = fleet.resourcesWithStatus(statuses[choice]);
        }
        out.println(formatter.resourceTable(shown, fleet));
        out.println(shown.length + " resources shown, " + fleet.countResources() + " in total across "
                + fleet.getStationCount() + " stations.");
    }

    private void changeAvailability() throws FireResponseException {
        out.println(formatter.title("Resource availability"));
        String[] actions = {"Set a resource out of service", "Return a resource to service"};
        int action = input.chooseIndex("Action", actions);
        int resourceId = input.readInt("Resource id", 1, 9999);
        if (action == 0) {
            dispatch.setOutOfService(resourceId);
            out.println("Resource " + resourceId + " is now out of service.");
        } else {
            dispatch.returnToService(resourceId);
            out.println("Resource " + resourceId + " is back in service and available.");
        }
    }

    private void searchIncidents() throws FireResponseException {
        out.println(formatter.title("Search and filter incidents"));
        String[] options = {"By id (linear vs binary search)", "By region", "By minimum severity", "By status",
            "By text in description / area"};
        int choice = input.chooseIndex("Search", options);
        Incident[] found;
        switch (choice) {
            case 0:
                found = searchById();
                break;
            case 1:
                found = incidents.filterByRegion(
                        Region.values()[input.chooseIndex("Region", formatter.regionLabels())]);
                break;
            case 2:
                found = incidents.filterByMinSeverity(
                        Severity.values()[input.chooseIndex("Minimum severity", severityLabels())]);
                break;
            case 3:
                IncidentStatus[] statuses = IncidentStatus.values();
                String[] labels = new String[statuses.length];
                for (int i = 0; i < statuses.length; i++) {
                    labels[i] = statuses[i].getLabel();
                }
                found = incidents.filterByStatus(statuses[input.chooseIndex("Status", labels)]);
                break;
            default:
                found = incidents.searchText(input.readNonBlank("Text to find"));
                break;
        }
        if (found.length == 0) {
            out.println("No incidents match.");
        } else {
            out.println(formatter.incidentTable(found));
            out.println(found.length + (found.length == 1 ? " incident matches" : " incidents match")
                    + " (most severe first).");
        }
    }

    /** Runs both search algorithms on the same id so the number of comparisons can be compared. */
    private Incident[] searchById() throws FireResponseException {
        int id = input.readInt("Incident id", 1, 99999);
        SearchResult linear = incidents.searchByIdLinear(id);
        SearchResult binary = incidents.searchByIdBinary(id);
        out.println("Linear search: " + describe(linear));
        out.println("Binary search: " + describe(binary));
        if (!binary.isFound()) {
            return new Incident[0];
        }
        return new Incident[] {incidents.findById(id)};
    }

    private String describe(SearchResult result) {
        return (result.isFound() ? "found at index " + result.getIndex() : "not found")
                + " after " + result.getComparisons() + " comparisons";
    }

    private void suggestNearest() throws FireResponseException {
        out.println(formatter.title("Suggest the nearest available resource"));
        Incident incident = askForIncident();
        ResourceType[] types = ResourceType.values();
        String[] labels = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            labels[i] = types[i].getLabel();
        }
        ResourceType type = types[input.chooseIndex("Resource type needed", labels)];

        Resource nearest = fleet.findNearestAvailable(incident, type);
        if (nearest == null) {
            out.println("No available " + type.getLabel().toLowerCase(Locale.ROOT)
                    + " suitable for a " + incident.getSeverity().getLabel() + " incident was found.");
            return;
        }
        FireStation station = fleet.findStationOf(nearest);
        double distance = station.getLocation().distanceKmTo(incident.getLocation());
        out.println(String.format(Locale.ROOT, "Nearest: %s (%s) at %s, about %.1f km from the incident.",
                nearest.getName(), nearest.describe(), station.getName(), distance));
        if (input.confirm("Assign it to incident #" + incident.getId() + " now")) {
            dispatch.assign(incident, nearest);
            out.println(nearest.getName() + " assigned to incident #" + incident.getId() + ".");
        }
    }

    private void showStatistics() {
        out.println(formatter.title("Statistics"));
        out.println("Incidents by severity");
        out.println(formatter.severityCounts(statistics.countBySeverity()));
        out.println();
        out.println("Incidents by status");
        out.println(formatter.statusCounts(statistics.countByStatus()));
        out.println();
        out.println("Incidents by region and severity (2D array: rows = regions, columns = severities)");
        out.println(formatter.regionSeverityMatrix(statistics.countByRegionAndSeverity()));
        out.println();
        out.println(statistics.countActiveIncidents() + " of " + incidents.getCount() + " incidents are active.");
    }

    private void showUtilisation() {
        out.println(formatter.title("Resource utilisation"));
        out.println("Utilisation = deployed / in-service resources (out-of-service resources are excluded).");
        out.println();
        out.println("By resource type");
        out.println(formatter.utilisationTable("Type", statistics.utilisationByType()));
        out.println();
        out.println("By station");
        out.println(formatter.utilisationTable("Station", statistics.utilisationByStation()));
        out.println();
        Utilisation overall = statistics.overallUtilisation();
        out.println(String.format(Locale.ROOT, "Overall: %d of %d in-service resources deployed (%.1f%%).",
                overall.getDeployed(), overall.getInService(), overall.getPercentDeployed()));
        out.println();
        out.println("Most frequently deployed resources");
        out.println(formatter.busiestTable(statistics.busiestResources(TOP_RESOURCES), fleet));
    }

    // ---- helpers ---------------------------------------------------------------------

    private Incident askForIncident() throws FireResponseException {
        int id = input.readInt("Incident id", 1, 99999);
        return incidents.findById(id);
    }

    private String[] severityLabels() {
        Severity[] severities = Severity.values();
        String[] labels = new String[severities.length];
        for (int i = 0; i < severities.length; i++) {
            labels[i] = severities[i].getLabel();
        }
        return labels;
    }
}
