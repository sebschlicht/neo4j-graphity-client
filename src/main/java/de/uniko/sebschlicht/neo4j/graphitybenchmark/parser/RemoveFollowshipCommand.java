package de.uniko.sebschlicht.neo4j.graphitybenchmark.parser;

public class RemoveFollowshipCommand extends Command {

    private static final long serialVersionUID = -3857879278729675685L;

    protected String idFollowing;

    protected String idFollowed;

    public RemoveFollowshipCommand(
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
