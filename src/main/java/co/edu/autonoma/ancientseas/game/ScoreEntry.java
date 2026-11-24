package co.edu.autonoma.ancientseas.game;

import co.edu.autonoma.ancientseas.model.GrowthStage;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * One finished game in the score history.
 *
 * @param playerName      who played
 * @param score           points earned
 * @param stage           life stage reached
 * @param outcome         how the game ended
 * @param durationSeconds simulation seconds played
 * @param difficulty      difficulty used
 * @param playedAt        when it ended
 */
public record ScoreEntry(String playerName, int score, GrowthStage stage, GameStatus outcome, double durationSeconds,
        Difficulty difficulty, LocalDateTime playedAt) {

    /**
     * Validates the entry.
     *
     * @throws IllegalArgumentException if the score is negative or the outcome is not final
     */
    public ScoreEntry {
        Objects.requireNonNull(playerName, "playerName");
        Objects.requireNonNull(stage, "stage");
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(difficulty, "difficulty");
        Objects.requireNonNull(playedAt, "playedAt");
        if (score < 0) {
            throw new IllegalArgumentException("score must not be negative: " + score);
        }
        if (!outcome.isFinal()) {
            throw new IllegalArgumentException("outcome must be final: " + outcome);
        }
    }
}
