package de.uniko.sebschlicht.neo4j.graphitybenchmark.parser;

public class ReadStatusUpdateCommand extends Command {

    private static final long serialVersionUID = 1611184101249304285L;

    protected String idReader;

    public ReadStatusUpdateCommand(
            String idReader) {
        super(0);
        this.idReader = idReader;
    }

    public String getReaderId() {
        return idReader;
    }
}
