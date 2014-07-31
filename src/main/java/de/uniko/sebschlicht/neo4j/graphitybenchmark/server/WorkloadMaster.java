package de.uniko.sebschlicht.neo4j.graphitybenchmark.server;

import de.metalcon.zmqworker.Server;
import de.uniko.sebschlicht.neo4j.graphitybenchmark.commands.GraphityBenchmarkRequest;

public class WorkloadMaster extends Server<GraphityBenchmarkRequest> {

    public WorkloadMaster(
            String configPath) {
        super(new GraphityBenchmarkServerConfig(configPath));
    }
}
