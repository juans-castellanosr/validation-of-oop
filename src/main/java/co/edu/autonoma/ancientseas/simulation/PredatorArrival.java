package co.edu.autonoma.ancientseas.simulation;

import co.edu.autonoma.ancientseas.catalog.Species;

/** A large hunter enters the scene from one side. */
public final class PredatorArrival extends EnvironmentEvent {

    private final Species predator;

    /**
     * Creates the arrival of a predator of the given species.
     *
     * @param predator        the arriving species
     * @param durationSeconds how long the event lasts
     * @param elapsedSeconds  time already elapsed, used when a saved game is restored
     */
    public PredatorArrival(Species predator, double durationSeconds, double elapsedSeconds) {
        super(durationSeconds, elapsedSeconds);
        this.predator = predator;
    }

    @Override
    protected void begin(Ocean ocean) {
        ocean.addArrivingPredator(predator);
    }

    @Override
    public String announcement() {
        return "¡Cuidado! Llegó un " + predator.commonName() + " con hambre.";
    }

    @Override
    public EventType type() {
        return EventType.PREDATOR_ARRIVAL;
    }

    @Override
    public EventState toState() {
        return new EventState(type(), elapsedSeconds(), durationSeconds(), 0, predator.id());
    }
}
