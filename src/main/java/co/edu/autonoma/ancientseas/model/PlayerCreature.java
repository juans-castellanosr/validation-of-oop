package co.edu.autonoma.ancientseas.model;

import co.edu.autonoma.ancientseas.catalog.Range;
import co.edu.autonoma.ancientseas.catalog.Species;

/** The young animal the visitor controls; it grows by eating and a bite hurts instead of killing. */
public class PlayerCreature extends Creature implements AirBreather {

    public static final double BITE_DAMAGE = 0.34;
    public static final double DROWNING_DAMAGE_PER_SECOND = 0.1;
    private static final double SECONDS_OF_AIR = 35;
    private static final double SAFE_SECONDS_AFTER_BITE = 2.0;

    private final Lungs lungs = new Lungs(SECONDS_OF_AIR);
    private final double adultLengthMeters;
    private double health = 1.0;
    private double growthPoints;
    private double safeSeconds;
    private Vector2 direction = Vector2.ZERO;
    private Vector2 destination;

    /**
     * Creates the visitor's hatchling with full health and energy.
     *
     * @param id       unique identifier
     * @param species  the player species; its length is the adult size to reach
     * @param position starting position
     */
    public PlayerCreature(long id, Species species, Vector2 position) {
        super(id, species, position, GrowthStage.lengthAt(0, species.lengthMeters()), 1.0);
        this.adultLengthMeters = species.lengthMeters();
    }

    /**
     * Sets the swimming direction, cancelling any destination.
     *
     * @param newDirection the direction; {@link Vector2#ZERO} stops
     */
    public void steerTowards(Vector2 newDirection) {
        this.direction = newDirection.normalized();
        this.destination = null;
    }

    /**
     * Sets a destination to swim to and stop at.
     *
     * @param point the destination in ocean coordinates
     */
    public void swimTo(Vector2 point) {
        this.destination = point;
    }

    @Override
    protected Vector2 steer(Surroundings world, double dt) {
        if (destination != null) {
            if (position().distanceTo(destination) < radius()) {
                destination = null;
                direction = Vector2.ZERO;
            } else {
                return seek(destination, cruiseSpeed());
            }
        }
        return direction.times(cruiseSpeed());
    }

    @Override
    protected void live(Surroundings world, double dt) {
        safeSeconds = Math.max(0, safeSeconds - dt);
        lungs.breathe(atSurface(world), dt);
        if (lungs.isEmpty()) {
            hurt(DROWNING_DAMAGE_PER_SECOND * dt);
        }
    }

    @Override
    protected void onAte(Edible food) {
        growthPoints += food.nutrition() / FoodParticle.NUTRITION;
        setSizeMeters(GrowthStage.lengthAt(growthPoints, adultLengthMeters));
    }

    @Override
    public boolean consume() {
        if (!isAlive() || safeSeconds > 0) {
            return false;
        }
        hurt(BITE_DAMAGE);
        safeSeconds = SAFE_SECONDS_AFTER_BITE;
        return true;
    }

    @Override
    public double nutrition() {
        return sizeMeters() * BITE_DAMAGE;
    }

    private void hurt(double amount) {
        health = Math.max(0, health - amount);
        if (health <= 0) {
            markDead();
        }
    }

    @Override
    protected void restore(CreatureState state) {
        super.restore(state);
        lungs.restore(state.oxygen());
        health = Math.max(0, Math.min(1, state.health()));
        growthPoints = Math.max(0, state.growthPoints());
        safeSeconds = Math.max(0, Math.min(SAFE_SECONDS_AFTER_BITE, state.protectionSeconds()));
        setSizeMeters(GrowthStage.lengthAt(growthPoints, adultLengthMeters));
        destination = state.destination();
    }

    @Override
    protected Vector2 destination() {
        return destination;
    }

    @Override
    protected double cruiseSpeed() {
        return 150;
    }

    @Override
    protected double agility() {
        return 6;
    }

    @Override
    protected double metabolism() {
        return 0;
    }

    @Override
    protected double digestTime() {
        return 0.15;
    }

    @Override
    protected boolean canReproduce() {
        return false;
    }

    @Override
    protected Range<Double> allowedBand() {
        return Range.of(0.0, 1.0);
    }

    @Override
    public Lungs lungs() {
        return lungs;
    }

    @Override
    public double oxygenLevel() {
        return lungs.oxygen();
    }

    @Override
    public double healthLevel() {
        return health;
    }

    @Override
    protected double protectionSeconds() {
        return safeSeconds;
    }

    @Override
    protected double growthPoints() {
        return growthPoints;
    }

    /**
     * Returns the food points eaten so far.
     *
     * @return the points
     */
    public double foodPoints() {
        return growthPoints;
    }

    /**
     * Returns the life stage reached.
     *
     * @return the current stage
     */
    public GrowthStage stage() {
        return GrowthStage.forPoints(growthPoints);
    }

    /**
     * Tells whether the creature is protected right after a bite.
     *
     * @return {@code true} during the safe time
     */
    public boolean isRecovering() {
        return safeSeconds > 0;
    }

    @Override
    public boolean isPlayer() {
        return true;
    }
}
