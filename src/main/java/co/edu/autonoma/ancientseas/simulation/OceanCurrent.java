package co.edu.autonoma.ancientseas.simulation;

import co.edu.autonoma.ancientseas.model.Vector2;

/** A sideways current that drags animals and food in one direction. */
public final class OceanCurrent extends EnvironmentEvent {

    public static final double STRENGTH = 45;

    private final double direction;

    /**
     * Creates a horizontal current.
     *
     * @param direction       positive flows right, negative flows left; zero is taken as right
     * @param durationSeconds how long the event lasts
     * @param elapsedSeconds  time already elapsed, used when a saved game is restored
     */
    public OceanCurrent(double direction, double durationSeconds, double elapsedSeconds) {
        super(durationSeconds, elapsedSeconds);
        this.direction = Math.signum(direction) == 0 ? 1 : Math.signum(direction);
    }

    @Override
    protected void apply(Ocean ocean) {
        ocean.setCurrent(new Vector2(direction * STRENGTH, 0));
    }

    @Override
    public String announcement() {
        return direction > 0 ? "¡Una corriente empuja el agua hacia la derecha!"
                : "¡Una corriente empuja el agua hacia la izquierda!";
    }

    @Override
    public EventType type() {
        return EventType.CURRENT;
    }

    @Override
    public EventState toState() {
        return new EventState(type(), elapsedSeconds(), durationSeconds(), direction, "");
    }
}
