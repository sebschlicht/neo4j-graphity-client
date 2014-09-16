package de.uniko.sebschlicht.graphity.benchmark.client;

public interface GraphityClient {

    boolean addFollowship(String followingId, String followedId);

    boolean removeFollowship(String followingId, String followedId);

    long addStatusUpdate(String authorId, String message);

    int readStatusUpdates(String readerId);
}
