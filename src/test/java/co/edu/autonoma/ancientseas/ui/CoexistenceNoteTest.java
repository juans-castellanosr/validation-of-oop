package co.edu.autonoma.ancientseas.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.autonoma.ancientseas.TestSpecies;
import co.edu.autonoma.ancientseas.catalog.Species;
import co.edu.autonoma.ancientseas.catalog.SpeciesCatalog;
import org.junit.jupiter.api.Test;

class CoexistenceNoteTest {

    private static Species cambrian() throws Exception {
        return TestSpecies.valid("anomalocaris").commonName("Anomalocaris").ageMa(505, 515).build();
    }

    @Test
    void aCatalogOfOneTimeSaysTheyAllLivedTogether() {
        String note = CoexistenceNote.text(TestSpecies.catalog());

        assertTrue(note.startsWith("Todos estos animales vivieron en la misma época"), note);
        assertTrue(note.contains("hace entre 75 y 80 millones de años"), note);
    }

    @Test
    void aSpeciesFromAnotherTimeIsNamedAndExplained() throws Exception {
        SpeciesCatalog catalog = TestSpecies.catalog();
        catalog.add(cambrian());

        String note = CoexistenceNote.text(catalog);

        assertFalse(note.startsWith("Todos"), note);
        assertTrue(note.contains("Anomalocaris (hace entre 505 y 515 millones de años) vivió en otra época"), note);
    }

    @Test
    void staffAreWarnedOnlyAboutSpeciesThatDoNotFit() throws Exception {
        SpeciesCatalog catalog = TestSpecies.catalog();

        assertEquals("", CoexistenceNote.warningFor(catalog, TestSpecies.valid("ok").build()));
        assertTrue(CoexistenceNote.warningFor(catalog, cambrian()).contains("vivió en otra época"));
    }

    @Test
    void anEmptyCatalogHasNoNote() {
        assertEquals("", CoexistenceNote.text(new SpeciesCatalog()));
    }
}
