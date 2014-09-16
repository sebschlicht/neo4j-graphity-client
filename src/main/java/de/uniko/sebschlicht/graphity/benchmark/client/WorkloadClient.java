package de.uniko.sebschlicht.graphity.benchmark.client;

import java.util.concurrent.TimeoutException;

import net.hh.request_dispatcher.Dispatcher;
import net.hh.request_dispatcher.RequestException;
import de.metalcon.api.responses.Response;
import de.uniko.sebschlicht.graphity.benchmark.commands.GraphityBenchmarkRequest;
import de.uniko.sebschlicht.graphity.benchmark.commands.WorkloadRequest;
import de.uniko.sebschlicht.graphity.benchmark.commands.WorkloadResponse;
import de.uniko.sebschlicht.graphity.benchmark.parser.AddFollowshipCommand;
import de.uniko.sebschlicht.graphity.benchmark.parser.AddStatusUpdateCommand;
import de.uniko.sebschlicht.graphity.benchmark.parser.Command;
import de.uniko.sebschlicht.graphity.benchmark.parser.ReadStatusUpdateCommand;
import de.uniko.sebschlicht.graphity.benchmark.parser.RemoveFollowshipCommand;

public class WorkloadClient implements Runnable {

    protected static final String GRAPHITY_BENCHMARK_MASTER_ENDPOINT =
            "tcp://127.0.0.1:13000";

    Dispatcher dispatcher;

    GraphityClient client;

    protected Thread thread;

    public WorkloadClient(
            GraphityClient client) {
        dispatcher = new Dispatcher();
        dispatcher.registerService(GraphityBenchmarkRequest.class,
                GRAPHITY_BENCHMARK_MASTER_ENDPOINT);
        this.client = client;
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
                        int numRead =
                                client.readStatusUpdates(readStatusUpdateCommand
                                        .getReaderId());
                        if (numRead > 0) {
                            System.out.println(numRead
                                    + " status updates read for "
                                    + readStatusUpdateCommand.getReaderId());
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
