package de.uniko.sebschlicht.graphity.benchmark.server;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import net.hh.request_dispatcher.RequestHandler;
import de.metalcon.api.responses.Response;
import de.uniko.sebschlicht.graphity.benchmark.commands.GraphityBenchmarkRequest;
import de.uniko.sebschlicht.graphity.benchmark.commands.WorkloadRequest;
import de.uniko.sebschlicht.graphity.benchmark.commands.WorkloadResponse;
import de.uniko.sebschlicht.graphity.benchmark.parser.AddStatusUpdateCommand;
import de.uniko.sebschlicht.graphity.benchmark.parser.BenchmarkFileReader;
import de.uniko.sebschlicht.graphity.benchmark.parser.Command;
import de.uniko.sebschlicht.graphity.benchmark.parser.ReadStatusUpdateCommand;

public class ClientReadRequestHandler implements
        RequestHandler<GraphityBenchmarkRequest, Response> {

    private static final long serialVersionUID = 9195494178443992775L;

    protected boolean _running;

    protected boolean _stopping;

    protected AtomicBoolean running;

    protected AtomicInteger progress;

    protected Set<String> pageIds = new HashSet<String>();

    protected Queue<String> primaryIds = new LinkedList<String>();

    protected Queue<String> secondaryIds = new LinkedList<String>();

    protected BenchmarkFileReader reader;

    public ClientReadRequestHandler(
            BenchmarkFileReader reader) {
        this.reader = reader;
        primaryIds = prepareAuthors(10000);
        secondaryIds = prepareAuthors(10000);
        running = new AtomicBoolean(false);
        progress = new AtomicInteger(0);
        setRunning(false);
    }

    private Queue<String> prepareAuthors(int numAuthors) {
        pageIds.clear();
        Queue<String> queue = new LinkedList<String>();
        try {
            LinkedList<Command> commands =
                    (LinkedList<Command>) reader.loadCommands(numAuthors);
            if (commands.size() > 0) {
                System.out.println(commands.size() + " commands parsed");

                Command command;
                while ((command = commands.poll()) != null) {
                    if (command instanceof AddStatusUpdateCommand) {
                        AddStatusUpdateCommand addStatusUpdateCommand =
                                (AddStatusUpdateCommand) command;
                        if (pageIds.add(addStatusUpdateCommand.getAuthorId())) {
                            queue.add(addStatusUpdateCommand.getAuthorId());
                        }
                    }
                }
                if (queue.size() > 0) {
                    System.out.println(queue.size() + " authors loaded");
                }
            }
            if (queue.size() == 0) {
                System.out.println("no more authors available");
            }
            return queue;
        } catch (IOException e) {
            throw new IllegalStateException("failed to prepare authors: "
                    + e.getMessage());
        }
    }

    @Override
    public Response handleRequest(GraphityBenchmarkRequest request)
            throws Exception {
        while (!_running && !_stopping) {
            long crrMs = System.currentTimeMillis();
            while (System.currentTimeMillis() < crrMs + 100) {
                // wait until started
            }
        }
        //TODO separate request necessary?
        if (_stopping) {
            return null;
        }

        if (request instanceof WorkloadRequest) {
            String userId = primaryIds.poll();
            if (userId == null) {
                //TODO make thread-safe
                //TODO start new thread for parsing
                if (secondaryIds != null && secondaryIds.size() > 0) {
                    // more commands available
                    primaryIds = secondaryIds;
                    try {
                        secondaryIds = prepareAuthors(1000000);
                    } catch (IllegalStateException e) {
                        secondaryIds = null;
                        System.err.println(e.getMessage());
                    }
                    userId = primaryIds.poll();
                }
            }
            if (userId != null) {
                progress.incrementAndGet();
            }

            return new WorkloadResponse(new ReadStatusUpdateCommand(userId));
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
