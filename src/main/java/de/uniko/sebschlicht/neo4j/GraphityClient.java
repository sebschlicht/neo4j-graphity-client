package de.uniko.sebschlicht.neo4j;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class GraphityClient {

    protected WebResource resStatus;

    protected WebResource resAddFollowship;

    protected WebResource resRemoveFollowship;

    protected WebResource resAddStatusUpdate;

    protected WebResource resReadStatusUpdates;

    public GraphityClient(
            String serverUrl) {
        String urlRoot = serverUrl + "db/data/";
        resStatus = createResource(urlRoot);
        String urlPlugin = urlRoot + "ext/GraphityBaselinePlugin/graphdb/";

        String urlAddFollowship = urlPlugin + "follow/";
        resAddFollowship = createResource(urlAddFollowship);

        String urlRemoveFollowship = urlPlugin + "unfollow/";
        resRemoveFollowship = createResource(urlRemoveFollowship);

        String urlAddStatusUpdate = urlPlugin + "post/";
        resAddStatusUpdate = createResource(urlAddStatusUpdate);

        String urlReadStatusUpdates = urlPlugin + "feeds/";
        resReadStatusUpdates = createResource(urlReadStatusUpdates);
    }

    public void init() {
        int statusCode = getStatus();
        if (statusCode != ClientResponse.Status.OK.getStatusCode()) {
            throw new IllegalStateException(
                    "server not ready for requests: status code " + statusCode);
        }
    }

    public int getStatus() {
        ClientResponse response = resStatus.get(ClientResponse.class);
        int statusCode = response.getStatus();
        response.close();
        return statusCode;
    }

    private static boolean parseBoolean(String sBoolean) {
        if ("true".equals(sBoolean)) {
            return true;
        } else if ("false".equals(sBoolean)) {
            return false;
        }
        throw new IllegalArgumentException(
                "String value does not represent a boolean value.\nvalue: \""
                        + sBoolean + "\"");
    }

    public boolean addFollowship(String idFollowing, String idFollowed) {
        String jsonString =
                "{\"following\":\"" + idFollowing + "\",\"followed\":\""
                        + idFollowed + "\"}";
        ClientResponse response =
                resAddFollowship.accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON).entity(jsonString)
                        .post(ClientResponse.class);
        String responseMessage = response.getEntity(String.class);
        response.close();
        return parseBoolean(responseMessage);
    }

    public boolean removeFollowship(String idFollowing, String idFollowed) {
        String jsonString =
                "{\"following\":\"" + idFollowing + "\",\"followed\":\""
                        + idFollowed + "\"}";
        ClientResponse response =
                resRemoveFollowship.accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON).entity(jsonString)
                        .post(ClientResponse.class);
        String responseMessage = response.getEntity(String.class);
        response.close();
        return parseBoolean(responseMessage);
    }

    public long addStatusUpdate(String idAuthor, String message) {
        String jsonString =
                "{\"author\":\"" + idAuthor + "\",\"message\":\"" + message
                        + "\"}";
        ClientResponse response =
                resAddStatusUpdate.accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON).entity(jsonString)
                        .post(ClientResponse.class);
        String responseMessage = response.getEntity(String.class);
        try {
            return Long.parseLong(responseMessage.substring(1,
                    responseMessage.length() - 1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "invalid status update id passed by server\nvalue: \""
                            + responseMessage + "\"");
        }
    }

    public int readStatusUpdates(String idReader) {
        String jsonString = "{\"reader\":\"" + idReader + "\"}";
        ClientResponse response =
                resReadStatusUpdates.accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON).entity(jsonString)
                        .post(ClientResponse.class);
        String responseMessage = response.getEntity(String.class);
        try {
            return Integer.parseInt(responseMessage.substring(1,
                    responseMessage.length() - 1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "invalid status update count passed by server\nvalue: \""
                            + responseMessage + "\"");
        }
    }

    protected static WebResource createResource(String url) {
        return Client.create().resource(url);
    }

    public static void main(String[] args) throws IOException {
        GraphityClient client =
                new GraphityClient("http://192.168.56.101:7474/");
        System.out.println(client.addFollowship("1", "3"));
    }
}
