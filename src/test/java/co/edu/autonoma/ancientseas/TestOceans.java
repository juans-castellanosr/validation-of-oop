package co.edu.autonoma.ancientseas;

import co.edu.autonoma.ancientseas.simulation.ResumableRandom;
import co.edu.autonoma.ancientseas.catalog.Species;
import co.edu.autonoma.ancientseas.model.Creature;
import co.edu.autonoma.ancientseas.model.CreatureFactory;
import co.edu.autonoma.ancientseas.model.CreatureState;
import co.edu.autonoma.ancientseas.simulation.Ocean;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class TestOceans {

    public static final double NO_EVENTS = 1e-9;

    private TestOceans() {
    }

    public static Ocean empty(Map<String, Integer> caps) {
        return new Ocean(Ocean.DEFAULT_WIDTH, Ocean.DEFAULT_HEIGHT, ResumableRandom.seeded(42), 1.0, NO_EVENTS, caps, List.of(), List.of());
    }

    public static Creature creature(Species species, long id, double x, double y, double size, double energy,
            double age, double oxygen) {
        return CreatureFactory.restore(species,
                new CreatureState(id, species.id(), false, x, y, 0, 0, size, energy, age, 0, 0, oxygen, 1, 0, 0, 0, 0, 0, 0, 0, null));
    }

    public static void run(Ocean ocean, double seconds) {
        int steps = (int) Math.round(seconds * 60);
        for (int i = 0; i < steps; i++) {
            ocean.step(1.0 / 60);
        }
    }
}
