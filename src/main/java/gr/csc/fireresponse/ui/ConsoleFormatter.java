package gr.csc.fireresponse.ui;

import gr.csc.fireresponse.model.Assignment;
import gr.csc.fireresponse.model.FireStation;
import gr.csc.fireresponse.model.Incident;
import gr.csc.fireresponse.model.IncidentStatus;
import gr.csc.fireresponse.model.Region;
import gr.csc.fireresponse.model.Resource;
import gr.csc.fireresponse.model.Severity;
import gr.csc.fireresponse.service.FleetRegistry;
import gr.csc.fireresponse.service.Utilisation;
import java.time.LocalDateTime;
import java.util.Locale;

/** Turns domain objects and statistics into readable text. It only formats: it never changes any state. */
public class ConsoleFormatter {

    public String title(String text) {
        String rule = TextTable.repeat('=', text.length() + 4);
        return rule + "\n  " + text + "\n" + rule;
    }

    public String dateTime(LocalDateTime value) {
        return value == null ? "-" : value.format(ConsoleInput.DATE_TIME_FORMAT);
    }

    public String incidentTable(Incident[] incidents) {
        String[] headers = {"ID", "Reported", "Region", "Area", "Severity", "Status", "Resources", "Points"};
        TextTable table = new TextTable(headers, Math.max(1, incidents.length)).alignRight(0, 6, 7);
        for (int i = 0; i < incidents.length; i++) {
            Incident incident = incidents[i];
            table.addRow(
                    String.valueOf(incident.getId()),
                    dateTime(incident.getReportedAt()),
                    incident.getRegion().getDisplayName(),
                    incident.getLocation().getAreaName(),
                    incident.getSeverity().getLabel(),
                    incident.getStatus().getLabel(),
                    String.valueOf(incident.getActiveAssignmentCount()),
                    incident.getAssignedPoints() + "/" + incident.getSeverity().getRecommendedPoints());
        }
        return table.render();
    }

    public String incidentDetails(Incident incident, FleetRegistry fleet) {
        StringBuilder text = new StringBuilder();
        text.append("Incident #").append(incident.getId()).append(": ").append(incident.getDescription()).append('\n');
        text.append("  Location     : ").append(incident.getLocation()).append(String.format(Locale.ROOT,
                " (%.3f, %.3f)", incident.getLocation().getLatitude(), incident.getLocation().getLongitude())).append('\n');
        text.append("  Severity     : ").append(incident.getSeverity().getLabel()).append('\n');
        text.append("  Status       : ").append(incident.getStatus().getLabel()).append('\n');
        text.append("  Reported     : ").append(dateTime(incident.getReportedAt())).append('\n');
        text.append("  Last update  : ").append(dateTime(incident.getLastUpdated())).append('\n');
        text.append("  Resourcing   : ").append(incident.getAssignedPoints()).append(" of ")
                .append(incident.getSeverity().getRecommendedPoints()).append(" recommended points (")
                .append(incident.isAdequatelyResourced() ? "adequate" : "insufficient").append(")\n");
        text.append("  Assignments  : ").append(incident.getAssignmentCount()).append(" of ")
                .append(Incident.MAX_ASSIGNMENTS).append(" slots used");
        if (incident.getAssignmentCount() > 0) {
            String[] headers = {"Resource", "Type", "Station", "Assigned", "Released"};
            TextTable table = new TextTable(headers, incident.getAssignmentCount());
            for (int i = 0; i < incident.getAssignmentCount(); i++) {
                Assignment assignment = incident.getAssignment(i);
                FireStation station = fleet.findStationOf(assignment.getResource());
                table.addRow(
                        assignment.getResource().getName(),
                        assignment.getResource().getType().getLabel(),
                        station == null ? "-" : station.getName(),
                        dateTime(assignment.getAssignedAt()),
                        dateTime(assignment.getReleasedAt()));
            }
            text.append('\n').append(table.render());
        }
        return text.toString();
    }

    public String resourceTable(Resource[] resources, FleetRegistry fleet) {
        String[] headers = {"ID", "Name", "Type", "Station", "Status", "Capability", "Points", "Deployments"};
        TextTable table = new TextTable(headers, Math.max(1, resources.length)).alignRight(0, 6, 7);
        for (int i = 0; i < resources.length; i++) {
            Resource resource = resources[i];
            FireStation station = fleet.findStationOf(resource);
            table.addRow(
                    String.valueOf(resource.getId()),
                    resource.getName(),
                    resource.getType().getLabel(),
                    station == null ? "-" : station.getName(),
                    resource.getStatus().getLabel(),
                    resource.describe(),
                    String.valueOf(resource.suppressionPoints()),
                    String.valueOf(resource.getDeploymentCount()));
        }
        return table.render();
    }

    // ---- statistics ------------------------------------------------------------------

    public String severityCounts(int[] counts) {
        Severity[] values = Severity.values();
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = values[i].getLabel();
        }
        return countTable("Severity", labels, counts);
    }

    public String statusCounts(int[] counts) {
        IncidentStatus[] values = IncidentStatus.values();
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = values[i].getLabel();
        }
        return countTable("Status", labels, counts);
    }

    public String regionCounts(int[] counts) {
        return countTable("Region", regionLabels(), counts);
    }

    /** Region x severity matrix with row and column totals. */
    public String regionSeverityMatrix(int[][] matrix) {
        Severity[] severities = Severity.values();
        String[] headers = new String[severities.length + 2];
        headers[0] = "Region";
        for (int s = 0; s < severities.length; s++) {
            headers[s + 1] = severities[s].getLabel();
        }
        headers[headers.length - 1] = "Total";

        int[] columnTotals = new int[severities.length];
        int grandTotal = 0;
        String[] regions = regionLabels();
        TextTable table = new TextTable(headers, regions.length + 1);
        for (int c = 1; c < headers.length; c++) {
            table.alignRight(c);
        }
        for (int r = 0; r < regions.length; r++) {
            String[] cells = new String[headers.length];
            cells[0] = regions[r];
            int rowTotal = 0;
            for (int s = 0; s < severities.length; s++) {
                cells[s + 1] = String.valueOf(matrix[r][s]);
                rowTotal += matrix[r][s];
                columnTotals[s] += matrix[r][s];
            }
            cells[cells.length - 1] = String.valueOf(rowTotal);
            grandTotal += rowTotal;
            table.addRow(cells);
        }
        String[] totals = new String[headers.length];
        totals[0] = "Total";
        for (int s = 0; s < severities.length; s++) {
            totals[s + 1] = String.valueOf(columnTotals[s]);
        }
        totals[totals.length - 1] = String.valueOf(grandTotal);
        table.addRow(totals);
        return table.render();
    }

    public String utilisationTable(String firstColumn, Utilisation[] rows) {
        String[] headers = {firstColumn, "Total", "Deployed", "Available", "Out of service", "Utilisation"};
        TextTable table = new TextTable(headers, Math.max(1, rows.length)).alignRight(1, 2, 3, 4, 5);
        for (int i = 0; i < rows.length; i++) {
            Utilisation row = rows[i];
            if (row.getTotal() == 0) {
                continue;
            }
            table.addRow(row.getLabel(), String.valueOf(row.getTotal()), String.valueOf(row.getDeployed()),
                    String.valueOf(row.getAvailable()), String.valueOf(row.getOutOfService()),
                    String.format(Locale.ROOT, "%.1f%%", row.getPercentDeployed()));
        }
        return table.render();
    }

    public String busiestTable(Resource[] resources, FleetRegistry fleet) {
        String[] headers = {"Rank", "Resource", "Type", "Station", "Deployments"};
        TextTable table = new TextTable(headers, Math.max(1, resources.length)).alignRight(0, 4);
        for (int i = 0; i < resources.length; i++) {
            FireStation station = fleet.findStationOf(resources[i]);
            table.addRow(String.valueOf(i + 1), resources[i].getName(), resources[i].getType().getLabel(),
                    station == null ? "-" : station.getName(), String.valueOf(resources[i].getDeploymentCount()));
        }
        return table.render();
    }

    public String[] regionLabels() {
        Region[] regions = Region.values();
        String[] labels = new String[regions.length];
        for (int i = 0; i < regions.length; i++) {
            labels[i] = regions[i].getDisplayName();
        }
        return labels;
    }

    /** A label/count table with a simple bar made of '#' characters, one per incident. */
    private String countTable(String firstColumn, String[] labels, int[] counts) {
        String[] headers = {firstColumn, "Incidents", "Bar"};
        TextTable table = new TextTable(headers, labels.length).alignRight(1);
        for (int i = 0; i < labels.length; i++) {
            table.addRow(labels[i], String.valueOf(counts[i]), TextTable.repeat('#', counts[i]));
        }
        return table.render();
    }
}
