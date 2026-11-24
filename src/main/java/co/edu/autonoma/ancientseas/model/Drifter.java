package co.edu.autonoma.ancientseas.model;

import co.edu.autonoma.ancientseas.catalog.Species;
import java.util.Optional;

/** A slow floater, such as an ammonite, that bobs and escapes threats with a short water jet. */
public class Drifter extends Creature {

    private static final double THREAT_SIGHT = 110;
    private static final double JET_SECONDS = 0.7;
    private static final double JET_RECOVERY_SECONDS = 3.5;
    private static final double FOOD_SIGHT = 90;
    private static final double HUNGRY = 0.9;

    private double jetSeconds;
    private double recoverySeconds;
    private Vector2 jetDirection = Vector2.ZERO;

    /**
     * Creates a drifting animal.
     *
     * @param id         unique identifier
     * @param species    species this creature belongs to
     * @param position   starting position
     * @param sizeMeters current body length in meters
     * @param energy     starting energy in [0, 1]
     */
    public Drifter(long id, Species species, Vector2 position, double sizeMeters, double energy) {
        super(id, species, position, sizeMeters, energy);
    }

    @Override
    protected Vector2 steer(Surroundings world, double dt) {
        recoverySeconds = Math.max(0, recoverySeconds - dt);
        if (jetSeconds > 0) {
            jetSeconds = Math.max(0, jetSeconds - dt);
            return jetDirection.times(cruiseSpeed() * 5);
        }
        Optional<Creature> threat = world.nearestThreat(this, THREAT_SIGHT);
        if (threat.isPresent() && recoverySeconds <= 0) {
            jetDirection = position().minus(threat.get().position()).normalized();
            jetSeconds = JET_SECONDS;
            recoverySeconds = JET_RECOVERY_SECONDS;
            return jetDirection.times(cruiseSpeed() * 5);
        }
        double bob = Math.sin(ageSeconds() * 0.8 + id()) * 0.6;
        Vector2 bobbing = new Vector2(0, bob * cruiseSpeed());
        if (energy() < HUNGRY) {
            Optional<Edible> food = world.nearestEdible(this, FOOD_SIGHT);
            if (food.isPresent()) {
                return seek(food.get().position(), cruiseSpeed()).plus(bobbing.times(0.3));
            }
        }
        return wander(world, dt, cruiseSpeed()).plus(bobbing);
    }

    @Override
    protected void restore(CreatureState state) {
        super.restore(state);
        jetSeconds = Math.max(0, state.burstSeconds());
        recoverySeconds = Math.max(0, state.burstRecoverySeconds());
        Vector2 saved = new Vector2(state.burstDirectionX(), state.burstDirectionY());
        if (jetSeconds > 0) {
            jetDirection = saved.equals(Vector2.ZERO) ? velocity().normalized() : saved;
        }
    }

    @Override
    protected Vector2 burstDirection() {
        return jetSeconds > 0 ? jetDirection : Vector2.ZERO;
    }

    @Override
    protected double burstSeconds() {
        return jetSeconds;
    }

    @Override
    protected double burstRecoverySeconds() {
        return recoverySeconds;
    }

    @Override
    protected double maxSpeed(Surroundings world) {
        return cruiseSpeed() * 5;
    }

    @Override
    protected double cruiseSpeed() {
        return 22;
    }

    @Override
    protected double agility() {
        return 6;
    }

    @Override
    protected double metabolism() {
        return 0.007;
    }

    @Override
    protected double maturitySeconds() {
        return 35;
    }

    @Override
    protected double reach() {
        return radius() * 1.5;
    }
}
