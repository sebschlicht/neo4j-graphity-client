package de.uniko.sebschlicht.graphity.benchmark.client.impl;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import de.uniko.sebschlicht.graphity.benchmark.client.GraphityClient;

abstract public class AbstractGraphityClient implements GraphityClient {

    protected static final JSONParser JSON_PARSER = new JSONParser();

    protected static final String KEY_RESPONSE = "responseValue";

    protected String serverUrl;

    protected WebResource resStatus;

    protected WebResource resAddFollowship;

    protected WebResource resRemoveFollowship;

    protected WebResource resAddStatusUpdate;

    protected WebResource resReadStatusUpdates;

    public AbstractGraphityClient(
            String serverUrl) {
        if (serverUrl != null && serverUrl.length() > 0) {
            if (serverUrl.charAt(serverUrl.length() - 1) != '/') {
                this.serverUrl = serverUrl + "/";
            } else {
                this.serverUrl = serverUrl;
            }
        } else {
            throw new IllegalArgumentException("a server URL is necessary");
        }
        init();
    }

    abstract protected String getUrlStatus();

    abstract protected String getUrlAddFollowship();

    abstract protected String getUrlRemoveFollowship();

    abstract protected String getUrlAddStatusUpdate();

    abstract protected String getUrlReadStatusUpdates();

    protected static WebResource getResourceFromUrl(String url) {
        return Client.create().resource(url);
    }

    public int getStatus() {
        ClientResponse response = resStatus.get(ClientResponse.class);
        int statusCode = response.getStatus();
        response.close();
        return statusCode;
    }

    private void init() {
        resStatus = getResourceFromUrl(getUrlStatus());
        resAddFollowship = getResourceFromUrl(getUrlAddFollowship());
        resRemoveFollowship = getResourceFromUrl(getUrlRemoveFollowship());
        resAddStatusUpdate = getResourceFromUrl(getUrlAddStatusUpdate());
        resReadStatusUpdates = getResourceFromUrl(getUrlReadStatusUpdates());

        int statusCode = getStatus();
        if (statusCode != ClientResponse.Status.OK.getStatusCode()) {
            throw new IllegalStateException(
                    "server not ready for requests: status code " + statusCode);
        }
    }

    protected static boolean parseBoolean(String sBoolean) {
        if ("true".equals(sBoolean)) {
            return true;
        } else if ("false".equals(sBoolean)) {
            return false;
        }
        throw new IllegalArgumentException(
                "String value does not represent a boolean value.\nvalue: \""
                        + sBoolean + "\"");
    }

    /**
     * 
     * @param json
     *            JSON string
     * @return parsed JSON object or <b>null</b>
     */
    protected static JSONObject parseJson(String json) {
        try {
            return (JSONObject) JSON_PARSER.parse(json);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
