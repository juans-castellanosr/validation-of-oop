package co.edu.autonoma.ancientseas.persistence;

import co.edu.autonoma.ancientseas.admin.AdminAccess;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

/** Keeps the {@link AdminAccess} hash in a {@code .properties} file; a damaged file falls back to the default PIN. */
public final class AdminAccessStore {

    private static final String PIN_KEY = "pin";

    /** Creates the store; each method receives the file it works on. */
    public AdminAccessStore() {
    }

    /**
     * Loads the stored protection.
     *
     * @param file the properties file; it may not exist yet
     * @return the stored protection, or the default PIN (with a Spanish problem if the file was unusable)
     */
    public LoadResult<AdminAccess> load(Path file) {
        if (!Files.exists(file)) {
            return LoadResult.clean(AdminAccess.withDefaultPin());
        }
        try {
            return LoadResult.clean(AdminAccess.parse(PropertiesFile.read(file).getProperty(PIN_KEY)));
        } catch (IOException | IllegalArgumentException e) {
            return LoadResult.withProblems(AdminAccess.withDefaultPin(), List.of(
                    "No se pudo leer el PIN guardado de administración; se usa el PIN inicial "
                            + AdminAccess.DEFAULT_PIN + ". Cámbielo en cuanto pueda."));
        }
    }

    /**
     * Saves the protection atomically.
     *
     * @param access the protection to keep
     * @param file   the target file
     * @throws IOException if the file cannot be written
     */
    public void save(AdminAccess access, Path file) throws IOException {
        Properties properties = new Properties();
        properties.setProperty(PIN_KEY, access.encode());
        PropertiesFile.write(properties, file, "Ancient Seas administration access (salted hash, not the PIN)");
    }
}
