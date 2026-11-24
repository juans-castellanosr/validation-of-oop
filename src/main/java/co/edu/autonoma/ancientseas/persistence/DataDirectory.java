package co.edu.autonoma.ancientseas.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * The folder holding everything staff may edit or back up; resolved from a caller-given path so the installation is portable.
 */
public final class DataDirectory {

    private static final String DEFAULT_FOLDER = "data";

    private final Path root;

    /**
     * Uses the given folder as the data directory; nothing is created until {@link #seedIfMissing()} runs.
     *
     * @param root the data folder
     */
    public DataDirectory(Path root) {
        this.root = root;
    }

    /** {@return the default {@code data} folder, relative to the working directory} */
    public static DataDirectory defaultLocation() {
        return new DataDirectory(Path.of(DEFAULT_FOLDER));
    }

    /** {@return the data folder itself} */
    public Path root() {
        return root;
    }

    /** {@return the species catalog CSV} */
    public Path speciesFile() {
        return root.resolve("species.csv");
    }

    /** {@return the game settings file} */
    public Path settingsFile() {
        return root.resolve("settings.properties");
    }

    /** {@return the administration PIN file} */
    public Path adminFile() {
        return root.resolve("admin.properties");
    }

    /** {@return the score history CSV} */
    public Path scoresFile() {
        return root.resolve("scores.csv");
    }

    /** {@return the application log} */
    public Path logFile() {
        return root.resolve("ancient-seas.log");
    }

    /** {@return the folder of saved games} */
    public Path savesDirectory() {
        return root.resolve("saves");
    }

    /** {@return the folder of exported XML files} */
    public Path exportsDirectory() {
        return root.resolve("exports");
    }

    /**
     * Creates the folders and copies the bundled samples that are missing, never overwriting staff edits.
     *
     * @return the files that were created by this call
     * @throws IOException if a folder or file cannot be created
     */
    public List<Path> seedIfMissing() throws IOException {
        Files.createDirectories(savesDirectory());
        Files.createDirectories(exportsDirectory());
        List<Path> seeded = new ArrayList<>();
        seed("/data/species.csv", speciesFile(), seeded);
        seed("/data/settings.properties", settingsFile(), seeded);
        return seeded;
    }

    private static void seed(String resource, Path target, List<Path> seeded) throws IOException {
        if (Files.exists(target)) {
            return;
        }
        try (InputStream in = DataDirectory.class.getResourceAsStream(resource)) {
            if (in != null) {
                AtomicFile.write(target, in.readAllBytes());
                seeded.add(target);
            }
        }
    }
}
