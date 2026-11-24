package co.edu.autonoma.ancientseas.simulation;

/** A sudden bloom of plankton and tiny prey falling from the surface. */
public final class FoodShower extends EnvironmentEvent {

    public static final int PARTICLES = 45;

    /**
     * Creates a food shower.
     *
     * @param durationSeconds how long the event lasts
     * @param elapsedSeconds  time already elapsed, used when a saved game is restored
     */
    public FoodShower(double durationSeconds, double elapsedSeconds) {
        super(durationSeconds, elapsedSeconds);
    }

    @Override
    protected void begin(Ocean ocean) {
        for (int i = 0; i < PARTICLES; i++) {
            ocean.addFoodNearSurface();
        }
    }

    @Override
    public String announcement() {
        return "¡Lluvia de comida! Mucho plancton cae desde la superficie.";
    }

    @Override
    public EventType type() {
        return EventType.FOOD_SHOWER;
    }

    @Override
    public EventState toState() {
        return new EventState(type(), elapsedSeconds(), durationSeconds(), 0, "");
    }
}
