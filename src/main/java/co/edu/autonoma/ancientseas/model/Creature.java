package co.edu.autonoma.ancientseas.model;

import co.edu.autonoma.ancientseas.catalog.PreyKind;
import co.edu.autonoma.ancientseas.catalog.Range;
import co.edu.autonoma.ancientseas.catalog.Species;
import co.edu.autonoma.ancientseas.catalog.Zone;
import java.util.Optional;

/**
 * A living animal of the ocean, advanced through the template method
 * {@link #update(Surroundings, double)}; subclasses differ in {@link #steer} and a few hooks.
 */
public abstract class Creature extends Entity implements Edible {

    public static final double PREY_SIZE_RATIO = 0.55;
    public static final double MIN_PREY_SIZE_RATIO = 0.005;
    protected static final double FULL_MEAL_FRACTION = 0.25;
    protected static final double BREEDING_ENERGY = 0.85;
    protected static final double BREEDING_COST = 0.4;

    private final Species species;
    private Vector2 velocity = Vector2.ZERO;
    private double sizeMeters;
    private double energy;
    private double ageSeconds;
    private double digestSeconds;
    private double reproductionSeconds;
    private double wanderAngle;

    protected Creature(long id, Species species, Vector2 position, double sizeMeters, double energy) {
        super(id, position);
        if (!(sizeMeters > 0)) {
            throw new IllegalArgumentException("sizeMeters must be positive: " + sizeMeters);
        }
        this.species = species;
        this.sizeMeters = sizeMeters;
        this.energy = clamp01(energy);
    }

    /**
     * Advances the creature by one simulation step.
     *
     * @param world what the creature can perceive
     * @param dt    elapsed simulation seconds
     */
    public final void update(Surroundings world, double dt) {
        if (!isAlive()) {
            return;
        }
        ageSeconds += dt;
        digestSeconds = Math.max(0, digestSeconds - dt);
        reproductionSeconds = Math.max(0, reproductionSeconds - dt);

        Vector2 desired = steer(world, dt).limit(maxSpeed(world));
        velocity = velocity.lerp(desired, Math.min(1.0, agility() * dt));
        Vector2 drift = world.currentAt(position()).times(currentExposure());
        moveTo(position().plus(velocity.times(world.activityFactor()).plus(drift).times(dt)));
        stayInHabitat(world);

        feed(world);
        live(world, dt);
        if (energy <= 0) {
            markDead();
            return;
        }
        if (canReproduce() && ageSeconds >= maturitySeconds() && energy >= BREEDING_ENERGY && reproductionSeconds <= 0
                && world.requestOffspring(this)) {
            energy -= BREEDING_COST;
            reproductionSeconds = maturitySeconds();
        }
    }

    protected abstract Vector2 steer(Surroundings world, double dt);

    protected abstract double cruiseSpeed();

    protected double maxSpeed(Surroundings world) {
        return cruiseSpeed();
    }

    protected double agility() {
        return 2.5;
    }

    protected abstract double metabolism();

    protected double maturitySeconds() {
        return 40.0;
    }

    protected double digestTime() {
        return 0.5;
    }

    protected double reach() {
        return radius();
    }

    protected boolean canReproduce() {
        return true;
    }

    protected Range<Double> allowedBand() {
        return species.zone().band();
    }

    protected void live(Surroundings world, double dt) {
        spendEnergy(metabolism() * dt);
    }

    protected void onAte(Edible food) {
    }

    protected void feed(Surroundings world) {
        if (digestSeconds > 0) {
            return;
        }
        Optional<Edible> target = world.nearestEdible(this, reach() * 2);
        if (target.isEmpty()) {
            return;
        }
        Edible food = target.get();
        boolean touching = position().distanceTo(food.position()) <= reach() + food.radius();
        if (touching && food.consume()) {
            energy = clamp01(energy + food.nutrition() / (sizeMeters * FULL_MEAL_FRACTION));
            digestSeconds = digestTime();
            onAte(food);
        }
    }

    /**
     * Tells whether this creature can eat the food: diet, size range and availability.
     *
     * @param food the candidate food
     * @return {@code true} if it can be eaten
     */
    public boolean canEat(Edible food) {
        return food != this && food.isAvailable() && !food.isOfSpecies(species)
                && species.diet().accepts(food.preyKind())
                && food.sizeMeters() <= sizeMeters * PREY_SIZE_RATIO
                && food.sizeMeters() >= sizeMeters * MIN_PREY_SIZE_RATIO;
    }
    @Override
    public boolean isOfSpecies(Species other) {
        return species.id().equals(other.id());
    }


    protected final Vector2 seek(Vector2 target, double speed) {
        return target.minus(position()).normalized().times(speed);
    }

    protected final Vector2 flee(Vector2 danger, double speed) {
        return position().minus(danger).normalized().times(speed);
    }

    protected final Vector2 wander(Surroundings world, double dt, double speed) {
        wanderAngle += (world.random().nextDouble() - 0.5) * 3.0 * dt;
        double vertical = Math.sin(wanderAngle) * 0.35;
        double horizontal = velocity.x() < 0 ? -1 : 1;
        if (Math.abs(velocity.x()) < 1e-6) {
            horizontal = world.random().nextBoolean() ? 1 : -1;
        }
        return new Vector2(horizontal, vertical).normalized().times(speed);
    }

    private void stayInHabitat(Surroundings world) {
        double r = radius();
        double x = position().x();
        double vx = velocity.x();
        if (x < r) {
            x = r;
            vx = Math.abs(vx);
        } else if (x > world.width() - r) {
            x = world.width() - r;
            vx = -Math.abs(vx);
        }
        Range<Double> band = allowedBand();
        double top = band.min() * world.height() + r;
        double bottom = band.max() * world.height() - r;
        if (top > bottom) {
            double middle = (band.min() + band.max()) / 2 * world.height();
            top = middle;
            bottom = middle;
        }
        double y = Math.max(top, Math.min(bottom, position().y()));
        double vy = y != position().y() ? 0 : velocity.y();
        moveTo(new Vector2(x, y));
        velocity = new Vector2(vx, vy);
    }

    protected final void spendEnergy(double amount) {
        energy = clamp01(energy - amount);
    }

    protected final void setSizeMeters(double newSizeMeters) {
        if (newSizeMeters > 0) {
            sizeMeters = newSizeMeters;
        }
    }

    protected void restore(CreatureState state) {
        moveTo(new Vector2(state.x(), state.y()));
        velocity = new Vector2(state.vx(), state.vy());
        setSizeMeters(state.sizeMeters());
        energy = clamp01(state.energy());
        ageSeconds = Math.max(0, state.ageSeconds());
        wanderAngle = state.wanderAngle();
        digestSeconds = Math.max(0, state.digestSeconds());
        reproductionSeconds = Math.max(0, state.reproductionSeconds());
    }

    /**
     * Captures the state needed to rebuild this creature exactly.
     *
     * @return the snapshot
     */
    public CreatureState toState() {
        return new CreatureState(id(), species.id(), isPlayer(), position().x(), position().y(), velocity.x(),
                velocity.y(), sizeMeters, energy, ageSeconds, digestSeconds, reproductionSeconds, oxygenLevel(),
                healthLevel(), growthPoints(), protectionSeconds(), wanderAngle, burstSeconds(),
                burstRecoverySeconds(), burstDirection().x(), burstDirection().y(), destination());
    }

    /**
     * Returns the oxygen level.
     *
     * @return a value in [0, 1]
     */
    public double oxygenLevel() {
        return 1.0;
    }

    /**
     * Returns the health level.
     *
     * @return a value in [0, 1]
     */
    public double healthLevel() {
        return 1.0;
    }

    protected double growthPoints() {
        return 0.0;
    }

    /**
     * Tells whether the creature is chasing prey right now.
     *
     * @return {@code false} unless a hunter is in pursuit
     */
    public boolean isHunting() {
        return false;
    }

    protected double protectionSeconds() {
        return 0.0;
    }

    protected double burstSeconds() {
        return 0.0;
    }

    protected double burstRecoverySeconds() {
        return 0.0;
    }

    protected Vector2 burstDirection() {
        return Vector2.ZERO;
    }

    protected Vector2 destination() {
        return null;
    }

    protected double currentExposure() {
        return 1.0;
    }

    /**
     * Tells whether this is the creature controlled by the visitor.
     *
     * @return {@code false} for autonomous creatures
     */
    public boolean isPlayer() {
        return false;
    }

    protected final boolean atSurface(Surroundings world) {
        return Zone.at((position().y() - radius()) / world.height()) == Zone.SURFACE;
    }

    /**
     * Returns the catalog data of this creature's species.
     *
     * @return the species
     */
    public final Species species() {
        return species;
    }

    /**
     * Returns the current velocity.
     *
     * @return the velocity in units per second
     */
    public final Vector2 velocity() {
        return velocity;
    }

    /**
     * Returns the energy reserve.
     *
     * @return a value in [0, 1]
     */
    public final double energy() {
        return energy;
    }

    /**
     * Tells whether the creature is resting after a meal and cannot eat yet.
     *
     * @return {@code true} while digesting
     */
    public final boolean isDigesting() {
        return digestSeconds > 0;
    }

    /**
     * Returns the simulation time this creature has lived.
     *
     * @return seconds
     */
    public final double ageSeconds() {
        return ageSeconds;
    }

    @Override
    public final double sizeMeters() {
        return sizeMeters;
    }

    @Override
    public double radius() {
        return 8.0 + 13.0 * Math.sqrt(sizeMeters);
    }

    @Override
    public final PreyKind preyKind() {
        return species.shape().preyKind();
    }

    @Override
    public double nutrition() {
        return sizeMeters;
    }

    @Override
    public final boolean isAvailable() {
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

    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
