package de.uniko.sebschlicht.neo4j.graphitybenchmark.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

import net.hh.request_dispatcher.Dispatcher;
import net.hh.request_dispatcher.RequestException;
import de.metalcon.api.responses.Response;
import de.uniko.sebschlicht.neo4j.GraphityClient;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.commands.GraphityBenchmarkRequest;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.commands.WorkloadRequest;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.commands.WorkloadResponse;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.parser.AddFollowshipCommand;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.parser.AddStatusUpdateCommand;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.parser.Command;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.parser.ReadStatusUpdateCommand;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.parser.RemoveFollowshipCommand;

public class WorkloadClient implements Runnable {

    protected static final String GRAPHITY_BENCHMARK_MASTER_ENDPOINT =
            "tcp://127.0.0.1:13000";

    Dispatcher dispatcher;

    GraphityClient client;

    protected Thread thread;

    public WorkloadClient(
            String serverUrl) {
        dispatcher = new Dispatcher();
        dispatcher.registerService(GraphityBenchmarkRequest.class,
                GRAPHITY_BENCHMARK_MASTER_ENDPOINT);
        client = new GraphityClient(serverUrl);
    }

    public Command getCommand(boolean first) throws RequestException,
            TimeoutException {
        Response response;
        if (!first) {
            response =
                    (Response) dispatcher.executeSync(new WorkloadRequest(),
                            10000);
        } else {
            response = (Response) dispatcher.executeSync(new WorkloadRequest());
        }

        if (response != null) {
            if (response instanceof WorkloadResponse) {
                return ((WorkloadResponse) response).getCommand();
            }
            throw new IllegalArgumentException("unknown master response "
                    + response.getClass());
        }
        throw new IllegalStateException("connection to master aborted");
    }

    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        dispatcher.shutdown();

    }

    public static void main(String[] args) throws IOException {
        final WorkloadClient client =
                new WorkloadClient("http://192.168.56.101:7474/");
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

    @Override
    public void run() {
        try {
            Command command = null;
            do {
                try {
                    command = getCommand(command == null);
                    if (command == null) {
                        break;
                    }

                    if (command instanceof AddFollowshipCommand) {
                        AddFollowshipCommand addFollowshipCommand =
                                (AddFollowshipCommand) command;
                        if (!client.addFollowship(
                                addFollowshipCommand.getFollowingId(),
                                addFollowshipCommand.getFollowedId())) {
                            System.err.println("failed to add followship #"
                                    + addFollowshipCommand.getTimestamp()
                                    + ": "
                                    + addFollowshipCommand.getFollowingId()
                                    + " -> "
                                    + addFollowshipCommand.getFollowedId());
                        }
                    } else if (command instanceof RemoveFollowshipCommand) {
                        RemoveFollowshipCommand removeFollowshipCommand =
                                (RemoveFollowshipCommand) command;
                        if (!client.removeFollowship(
                                removeFollowshipCommand.getFollowingId(),
                                removeFollowshipCommand.getFollowedId())) {
                            System.err.println("failed to remove followship #"
                                    + removeFollowshipCommand.getTimestamp()
                                    + ": "
                                    + removeFollowshipCommand.getFollowingId()
                                    + " -> "
                                    + removeFollowshipCommand.getFollowedId());
                        }
                    } else if (command instanceof AddStatusUpdateCommand) {
                        AddStatusUpdateCommand addStatusUpdateCommand =
                                (AddStatusUpdateCommand) command;
                        client.addStatusUpdate(
                                addStatusUpdateCommand.getAuthorId(),
                                addStatusUpdateCommand.getMessage());
                    } else if (command instanceof ReadStatusUpdateCommand) {
                        ReadStatusUpdateCommand readStatusUpdateCommand =
                                (ReadStatusUpdateCommand) command;
                        System.out.println(client
                                .readStatusUpdates(readStatusUpdateCommand
                                        .getReaderId())
                                + " status updates read for "
                                + readStatusUpdateCommand.getReaderId());
                    } else {
                        throw new IllegalArgumentException(
                                "unknown response: \"" + command.getClass()
                                        + "\"");
                    }
                } catch (TimeoutException e) {
                    // ignore
                    System.err.println("master timed out");
                }
            } while (true);
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }
}
