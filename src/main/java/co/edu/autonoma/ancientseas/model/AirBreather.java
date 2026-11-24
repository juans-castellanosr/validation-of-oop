package co.edu.autonoma.ancientseas.model;

/** Capability of animals that breathe air and must return to the surface. */
public interface AirBreather {

    double SURFACING_THRESHOLD = 0.3;

    /**
     * Returns the oxygen store.
     *
     * @return the lungs component
     */
    Lungs lungs();

    /**
     * Tells whether the animal should go up to breathe.
     *
     * @return {@code true} when oxygen is low
     */
    default boolean needsAir() {
        return lungs().oxygen() < SURFACING_THRESHOLD;
    }
}
