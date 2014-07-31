package de.uniko.sebschlicht.neo4j.graphitybenchmark.parser;

public class RemoveFollowshipCommand extends Command {

    private static final long serialVersionUID = -3857879278729675685L;

    protected String idFollowing;

    protected String idFollowed;

    public RemoveFollowshipCommand(
            String idFollowing,
            String idFollowed) {
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
