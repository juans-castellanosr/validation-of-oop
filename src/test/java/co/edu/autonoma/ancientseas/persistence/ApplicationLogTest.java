package co.edu.autonoma.ancientseas.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ApplicationLogTest {

    @TempDir
    Path dir;

    @AfterEach
    void release() {
        ApplicationLog.uninstall();
    }

    private static long fileHandlers() {
        return Arrays.stream(Logger.getLogger("").getHandlers()).filter(FileHandler.class::isInstance).count();
    }

    @Test
    void writesOneLineWithTimestampLevelLoggerAndMessageAndAppendsAcrossRuns() throws Exception {
        Path log = dir.resolve("logs/app.log");
        Logger logger = Logger.getLogger("test.application.log");

        ApplicationLog.install(log);
        logger.log(Level.WARNING, "hola {0}", "mundo");
        ApplicationLog.uninstall();
        ApplicationLog.install(log);
        logger.log(Level.SEVERE, "falló", new IllegalStateException("causa"));
        ApplicationLog.uninstall();

        String text = Files.readString(log);
        String[] lines = text.split("\\R");
        assertTrue(lines[0].matches("\\d{4}-\\d\\d-\\d\\dT.* WARNING test\\.application\\.log: hola mundo"), lines[0]);
        assertTrue(lines[1].endsWith("SEVERE test.application.log: falló"), lines[1]);
        assertTrue(text.contains("java.lang.IllegalStateException: causa"));
    }

    @Test
    void installingTwiceDoesNotDuplicateLines() throws Exception {
        Path log = dir.resolve("app.log");
        long before = fileHandlers();

        ApplicationLog.install(log);
        ApplicationLog.install(log);
        Logger.getLogger("test.once").warning("una vez");

        assertEquals(before + 1, fileHandlers());
        ApplicationLog.uninstall();
        assertEquals(1, Files.readAllLines(log).stream().filter(l -> l.contains("una vez")).count());
        assertEquals(before, fileHandlers());
    }
}
