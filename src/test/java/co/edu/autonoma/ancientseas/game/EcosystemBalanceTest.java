package co.edu.autonoma.ancientseas.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.autonoma.ancientseas.catalog.Behavior;
import co.edu.autonoma.ancientseas.catalog.PreyKind;
import co.edu.autonoma.ancientseas.catalog.Species;
import co.edu.autonoma.ancientseas.catalog.SpeciesCatalog;
import co.edu.autonoma.ancientseas.model.Creature;
import co.edu.autonoma.ancientseas.model.CreatureFactory;
import co.edu.autonoma.ancientseas.model.Edible;
import co.edu.autonoma.ancientseas.model.PlayerCreature;
import co.edu.autonoma.ancientseas.model.Vector2;
import co.edu.autonoma.ancientseas.persistence.SpeciesCsv;
import co.edu.autonoma.ancientseas.simulation.Ocean;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

@Timeout(60)
class EcosystemBalanceTest {

    private static final double DT = 1.0 / 60;
    private static SpeciesCatalog catalog;

    @BeforeAll
    static void loadBundledCatalog(@TempDir Path dir) throws Exception {
        Path csv = dir.resolve("species.csv");
        try (InputStream in = EcosystemBalanceTest.class.getResourceAsStream("/data/species.csv")) {
            Files.copy(in, csv);
        }
        catalog = new SpeciesCsv().read(csv).value();
    }

    private record OceanRun(int initialPrey, int minPrey, int finalPrey, int maxTotal, int capTotal,
            Set<String> extinctPrey, int births, int deaths) {
    }

    private static boolean isPrey(Creature c) {
        return !c.isPlayer() && c.species().behavior() != Behavior.HUNTER;
    }

    private static OceanRun runOcean(int seed, double seconds) {
        GameSession game = GameSession.newGame(catalog, GameSettings.defaults(), new Random(seed));
        Ocean ocean = game.ocean();
        int capTotal = ocean.populationCaps().values().stream().mapToInt(Integer::intValue).sum();
        Set<String> preySpecies = ocean.creatures().stream().filter(EcosystemBalanceTest::isPrey)
                .map(c -> c.species().id()).collect(Collectors.toSet());
        int initialPrey = (int) ocean.creatures().stream().filter(EcosystemBalanceTest::isPrey).count();
        int minPrey = initialPrey;
        int maxTotal = 0;
        Set<Long> seen = new HashSet<>();
        ocean.creatures().forEach(c -> seen.add(c.id()));
        int births = 0;
        int deaths = 0;
        long steps = Math.round(seconds / DT);
        for (long s = 0; s < steps; s++) {
            int before = ocean.creatures().size();
            ocean.step(DT);
            int newborn = 0;
            for (Creature c : ocean.creatures()) {
                if (seen.add(c.id())) {
                    newborn++;
                }
            }
            births += newborn;
            deaths += Math.max(0, before + newborn - ocean.creatures().size());
            int prey = (int) ocean.creatures().stream().filter(EcosystemBalanceTest::isPrey).count();
            minPrey = Math.min(minPrey, prey);
            maxTotal = Math.max(maxTotal, ocean.creatures().size());
        }
        Map<String, Long> alive = ocean.creatures().stream().filter(EcosystemBalanceTest::isPrey)
                .collect(Collectors.groupingBy(c -> c.species().id(), Collectors.counting()));
        Set<String> extinct = preySpecies.stream().filter(id -> !alive.containsKey(id)).collect(Collectors.toSet());
        int finalPrey = (int) ocean.creatures().stream().filter(EcosystemBalanceTest::isPrey).count();
        OceanRun run = new OceanRun(initialPrey, minPrey, finalPrey, maxTotal, capTotal, extinct, births, deaths);
        System.out.println("ocean seed " + seed + ": " + run);
        return run;
    }

    private record Match(GameStatus outcome, double seconds, double firstBite) {
    }

    private static Match play(int seed, Difficulty difficulty, Consumer<GameSession> controller, double limit) {
        GameSettings settings = new GameSettings(difficulty, GameSettings.defaults().creatureCount(),
                GameSettings.defaults().eventFrequency(), "Prueba");
        GameSession game = GameSession.newGame(catalog, settings, new Random(seed));
        game.start();
        double firstBite = -1;
        double health = 1;
        long steps = Math.round(limit / DT);
        for (long s = 0; s < steps && game.status() == GameStatus.RUNNING; s++) {
            controller.accept(game);
            game.step(DT);
            if (firstBite < 0 && game.player().healthLevel() < health - 0.01) {
                firstBite = game.elapsedSeconds();
            }
            health = game.player().healthLevel();
        }
        Match match = new Match(game.status(), game.elapsedSeconds(), firstBite);
        System.out.println("match seed " + seed + " " + difficulty + ": " + match);
        return match;
    }

    private static void competent(GameSession game) {
        PlayerCreature player = game.player();
        Ocean ocean = game.ocean();
        Optional<Creature> threat = ocean.nearestThreat(player, 240);
        if (player.oxygenLevel() < 0.35) {
            player.steerTowards(new Vector2(0, -1));
        } else if (threat.isPresent()) {
            player.steerTowards(player.position().minus(threat.get().position()));
        } else {
            Optional<Edible> food = ocean.nearestEdible(player, 2000);
            player.steerTowards(food.map(f -> f.position().minus(player.position())).orElse(Vector2.ZERO));
        }
    }

    @Test
    void preyRenewsAndStaysWithinTheCapsOverALongGame() {
        for (int seed = 1; seed <= 3; seed++) {
            OceanRun run = runOcean(seed, 300);

            assertTrue(run.maxTotal() <= run.capTotal() + 1, "never more animals than the caps allow: " + run);
            assertTrue(run.minPrey() >= run.initialPrey() / 2, "prey never drops below half: " + run);
            assertTrue(run.extinctPrey().isEmpty(), "no prey species dies out: " + run);
            assertTrue(run.births() > 0, "prey keep being born: " + run);
        }
    }

    @Test
    void onlyReptileEatersBigEnoughCanTakeAHatchlingArchelon() {
        Species archelon = catalog.find(GameSession.PLAYER_SPECIES_ID).orElseThrow();
        PlayerCreature hatchling = new PlayerCreature(1, archelon, new Vector2(800, 300));
        Set<String> able = new HashSet<>();
        long id = 2;
        for (Species species : catalog.all()) {
            Creature adult = CreatureFactory.create(id++, species, new Vector2(800, 450), species.lengthMeters(), 0.3);
            if (adult.canEat(hatchling)) {
                able.add(species.id());
                assertTrue(species.diet().accepts(PreyKind.REPTILE), species.id() + " does not eat reptiles");
            }
        }
        assertEquals(Set.of("tylosaurus-proriger", "cretoxyrhina-mantelli"), able);
    }

    @Test
    void aCompetentPlayerCanWinOnEveryDifficulty() {
        for (Difficulty difficulty : Difficulty.values()) {
            int wins = 0;
            for (int seed = 1; seed <= 3; seed++) {
                if (play(seed, difficulty, EcosystemBalanceTest::competent, 600).outcome() == GameStatus.WON) {
                    wins++;
                }
            }
            assertTrue(wins >= 2, difficulty + ": a sensible strategy wins at least 2 of 3 games, won " + wins);
        }
    }
}
