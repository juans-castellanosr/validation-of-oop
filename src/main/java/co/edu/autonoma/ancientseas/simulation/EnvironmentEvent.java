package co.edu.autonoma.ancientseas.simulation;

/** Something that happens to the whole ocean for a while. */
public abstract class EnvironmentEvent {

    private final double durationSeconds;
    private double elapsedSeconds;

    protected EnvironmentEvent(double durationSeconds, double elapsedSeconds) {
        this.durationSeconds = durationSeconds;
        this.elapsedSeconds = elapsedSeconds;
    }

    final void tick(Ocean ocean, double dt) {
        if (elapsedSeconds == 0) {
            begin(ocean);
        }
        apply(ocean);
        elapsedSeconds += dt;
    }

    protected void begin(Ocean ocean) {
    }

    protected void apply(Ocean ocean) {
    }

    /**
     * Returns the message announced to the visitor when the event starts.
     *
     * @return a short, friendly Spanish sentence
     */
    public abstract String announcement();

    /**
     * Returns the event kind.
     *
     * @return the type
     */
    public abstract EventType type();

    /**
     * Captures the event for a saved game.
     *
     * @return the saved form
     */
    public abstract EventState toState();

    /**
     * Tells whether the event is over.
     *
     * @return {@code true} once its duration has passed
     */
    public final boolean isFinished() {
        return elapsedSeconds >= durationSeconds;
    }

    protected final double elapsedSeconds() {
        return elapsedSeconds;
    }

    protected final double durationSeconds() {
        return durationSeconds;
    }
}
