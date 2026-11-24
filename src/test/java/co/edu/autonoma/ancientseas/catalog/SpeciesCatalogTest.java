package co.edu.autonoma.ancientseas.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.autonoma.ancientseas.TestSpecies;
import co.edu.autonoma.ancientseas.error.DuplicateSpeciesException;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class SpeciesCatalogTest {

    @Test
    void duplicateIdIsRejectedAndCatalogUnchanged() throws Exception {
        SpeciesCatalog catalog = new SpeciesCatalog();
        catalog.add(TestSpecies.valid("a").build());
        assertThrows(DuplicateSpeciesException.class, () -> catalog.add(TestSpecies.valid("a").lengthMeters(9).build()));
        assertEquals(1, catalog.size());
        assertEquals(2, catalog.find("a").orElseThrow().lengthMeters());
    }

    @Test
    void replaceKeepsOrderEvenWhenIdChanges() throws Exception {
        SpeciesCatalog catalog = new SpeciesCatalog();
        for (String id : List.of("a", "b", "c")) {
            catalog.add(TestSpecies.valid(id).build());
        }
        catalog.replace("b", TestSpecies.valid("z").build());
        assertEquals(List.of("a", "z", "c"), catalog.all().stream().map(Species::id).toList());
        assertTrue(catalog.find("b").isEmpty());
    }

    @Test
    void replaceCannotTakeAnotherSpeciesId() throws Exception {
        SpeciesCatalog catalog = new SpeciesCatalog();
        catalog.add(TestSpecies.valid("a").build());
        catalog.add(TestSpecies.valid("b").build());
        assertThrows(DuplicateSpeciesException.class, () -> catalog.replace("a", TestSpecies.valid("b").build()));
        assertEquals(List.of("a", "b"), catalog.all().stream().map(Species::id).toList());
    }

    @Test
    void replacingUnknownSpeciesFails() {
        SpeciesCatalog catalog = new SpeciesCatalog();
        assertThrows(NoSuchElementException.class, () -> catalog.replace("ghost", TestSpecies.valid("ghost").build()));
    }

    @Test
    void removeReportsWhetherSomethingWasRemoved() throws Exception {
        SpeciesCatalog catalog = new SpeciesCatalog();
        catalog.add(TestSpecies.valid("a").build());
        assertTrue(catalog.remove("a"));
        assertFalse(catalog.remove("a"));
        assertEquals(0, catalog.size());
    }

    @Test
    void filterAndSortWithLambdas() {
        SpeciesCatalog catalog = TestSpecies.catalog();
        List<Species> floor = catalog.filter(s -> s.zone() == Zone.SEAFLOOR);
        assertEquals(List.of("inoceramus"), floor.stream().map(Species::id).toList());
        List<Species> bySize = catalog.sorted(Comparator.comparingDouble(Species::lengthMeters).reversed());
        assertEquals("tylosaurus", bySize.get(0).id());
        assertThrows(UnsupportedOperationException.class, () -> bySize.add(bySize.get(0)));
    }

    @Test
    void aCopyIsIndependentSoUnsavedAdminChangesNeverReachTheGame() throws Exception {
        SpeciesCatalog original = TestSpecies.catalog();
        SpeciesCatalog draft = new SpeciesCatalog(original);

        draft.add(TestSpecies.valid("nueva").build());
        draft.remove("tylosaurus");

        assertTrue(original.find("tylosaurus").isPresent());
        assertFalse(original.find("nueva").isPresent());
        assertEquals(TestSpecies.catalog().size(), original.size());
        assertEquals(original.all(), new SpeciesCatalog(original).all(), "same species in the same order");
    }
}
