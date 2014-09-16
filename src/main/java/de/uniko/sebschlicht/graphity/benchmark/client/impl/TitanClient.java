package de.uniko.sebschlicht.graphity.benchmark.client.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sun.jersey.api.client.ClientResponse;

import de.uniko.sebschlicht.graphity.benchmark.client.GraphityClient;
import de.uniko.sebschlicht.graphity.benchmark.client.WorkloadClient;

public class TitanClient extends AbstractGraphityClient {

    protected static final String URL_EXTENSIONS = "graphs/graph/graphity/";

    public TitanClient(
            String serverUrl) {
        super(serverUrl);
    }

    @Override
    protected String getUrlStatus() {
        return serverUrl + "graphs/graph/";
    }

    @Override
    protected String getUrlAddFollowship() {
        return serverUrl + URL_EXTENSIONS + "follow/";
    }

    @Override
    protected String getUrlRemoveFollowship() {
        return serverUrl + URL_EXTENSIONS + "unfollow/";
    }

    @Override
    protected String getUrlAddStatusUpdate() {
        return serverUrl + URL_EXTENSIONS + "post/";
    }

    @Override
    protected String getUrlReadStatusUpdates() {
        return serverUrl + URL_EXTENSIONS + "feeds/";
    }

    @Override
    public boolean addFollowship(String followingId, String followedId) {
        String jsonString =
                "{\"following\":\"" + followingId + "\",\"followed\":\""
                        + followedId + "\"}";
        ClientResponse response =
                resAddFollowship.accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON).entity(jsonString)
                        .post(ClientResponse.class);
        String responseMessage = response.getEntity(String.class);
        response.close();
        JSONObject json = parseJson(responseMessage);
        return (json != null)
                ? parseBoolean((String) json.get(KEY_RESPONSE))
                : false;
    }

    @Override
    public boolean removeFollowship(String followingId, String followedId) {
        String jsonString =
                "{\"following\":\"" + followingId + "\",\"followed\":\""
                        + followedId + "\"}";
        ClientResponse response =
                resRemoveFollowship.accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON).entity(jsonString)
                        .post(ClientResponse.class);
        String responseMessage = response.getEntity(String.class);
        response.close();
        JSONObject json = parseJson(responseMessage);
        return (json != null)
                ? parseBoolean((String) json.get(KEY_RESPONSE))
                : false;
    }

    @Override
    public long addStatusUpdate(String authorId, String message) {
        String jsonString =
                "{\"author\":\"" + authorId + "\",\"message\":\"" + message
                        + "\"}";
        ClientResponse response =
                resAddStatusUpdate.accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON).entity(jsonString)
                        .post(ClientResponse.class);
        String responseMessage = response.getEntity(String.class);
        response.close();
        JSONObject json = parseJson(responseMessage);
        return (json != null)
                ? Long.valueOf((String) json.get(KEY_RESPONSE))
                : 0;
    }

    @Override
    public int readStatusUpdates(String readerId) {
        String jsonString = "{\"reader\":\"" + readerId + "\"}";
        ClientResponse response =
                resReadStatusUpdates.accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON).entity(jsonString)
                        .post(ClientResponse.class);
        String responseMessage = response.getEntity(String.class);
        response.close();
        JSONObject json = parseJson(responseMessage);
        if (json != null) {
            JSONArray statusUpdates = (JSONArray) json.get(KEY_RESPONSE);
            return statusUpdates.size();
        }
        throw new IllegalStateException("no status update list");
    }

    public static void main(String[] args) throws IOException {
        String serverUrl = "http://192.168.56.101:8182";
        GraphityClient titanClient = new TitanClient(serverUrl);
        long t = System.currentTimeMillis();
        System.out.println(titanClient.addStatusUpdate("1", "test"));
        System.out.println(System.currentTimeMillis() - t);
        t = System.currentTimeMillis();
        System.out.println(titanClient.addFollowship("2", "1"));
        System.out.println(System.currentTimeMillis() - t);
        t = System.currentTimeMillis();
        System.out.println(titanClient.readStatusUpdates("2"));
        System.out.println(System.currentTimeMillis() - t);
        t = System.currentTimeMillis();
        boolean debug = true;
        if (debug) {
            return;
        }

        final WorkloadClient client = new WorkloadClient(titanClient);
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
