package co.edu.autonoma.ancientseas.model;

import co.edu.autonoma.ancientseas.catalog.PreyKind;

/** A bit of plankton that sinks slowly and eventually decays. */
public final class FoodParticle extends Entity implements Edible {

    public static final double NUTRITION = 0.05;
    private static final double SIZE_METERS = 0.02;
    private static final double SINK_SPEED = 6.0;
    private static final double RADIUS = 3.0;

    private double secondsLeft;

    /**
     * Creates a food particle.
     *
     * @param id          unique identifier
     * @param position    starting position
     * @param secondsLeft time before it dissolves
     */
    public FoodParticle(long id, Vector2 position, double secondsLeft) {
        super(id, position);
        this.secondsLeft = secondsLeft;
    }

    /**
     * Sinks the particle towards the floor and ages it.
     *
     * @param floorY the y coordinate of the sea floor
     * @param drift  water current at the particle
     * @param dt     elapsed simulation seconds
     */
    public void update(double floorY, Vector2 drift, double dt) {
        if (!isAlive()) {
            return;
        }
        Vector2 next = position().plus(new Vector2(drift.x(), SINK_SPEED + drift.y()).times(dt));
        moveTo(new Vector2(next.x(), Math.min(floorY - RADIUS, next.y())));
        secondsLeft -= dt;
        if (secondsLeft <= 0) {
            markDead();
        }
    }

    /**
     * Returns the remaining lifetime.
     *
     * @return seconds left
     */
    public double secondsLeft() {
        return secondsLeft;
    }

    @Override
    public double radius() {
        return RADIUS;
    }

    @Override
    public PreyKind preyKind() {
        return PreyKind.SMALL_PREY;
    }

    @Override
    public double sizeMeters() {
        return SIZE_METERS;
    }

    @Override
    public double nutrition() {
        return NUTRITION;
    }

    @Override
    public boolean isAvailable() {
        return isAlive();
    }

    @Override
    public boolean consume() {
        if (!isAlive()) {
            return false;
        }
        markDead();
        return true;
    }
}
