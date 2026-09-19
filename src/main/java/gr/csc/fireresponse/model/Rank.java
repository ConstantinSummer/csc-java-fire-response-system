package gr.csc.fireresponse.model;

/** A simplified rank scale for team members (not the official Fire Service rank structure). */
public enum Rank {
    FIREFIGHTER(1, "Firefighter"),
    SENIOR_FIREFIGHTER(2, "Senior firefighter"),
    OFFICER(3, "Officer");

    private final int level;
    private final String label;

    Rank(int level, String label) {
        this.level = level;
        this.label = label;
    }

    public int getLevel() {
        return level;
    }

    public String getLabel() {
        return label;
    }
}
