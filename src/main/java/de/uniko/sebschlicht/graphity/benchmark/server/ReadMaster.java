package de.uniko.sebschlicht.graphity.benchmark.server;

import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

import de.metalcon.zmqworker.Server;
import de.uniko.sebschlicht.benchmarking.BenchmarkWatch;
import de.uniko.sebschlicht.benchmarking.Benchmarkable;
import de.uniko.sebschlicht.graphity.benchmark.commands.GraphityBenchmarkRequest;
import de.uniko.sebschlicht.graphity.benchmark.parser.BenchmarkFileReader;

public class ReadMaster extends Server<GraphityBenchmarkRequest> implements
        Benchmarkable {

    protected GraphityBenchmarkServerConfig config;

    protected BenchmarkFileReader reader;

    protected ClientReadRequestHandler requestHandler;

    public ReadMaster(
            String configPath) throws FileNotFoundException {
        super(new GraphityBenchmarkServerConfig(configPath));
        config = (GraphityBenchmarkServerConfig) getConfig();
        reader = new BenchmarkFileReader(config.getBenchmarkFilePath());
        requestHandler = new ClientReadRequestHandler(reader);
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
        requestHandler.setRunning(true);

        // TODO start clients
    }

    @Override
    public boolean isRunning() {
        return requestHandler.isRunning();
    }

    @Override
    public void stop() {
        requestHandler.setRunning(false);
        super.close();
    }

    public static void main(String[] args) throws FileNotFoundException {
        String configPath =
                "src/main/resources/graphity_benchmark_server_config.conf";

        ReadMaster master = new ReadMaster(configPath);
        BenchmarkWatch watch = new BenchmarkWatch(master);

        watch.setDuration(10, TimeUnit.SECONDS);
        watch.setNumCheckpoints(5);
        watch.measure();
    }
}
