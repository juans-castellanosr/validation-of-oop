package co.edu.autonoma.ancientseas.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.autonoma.ancientseas.catalog.Behavior;
import co.edu.autonoma.ancientseas.catalog.Range;
import co.edu.autonoma.ancientseas.catalog.Species;
import co.edu.autonoma.ancientseas.catalog.SpeciesCatalog;
import co.edu.autonoma.ancientseas.game.GameSession;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BundledCatalogTest {

    @TempDir
    Path dir;

    private LoadResult<SpeciesCatalog> loadBundled() throws Exception {
        Path copy = dir.resolve("species.csv");
        try (InputStream in = BundledCatalogTest.class.getResourceAsStream("/data/species.csv")) {
            Files.copy(in, copy);
        }
        return new SpeciesCsv().read(copy);
    }

    @Test
    void loadsCleanlyWithThePlayerSpecies() throws Exception {
        LoadResult<SpeciesCatalog> result = loadBundled();
        assertEquals(java.util.List.of(), result.problems());
        assertTrue(result.value().find(GameSession.PLAYER_SPECIES_ID).isPresent());
        assertTrue(result.value().size() >= 6);
    }

    @Test
    void everySpeciesBelongsToTheCampanianSea() throws Exception {
        Range<Double> campanian = Range.of(72.1, 83.6);
        for (Species s : loadBundled().value().all()) {
            assertTrue(s.ageMa().overlaps(campanian), s.id() + " must have lived in the Campanian");
        }
    }

    @Test
    void allSpeciesShareACommonAgeWindowSoTheSceneDoesNotMixPeriods() throws Exception {
        java.util.List<Species> all = loadBundled().value().all();
        double youngestStart = all.stream().mapToDouble(s -> s.ageMa().min()).max().orElseThrow();
        double oldestEnd = all.stream().mapToDouble(s -> s.ageMa().max()).min().orElseThrow();
        assertTrue(youngestStart <= oldestEnd, "age ranges must overlap: " + youngestStart + " > " + oldestEnd);
    }

    @Test
    void hasPreyAndPredatorsForAnEcosystem() throws Exception {
        SpeciesCatalog catalog = loadBundled().value();
        assertTrue(catalog.filter(s -> s.behavior() == Behavior.HUNTER).size() >= 2);
        assertTrue(catalog.filter(s -> s.behavior() != Behavior.HUNTER).size() >= 2);
    }
}
