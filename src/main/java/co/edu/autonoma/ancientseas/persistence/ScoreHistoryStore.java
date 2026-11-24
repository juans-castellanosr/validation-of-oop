package co.edu.autonoma.ancientseas.persistence;

import co.edu.autonoma.ancientseas.game.Difficulty;
import co.edu.autonoma.ancientseas.game.GameStatus;
import co.edu.autonoma.ancientseas.game.ScoreEntry;
import co.edu.autonoma.ancientseas.model.GrowthStage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Append-only CSV history of finished games; a crash can only cut the last line, which the loader skips and reports.
 */
public final class ScoreHistoryStore {

    private static final List<String> COLUMNS =
            List.of("playerName", "score", "stage", "outcome", "durationSeconds", "difficulty", "playedAt");

    /** Creates the store; each method receives the file it works on. */
    public ScoreHistoryStore() {
    }

    /**
     * Loads the history in the order it was written.
     *
     * @param file the CSV file; it may not exist yet
     * @return the valid entries and a Spanish problem for each line that was skipped
     */
    public LoadResult<List<ScoreEntry>> load(Path file) {
        List<ScoreEntry> entries = new ArrayList<>();
        if (!Files.exists(file)) {
            return LoadResult.clean(entries);
        }
        String text;
        try {
            text = Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return LoadResult.withProblems(entries, List.of("No se pudo leer el historial de partidas."));
        }
        List<String> problems = new ArrayList<>();
        List<CsvRecord> records;
        try {
            records = CsvParser.parse(text, ',');
        } catch (UnterminatedQuoteException e) {
            records = e.recordsBefore();
            problems.add("Línea " + e.line() + " del historial: quedó incompleta. Se omitió.");
        }
        for (int i = 0; i < records.size(); i++) {
            CsvRecord record = records.get(i);
            if (i == 0 && record.fields().equals(COLUMNS)) {
                continue;
            }
            try {
                entries.add(parse(record));
            } catch (IllegalArgumentException | DateTimeException e) {
                problems.add("Línea " + record.line() + " del historial: no se pudo leer (" + e.getMessage()
                        + "). Se omitió.");
            }
        }
        return LoadResult.withProblems(entries, problems);
    }

    /**
     * Adds one entry at the end, creating the file with its header if needed.
     *
     * @param entry the finished game
     * @param file  the CSV file
     * @throws IOException if the file cannot be written
     */
    public void append(ScoreEntry entry, Path file) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        setAsideIncompleteLastRecord(file);
        boolean isNew = !Files.exists(file) || Files.size(file) == 0;
        String separator = !isNew && !endsWithLineFeed(file) ? "\n" : "";
        String text = (isNew ? CsvWriter.line(COLUMNS) : separator) + CsvWriter.line(toFields(entry));
        Files.writeString(file, text, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private static void setAsideIncompleteLastRecord(Path file) throws IOException {
        if (!Files.exists(file)) {
            return;
        }
        String text = Files.readString(file, StandardCharsets.UTF_8);
        try {
            CsvParser.parse(text, ',');
        } catch (UnterminatedQuoteException e) {
            int cut = 0;
            for (int line = 1; line < e.line(); line++) {
                cut = text.indexOf('\n', cut) + 1;
            }
            Path aside = file.resolveSibling(file.getFileName() + ".incomplete");
            Files.writeString(aside, text.substring(cut) + "\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
            AtomicFile.write(file, text.substring(0, cut).getBytes(StandardCharsets.UTF_8));
        }
    }

    private static boolean endsWithLineFeed(Path file) throws IOException {
        try (SeekableByteChannel channel = Files.newByteChannel(file)) {
            channel.position(channel.size() - 1);
            ByteBuffer last = ByteBuffer.allocate(1);
            channel.read(last);
            return last.get(0) == '\n';
        }
    }

    private static ScoreEntry parse(CsvRecord record) {
        List<String> f = record.fields();
        if (f.size() != COLUMNS.size()) {
            throw new IllegalArgumentException("tiene " + f.size() + " campos y se esperaban " + COLUMNS.size());
        }
        String name = f.get(0).strip();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("falta el nombre");
        }
        double duration = Double.parseDouble(f.get(4).strip());
        if (!Double.isFinite(duration) || duration < 0) {
            throw new IllegalArgumentException("la duración debe ser un número positivo");
        }
        return new ScoreEntry(name, Integer.parseInt(f.get(1).strip()), EnumParser.parse(GrowthStage.class, f.get(2)),
                EnumParser.parse(GameStatus.class, f.get(3)), duration, EnumParser.parse(Difficulty.class, f.get(5)),
                LocalDateTime.parse(f.get(6).strip()));
    }

    private static List<String> toFields(ScoreEntry e) {
        return List.of(e.playerName(), Integer.toString(e.score()), e.stage().name(), e.outcome().name(),
                Double.toString(e.durationSeconds()), e.difficulty().name(), e.playedAt().toString());
    }
}
