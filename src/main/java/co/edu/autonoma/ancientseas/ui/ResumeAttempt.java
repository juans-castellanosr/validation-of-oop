package co.edu.autonoma.ancientseas.ui;

import co.edu.autonoma.ancientseas.game.GameSession;
import java.util.List;
import java.util.Optional;

/**
 * Outcome of trying to continue a saved game.
 *
 * @param session       the restored game, ready to resume; empty if no save could be opened
 * @param skippedSaves  friendly descriptions of newer saves that were damaged or incompatible
 * @param anySaveFound  whether there was any saved game at all
 */
public record ResumeAttempt(Optional<GameSession> session, List<String> skippedSaves, boolean anySaveFound) {

    /** Copies the list of skipped saves. */
    public ResumeAttempt {
        skippedSaves = List.copyOf(skippedSaves);
    }
}
