package co.edu.autonoma.ancientseas.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.autonoma.ancientseas.game.Difficulty;
import co.edu.autonoma.ancientseas.game.EventFrequency;
import co.edu.autonoma.ancientseas.game.GameSettings;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SettingsStoreTest {

    @TempDir
    Path dir;

    private final SettingsStore store = new SettingsStore();

    @Test
    void missingFileGivesDefaultsWithoutProblems() {
        LoadResult<GameSettings> result = store.load(dir.resolve("nothing.properties"));

        assertEquals(GameSettings.defaults(), result.value());
        assertFalse(result.hasProblems());
    }

    @Test
    void saveThenLoadKeepsUnicodeNames() throws Exception {
        GameSettings settings = new GameSettings(Difficulty.HARD, 80, EventFrequency.HIGH, "Niño Ñandú ✓");
        Path file = dir.resolve("s.properties");

        store.save(settings, file);

        assertEquals(settings, store.load(file).value());
    }

    @Test
    void garbageValuesFallBackOneByOneAndAreReported() throws Exception {
        Path file = dir.resolve("s.properties");
        Files.writeString(file, "difficulty=hard\ncreatureCount=lots\neventFrequency=sometimes\nplayerName=   \n",
                StandardCharsets.UTF_8);

        LoadResult<GameSettings> result = store.load(file);

        GameSettings defaults = GameSettings.defaults();
        assertEquals(Difficulty.HARD, result.value().difficulty());
        assertEquals(defaults.creatureCount(), result.value().creatureCount());
        assertEquals(defaults.eventFrequency(), result.value().eventFrequency());
        assertEquals(defaults.playerName(), result.value().playerName());
        assertEquals(3, result.problems().size());
    }

    @Test
    void outOfRangeCountAndTooLongNameAreRejected() throws Exception {
        Path file = dir.resolve("s.properties");
        Files.writeString(file, "creatureCount=1000\nplayerName=" + "x".repeat(21) + "\n");

        LoadResult<GameSettings> result = store.load(file);

        assertEquals(GameSettings.defaults(), result.value());
        assertEquals(2, result.problems().size());
    }

    @Test
    void unreadableFileGivesDefaultsAndAProblem() throws Exception {
        Path bad = dir.resolve("bad.properties");
        Files.writeString(bad, "playerName=\\uZZZZ\n");
        Path binary = dir.resolve("binary.properties");
        Files.write(binary, new byte[] {(byte) 0xC3, (byte) 0x28});

        assertTrue(store.load(bad).hasProblems());
        assertEquals(GameSettings.defaults(), store.load(bad).value());
        assertTrue(store.load(binary).hasProblems());
        assertTrue(store.load(dir).hasProblems());
    }
}
