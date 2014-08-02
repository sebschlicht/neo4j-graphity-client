package de.uniko.sebschlicht.neo4j.graphitybenchmark.server;

import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

import de.metalcon.zmqworker.Server;
import de.uniko.sebschlicht.benchmarking.BenchmarkWatch;
import de.uniko.sebschlicht.benchmarking.Benchmarkable;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.commands.GraphityBenchmarkRequest;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.parser.BenchmarkFileReader;

public class WorkloadMaster extends Server<GraphityBenchmarkRequest> implements
        Benchmarkable {

    protected GraphityBenchmarkServerConfig config;

    protected BenchmarkFileReader reader;

    protected ClientWorkloadRequestHandler requestHandler;

    public WorkloadMaster(
            String configPath) throws FileNotFoundException {
        super(new GraphityBenchmarkServerConfig(configPath));
        config = (GraphityBenchmarkServerConfig) getConfig();
        reader = new BenchmarkFileReader(config.getBenchmarkFilePath());
        requestHandler = new ClientWorkloadRequestHandler(reader);
    }

    @Override
    public void init() {
        start(requestHandler);
    }

    @Override
    public long getProgress() {
        return requestHandler.getProgress();
    }

    @Override
    public void close() {
        reader.close();
        super.close();
    }

    @Override
    public void run() {
        // TODO start clients

        requestHandler.setRunning(true);
    }

    @Override
    public boolean isRunning() {
        return requestHandler.isRunning();
    }

    @Override
    public void stop() {
        requestHandler.setRunning(false);
    }

    public static void main(String[] args) throws FileNotFoundException {
        String configPath =
                "src/main/resources/graphity_benchmark_server_config.conf";

        WorkloadMaster master = new WorkloadMaster(configPath);
        BenchmarkWatch watch = new BenchmarkWatch(master);

        watch.setDuration(10, TimeUnit.SECONDS);
        watch.setNumCheckpoints(5);
        watch.measure();
    }
}
