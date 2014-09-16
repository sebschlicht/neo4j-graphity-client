package de.uniko.sebschlicht.graphity.benchmark.parser;

public class AddFollowshipCommand extends Command {

    private static final long serialVersionUID = 1454125563140301100L;

    protected String idFollowing;

    protected String idFollowed;

    public AddFollowshipCommand(
            long timestamp,
            String idFollowing,
            String idFollowed) {
        super(timestamp);
        this.idFollowing = idFollowing;
        this.idFollowed = idFollowed;
    }

    public String getFollowingId() {
        return idFollowing;
    }

    public String getFollowedId() {
        return idFollowed;
    }
}
