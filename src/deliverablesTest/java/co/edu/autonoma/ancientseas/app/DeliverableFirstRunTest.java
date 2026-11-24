package co.edu.autonoma.ancientseas.app;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.autonoma.ancientseas.game.GameSession;
import co.edu.autonoma.ancientseas.game.GameSettings;
import co.edu.autonoma.ancientseas.persistence.DataDirectory;
import co.edu.autonoma.ancientseas.ui.ResumeAttempt;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DeliverableFirstRunTest {

    private static final Path APP = Path.of(System.getProperty("deliverables.app", "build/deliverables/app"));

    @TempDir
    Path installation;

    private Path data;

    @BeforeEach
    void copyTheDeliveredDataFolder() throws IOException {
        Path source = APP.resolve("data");
        assertTrue(Files.isDirectory(source), "missing delivered data folder " + source);
        data = installation.resolve("data");
        List<Path> tree;
        try (Stream<Path> files = Files.walk(source)) {
            tree = files.toList();
        }
        for (Path from : tree) {
            Path to = data.resolve(source.relativize(from).toString());
            if (Files.isDirectory(from)) {
                Files.createDirectories(to);
            } else {
                Files.copy(from, to);
            }
        }
    }

    private FileMuseumServices start() throws IOException {
        DataDirectory directory = new DataDirectory(data);
        directory.seedIfMissing();
        return new FileMuseumServices(directory);
    }

    @Test
    void theDeliveredDataFolderHoldsNoSavedGame() throws IOException {
        Path saves = APP.resolve("data").resolve("saves");
        if (Files.isDirectory(saves)) {
            try (Stream<Path> files = Files.list(saves)) {
                assertFalse(files.findAny().isPresent(), "the delivered saves folder must be empty");
            }
        }
        assertTrue(Files.isRegularFile(APP.resolve("examples").resolve("sample-save.xml")),
                "the sample save must ship in the examples folder");
    }

    @Test
    void aFirstRunFindsNoSavedGame() throws IOException {
        ResumeAttempt attempt = start().resumeLatestGame();

        assertFalse(attempt.session().isPresent());
        assertFalse(attempt.anySaveFound(), "Continuar must report that there are no saved games");
    }

    @Test
    void aGameSavedOnTheFirstRunResumesAfterARestart() throws IOException {
        FileMuseumServices firstRun = start();
        GameSession game = GameSession.newGame(firstRun.catalog(), GameSettings.defaults(), new Random(7));
        firstRun.saveGame(game.toMemento());

        ResumeAttempt attempt = start().resumeLatestGame();

        assertTrue(attempt.session().isPresent(), "the visitor's own save must resume");
    }

    @Test
    void theSampleSaveResumesWhenCopiedIntoTheSavesFolder() throws IOException {
        FileMuseumServices services = start();
        Files.copy(APP.resolve("examples").resolve("sample-save.xml"), data.resolve("saves").resolve("sample-save.xml"));

        ResumeAttempt attempt = services.resumeLatestGame();

        assertTrue(attempt.session().isPresent(), "the delivered sample save must stay valid");
        assertTrue(attempt.skippedSaves().isEmpty());
    }
}
