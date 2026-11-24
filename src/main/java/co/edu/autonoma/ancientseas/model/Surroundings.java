package co.edu.autonoma.ancientseas.model;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/** What a creature can perceive of, and ask from, the world around it. */
public interface Surroundings {

    /**
     * Returns the ocean width.
     *
     * @return width in ocean units
     */
    double width();

    /**
     * Returns the ocean height, from surface (0) to floor.
     *
     * @return height in ocean units
     */
    double height();

    /**
     * Finds the closest food a creature is able to eat.
     *
     * @param eater  the hungry creature
     * @param radius how far it can sense
     * @return the nearest edible thing within {@code radius}, if any
     */
    Optional<Edible> nearestEdible(Creature eater, double radius);

    /**
     * Finds the closest creature that could eat the given one.
     *
     * @param target the potential prey
     * @param radius how far it can sense
     * @return the nearest threat within {@code radius}, if any
     */
    Optional<Creature> nearestThreat(Creature target, double radius);

    /**
     * Lists living creatures of the same species near a creature, for schooling.
     *
     * @param creature the reference creature (excluded from the result)
     * @param radius   neighbourhood radius
     * @return the neighbours
     */
    List<Creature> sameSpeciesNear(Creature creature, double radius);

    /**
     * Returns the water current at a point.
     *
     * @param point the point
     * @return drift velocity in units per second
     */
    Vector2 currentAt(Vector2 point);

    /**
     * Returns how active animals are right now; cold water slows everyone down.
     *
     * @return a factor in (0, 1]
     */
    double activityFactor();

    /**
     * Returns the difficulty multiplier applied to hunters' chasing speed.
     *
     * @return a positive factor
     */
    double hunterSpeedFactor();

    /**
     * Returns the simulation's random source.
     *
     * @return the random generator
     */
    Random random();

    /**
     * Asks the world to add an offspring of a creature; refused when its population is full.
     *
     * @param parent the reproducing creature
     * @return {@code true} if the offspring was scheduled
     */
    boolean requestOffspring(Creature parent);
}
