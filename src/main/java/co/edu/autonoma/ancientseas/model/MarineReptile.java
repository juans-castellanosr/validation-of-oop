package co.edu.autonoma.ancientseas.model;

import co.edu.autonoma.ancientseas.catalog.Species;

/** An air-breathing {@link Hunter} that swims up to breathe when its air runs low. */
public class MarineReptile extends Hunter implements AirBreather {

    private static final double SECONDS_OF_AIR = 50;
    private static final double SUFFOCATION_ENERGY_PER_SECOND = 0.05;

    private final Lungs lungs = new Lungs(SECONDS_OF_AIR);

    /**
     * Creates an air-breathing hunter with full lungs.
     *
     * @param id         unique identifier
     * @param species    species this creature belongs to
     * @param position   starting position
     * @param sizeMeters current body length in meters
     * @param energy     starting energy in [0, 1]
     */
    public MarineReptile(long id, Species species, Vector2 position, double sizeMeters, double energy) {
        super(id, species, position, sizeMeters, energy);
    }

    @Override
    protected Vector2 steer(Surroundings world, double dt) {
        if (needsAir() && !atSurface(world)) {
            return seek(new Vector2(position().x() + velocity().x(), 0), cruiseSpeed() * 1.3);
        }
        return super.steer(world, dt);
    }

    @Override
    protected void live(Surroundings world, double dt) {
        super.live(world, dt);
        lungs.breathe(atSurface(world), dt);
        if (lungs.isEmpty()) {
            spendEnergy(SUFFOCATION_ENERGY_PER_SECOND * dt);
        }
    }

    @Override
    protected void restore(CreatureState state) {
        super.restore(state);
        lungs.restore(state.oxygen());
    }

    @Override
    public Lungs lungs() {
        return lungs;
    }

    @Override
    public double oxygenLevel() {
        return lungs.oxygen();
    }
}
