package gr.csc.fireresponse.service;

/**
 * A snapshot of how busy a group of resources is. Utilisation is measured against the resources
 * that are in service: out-of-service resources cannot be deployed, so they are not counted as idle.
 */
public class Utilisation {

    private final String label;
    private final int total;
    private final int deployed;
    private final int available;
    private final int outOfService;

    public Utilisation(String label, int total, int deployed, int available, int outOfService) {
        this.label = label;
        this.total = total;
        this.deployed = deployed;
        this.available = available;
        this.outOfService = outOfService;
    }

    public String getLabel() {
        return label;
    }

    public int getTotal() {
        return total;
    }

    public int getDeployed() {
        return deployed;
    }

    public int getAvailable() {
        return available;
    }

    public int getOutOfService() {
        return outOfService;
    }

    public int getInService() {
        return total - outOfService;
    }

    public double getPercentDeployed() {
        if (getInService() == 0) {
            return 0.0;
        }
        return 100.0 * deployed / getInService();
    }
}
