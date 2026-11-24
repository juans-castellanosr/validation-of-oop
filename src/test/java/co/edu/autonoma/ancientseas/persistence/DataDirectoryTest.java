package co.edu.autonoma.ancientseas.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DataDirectoryTest {

    @TempDir
    Path dir;

    @Test
    void pathsAreResolvedInsideTheGivenRootAndTheDefaultIsRelative() {
        DataDirectory data = new DataDirectory(dir.resolve("data"));

        assertEquals(dir.resolve("data/species.csv"), data.speciesFile());
        assertEquals(dir.resolve("data/saves"), data.savesDirectory());
        assertFalse(DataDirectory.defaultLocation().root().isAbsolute());
    }

    @Test
    void seedingCreatesFoldersAndOnlyBundledMissingFiles() throws Exception {
        DataDirectory data = new DataDirectory(dir.resolve("data"));

        List<Path> seeded = data.seedIfMissing();

        assertTrue(Files.isDirectory(data.savesDirectory()));
        assertTrue(Files.isDirectory(data.exportsDirectory()));
        seeded.forEach(p -> assertTrue(Files.isRegularFile(p), p.toString()));
        assertEquals(seeded.contains(data.speciesFile()),
                DataDirectoryTest.class.getResource("/data/species.csv") != null);
    }

    @Test
    void seedingNeverOverwritesStaffEdits() throws Exception {
        DataDirectory data = new DataDirectory(dir.resolve("data"));
        Files.createDirectories(data.root());
        Files.writeString(data.speciesFile(), "edited by staff");

        List<Path> seeded = data.seedIfMissing();

        assertEquals("edited by staff", Files.readString(data.speciesFile()));
        assertFalse(seeded.contains(data.speciesFile()));
        assertTrue(data.seedIfMissing().isEmpty());
    }

    @Test
    void aNewDataFolderStartsWithoutAnySavedGame() throws Exception {
        DataDirectory data = new DataDirectory(dir.resolve("fresh"));

        data.seedIfMissing();

        try (Stream<Path> saves = Files.list(data.savesDirectory())) {
            assertEquals(0, saves.count(), "the sample game is not offered as the player's own progress");
        }
    }
}
