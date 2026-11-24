package co.edu.autonoma.ancientseas.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.autonoma.ancientseas.TestSpecies;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CoexistenceTest {

    private static Species aged(String id, double youngest, double oldest) {
        try {
            return TestSpecies.valid(id).ageMa(youngest, oldest).build();
        } catch (co.edu.autonoma.ancientseas.error.AncientSeasException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void speciesOfTheSameTimeAreAllTogether() {
        Coexistence result = Coexistence.of(List.of(aged("a", 74, 80), aged("b", 78, 85), aged("c", 79, 79.5)));

        assertTrue(result.apart().isEmpty());
        assertEquals(3, result.together().size());
        assertEquals(Range.of(79.0, 79.5), result.sharedPeriod().orElseThrow());
    }

    @Test
    void aCambrianSpeciesAddedByStaffIsDetectedAsFromAnotherTime() {
        List<Species> species = new ArrayList<>(TestSpecies.catalog().all());
        Species anomalocaris = aged("anomalocaris", 505, 515);
        species.add(anomalocaris);

        Coexistence result = Coexistence.of(species);

        assertEquals(List.of(anomalocaris), result.apart());
        assertFalse(result.together().contains(anomalocaris));
        assertFalse(result.fits(anomalocaris));
    }

    @Test
    void rangesThatOnlyTouchAtABoundStillCoexist() {
        Coexistence result = Coexistence.of(List.of(aged("older", 80, 89.8), aged("younger", 74, 80)));

        assertTrue(result.apart().isEmpty());
        assertEquals(Range.of(80.0, 80.0), result.sharedPeriod().orElseThrow());
    }

    @Test
    void noSpeciesMeansNoSharedPeriodAndAnythingFits() {
        Coexistence result = Coexistence.of(List.of());

        assertTrue(result.sharedPeriod().isEmpty());
        assertTrue(result.fits(aged("any", 1, 2)));
    }

    @Test
    void livedAtAMomentAndLivedAtAPeriodAreDifferentQuestions() {
        Species species = aged("s", 74, 80);

        assertTrue(species.livedAt(80.0));
        assertFalse(species.livedAt(81.0));
        assertTrue(species.livedAt(Range.of(79.0, 90.0)));
        assertFalse(species.livedAt(Range.of(81.0, 90.0)));
    }
}
