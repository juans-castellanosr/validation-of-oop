package co.edu.autonoma.ancientseas.persistence;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.autonoma.ancientseas.admin.AdminAccess;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AdminAccessStoreTest {

    @TempDir
    Path dir;

    private final AdminAccessStore store = new AdminAccessStore();

    @Test
    void missingFileUsesTheDefaultPinWithoutProblems() {
        LoadResult<AdminAccess> result = store.load(dir.resolve("admin.properties"));

        assertFalse(result.hasProblems());
        assertTrue(result.value().verify("1234".toCharArray()));
    }

    @Test
    void savedPinIsRestoredAndTheOldOneStopsWorking() throws Exception {
        Path file = dir.resolve("admin.properties");

        store.save(AdminAccess.fromPin("555000".toCharArray()), file);
        LoadResult<AdminAccess> result = store.load(file);

        assertFalse(result.hasProblems());
        assertTrue(result.value().verify("555000".toCharArray()));
        assertFalse(result.value().verify("1234".toCharArray()));
        assertFalse(Files.readString(file).contains("555000"));
    }

    @Test
    void malformedFileFallsBackToTheDefaultPinAndReportsIt() throws Exception {
        Path file = dir.resolve("admin.properties");
        Files.writeString(file, "pin=not-a-hash\n");
        Path empty = dir.resolve("empty.properties");
        Files.writeString(empty, "");

        for (Path bad : new Path[] {file, empty, dir}) {
            LoadResult<AdminAccess> result = store.load(bad);
            assertTrue(result.hasProblems());
            assertTrue(result.value().verify("1234".toCharArray()));
        }
    }
}
