package de.uniko.sebschlicht.neo4j.graphitybenchmark.parser;

import java.io.Serializable;

public abstract class Command implements Serializable {

    private static final long serialVersionUID = -4458366361702169885L;

    protected long timestamp;

    public Command(
            long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
