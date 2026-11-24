package co.edu.autonoma.ancientseas.game;

/** Life cycle of one game. */
public enum GameStatus {
    READY,
    RUNNING,
    PAUSED,
    WON,
    LOST,
    ENDED;

    /**
     * Tells whether the game is over and can no longer change.
     *
     * @return {@code true} for won, lost and ended games
     */
    public boolean isFinal() {
        return this == WON || this == LOST || this == ENDED;
    }
}
