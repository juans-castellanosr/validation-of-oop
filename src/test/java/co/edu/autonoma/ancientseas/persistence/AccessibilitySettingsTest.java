package co.edu.autonoma.ancientseas.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.autonoma.ancientseas.TestSpecies;
import co.edu.autonoma.ancientseas.game.GameMemento;
import co.edu.autonoma.ancientseas.game.GameSession;
import co.edu.autonoma.ancientseas.game.GameSettings;
import co.edu.autonoma.ancientseas.game.InterfaceSize;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AccessibilitySettingsTest {

    @TempDir
    Path dir;

    @Test
    void accessibilityOptionsSurviveSaveAndLoad() throws Exception {
        GameSettings chosen = GameSettings.defaults().withAccessibility(true, InterfaceSize.EXTRA_LARGE);
        Path file = dir.resolve("settings.properties");
        new SettingsStore().save(chosen, file);
        LoadResult<GameSettings> loaded = new SettingsStore().load(file);
        assertEquals(chosen, loaded.value());
        assertFalse(loaded.hasProblems());
    }

    @Test
    void invalidAccessibilityValuesFallBackAndAreReported() throws Exception {
        Path file = dir.resolve("settings.properties");
        Files.writeString(file, "reducedMotion=quizás\ninterfaceSize=GIGANTE\n");
        LoadResult<GameSettings> loaded = new SettingsStore().load(file);
        assertFalse(loaded.value().reducedMotion());
        assertEquals(InterfaceSize.NORMAL, loaded.value().interfaceSize());
        assertEquals(2, loaded.problems().size());
    }

    @Test
    void settingsFileWithoutTheNewKeysUsesTheDefaults() throws Exception {
        Path file = dir.resolve("settings.properties");
        Files.writeString(file, "difficulty=HARD\ncreatureCount=30\neventFrequency=LOW\nplayerName=Ana\n");
        LoadResult<GameSettings> loaded = new SettingsStore().load(file);
        assertFalse(loaded.hasProblems());
        assertFalse(loaded.value().reducedMotion());
        assertEquals(InterfaceSize.NORMAL, loaded.value().interfaceSize());
    }

    @Test
    void saveRoundTripKeepsTheAccessibilityOptions() throws Exception {
        GameSettings chosen = GameSettings.defaults().withAccessibility(true, InterfaceSize.LARGE);
        GameMemento memento = GameSession.newGame(TestSpecies.catalog(), chosen, new Random(4)).toMemento();
        Path file = dir.resolve("save.xml");
        new SaveGameXml().write(memento, file);
        assertEquals(memento, new SaveGameXml().read(file));
    }

    @Test
    void bundledSampleSaveStillLoads() throws Exception {
        Path copy = dir.resolve("sample.xml");
        try (InputStream in = AccessibilitySettingsTest.class.getResourceAsStream("/data/saves/sample-save.xml")) {
            Files.copy(in, copy);
        }
        GameMemento memento = new SaveGameXml().read(copy);
        assertTrue(memento.creatures().stream().anyMatch(c -> c.player()));
    }
}
