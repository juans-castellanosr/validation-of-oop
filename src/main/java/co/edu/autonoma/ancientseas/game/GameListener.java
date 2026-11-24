package co.edu.autonoma.ancientseas.game;

/** Receives what happens in a running game, always on the simulation thread (not the Swing one). */
public interface GameListener {

    /**
     * A new picture of the game is available.
     *
     * @param snapshot the latest snapshot
     */
    void onFrame(GameSnapshot snapshot);

    /**
     * Something worth telling the visitor happened.
     *
     * @param message a short Spanish message
     */
    void onMessage(String message);

    /**
     * The game is over (won, lost or ended).
     *
     * @param finalSnapshot the last snapshot
     */
    void onFinished(GameSnapshot finalSnapshot);

    /**
     * The simulation failed unexpectedly and was stopped.
     *
     * @param error the cause
     */
    void onFailure(Throwable error);
}
