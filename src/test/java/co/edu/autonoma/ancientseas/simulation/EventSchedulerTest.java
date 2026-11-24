package co.edu.autonoma.ancientseas.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.autonoma.ancientseas.TestOceans;
import co.edu.autonoma.ancientseas.TestSpecies;
import co.edu.autonoma.ancientseas.model.Creature;
import co.edu.autonoma.ancientseas.model.Vector2;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;

class EventSchedulerTest {

    private static Ocean busyOcean() {
        return new Ocean(Ocean.DEFAULT_WIDTH, Ocean.DEFAULT_HEIGHT, ResumableRandom.seeded(7), 1.0, 50, Map.of(), List.of(),
                List.of(TestSpecies.mosasaur()));
    }

    @Test
    void eventsHappenAndAreAnnouncedOnce() {
        Ocean ocean = busyOcean();
        List<String> messages = new ArrayList<>();
        for (int i = 0; i < 60 * 20; i++) {
            messages.addAll(ocean.step(1.0 / 60));
        }
        assertFalse(messages.isEmpty());
        assertTrue(messages.stream().allMatch(m -> !m.isBlank()));
    }

    @Test
    void currentDragsCreaturesWhileActiveAndStopsAfterwards() throws Exception {
        Ocean ocean = TestOceans.empty(Map.of());
        Creature clamLikeStill = TestOceans.creature(TestSpecies.ammonite(), 1, 800, 450, 0.6, 1, 0, 1);
        ocean.add(clamLikeStill);
        ocean.events().restore(List.of(new EventState(EventType.CURRENT, 0.5, 2, 1, "")), 1e9, id -> Optional.empty());
        ocean.step(1.0 / 60);
        assertEquals(new Vector2(OceanCurrent.STRENGTH, 0), ocean.current());
        TestOceans.run(ocean, 2);
        assertEquals(Vector2.ZERO, ocean.current(), "the environment is derived from active events only");
    }

    @Test
    void restoredOneOffEventDoesNotRepeatItsEffect() {
        Ocean ocean = TestOceans.empty(Map.of());
        ocean.events().restore(List.of(new EventState(EventType.FOOD_SHOWER, 1, 4, 0, "")), 1e9, id -> Optional.empty());
        ocean.step(1.0 / 60);
        assertTrue(ocean.food().size() < FoodShower.PARTICLES, "the shower already fell before saving");
    }

    @Test
    void newFoodShowerAddsItsParticles() {
        Ocean ocean = TestOceans.empty(Map.of());
        ocean.events().restore(List.of(new EventState(EventType.FOOD_SHOWER, 0, 4, 0, "")), 1e9, id -> Optional.empty());
        ocean.step(1.0 / 60);
        assertTrue(ocean.food().size() >= FoodShower.PARTICLES);
    }

    @Test
    void coldSnapSlowsActivity() {
        Ocean ocean = TestOceans.empty(Map.of());
        ocean.events().restore(List.of(new EventState(EventType.COLD_SNAP, 0, 5, 0, "")), 1e9, id -> Optional.empty());
        ocean.step(1.0 / 60);
        assertEquals(ColdSnap.ACTIVITY, ocean.activityFactor());
    }

    @Test
    void predatorArrivalBringsAnAdultHunter() {
        Ocean ocean = TestOceans.empty(Map.of());
        ocean.events().restore(List.of(new EventState(EventType.PREDATOR_ARRIVAL, 0, 3, 0, "tylosaurus")), 1e9,
                id -> Optional.of(TestSpecies.mosasaur()));
        ocean.step(1.0 / 60);
        assertEquals(1, ocean.populationOf("tylosaurus"));
    }

    @Test
    void predatorEventOfRemovedSpeciesIsDroppedOnRestore() {
        Ocean ocean = TestOceans.empty(Map.of());
        ocean.events().restore(List.of(new EventState(EventType.PREDATOR_ARRIVAL, 1, 3, 0, "gone")), 10,
                id -> Optional.empty());
        assertTrue(ocean.events().active().isEmpty());
        assertEquals(10, ocean.events().secondsUntilNext());
    }

    @Test
    void sameKindOfEventNeverOverlaps() {
        Ocean ocean = busyOcean();
        for (int i = 0; i < 60 * 60; i++) {
            ocean.step(1.0 / 60);
            List<EventType> kinds = ocean.events().active().stream().map(EnvironmentEvent::type).toList();
            assertEquals(kinds.size(), kinds.stream().distinct().count());
        }
    }
}
