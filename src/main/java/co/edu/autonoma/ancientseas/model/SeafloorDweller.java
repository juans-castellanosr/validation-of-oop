package co.edu.autonoma.ancientseas.model;

import co.edu.autonoma.ancientseas.catalog.Range;
import co.edu.autonoma.ancientseas.catalog.Species;

/** An immobile animal fixed to the sea floor, such as a giant clam, that filters passing food. */
public class SeafloorDweller extends Creature {

    public static final Range<Double> RESTING_BAND = Range.of(0.88, 0.94);

    /**
     * Creates an animal fixed to the seafloor.
     *
     * @param id         unique identifier
     * @param species    species this creature belongs to
     * @param position   starting position
     * @param sizeMeters current body length in meters
     * @param energy     starting energy in [0, 1]
     */
    public SeafloorDweller(long id, Species species, Vector2 position, double sizeMeters, double energy) {
        super(id, species, position, sizeMeters, energy);
    }

    @Override
    protected Vector2 steer(Surroundings world, double dt) {
        return Vector2.ZERO;
    }

    @Override
    protected double cruiseSpeed() {
        return 0;
    }

    @Override
    protected double metabolism() {
        return 0.003;
    }

    @Override
    protected double maturitySeconds() {
        return 90;
    }

    @Override
    protected double currentExposure() {
        return 0.0;
    }

    @Override
    protected double reach() {
        return radius() * 3;
    }

    @Override
    protected Range<Double> allowedBand() {
        return RESTING_BAND;
    }
}
