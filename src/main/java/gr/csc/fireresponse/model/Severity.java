package gr.csc.fireresponse.model;

/**
 * How serious an incident is. Each level also carries the suppression capability
 * ("points") that responders should bring, an educational simplification of real dispatch rules.
 */
public enum Severity {
    LOW(1, "Low", 20),
    MODERATE(2, "Moderate", 60),
    HIGH(3, "High", 120),
    CRITICAL(4, "Critical", 200);

    private final int level;
    private final String label;
    private final int recommendedPoints;

    Severity(int level, String label, int recommendedPoints) {
        this.level = level;
        this.label = label;
        this.recommendedPoints = recommendedPoints;
    }

    public int getLevel() {
        return level;
    }

    public String getLabel() {
        return label;
    }

    public int getRecommendedPoints() {
        return recommendedPoints;
    }

    /** Returns true when this severity is the same as or higher than {@code other}. */
    public boolean isAtLeast(Severity other) {
        return this.level >= other.level;
    }
}
