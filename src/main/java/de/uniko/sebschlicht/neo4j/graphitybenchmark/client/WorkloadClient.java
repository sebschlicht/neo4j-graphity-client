package de.uniko.sebschlicht.neo4j.graphitybenchmark.client;

import java.util.concurrent.TimeoutException;

import net.hh.request_dispatcher.Dispatcher;
import net.hh.request_dispatcher.RequestException;
import de.uniko.sebschlicht.neo4j.GraphityClient;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.commands.GraphityBenchmarkRequest;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.commands.WorkloadRequest;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.commands.WorkloadResponse;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.parser.AddFollowshipCommand;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.parser.AddStatusUpdateCommand;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.parser.Command;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.parser.RemoveFollowshipCommand;

public class WorkloadClient implements Runnable {

    protected static final String GRAPHITY_BENCHMARK_MASTER_ENDPOINT =
            "tcp://127.0.0.1:13000";

    Dispatcher dispatcher;

    GraphityClient client;

    public WorkloadClient() {
        dispatcher = new Dispatcher();
        dispatcher.registerService(GraphityBenchmarkRequest.class,
                GRAPHITY_BENCHMARK_MASTER_ENDPOINT);
        client = new GraphityClient("http://192.168.56.101:7474/");
    }

    public Command getCommand() throws RequestException, TimeoutException {
        WorkloadResponse response =
                (WorkloadResponse) dispatcher.executeSync(
                        new WorkloadRequest(), 100);
        return response.getCommand();
    }

    @Override
    public void run() {
        try {
            Command command;
            boolean exit = false;
            do {
                try {
                    command = getCommand();
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
                        if (client.addStatusUpdate(
                                addStatusUpdateCommand.getAuthorId(),
                                addStatusUpdateCommand.getMessage()) == null) {
                            System.err.println("failed to add status update #"
                                    + addStatusUpdateCommand.getTimestamp()
                                    + " to "
                                    + addStatusUpdateCommand.getAuthorId());
                        }
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
