package co.edu.autonoma.ancientseas.model;

import co.edu.autonoma.ancientseas.catalog.Range;
import co.edu.autonoma.ancientseas.catalog.Species;
import java.util.Optional;

/** A predator that patrols and chases the prey it can swallow, only while hungry. */
public class Hunter extends Creature {

    private static final double SIGHT = 260;
    private static final double CHASE_FACTOR = 1.9;
    public static final double HUNGER_THRESHOLD = 0.6;

    private boolean chasing;

    /**
     * Creates a hunter.
     *
     * @param id         unique identifier
     * @param species    species this creature belongs to
     * @param position   starting position
     * @param sizeMeters current body length in meters
     * @param energy     starting energy in [0, 1]
     */
    public Hunter(long id, Species species, Vector2 position, double sizeMeters, double energy) {
        super(id, species, position, sizeMeters, energy);
    }

    @Override
    protected Vector2 steer(Surroundings world, double dt) {
        Optional<Edible> prey = isHungry() ? world.nearestEdible(this, SIGHT) : Optional.empty();
        chasing = prey.isPresent();
        return prey.map(p -> seek(p.position(), maxSpeed(world)))
                .orElseGet(() -> wander(world, dt, cruiseSpeed()));
    }

    @Override
    protected double maxSpeed(Surroundings world) {
        return chasing ? cruiseSpeed() * CHASE_FACTOR * world.hunterSpeedFactor() : cruiseSpeed();
    }

    @Override
    protected void feed(Surroundings world) {
        if (isHungry()) {
            super.feed(world);
        }
    }

    /**
     * Tells whether the hunter wants to eat.
     *
     * @return {@code true} when it will chase prey
     */
    public final boolean isHungry() {
        return !isDigesting() && energy() < HUNGER_THRESHOLD;
    }

    @Override
    protected double cruiseSpeed() {
        return 55;
    }

    @Override
    protected double metabolism() {
        return 0.008;
    }

    @Override
    protected double maturitySeconds() {
        return 120;
    }

    @Override
    protected double digestTime() {
        return 7;
    }

    @Override
    protected Range<Double> allowedBand() {
        return Range.of(0.0, species().zone().band().max());
    }

    @Override
    public final boolean isHunting() {
        return chasing;
    }
}
