package co.edu.autonoma.ancientseas.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.autonoma.ancientseas.game.Difficulty;
import co.edu.autonoma.ancientseas.game.GameStatus;
import co.edu.autonoma.ancientseas.game.ScoreEntry;
import co.edu.autonoma.ancientseas.model.GrowthStage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ScoreHistoryStoreTest {

    @TempDir
    Path dir;

    private final ScoreHistoryStore store = new ScoreHistoryStore();

    private static ScoreEntry entry(String name, int score) {
        return new ScoreEntry(name, score, GrowthStage.ADULT, GameStatus.WON, 123.456, Difficulty.HARD,
                LocalDateTime.of(2026, 11, 21, 10, 30));
    }

    @Test
    void missingFileIsAnEmptyHistory() {
        LoadResult<List<ScoreEntry>> result = store.load(dir.resolve("none.csv"));

        assertTrue(result.value().isEmpty());
        assertFalse(result.hasProblems());
    }

    @Test
    void appendCreatesTheHeaderOnceAndKeepsOrderAndAwkwardNames() throws Exception {
        Path file = dir.resolve("sub/scores.csv");
        ScoreEntry first = entry("Ana, \"la\" valiente\nsegunda línea", 10);
        ScoreEntry second = entry("Luis", 20);

        store.append(first, file);
        store.append(second, file);

        assertEquals(1, Files.readAllLines(file).stream().filter(l -> l.startsWith("playerName")).count());
        LoadResult<List<ScoreEntry>> loaded = store.load(file);
        assertFalse(loaded.hasProblems(), loaded.problems().toString());
        assertEquals(List.of(first, second), loaded.value());
    }

    @Test
    void brokenLinesAreSkippedAndReported() throws Exception {
        Path file = dir.resolve("scores.csv");
        store.append(entry("Ana", 10), file);
        Files.writeString(file, String.join("\n",
                "Roto,abc,ADULT,WON,1.0,HARD,2026-01-01T10:00",
                "Corto,1",
                "Negativo,-5,ADULT,WON,1.0,HARD,2026-01-01T10:00",
                "NoFinal,5,ADULT,RUNNING,1.0,HARD,2026-01-01T10:00",
                "Fecha,5,ADULT,WON,1.0,HARD,ayer",
                "Nan,5,ADULT,WON,NaN,HARD,2026-01-01T10:00",
                "Bien,5,adult,lost,1,easy,2026-01-01T10:00:30",
                "Cortada,5,ADU"), java.nio.file.StandardOpenOption.APPEND);

        LoadResult<List<ScoreEntry>> result = store.load(file);

        assertEquals(List.of("Ana", "Bien"), result.value().stream().map(ScoreEntry::playerName).toList());
        assertEquals(7, result.problems().size());
        assertTrue(result.problems().get(0).startsWith("Línea 3"));
    }

    @Test
    void unreadableHistoryGivesAnEmptyListAndAProblem() {
        LoadResult<List<ScoreEntry>> result = store.load(dir);

        assertTrue(result.value().isEmpty());
        assertTrue(result.hasProblems());
    }

    @Test
    void truncatedQuotedLastRecordKeepsEarlierEntriesAndIsReported() throws Exception {
        Path file = dir.resolve("scores.csv");
        store.append(entry("Ana", 10), file);
        Files.writeString(file, "\"Cortado,5,ADU", java.nio.file.StandardOpenOption.APPEND);

        LoadResult<List<ScoreEntry>> result = store.load(file);

        assertEquals(List.of("Ana"), result.value().stream().map(ScoreEntry::playerName).toList());
        assertEquals(1, result.problems().size());
        assertTrue(result.problems().get(0).startsWith("Línea 3"));
    }

    @Test
    void playerNamedLikeTheFirstHeaderWordIsDataNotHeader() throws Exception {
        Path file = dir.resolve("scores.csv");
        ScoreEntry tricky = entry("playerName", 7);
        store.append(tricky, file);
        store.append(entry("Luis", 8), file);

        LoadResult<List<ScoreEntry>> result = store.load(file);

        assertFalse(result.hasProblems(), result.problems().toString());
        assertEquals(List.of("playerName", "Luis"), result.value().stream().map(ScoreEntry::playerName).toList());
    }

    @Test
    void appendAfterACutLastLineStartsANewLine() throws Exception {
        Path file = dir.resolve("scores.csv");
        store.append(entry("Ana", 10), file);
        Files.writeString(file, "Cortada,5,ADU", java.nio.file.StandardOpenOption.APPEND);
        store.append(entry("Luis", 20), file);

        LoadResult<List<ScoreEntry>> result = store.load(file);

        assertEquals(List.of("Ana", "Luis"), result.value().stream().map(ScoreEntry::playerName).toList());
        assertEquals(1, result.problems().size());
    }

    @Test
    void aBrokenQuotedRecordDoesNotSwallowTheGamesAppendedAfterIt() throws Exception {
        Path file = dir.resolve("scores.csv");
        ScoreEntry first = entry("Ana", 10);
        store.append(first, file);
        Files.writeString(file, "\"Cortado,5,ADU", java.nio.file.StandardOpenOption.APPEND);
        ScoreEntry second = entry("Luis", 20);
        ScoreEntry third = entry("María \"la\" curiosa", 30);

        store.append(second, file);
        store.append(third, file);

        LoadResult<List<ScoreEntry>> loaded = store.load(file);
        assertEquals(List.of(first, second, third), loaded.value());
        assertFalse(loaded.hasProblems(), loaded.problems().toString());
        assertTrue(Files.readString(dir.resolve("scores.csv.incomplete")).contains("Cortado,5,ADU"),
                "the broken fragment is kept, not thrown away");
    }
}
