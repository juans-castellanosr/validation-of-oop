package co.edu.autonoma.ancientseas.persistence;

import java.util.List;

final class UnterminatedQuoteException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    private final int line;
    private final transient List<CsvRecord> recordsBefore;

    UnterminatedQuoteException(int line, List<CsvRecord> recordsBefore) {
        super("Hay unas comillas abiertas en la línea " + line + " que nunca se cierran.");
        this.line = line;
        this.recordsBefore = List.copyOf(recordsBefore);
    }

    int line() {
        return line;
    }

    List<CsvRecord> recordsBefore() {
        return recordsBefore;
    }
}
