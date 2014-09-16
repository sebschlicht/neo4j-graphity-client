package de.uniko.sebschlicht.graphity.benchmark.client.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

import com.sun.jersey.api.client.ClientResponse;

import de.uniko.sebschlicht.graphity.benchmark.client.GraphityClient;
import de.uniko.sebschlicht.graphity.benchmark.client.WorkloadClient;

public class Neo4jClient extends AbstractGraphityClient {

    protected static final String URL_PLUGIN =
            "ext/GraphityBaselinePlugin/graphdb/";

    public Neo4jClient(
            String serverUrl) {
        super(serverUrl);
    }

    @Override
    protected String getUrlStatus() {
        return serverUrl + "db/data";
    }

    @Override
    protected String getUrlAddFollowship() {
        return serverUrl + URL_PLUGIN + "follow/";
    }

    @Override
    protected String getUrlRemoveFollowship() {
        return serverUrl + URL_PLUGIN + "unfollow/";
    }

    @Override
    protected String getUrlAddStatusUpdate() {
        return serverUrl + URL_PLUGIN + "post/";
    }

    @Override
    protected String getUrlReadStatusUpdates() {
        return serverUrl + URL_PLUGIN + "feeds/";
    }

    @Override
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

    @Override
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

    @Override
    public long addStatusUpdate(String idAuthor, String message) {
        String jsonString =
                "{\"author\":\"" + idAuthor + "\",\"message\":\"" + message
                        + "\"}";
        ClientResponse response =
                resAddStatusUpdate.accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON).entity(jsonString)
                        .post(ClientResponse.class);
        String responseMessage = response.getEntity(String.class);
        response.close();
        try {
            return Long.parseLong(responseMessage.substring(1,
                    responseMessage.length() - 1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "invalid status update id passed by server\nvalue: \""
                            + responseMessage + "\"");
        }
    }

    @Override
    public int readStatusUpdates(String idReader) {
        String jsonString = "{\"reader\":\"" + idReader + "\"}";
        ClientResponse response =
                resReadStatusUpdates.accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON).entity(jsonString)
                        .post(ClientResponse.class);
        String responseMessage = response.getEntity(String.class);
        response.close();
        try {
            JSONArray statusUpdates =
                    (JSONArray) JSON_PARSER
                            .parse(responseMessage.substring(1,
                                    responseMessage.length() - 1).replace(
                                    "\\\"", "\""));

            return statusUpdates.size();
        } catch (ParseException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(
                    "invalid status update list passed by server\nvalue: \""
                            + responseMessage + "\"");
        }
    }

    public static void main(String[] args) throws IOException {
        String serverUrl = "http://192.168.56.101:7474";
        GraphityClient neoClient = new Neo4jClient(serverUrl);
        final WorkloadClient client = new WorkloadClient(neoClient);
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                try {
                    client.stop();
                } catch (Exception e) {
                    // ship sinking
                }
            }
        });

        BufferedReader in =
                new BufferedReader(new InputStreamReader(System.in));
        String cmd;

        client.start();
        while ((cmd = in.readLine()) != null) {
            if ("exit".equals(cmd)) {
                break;
            } else {
                System.out
                        .println("unknown command. type \"exit\" to shutdown client.");
            }
        }
    }
}
