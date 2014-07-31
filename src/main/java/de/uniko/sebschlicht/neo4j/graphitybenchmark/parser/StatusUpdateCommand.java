package de.uniko.sebschlicht.neo4j.graphitybenchmark.parser;

public class StatusUpdateCommand extends Command {

    private static final long serialVersionUID = -4318333514414804841L;

    protected String idAuthor;

    public StatusUpdateCommand(
            String idAuthor) {
        this.idAuthor = idAuthor;
    }

    public String getAuthorId() {
        return idAuthor;
    }
}
