package de.uniko.sebschlicht.neo4j.graphitybenchmark.server;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.hh.request_dispatcher.server.RequestHandler;
import de.metalcon.api.responses.Response;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.commands.GraphityBenchmarkRequest;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.commands.WorkloadRequest;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.commands.WorkloadResponse;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.parser.BenchmarkFileReader;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.parser.Command;

public class ClientWorkloadRequestHandler implements
        RequestHandler<GraphityBenchmarkRequest, Response> {

    protected BlockingQueue<Command> primaryCommands;

    protected BlockingQueue<Command> secondaryCommands;

    protected BenchmarkFileReader reader;

    public ClientWorkloadRequestHandler(
            BenchmarkFileReader reader) {
        this.reader = reader;
        primaryCommands = prepareCommands();
        secondaryCommands = prepareCommands();
    }

    private BlockingQueue<Command> prepareCommands() {
        BlockingQueue<Command> queue = new LinkedBlockingQueue<Command>();
        try {
            LinkedList<Command> commands =
                    (LinkedList<Command>) reader.loadCommands(10000000);

            Command command;
            while ((command = commands.getFirst()) != null) {
                queue.add(command);
            }
            return queue;
        } catch (IOException e) {
            throw new IllegalStateException("failed to prepare commands: "
                    + e.getMessage());
        }
    }

    @Override
    public Response handleRequest(GraphityBenchmarkRequest request)
            throws Exception {
        if (request instanceof WorkloadRequest) {
            Command command = primaryCommands.poll();
            if (command == null) {
                //TODO make thread-safe
                //TODO start new thread for parsing
                if (secondaryCommands.size() > 0) {
                    // more commands available
                    primaryCommands = secondaryCommands;
                    secondaryCommands = prepareCommands();
                    command = primaryCommands.poll();
                }
            }

            return new WorkloadResponse(command);
        }

        throw new IllegalArgumentException("unknown request type \""
                + request.getClass() + "\"");
    }
}
