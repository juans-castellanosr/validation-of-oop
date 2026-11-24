package co.edu.autonoma.ancientseas.ui;

import co.edu.autonoma.ancientseas.catalog.SpeciesCatalog;
import co.edu.autonoma.ancientseas.game.GameMemento;
import co.edu.autonoma.ancientseas.game.GameSettings;
import co.edu.autonoma.ancientseas.game.Leaderboard;
import co.edu.autonoma.ancientseas.game.ScoreEntry;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * What the interface needs from the rest of the application: data, files and the log. Methods that touch
 * files may block. The short writes ({@link #saveSettings}, {@link #changeAdminPin}, {@link #saveCatalog} and
 * {@link #recordScore}) may run on the Event Dispatch Thread because they happen outside the game and take
 * milliseconds; the other file methods must not.
 */
public interface MuseumServices {

    /** {@return the catalog games use; staff edit a copy, which replaces it when saved} */
    SpeciesCatalog catalog();

    /** {@return friendly messages about problems found while loading the catalog CSV, possibly empty} */
    List<String> catalogProblems();

    /** {@return friendly messages about problems found loading settings, PIN or score history, possibly empty} */
    List<String> startupProblems();

    /** {@return the current settings} */
    GameSettings settings();

    /**
     * Stores new settings for this and future sessions.
     *
     * @param settings the new settings
     * @throws IOException if they cannot be written
     */
    void saveSettings(GameSettings settings) throws IOException;

    /**
     * Checks the administration PIN.
     *
     * @param pin the typed PIN; the caller wipes it
     * @return {@code true} if it is right
     */
    boolean verifyAdminPin(char[] pin);

    /**
     * Replaces the administration PIN.
     *
     * @param pin the new PIN; the caller wipes it
     * @throws IOException              if it cannot be stored
     * @throws IllegalArgumentException if it is not 4 to 12 digits
     */
    void changeAdminPin(char[] pin) throws IOException;

    /**
     * Writes an edited catalog to its CSV file and, once written, makes it the catalog new games use.
     *
     * @param catalog the catalog
     * @throws IOException if it cannot be written
     */
    void saveCatalog(SpeciesCatalog catalog) throws IOException;

    /**
     * Records something that happened in the application log.
     *
     * @param message what happened
     */
    void recordEvent(String message);

    /**
     * Records an unexpected error in the application log.
     *
     * @param message what was being done
     * @param error   the error
     */
    void recordError(String message, Throwable error);

    /** {@return the best scores} */
    Leaderboard leaderboard();

    /**
     * Adds a finished game to the history and the leaderboard.
     *
     * @param entry the finished game
     * @throws IOException if the history cannot be written
     */
    void recordScore(ScoreEntry entry) throws IOException;

    /**
     * Saves a game in progress.
     *
     * @param memento the game state
     * @return the file written
     * @throws IOException if it cannot be written
     */
    Path saveGame(GameMemento memento) throws IOException;

    /**
     * Restores the most recent playable saved game, skipping (and reporting) damaged or incompatible newer ones.
     *
     * @return the outcome
     */
    ResumeAttempt resumeLatestGame();

    /**
     * Exports the whole score history as XML.
     *
     * @return the file written
     * @throws IOException if it cannot be written
     */
    Path exportScores() throws IOException;

    /**
     * Exports the report of one expedition as XML.
     *
     * @param result the finished game
     * @param census living individuals per species at the end
     * @return the file written
     * @throws IOException if it cannot be written
     */
    Path exportReport(ScoreEntry result, Map<String, Integer> census) throws IOException;
}
