package co.edu.autonoma.ancientseas.persistence;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/** Sends {@code java.util.logging} output to an appended plain-text file; installing again replaces the handler. */
public final class ApplicationLog {

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault());

    private static FileHandler installed;

    private ApplicationLog() {
    }

    /**
     * Starts writing log records to the file, replacing a previous installation.
     *
     * @param logFile the log file; created, with its folders, if missing
     * @throws IOException if the file cannot be opened
     */
    public static synchronized void install(Path logFile) throws IOException {
        Files.createDirectories(logFile.toAbsolutePath().getParent());
        uninstall();
        FileHandler handler = new FileHandler(logFile.toAbsolutePath().toString().replace("%", "%%"), true);
        handler.setEncoding(StandardCharsets.UTF_8.name());
        handler.setFormatter(new LineFormatter());
        Logger.getLogger("").addHandler(handler);
        installed = handler;
    }

    /** Stops logging to the file and releases it; does nothing if nothing is installed. */
    public static synchronized void uninstall() {
        if (installed != null) {
            Logger.getLogger("").removeHandler(installed);
            installed.close();
            installed = null;
        }
    }

    private static final class LineFormatter extends Formatter {

        @Override
        public String format(LogRecord record) {
            StringBuilder line = new StringBuilder()
                    .append(TIMESTAMP.format(record.getInstant())).append(' ')
                    .append(record.getLevel().getName()).append(' ')
                    .append(record.getLoggerName()).append(": ")
                    .append(formatMessage(record)).append(System.lineSeparator());
            if (record.getThrown() != null) {
                StringWriter trace = new StringWriter();
                record.getThrown().printStackTrace(new PrintWriter(trace));
                line.append(trace);
            }
            return line.toString();
        }
    }
}
