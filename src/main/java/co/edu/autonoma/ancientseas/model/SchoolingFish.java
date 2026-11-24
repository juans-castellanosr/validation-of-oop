package co.edu.autonoma.ancientseas.model;

import co.edu.autonoma.ancientseas.catalog.Range;
import co.edu.autonoma.ancientseas.catalog.Species;
import java.util.List;
import java.util.Optional;

/** A fish that swims in groups; it escapes first, then eats, then stays with the group. */
public class SchoolingFish extends Creature {

    private static final double THREAT_SIGHT = 150;
    private static final double FOOD_SIGHT = 130;
    private static final double SCHOOL_RADIUS = 120;

    /**
     * Creates a schooling fish.
     *
     * @param id         unique identifier
     * @param species    species this creature belongs to
     * @param position   starting position
     * @param sizeMeters current body length in meters
     * @param energy     starting energy in [0, 1]
     */
    public SchoolingFish(long id, Species species, Vector2 position, double sizeMeters, double energy) {
        super(id, species, position, sizeMeters, energy);
    }

    @Override
    protected Vector2 steer(Surroundings world, double dt) {
        Optional<Creature> threat = world.nearestThreat(this, THREAT_SIGHT);
        if (threat.isPresent()) {
            return flee(threat.get().position(), cruiseSpeed() * 2.2);
        }
        Optional<Edible> food = world.nearestEdible(this, FOOD_SIGHT);
        if (food.isPresent()) {
            return seek(food.get().position(), cruiseSpeed());
        }
        List<Creature> school = world.sameSpeciesNear(this, SCHOOL_RADIUS);
        Vector2 roaming = wander(world, dt, cruiseSpeed());
        if (school.isEmpty()) {
            return roaming;
        }
        Vector2 centre = school.stream().map(Entity::position).reduce(Vector2.ZERO, Vector2::plus)
                .times(1.0 / school.size());
        return roaming.plus(seek(centre, cruiseSpeed() * 0.6));
    }

    @Override
    protected double maxSpeed(Surroundings world) {
        return cruiseSpeed() * 2.2;
    }

    @Override
    protected double cruiseSpeed() {
        return 60;
    }

    @Override
    protected double metabolism() {
        return 0.012;
    }

    @Override
    protected double maturitySeconds() {
        return 30;
    }

    @Override
    protected Range<Double> allowedBand() {
        return Range.of(0.0, species().zone().band().max());
    }
}
