package co.edu.autonoma.ancientseas.game;

import co.edu.autonoma.ancientseas.catalog.Range;
import java.util.Objects;

/**
 * Settings kept between sessions.
 *
 * @param difficulty     how hard the hunters are
 * @param creatureCount  how many animals the ocean starts with
 * @param eventFrequency how often environmental events happen
 * @param playerName     name used by default in the score table
 * @param reducedMotion  whether decorative animation is turned off; never changes how the game plays
 * @param interfaceSize  how large the interface is drawn; applied on the next start
 */
public record GameSettings(Difficulty difficulty, int creatureCount, EventFrequency eventFrequency,
        String playerName, boolean reducedMotion, InterfaceSize interfaceSize) {

    public static final Range<Integer> CREATURE_COUNT = Range.of(15, 90);
    public static final int MAX_NAME_LENGTH = 20;

    /**
     * Validates the settings and trims the player name.
     *
     * @throws IllegalArgumentException if the creature count is out of range or the name is empty or too long
     */
    public GameSettings {
        Objects.requireNonNull(difficulty, "difficulty");
        Objects.requireNonNull(eventFrequency, "eventFrequency");
        Objects.requireNonNull(playerName, "playerName");
        Objects.requireNonNull(interfaceSize, "interfaceSize");
        if (!CREATURE_COUNT.contains(creatureCount)) {
            throw new IllegalArgumentException("creatureCount out of range " + CREATURE_COUNT + ": " + creatureCount);
        }
        playerName = playerName.strip();
        if (playerName.isEmpty() || playerName.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("playerName must have 1 to " + MAX_NAME_LENGTH + " characters");
        }
    }

    /**
     * Creates settings with the default accessibility options.
     *
     * @param difficulty     how hard the game is
     * @param creatureCount  creatures at the start
     * @param eventFrequency how often events happen
     * @param playerName     name shown in the scores
     */
    public GameSettings(Difficulty difficulty, int creatureCount, EventFrequency eventFrequency, String playerName) {
        this(difficulty, creatureCount, eventFrequency, playerName, false, InterfaceSize.NORMAL);
    }

    /**
     * Returns a copy with other accessibility options.
     *
     * @param newReducedMotion whether decorative animation is off
     * @param newSize          the interface size
     * @return the new settings
     */
    public GameSettings withAccessibility(boolean newReducedMotion, InterfaceSize newSize) {
        return new GameSettings(difficulty, creatureCount, eventFrequency, playerName, newReducedMotion, newSize);
    }

    /**
     * Returns the settings used when nothing was saved yet.
     *
     * @return the defaults
     */
    public static GameSettings defaults() {
        return new GameSettings(Difficulty.NORMAL, 45, EventFrequency.NORMAL, "Explorador");
    }
}
