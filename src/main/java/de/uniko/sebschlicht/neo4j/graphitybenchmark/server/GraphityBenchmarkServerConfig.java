package de.uniko.sebschlicht.neo4j.graphitybenchmark.server;

import de.metalcon.utils.Config;
import de.metalcon.zmqworker.ZmqConfig;

public class GraphityBenchmarkServerConfig extends Config implements ZmqConfig {

    public String endpoint;

    public GraphityBenchmarkServerConfig(
            String configPath) {
        super(configPath);
    }

    @Override
    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public int getNumIOThreads() {
        return 1;
    }
}
