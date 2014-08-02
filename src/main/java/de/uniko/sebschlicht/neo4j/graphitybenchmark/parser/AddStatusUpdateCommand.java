package de.uniko.sebschlicht.neo4j.graphitybenchmark.parser;

public class AddStatusUpdateCommand extends Command {

    private static final long serialVersionUID = -4318333514414804841L;

    protected String idAuthor;

    protected String message;

    public AddStatusUpdateCommand(
            long timestamp,
            String idAuthor,
            String message) {
        super(timestamp);
        this.idAuthor = idAuthor;
        this.message = message;
    }

    public String getAuthorId() {
        return idAuthor;
    }

    public String getMessage() {
        return message;
    }
}
