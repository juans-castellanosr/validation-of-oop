package co.edu.autonoma.ancientseas.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.autonoma.ancientseas.TestOceans;
import co.edu.autonoma.ancientseas.TestSpecies;
import co.edu.autonoma.ancientseas.catalog.Behavior;
import co.edu.autonoma.ancientseas.catalog.BodyShape;
import co.edu.autonoma.ancientseas.catalog.Diet;
import co.edu.autonoma.ancientseas.catalog.Species;
import co.edu.autonoma.ancientseas.error.HabitatViolationException;
import co.edu.autonoma.ancientseas.simulation.Ocean;
import co.edu.autonoma.ancientseas.simulation.ResumableRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CreatureBehaviorTest {

    private final Ocean ocean = TestOceans.empty(Map.of());

    @Test
    void creatureAtTheRightEdgeStaysInsideAndTurnsBack() throws Exception {
        Creature fish = TestOceans.creature(TestSpecies.bigFish(), 1, ocean.width(), 450, 5, 1, 0, 1);
        ocean.add(fish);
        TestOceans.run(ocean, 0.5);
        assertTrue(fish.position().x() <= ocean.width() - fish.radius() + 1e-9);
        assertTrue(fish.velocity().x() <= 0, "moving away from the wall");
    }

    @Test
    void creatureInTheTopLeftCornerIsPushedInside() throws Exception {
        Creature fish = TestOceans.creature(TestSpecies.smallFish(), 1, 0, 0, 1, 1, 0, 1);
        ocean.add(fish);
        ocean.step(1.0 / 60);
        assertTrue(fish.position().x() >= fish.radius());
        assertTrue(fish.position().y() >= fish.radius());
    }

    @Test
    void creaturesOutsideTheOceanAreRejected() {
        Creature fish = TestOceans.creature(TestSpecies.smallFish(), 1, -1, 300, 1, 1, 0, 1);
        assertThrows(HabitatViolationException.class, () -> ocean.add(fish));
    }

    @Test
    void sessileAnimalAwayFromTheFloorIsAHabitatViolation() {
        Creature clam = TestOceans.creature(TestSpecies.clam(), 1, 500, 300, 1, 1, 0, 1);
        assertThrows(HabitatViolationException.class, () -> ocean.add(clam));
    }

    @Test
    void preyReachedByTwoHuntersInTheSameStepIsEatenOnce() throws Exception {
        Creature prey = TestOceans.creature(TestSpecies.smallFish(), 1, 800, 450, 1, 1, 0, 1);
        Creature left = TestOceans.creature(TestSpecies.bigFish(), 2, 790, 450, 5, 0.2, 0, 1);
        Creature right = TestOceans.creature(TestSpecies.bigFish(), 3, 810, 450, 5, 0.2, 0, 1);
        ocean.add(prey);
        ocean.add(left);
        ocean.add(right);

        ocean.step(1.0 / 60);

        assertFalse(prey.isAlive());
        assertEquals(1, (left.isDigesting() ? 1 : 0) + (right.isDigesting() ? 1 : 0), "exactly one hunter ate");
        double winnerEnergy = Math.max(left.energy(), right.energy());
        double loserEnergy = Math.min(left.energy(), right.energy());
        assertTrue(winnerEnergy > 0.2 && loserEnergy < 0.2);
        assertEquals(2, ocean.creatures().size(), "the eaten prey is removed at the end of the step");
    }

    @Test
    void foodParticleEatenByTwoFishGoesToOnlyOne() throws Exception {
        Creature a = TestOceans.creature(TestSpecies.smallFish(), 1, 800, 450, 1, 0.5, 0, 1);
        Creature b = TestOceans.creature(TestSpecies.smallFish(), 2, 802, 450, 1, 0.5, 0, 1);
        ocean.add(a);
        ocean.add(b);
        ocean.restoreFood(new FoodState(3, 801, 450, 30));
        ocean.step(1.0 / 60);
        assertEquals(1, (a.isDigesting() ? 1 : 0) + (b.isDigesting() ? 1 : 0));
    }

    @Test
    void dietAndSizeDecideWhatCanBeEaten() {
        Creature mosasaur = TestOceans.creature(TestSpecies.mosasaur(), 1, 100, 100, 12, 1, 0, 1);
        Creature smallFish = TestOceans.creature(TestSpecies.smallFish(), 2, 100, 100, 1, 1, 0, 1);
        Creature bigFish = TestOceans.creature(TestSpecies.bigFish(), 3, 100, 100, 5, 1, 0, 1);
        Creature clam = TestOceans.creature(TestSpecies.clam(), 4, 100, 880, 1, 1, 0, 1);
        FoodParticle plankton = new FoodParticle(5, new Vector2(1, 1), 10);
        assertTrue(mosasaur.canEat(bigFish));
        assertFalse(bigFish.canEat(mosasaur), "too big to swallow");
        assertFalse(mosasaur.canEat(plankton), "big hunters ignore food too small to be worth it");
        assertTrue(smallFish.canEat(plankton));
        assertFalse(bigFish.canEat(clam), "piscivores do not eat shellfish");
        assertTrue(clam.canEat(plankton));
        assertFalse(mosasaur.canEat(mosasaur), "never itself");
        assertTrue(bigFish.canEat(smallFish));
        smallFish.consume();
        assertFalse(bigFish.canEat(smallFish), "dead prey is not available");
    }

    @Test
    void smallCarnivoreEatsPlanktonButBigOneDoesNot() throws Exception {
        Creature smallHunter = TestOceans.creature(
                co.edu.autonoma.ancientseas.TestSpecies.valid("small").lengthMeters(1).build(), 1, 100, 100, 1, 1, 0, 1);
        FoodParticle plankton = new FoodParticle(2, new Vector2(1, 1), 10);
        assertTrue(smallHunter.canEat(plankton));
    }

    @Test
    void starvingCreatureDiesAndLeavesTheOcean() throws Exception {
        Creature clam = TestOceans.creature(TestSpecies.clam(), 1, 500, 880, 1, 0.005, 0, 1);
        ocean.add(clam);
        TestOceans.run(ocean, 3);
        assertFalse(clam.isAlive());
        assertTrue(ocean.creatures().isEmpty());
    }

    @Test
    void reproductionStopsAtThePopulationCap() throws Exception {
        Ocean capped = TestOceans.empty(Map.of(TestSpecies.ammonite().id(), 3));
        for (int i = 0; i < 2; i++) {
            capped.add(TestOceans.creature(TestSpecies.ammonite(), i + 1, 400 + i * 300, 450, 0.6, 1, 500, 1));
        }
        capped.step(1.0 / 60);
        assertEquals(3, capped.populationOf(TestSpecies.ammonite().id()), "only one birth fits under the cap");
        TestOceans.run(capped, 5);
        assertTrue(capped.populationOf(TestSpecies.ammonite().id()) <= 3);
    }

    @Test
    void speciesWithoutCapNeverBreeds() throws Exception {
        ocean.add(TestOceans.creature(TestSpecies.ammonite(), 1, 400, 450, 0.6, 1, 500, 1));
        ocean.step(1.0 / 60);
        assertEquals(1, ocean.creatures().size());
    }

    @Test
    void marineReptileOutOfAirSwimsUp() throws Exception {
        Creature mosasaur = TestOceans.creature(TestSpecies.mosasaur(), 1, 800, 600, 12, 1, 0, 0.05);
        ocean.add(mosasaur);
        TestOceans.run(ocean, 1);
        assertTrue(mosasaur.position().y() < 600 - 30, "it rose towards the surface");
    }

    @Test
    void marineReptileRefillsItsLungsAtTheSurface() throws Exception {
        Creature mosasaur = TestOceans.creature(TestSpecies.mosasaur(), 1, 800, 0, 12, 1, 0, 0.05);
        ocean.add(mosasaur);
        TestOceans.run(ocean, 2);
        assertTrue(mosasaur.oxygenLevel() > 0.05);
    }

    @Test
    void ammoniteEscapesWithAJet() throws Exception {
        Creature ammonite = TestOceans.creature(TestSpecies.ammonite(), 1, 800, 450, 0.6, 1, 0, 1);
        Creature hunter = TestOceans.creature(TestSpecies.bigFish(), 2, 720, 450, 5, 1, 0, 1);
        ocean.add(ammonite);
        ocean.add(hunter);
        ocean.step(1.0 / 60);
        assertTrue(ammonite.velocity().x() > 0, "it shoots away from the hunter");
        TestOceans.run(ocean, 0.3);
        assertTrue(ammonite.velocity().length() > 60, "a jet is much faster than drifting");
    }

    @Test
    void schoolingFishFleesFromAThreat() throws Exception {
        Creature fish = TestOceans.creature(TestSpecies.smallFish(), 1, 800, 450, 1, 1, 0, 1);
        Creature hunter = TestOceans.creature(TestSpecies.bigFish(), 2, 700, 450, 5, 1, 0, 1);
        ocean.add(fish);
        ocean.add(hunter);
        TestOceans.run(ocean, 0.2);
        assertTrue(fish.velocity().x() > 0, "moving away from the hunter");
    }

    @Test
    void fullHunterDoesNotEatPreyThatBumpsIntoIt() throws Exception {
        Creature prey = TestOceans.creature(TestSpecies.smallFish(), 1, 800, 450, 1, 1, 0, 1);
        Creature full = TestOceans.creature(TestSpecies.bigFish(), 2, 805, 450, 5, 0.95, 0, 1);
        ocean.add(prey);
        ocean.add(full);
        ocean.step(1.0 / 60);
        assertTrue(prey.isAlive());
    }

    @Test
    void clamIsNotCarriedByACurrent() throws Exception {
        Creature clam = TestOceans.creature(TestSpecies.clam(), 1, 500, 820, 1, 1, 0, 1);
        ocean.add(clam);
        ocean.events().restore(java.util.List.of(new co.edu.autonoma.ancientseas.simulation.EventState(
                co.edu.autonoma.ancientseas.simulation.EventType.CURRENT, 0.5, 5, 1, "")), 1e9,
                id -> java.util.Optional.empty());
        TestOceans.run(ocean, 1);
        assertEquals(500, clam.position().x(), 1e-9);
    }

    @Test
    void clamNeverMoves() throws Exception {
        Creature clam = TestOceans.creature(TestSpecies.clam(), 1, 500, 880, 1, 1, 0, 1);
        ocean.add(clam);
        TestOceans.run(ocean, 2);
        assertEquals(500, clam.position().x(), 1e-9);
    }

    @Test
    void aDrifterRestoredMidJetKeepsItsJetDirectionNotItsVelocity() {
        CreatureState mid = new CreatureState(9, "baculites", false, 500, 400, 3, 0, 0.6, 0.8, 10, 0, 0, 1, 1, 0, 0,
                0, 1.2, 0, 0, 1, null);

        Creature restored = CreatureFactory.restore(TestSpecies.ammonite(), mid);

        assertEquals(0.0, restored.toState().burstDirectionX());
        assertEquals(1.0, restored.toState().burstDirectionY());
    }

    @Test
    void aNewbornNextToAdultsOfItsSpeciesIsNotEatenButOtherSmallPreyIs() {
        Creature adult = TestOceans.creature(TestSpecies.smallFish(), 1, 500, 400, 1, 0.3, 0, 1);
        Creature newborn = TestOceans.creature(TestSpecies.smallFish(), 2, 505, 400, 0.35, 0.5, 0, 1);
        Creature otherYoung = TestOceans.creature(TestSpecies.ammonite(), 3, 510, 400, 0.3, 0.5, 0, 1);

        assertFalse(adult.canEat(newborn));
        assertTrue(adult.canEat(otherYoung));
    }

    @Test
    void aVanishedSpeciesGetsNewcomersFromTheEdgeWithoutExceedingItsCap() {
        Ocean sea = new Ocean(Ocean.DEFAULT_WIDTH, Ocean.DEFAULT_HEIGHT, ResumableRandom.seeded(3), 1.0,
                TestOceans.NO_EVENTS, Map.of("enchodus", 8), List.of(TestSpecies.smallFish()), List.of());
        Set<Long> seen = new HashSet<>();
        int most = 0;

        for (int step = 0; step < 120 * 60; step++) {
            sea.step(1.0 / 60);
            for (Creature c : sea.creatures()) {
                if (seen.add(c.id()) && seen.size() == 1) {
                    double x = c.position().x();
                    assertTrue(x <= 2 || x >= sea.width() - 2, "the first newcomer enters at a side edge: " + x);
                }
            }
            most = Math.max(most, sea.populationOf("enchodus"));
        }

        assertTrue(sea.populationOf("enchodus") >= 1, "the species came back");
        assertTrue(most <= 8, "never more than the cap: " + most);
    }

    @Test
    void aPlanktivoreFeedsOnPlanktonButNeverOnFishOrAHatchlingTurtle() throws Exception {
        Species baculites = TestSpecies.valid("baculites").diet(Diet.PLANKTIVORE).behavior(Behavior.DRIFTER)
                .shape(BodyShape.STRAIGHT_AMMONITE).lengthMeters(1).build();
        Species archelon = TestSpecies.valid("archelon-ischyros").diet(Diet.OMNIVORE).airBreather(true)
                .shape(BodyShape.TURTLE).lengthMeters(3.5).build();
        Creature adult = TestOceans.creature(baculites, 1, 500, 400, 1.0, 0.3, 0, 1);
        Creature youngFish = TestOceans.creature(TestSpecies.smallFish(), 2, 505, 400, 0.28, 0.5, 0, 1);
        PlayerCreature hatchling = new PlayerCreature(3, archelon, new Vector2(510, 400));
        ocean.add(adult);
        ocean.restoreFood(new FoodState(4, 500, 400, 30));

        assertTrue(hatchling.sizeMeters() <= adult.sizeMeters() * Creature.PREY_SIZE_RATIO, "small enough to fit");
        assertFalse(adult.canEat(youngFish));
        assertFalse(adult.canEat(hatchling));
        TestOceans.run(ocean, 0.5);
        assertTrue(ocean.food().stream().noneMatch(f -> f.id() == 4), "the plankton particle was eaten");
        assertTrue(adult.energy() > 0.3);
    }
}
