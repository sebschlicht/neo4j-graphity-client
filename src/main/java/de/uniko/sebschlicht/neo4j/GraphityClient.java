package de.uniko.sebschlicht.neo4j;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import de.uniko.sebschlicht.neo4j.graphitybenchmark.parser.BenchmarkFileReader;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.parser.Command;

public class GraphityClient {

    protected WebResource resStatus;

    protected WebResource resAddFollowship;

    protected WebResource resRemoveFollowship;

    protected WebResource resAddStatusUpdate;

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
        return "true".equals(responseMessage);
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
        return "true".equals(responseMessage);
    }

    public Long addStatusUpdate(String idAuthor, String message) {
        String jsonString =
                "{\"author\":\"" + idAuthor + "\",\"message\":\"" + message
                        + "\"}";
        ClientResponse response =
                resAddStatusUpdate.accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON).entity(jsonString)
                        .post(ClientResponse.class);
        String responseMessage = response.getEntity(String.class);
        try {
            return Long.parseLong(responseMessage);
        } catch (NumberFormatException e) {
            System.err
                    .println("\"" + responseMessage + "\" is no valid number");
            return null;
        }
    }

    protected static WebResource createResource(String url) {
        return Client.create().resource(url);
    }

    public static void main(String[] args) throws IOException {
        //        String filePath = "/media/ubuntu-prog/wiki-data/de-events.log";
        String filePath = "/tmp/de-events.log";
        BenchmarkFileReader reader = new BenchmarkFileReader(filePath);

        int i = 0;
        List<Command> commands;
        do {
            commands = reader.loadCommands(10000000);
            i += commands.size();
            System.out.println(i);
        } while (commands.size() > 0);
    }
}
