package de.uniko.sebschlicht.graphity.benchmark.parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class BenchmarkFileReader extends CsvParser<Command> {

    public BenchmarkFileReader(
            String filePath) throws FileNotFoundException {
        super(filePath);
    }

    public List<Command> loadCommands(int numCommands) throws IOException {
        String[] entry;
        List<Command> commands = new LinkedList<Command>();

        int numParsed = 0;
        while (numCommands > numParsed && (entry = getEntry()) != null) {
            long timestamp = Long.valueOf(entry[0]);
            if (entry.length == 3 && "U".equals(entry[1])) {
                // update
                commands.add(new AddStatusUpdateCommand(timestamp, entry[2],
                        entry[0]));
            } else if (entry.length == 4 && "A".equals(entry[1])) {
                // add followship
                commands.add(new AddFollowshipCommand(timestamp, entry[2],
                        entry[3]));
            } else if (entry.length == 4 && "R".equals(entry[1])) {
                // remove followship
                commands.add(new RemoveFollowshipCommand(timestamp, entry[2],
                        entry[3]));
            } else {
                throw new IllegalArgumentException("unknown entry of length "
                        + entry.length + ": " + entry[1]);
            }

            ++numParsed;
        }

        return commands;
    }
}
