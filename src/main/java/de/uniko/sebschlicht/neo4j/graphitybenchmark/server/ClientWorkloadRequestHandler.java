package de.uniko.sebschlicht.neo4j.graphitybenchmark.server;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import net.hh.request_dispatcher.RequestHandler;
import de.metalcon.api.responses.Response;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.commands.GraphityBenchmarkRequest;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.commands.WorkloadRequest;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.commands.WorkloadResponse;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.parser.BenchmarkFileReader;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.parser.Command;

public class ClientWorkloadRequestHandler implements
        RequestHandler<GraphityBenchmarkRequest, Response> {

    protected boolean _running;

    protected boolean _stopping;

    protected AtomicBoolean running;

    protected AtomicInteger progress;

    protected BlockingQueue<Command> primaryCommands;

    protected BlockingQueue<Command> secondaryCommands;

    protected BenchmarkFileReader reader;

    public ClientWorkloadRequestHandler(
            BenchmarkFileReader reader) {
        this.reader = reader;
        primaryCommands = prepareCommands(1000000);
        secondaryCommands = prepareCommands(0);
        running = new AtomicBoolean(false);
        progress = new AtomicInteger(0);
        setRunning(false);
    }

    private BlockingQueue<Command> prepareCommands(int numCommands) {
        BlockingQueue<Command> queue = new LinkedBlockingQueue<Command>();
        try {
            LinkedList<Command> commands =
                    (LinkedList<Command>) reader.loadCommands(numCommands);
            if (commands.size() > 0) {
                System.out.println(commands.size() + " commands parsed");

                Command command;
                while ((command = commands.poll()) != null) {
                    queue.add(command);
                }
                System.out.println("commands loaded");
            } else {
                System.out.println("no more commands available");
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
        while (!_running && !_stopping) {
            long crrMs = System.currentTimeMillis();
            while (System.currentTimeMillis() < crrMs + 100) {
                //TODO how long will clients wait for a response?
                // wait until started
            }
        }
        //TODO separate request necessary?
        if (_stopping) {
            return null;
        }

        if (request instanceof WorkloadRequest) {
            Command command = primaryCommands.poll();
            if (command == null) {
                //TODO make thread-safe
                //TODO start new thread for parsing
                if (secondaryCommands != null && secondaryCommands.size() > 0) {
                    // more commands available
                    primaryCommands = secondaryCommands;
                    try {
                        secondaryCommands = prepareCommands(1000000);
                    } catch (IllegalStateException e) {
                        secondaryCommands = null;
                        System.err.println(e.getMessage());
                    }
                    command = primaryCommands.poll();
                }
            }
            if (command != null) {
                progress.incrementAndGet();
            }
            return new WorkloadResponse(command);
        }

        throw new IllegalArgumentException("unknown request type \""
                + request.getClass() + "\"");
    }

    public boolean isRunning() {
        return running.get();
    }

    public void setRunning(boolean running) {
        _running = running;
        this.running.compareAndSet(!running, running);
    }

    public long getProgress() {
        return progress.get();
    }
}
