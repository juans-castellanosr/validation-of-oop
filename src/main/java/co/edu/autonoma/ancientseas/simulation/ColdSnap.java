package co.edu.autonoma.ancientseas.simulation;

/** A mass of cold water that makes every animal slower for a while. */
public final class ColdSnap extends EnvironmentEvent {

    public static final double ACTIVITY = 0.55;

    /**
     * Creates a cold snap.
     *
     * @param durationSeconds how long the event lasts
     * @param elapsedSeconds  time already elapsed, used when a saved game is restored
     */
    public ColdSnap(double durationSeconds, double elapsedSeconds) {
        super(durationSeconds, elapsedSeconds);
    }

    @Override
    protected void apply(Ocean ocean) {
        ocean.setActivityFactor(ACTIVITY);
    }

    @Override
    public String announcement() {
        return "¡Llega agua fría! Todos los animales nadan más despacio.";
    }

    @Override
    public EventType type() {
        return EventType.COLD_SNAP;
    }

    @Override
    public EventState toState() {
        return new EventState(type(), elapsedSeconds(), durationSeconds(), 0, "");
    }
}
