package gr.csc.fireresponse.model;

import gr.csc.fireresponse.util.Validate;

/** An immutable team member. Firefighters are part of a {@link ResponseTeam}; they are not assigned individually. */
public class Firefighter {

    private final String name;
    private final Rank rank;
    private final int yearsOfService;

    public Firefighter(String name, Rank rank, int yearsOfService) {
        this.name = Validate.notBlank(name, "name");
        Validate.notNull(rank, "rank");
        this.rank = rank;
        this.yearsOfService = Validate.inRange(yearsOfService, 0, 45, "yearsOfService");
    }

    // Constructor chaining: the shorter constructors delegate to the full one.
    public Firefighter(String name, Rank rank) {
        this(name, rank, 0);
    }

    public Firefighter(String name) {
        this(name, Rank.FIREFIGHTER, 0);
    }

    public String getName() {
        return name;
    }

    public Rank getRank() {
        return rank;
    }

    public int getYearsOfService() {
        return yearsOfService;
    }

    @Override
    public String toString() {
        return name + " (" + rank.getLabel() + ")";
    }
}
