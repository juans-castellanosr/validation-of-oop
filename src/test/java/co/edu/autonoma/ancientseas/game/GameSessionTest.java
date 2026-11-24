package co.edu.autonoma.ancientseas.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.autonoma.ancientseas.TestSpecies;
import co.edu.autonoma.ancientseas.catalog.Behavior;
import co.edu.autonoma.ancientseas.catalog.SpeciesCatalog;
import co.edu.autonoma.ancientseas.error.CorruptSaveException;
import co.edu.autonoma.ancientseas.error.IncompatibleSaveException;
import co.edu.autonoma.ancientseas.model.Creature;
import co.edu.autonoma.ancientseas.model.CreatureState;
import co.edu.autonoma.ancientseas.model.FoodState;
import co.edu.autonoma.ancientseas.model.GrowthStage;
import co.edu.autonoma.ancientseas.model.Vector2;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class GameSessionTest {

    private final SpeciesCatalog catalog = TestSpecies.catalog();

    private GameSession newGame() {
        return GameSession.newGame(catalog, GameSettings.defaults(), new Random(1));
    }

    private static void run(GameSession game, double seconds) {
        for (int i = 0; i < Math.round(seconds * 60); i++) {
            game.step(1.0 / 60);
        }
    }

    @Test
    void newGameIsPopulatedAndHuntersStartAwayFromThePlayer() {
        GameSession game = newGame();
        assertEquals(GameStatus.READY, game.status());
        assertTrue(game.ocean().creatures().size() >= catalog.size());
        assertTrue(game.ocean().creatures().stream().filter(c -> c.species().behavior() == Behavior.HUNTER)
                .filter(c -> !c.isPlayer())
                .allMatch(h -> h.position().distanceTo(game.player().position()) > 300));
        assertEquals(1, game.ocean().creatures().stream().filter(Creature::isPlayer).count());
    }

    @Test
    void gameWithoutThePlayerSpeciesCannotBeCreated() {
        catalog.remove(GameSession.PLAYER_SPECIES_ID);
        assertThrows(IllegalStateException.class, this::newGame);
    }

    @Test
    void lifeCycleRejectsInvalidTransitions() {
        GameSession game = newGame();
        assertThrows(IllegalStateException.class, game::pause);
        assertThrows(IllegalStateException.class, game::resume);
        game.start();
        assertThrows(IllegalStateException.class, game::start);
        game.pause();
        assertThrows(IllegalStateException.class, game::pause);
        game.resume();
        game.end();
        assertEquals(GameStatus.ENDED, game.status());
        game.end();
        assertEquals(GameStatus.ENDED, game.status(), "ending twice is harmless");
        assertThrows(IllegalStateException.class, game::resume);
    }

    @Test
    void pausedGameFreezesTimeAndEventCountdown() {
        GameSession game = newGame();
        game.start();
        run(game, 1);
        game.pause();
        GameMemento before = game.toMemento();
        run(game, 30);
        GameMemento after = game.toMemento();
        assertEquals(before, after, "nothing at all changes while paused");
    }

    @Test
    void notStartedGameDoesNotAdvance() {
        GameSession game = newGame();
        assertTrue(game.step(1).isEmpty());
        assertEquals(0, game.elapsedSeconds());
    }

    @Test
    void saveAndRestoreReproducesTheExactState() throws Exception {
        GameSession game = newGame();
        game.start();
        run(game, 20);
        GameMemento saved = game.toMemento();

        GameSession restored = GameSession.restore(saved, catalog);

        assertEquals(GameStatus.PAUSED, restored.status(), "a resumed game waits for the visitor");
        assertEquals(saved, restored.toMemento());
        assertEquals(game.score(), restored.score());
        restored.resume();
        run(restored, 1);
        assertTrue(restored.elapsedSeconds() > saved.elapsedSeconds());
    }

    @Test
    void aRestoredOceanContinuesExactlyAsTheOriginalWould() throws Exception {
        GameSession original = newGame();
        original.start();
        run(original, 15);
        GameMemento saved = original.toMemento();
        GameSession restored = GameSession.restore(saved, catalog);
        restored.resume();

        run(original, 10);
        run(restored, 10);

        assertEquals(original.toMemento(), restored.toMemento(),
                "same creatures, food, events and random sequence after 10 more seconds");
    }

    @Test
    void aClickDestinationSurvivesSaveAndRestore() throws Exception {
        GameSession original = newGame();
        original.start();
        run(original, 2);
        original.player().swimTo(new Vector2(1500, 800));
        run(original, 0.5);
        GameSession restored = GameSession.restore(original.toMemento(), catalog);
        restored.resume();

        run(original, 3);
        run(restored, 3);

        assertEquals(original.toMemento(), restored.toMemento());
    }

    @Test
    void aSaveIsRefusedAfterStaffChangeOneOfItsSpecies() throws Exception {
        GameMemento saved = newGame().toMemento();
        SpeciesCatalog edited = new SpeciesCatalog(catalog);
        edited.replace("baculites", catalog.find("baculites").orElseThrow().toBuilder()
                .behavior(Behavior.HUNTER).build());

        IncompatibleSaveException refused = assertThrows(IncompatibleSaveException.class,
                () -> GameSession.restore(saved, edited));
        assertTrue(refused.getFriendlyMessage().contains("cambio en el catálogo"));
    }

    @Test
    void impossibleClocksFoodIdsCapsAndEventsAreReportedAsDamage() {
        GameMemento m = newGame().toMemento();
        CreatureState anyone = m.creatures().get(0);
        List<GameMemento> broken = List.of(
                new GameMemento(m.settings(), -500, m.nextId(), m.creatures(), m.food(), m.events(),
                        m.secondsUntilNextEvent(), m.populationCaps(), m.planktonDue()),
                new GameMemento(m.settings(), m.elapsedSeconds(), m.nextId(), m.creatures(),
                        List.of(new FoodState(anyone.id(), 10, 10, 5)), m.events(), m.secondsUntilNextEvent(),
                        m.populationCaps(), m.planktonDue()),
                new GameMemento(m.settings(), m.elapsedSeconds(), m.nextId(), m.creatures(),
                        List.of(new FoodState(m.nextId() + 5, 10, 10, -1)), m.events(), m.secondsUntilNextEvent(),
                        m.populationCaps(), m.planktonDue()),
                new GameMemento(m.settings(), m.elapsedSeconds(), m.nextId(), m.creatures(), m.food(), m.events(),
                        m.secondsUntilNextEvent(), java.util.Map.of("tylosaurus", -3), m.planktonDue()));
        for (GameMemento damaged : broken) {
            assertThrows(CorruptSaveException.class, () -> GameSession.restore(damaged, catalog));
        }
        GameMemento ghost = new GameMemento(m.settings(), m.elapsedSeconds(), m.nextId(), m.creatures(), m.food(),
                m.events(), m.secondsUntilNextEvent(), java.util.Map.of("ghost", 3), m.planktonDue());
        assertThrows(IncompatibleSaveException.class, () -> GameSession.restore(ghost, catalog));
    }

    @Test
    void restoreFailsWhenTheCatalogLostASpecies() {
        GameSession game = newGame();
        GameMemento saved = game.toMemento();
        catalog.remove("enchodus");
        assertThrows(IncompatibleSaveException.class, () -> GameSession.restore(saved, catalog));
    }

    @Test
    void restoreFailsWithoutAPlayer() {
        GameMemento saved = newGame().toMemento();
        List<CreatureState> noPlayer = saved.creatures().stream().filter(c -> !c.player()).toList();
        GameMemento broken = withCreatures(saved, noPlayer);
        assertThrows(CorruptSaveException.class, () -> GameSession.restore(broken, catalog));
    }

    @Test
    void restoreFailsWithTwoPlayers() {
        GameMemento saved = newGame().toMemento();
        List<CreatureState> creatures = new ArrayList<>(saved.creatures());
        CreatureState player = creatures.stream().filter(CreatureState::player).findFirst().orElseThrow();
        creatures.add(copy(player, 9999, player.x(), player.y(), player.health(), player.growthPoints()));
        assertThrows(CorruptSaveException.class, () -> GameSession.restore(withCreatures(saved, creatures), catalog));
    }

    @Test
    void restoreFailsWhenACreatureIsOutsideTheOcean() {
        GameMemento saved = newGame().toMemento();
        List<CreatureState> creatures = saved.creatures().stream()
                .map(c -> c.player() ? c : copy(c, c.id(), -500, c.y(), c.health(), c.growthPoints())).toList();
        assertThrows(CorruptSaveException.class, () -> GameSession.restore(withCreatures(saved, creatures), catalog));
    }

    @Test
    void reachingAdulthoodWinsTheGame() throws Exception {
        GameSession game = almostDone(1.0, GrowthStage.ADULT.pointsRequired() - 0.5);
        CreatureState p = playerState(game);
        game.ocean().restoreFood(new FoodState(77777, p.x(), p.y(), 30));
        game.resume();
        game.step(1.0 / 60);
        assertEquals(GameStatus.WON, game.status());
        ScoreEntry result = game.result("Ana", LocalDateTime.now()).orElseThrow();
        assertEquals(GameStatus.WON, result.outcome());
        assertTrue(result.score() >= 1000);
    }

    @Test
    void dyingLosesTheGame() throws Exception {
        GameSession game = almostDone(0.01, 0);
        game.player().consume();
        game.resume();
        game.step(1.0 / 60);
        assertEquals(GameStatus.LOST, game.status());
        assertFalse(game.player().isAlive());
    }

    @Test
    void finishedGameCannotBeSaved() throws Exception {
        GameSession game = almostDone(0.01, 0);
        game.player().consume();
        game.resume();
        game.step(1.0 / 60);
        assertThrows(IllegalStateException.class, game::toMemento);
    }

    @Test
    void saveOfAnAlreadyWonGameIsRejected() {
        GameMemento saved = newGame().toMemento();
        List<CreatureState> creatures = saved.creatures().stream()
                .map(c -> c.player() ? copy(c, c.id(), c.x(), c.y(), 1, GrowthStage.ADULT.pointsRequired()) : c)
                .toList();
        assertThrows(CorruptSaveException.class, () -> GameSession.restore(withCreatures(saved, creatures), catalog));
    }

    @Test
    void playerOfAnotherSpeciesIsRejected() {
        GameMemento saved = newGame().toMemento();
        List<CreatureState> creatures = saved.creatures().stream()
                .map(c -> c.player() ? new CreatureState(c.id(), "tylosaurus", true, c.x(), c.y(), 0, 0, 12, 1, 0, 0, 0,
                        1, 1, 0, 0, 0, 0, 0, 0, 0, null) : c)
                .toList();
        assertThrows(CorruptSaveException.class, () -> GameSession.restore(withCreatures(saved, creatures), catalog));
    }

    @Test
    void repeatedIdsAndImpossibleValuesAreCorruptNotCrashes() {
        GameMemento saved = newGame().toMemento();
        List<CreatureState> repeated = new ArrayList<>(saved.creatures());
        CreatureState other = repeated.stream().filter(c -> !c.player()).findFirst().orElseThrow();
        repeated.add(other);
        assertThrows(CorruptSaveException.class, () -> GameSession.restore(withCreatures(saved, repeated), catalog));
        List<CreatureState> zeroSize = saved.creatures().stream()
                .map(c -> c.player() ? c : new CreatureState(c.id(), c.speciesId(), false, c.x(), c.y(), 0, 0, 0,
                        1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, null))
                .toList();
        assertThrows(CorruptSaveException.class, () -> GameSession.restore(withCreatures(saved, zeroSize), catalog));
    }

    @Test
    void safeTimeAfterABiteSurvivesSaveAndRestore() throws Exception {
        GameSession game = almostDone(1.0, 0);
        game.player().consume();
        GameSession restored = GameSession.restore(game.toMemento(), catalog);
        assertTrue(restored.player().isRecovering());
        assertFalse(restored.player().consume(), "still protected right after the restore");
    }

    @Test
    void idsOfDeadEntitiesAreNeverReusedAfterRestore() throws Exception {
        GameMemento saved = newGame().toMemento();
        GameMemento advanced = new GameMemento(saved.settings(), saved.elapsedSeconds(), saved.nextId() + 1000,
                saved.creatures(), saved.food(), saved.events(), saved.secondsUntilNextEvent(),
                saved.populationCaps(), saved.planktonDue());
        GameSession restored = GameSession.restore(advanced, catalog);
        assertEquals(saved.nextId() + 1000, restored.ocean().nextId());
    }

    @Test
    void unfinishedGameHasNoResult() {
        assertTrue(newGame().result("Ana", LocalDateTime.now()).isEmpty());
    }

    @Test
    void harderGamesScoreMore() {
        GameSession easy = GameSession.newGame(catalog,
                new GameSettings(Difficulty.EASY, 45, EventFrequency.NORMAL, "a"), new Random(1));
        GameSession hard = GameSession.newGame(catalog,
                new GameSettings(Difficulty.HARD, 45, EventFrequency.NORMAL, "a"), new Random(1));
        easy.start();
        hard.start();
        easy.step(1);
        hard.step(1);
        assertTrue(hard.score() > easy.score());
    }

    private GameSession almostDone(double health, double growthPoints) throws Exception {
        GameMemento saved = GameSession.newGame(catalog,
                new GameSettings(Difficulty.EASY, 15, EventFrequency.LOW, "a"), new Random(3)).toMemento();
        List<CreatureState> creatures = saved.creatures().stream().filter(CreatureState::player)
                .map(c -> copy(c, c.id(), 800, 100, health, growthPoints)).toList();
        return GameSession.restore(withCreatures(saved, creatures), catalog);
    }

    private static CreatureState playerState(GameSession game) {
        return game.player().toState();
    }

    private static CreatureState copy(CreatureState c, long id, double x, double y, double health, double growth) {
        return new CreatureState(id, c.speciesId(), c.player(), x, y, c.vx(), c.vy(), c.sizeMeters(), c.energy(),
                c.ageSeconds(), c.digestSeconds(), c.reproductionSeconds(), c.oxygen(), health, growth,
                c.protectionSeconds(), c.wanderAngle(), c.burstSeconds(), c.burstRecoverySeconds(),
                c.burstDirectionX(), c.burstDirectionY(), c.destination());
    }

    private static GameMemento withCreatures(GameMemento m, List<CreatureState> creatures) {
        return new GameMemento(m.settings(), m.elapsedSeconds(), m.nextId(), creatures, m.food(), m.events(),
                m.secondsUntilNextEvent(), m.populationCaps(), m.planktonDue(), m.randomState(),
                m.speciesDefinitions());
    }
}
