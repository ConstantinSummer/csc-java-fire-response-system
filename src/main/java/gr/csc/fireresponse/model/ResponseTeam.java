package gr.csc.fireresponse.model;

import gr.csc.fireresponse.exception.CapacityExceededException;
import gr.csc.fireresponse.util.Validate;

/**
 * A ground crew. A team <em>has</em> firefighters (composition): they live in a fixed-size array
 * whose capacity is {@link #MAX_MEMBERS}, with {@code memberCount} tracking how many slots are used.
 */
public class ResponseTeam extends Resource {

    public static final int MAX_MEMBERS = 6;
    public static final int MIN_MEMBERS_TO_SERVE = 3;

    private final Firefighter[] members = new Firefighter[MAX_MEMBERS];
    private int memberCount;

    public ResponseTeam(int id, String name) {
        super(id, name, ResourceType.RESPONSE_TEAM);
    }

    public void addMember(Firefighter firefighter) throws CapacityExceededException {
        Validate.notNull(firefighter, "firefighter");
        if (memberCount == MAX_MEMBERS) {
            throw new CapacityExceededException("Team " + getName(), MAX_MEMBERS);
        }
        members[memberCount] = firefighter;
        memberCount++;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public Firefighter getMember(int index) {
        Validate.inRange(index, 0, memberCount - 1, "index");
        return members[index];
    }

    /** The leader is the member with the highest rank; the first such member wins a tie. */
    public Firefighter getLeader() {
        Firefighter leader = null;
        for (int i = 0; i < memberCount; i++) {
            if (leader == null || members[i].getRank().getLevel() > leader.getRank().getLevel()) {
                leader = members[i];
            }
        }
        return leader;
    }

    /** A team is only fit to serve when it has enough people: this answer depends on the object's state. */
    @Override
    public boolean canServe(Severity severity) {
        return memberCount >= MIN_MEMBERS_TO_SERVE;
    }

    @Override
    public int suppressionPoints() {
        int points = 0;
        for (int i = 0; i < memberCount; i++) {
            points += 3 * members[i].getRank().getLevel();
        }
        return points;
    }

    @Override
    public String describe() {
        Firefighter leader = getLeader();
        String leaderName = leader == null ? "none" : leader.getName();
        return memberCount + " members, leader " + leaderName;
    }
}
