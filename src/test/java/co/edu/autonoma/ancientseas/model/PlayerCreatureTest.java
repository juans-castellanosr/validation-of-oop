package co.edu.autonoma.ancientseas.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.autonoma.ancientseas.TestOceans;
import co.edu.autonoma.ancientseas.TestSpecies;
import co.edu.autonoma.ancientseas.simulation.Ocean;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PlayerCreatureTest {

    private final Ocean ocean = TestOceans.empty(Map.of());

    private PlayerCreature playerAt(double x, double y) throws Exception {
        PlayerCreature player = new PlayerCreature(100, TestSpecies.archelon(), new Vector2(x, y));
        ocean.add(player);
        return player;
    }

    @Test
    void startsAsAHatchlingOfTheRightSize() throws Exception {
        PlayerCreature player = playerAt(800, 100);
        assertEquals(GrowthStage.HATCHLING, player.stage());
        assertEquals(4 * 0.15, player.sizeMeters(), 1e-9);
    }

    @Test
    void eatingMakesItGrow() throws Exception {
        PlayerCreature player = playerAt(800, 100);
        double before = player.sizeMeters();
        ocean.restoreFood(new FoodState(1, 800, 100, 30));
        ocean.step(1.0 / 60);
        assertEquals(1, player.foodPoints(), 1e-9);
        assertTrue(player.sizeMeters() > before);
    }

    @Test
    void twoHuntersBitingAtOnceHurtOnlyOnce() throws Exception {
        PlayerCreature player = playerAt(800, 450);
        ocean.add(TestOceans.creature(TestSpecies.mosasaur(), 1, 790, 450, 12, 0.5, 0, 1));
        ocean.add(TestOceans.creature(TestSpecies.mosasaur(), 2, 810, 450, 12, 0.5, 0, 1));
        ocean.step(1.0 / 60);
        assertEquals(1 - PlayerCreature.BITE_DAMAGE, player.healthLevel(), 1e-9);
        assertTrue(player.isAlive());
        assertTrue(player.isRecovering());
    }

    @Test
    void threeBitesAreFatal() throws Exception {
        PlayerCreature player = playerAt(800, 450);
        for (int bite = 0; bite < 3; bite++) {
            assertTrue(player.consume());
            TestOceans.run(ocean, 2.1);
        }
        assertFalse(player.isAlive());
    }

    @Test
    void runsOutOfAirUnderwaterAndGetsHurt() throws Exception {
        PlayerCreature player = playerAt(800, 880);
        TestOceans.run(ocean, 34);
        assertEquals(1.0, player.healthLevel(), 1e-9, "still has some air");
        TestOceans.run(ocean, 5);
        assertTrue(player.lungs().isEmpty());
        assertTrue(player.healthLevel() < 1.0);
    }

    @Test
    void breathesAtTheSurface() throws Exception {
        PlayerCreature player = playerAt(800, 880);
        TestOceans.run(ocean, 20);
        double low = player.oxygenLevel();
        player.swimTo(new Vector2(800, 0));
        TestOceans.run(ocean, 12);
        assertTrue(player.oxygenLevel() > low);
    }

    @Test
    void mouseDestinationIsReachedAndThenItStops() throws Exception {
        PlayerCreature player = playerAt(200, 300);
        player.swimTo(new Vector2(500, 300));
        TestOceans.run(ocean, 5);
        assertEquals(500, player.position().x(), player.radius() + 5);
    }

    @Test
    void keyboardDirectionMovesIt() throws Exception {
        PlayerCreature player = playerAt(800, 300);
        player.steerTowards(new Vector2(-1, 0));
        TestOceans.run(ocean, 1);
        assertTrue(player.position().x() < 800 - 50);
    }

    @Test
    void growthStagesFollowPoints() {
        double juvenile = GrowthStage.JUVENILE.pointsRequired();
        assertEquals(GrowthStage.HATCHLING, GrowthStage.forPoints(juvenile - 0.01));
        assertEquals(GrowthStage.JUVENILE, GrowthStage.forPoints(juvenile));
        assertEquals(GrowthStage.ADULT, GrowthStage.forPoints(1e6));
        assertEquals(4.0, GrowthStage.lengthAt(GrowthStage.ADULT.pointsRequired(), 4.0), 1e-9);
        assertTrue(GrowthStage.lengthAt(juvenile + 5, 4) > GrowthStage.lengthAt(juvenile, 4));
        assertEquals(1.0, GrowthStage.overallProgress(500));
    }

    @Test
    void aHungryHunterInSightHuntsAStillPlayer() throws Exception {
        PlayerCreature player = playerAt(800, 300);
        player.steerTowards(Vector2.ZERO);
        ocean.add(TestOceans.creature(TestSpecies.mosasaur(), 1, 1000, 300, 12, 0.3, 0, 1));

        TestOceans.run(ocean, 8);

        assertTrue(player.healthLevel() < 1, "a still player is not safe from a hungry hunter nearby");
    }

    @Test
    void aFullHunterLeavesAStillPlayerAlone() throws Exception {
        PlayerCreature player = playerAt(800, 300);
        player.steerTowards(Vector2.ZERO);
        ocean.add(TestOceans.creature(TestSpecies.mosasaur(), 1, 1000, 300, 12, 1.0, 0, 1));

        TestOceans.run(ocean, 5);

        assertEquals(1.0, player.healthLevel(), 1e-9);
    }
}
