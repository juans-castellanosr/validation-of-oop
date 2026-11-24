package co.edu.autonoma.ancientseas.persistence;

import java.util.ArrayList;
import java.util.List;

final class CsvParser {

    private CsvParser() {
    }

    static List<CsvRecord> parse(String text, char delimiter) {
        List<CsvRecord> records = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        int line = 1;
        int recordLine = 1;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inQuotes) {
                if (c == '"' && i + 1 < text.length() && text.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else if (c == '"') {
                    inQuotes = false;
                } else {
                    field.append(c);
                    if (c == '\n') {
                        line++;
                    }
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == delimiter) {
                fields.add(field.toString());
                field.setLength(0);
            } else if (c == '\n' || (c == '\r' && !nextIs(text, i, '\n'))) {
                finishRecord(records, fields, field, recordLine);
                line++;
                recordLine = line;
            } else if (c != '\r') {
                field.append(c);
            }
        }
        if (inQuotes) {
            throw new UnterminatedQuoteException(recordLine, records);
        }
        finishRecord(records, fields, field, recordLine);
        return records;
    }

    private static boolean nextIs(String text, int index, char expected) {
        return index + 1 < text.length() && text.charAt(index + 1) == expected;
    }

    private static void finishRecord(List<CsvRecord> records, List<String> fields, StringBuilder field,
            int recordLine) {
        fields.add(field.toString());
        field.setLength(0);
        boolean blankLine = fields.size() == 1 && fields.get(0).isBlank();
        if (!blankLine) {
            records.add(new CsvRecord(recordLine, List.copyOf(fields)));
        }
        fields.clear();
    }
}
