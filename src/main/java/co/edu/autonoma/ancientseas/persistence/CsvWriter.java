package co.edu.autonoma.ancientseas.persistence;

import java.util.List;
import java.util.stream.Collectors;

final class CsvWriter {

    private CsvWriter() {
    }

    static String line(List<String> fields) {
        return fields.stream().map(CsvWriter::quoteIfNeeded).collect(Collectors.joining(",", "", "\n"));
    }

    private static String quoteIfNeeded(String value) {
        boolean needsQuotes = value.indexOf(',') >= 0 || value.indexOf('"') >= 0 || value.indexOf('\n') >= 0
                || value.indexOf('\r') >= 0 || !value.equals(value.strip());
        return needsQuotes ? '"' + value.replace("\"", "\"\"") + '"' : value;
    }
}
