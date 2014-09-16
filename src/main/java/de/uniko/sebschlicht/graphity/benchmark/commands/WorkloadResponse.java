package de.uniko.sebschlicht.graphity.benchmark.commands;

import de.metalcon.api.responses.Response;
import de.uniko.sebschlicht.graphity.benchmark.parser.Command;

public class WorkloadResponse extends Response {

    private static final long serialVersionUID = 6356828564844091669L;

    protected Command command;

    public WorkloadResponse(
            Command command) {
        super(null);
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }
}
