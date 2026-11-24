package co.edu.autonoma.ancientseas.persistence;

import co.edu.autonoma.ancientseas.game.Difficulty;
import co.edu.autonoma.ancientseas.game.EventFrequency;
import co.edu.autonoma.ancientseas.game.GameSettings;
import co.edu.autonoma.ancientseas.game.InterfaceSize;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Keeps the visitor's {@link GameSettings} in a {@code .properties} file; bad values fall back to defaults and are reported.
 */
public final class SettingsStore {

    private static final String DIFFICULTY = "difficulty";
    private static final String CREATURE_COUNT = "creatureCount";
    private static final String EVENT_FREQUENCY = "eventFrequency";
    private static final String PLAYER_NAME = "playerName";
    private static final String REDUCED_MOTION = "reducedMotion";
    private static final String INTERFACE_SIZE = "interfaceSize";

    /** Creates the store; each method receives the file it works on. */
    public SettingsStore() {
    }

    /**
     * Loads the settings, falling back to the defaults value by value.
     *
     * @param file the properties file; it may not exist yet
     * @return the settings and a Spanish problem for each value or file that could not be used
     */
    public LoadResult<GameSettings> load(Path file) {
        GameSettings defaults = GameSettings.defaults();
        if (!Files.exists(file)) {
            return LoadResult.clean(defaults);
        }
        Properties properties;
        try {
            properties = PropertiesFile.read(file);
        } catch (IOException e) {
            return LoadResult.withProblems(defaults,
                    List.of("No se pudo leer el archivo de ajustes; se usan los valores normales."));
        }
        List<String> problems = new ArrayList<>();
        Difficulty difficulty = readConstant(properties, DIFFICULTY, Difficulty.class, defaults.difficulty(), problems);
        EventFrequency frequency = readConstant(properties, EVENT_FREQUENCY, EventFrequency.class,
                defaults.eventFrequency(), problems);
        int creatureCount = readCreatureCount(properties, defaults.creatureCount(), problems);
        String playerName = readPlayerName(properties, defaults.playerName(), problems);
        boolean reducedMotion = readFlag(properties, REDUCED_MOTION, defaults.reducedMotion(), problems);
        InterfaceSize size = readConstant(properties, INTERFACE_SIZE, InterfaceSize.class, defaults.interfaceSize(),
                problems);
        return LoadResult.withProblems(
                new GameSettings(difficulty, creatureCount, frequency, playerName, reducedMotion, size), problems);
    }

    /**
     * Saves the settings atomically.
     *
     * @param settings the settings to keep
     * @param file     the target file
     * @throws IOException if the file cannot be written
     */
    public void save(GameSettings settings, Path file) throws IOException {
        Properties properties = new Properties();
        properties.setProperty(DIFFICULTY, settings.difficulty().name());
        properties.setProperty(CREATURE_COUNT, Integer.toString(settings.creatureCount()));
        properties.setProperty(EVENT_FREQUENCY, settings.eventFrequency().name());
        properties.setProperty(PLAYER_NAME, settings.playerName());
        properties.setProperty(REDUCED_MOTION, Boolean.toString(settings.reducedMotion()));
        properties.setProperty(INTERFACE_SIZE, settings.interfaceSize().name());
        PropertiesFile.write(properties, file, "Ancient Seas settings");
    }

    private static <E extends Enum<E>> E readConstant(Properties properties, String key, Class<E> type, E fallback,
            List<String> problems) {
        String raw = properties.getProperty(key);
        if (raw == null) {
            return fallback;
        }
        try {
            return EnumParser.parse(type, raw);
        } catch (IllegalArgumentException e) {
            problems.add("El ajuste \"" + key + "\": " + e.getMessage() + " Se usa el valor normal.");
            return fallback;
        }
    }

    private static boolean readFlag(Properties properties, String key, boolean fallback, List<String> problems) {
        String raw = properties.getProperty(key);
        if (raw == null) {
            return fallback;
        }
        String value = raw.strip().toLowerCase(java.util.Locale.ROOT);
        if (value.equals("true") || value.equals("false")) {
            return Boolean.parseBoolean(value);
        }
        problems.add("El ajuste \"" + key + "\" debe ser true o false, pero dice \"" + raw + "\". Se usa el valor normal.");
        return fallback;
    }

    private static int readCreatureCount(Properties properties, int fallback, List<String> problems) {
        String raw = properties.getProperty(CREATURE_COUNT);
        if (raw == null) {
            return fallback;
        }
        String invalid = "El ajuste \"" + CREATURE_COUNT + "\" debe ser un número entre "
                + GameSettings.CREATURE_COUNT.min() + " y " + GameSettings.CREATURE_COUNT.max()
                + ", pero dice \"" + raw + "\". Se usa el valor normal.";
        try {
            int count = Integer.parseInt(raw.strip());
            if (GameSettings.CREATURE_COUNT.contains(count)) {
                return count;
            }
        } catch (NumberFormatException e) {
            problems.add(invalid);
            return fallback;
        }
        problems.add(invalid);
        return fallback;
    }

    private static String readPlayerName(Properties properties, String fallback, List<String> problems) {
        String raw = properties.getProperty(PLAYER_NAME);
        if (raw == null) {
            return fallback;
        }
        String name = raw.strip();
        if (name.isEmpty() || name.length() > GameSettings.MAX_NAME_LENGTH) {
            problems.add("El ajuste \"" + PLAYER_NAME + "\" debe tener entre 1 y " + GameSettings.MAX_NAME_LENGTH
                    + " letras. Se usa el valor normal.");
            return fallback;
        }
        return name;
    }
}
