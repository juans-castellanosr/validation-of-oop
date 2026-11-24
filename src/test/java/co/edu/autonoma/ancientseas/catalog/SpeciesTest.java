package co.edu.autonoma.ancientseas.catalog;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.autonoma.ancientseas.TestSpecies;
import co.edu.autonoma.ancientseas.error.HabitatViolationException;
import co.edu.autonoma.ancientseas.error.InvalidScientificValueException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

class SpeciesTest {

    @ParameterizedTest
    @ValueSource(doubles = {-1, 0, Species.MAX_LENGTH_METERS + 0.01, Double.NaN})
    void impossibleLengthsAreRejected(double length) {
        InvalidScientificValueException e = assertThrows(InvalidScientificValueException.class,
                () -> TestSpecies.valid("x").lengthMeters(length).build());
        assertEquals("tamaño", e.getField());
    }

    @Test
    void maximumLengthIsAccepted() {
        assertDoesNotThrow(() -> TestSpecies.valid("x").lengthMeters(Species.MAX_LENGTH_METERS).build());
    }

    @Test
    void reversedOrEqualAgesAreRejected() {
        assertThrows(InvalidScientificValueException.class, () -> TestSpecies.valid("x").ageMa(80, 75).build());
        assertThrows(InvalidScientificValueException.class, () -> TestSpecies.valid("x").ageMa(80, 80).build());
        assertThrows(InvalidScientificValueException.class, () -> TestSpecies.valid("x").ageMa(-1, 5).build());
        assertThrows(InvalidScientificValueException.class, () -> TestSpecies.valid("x").ageMa(1, 5000).build());
    }

    @Test
    void missingAgeIsRejected() {
        assertThrows(InvalidScientificValueException.class,
                () -> TestSpecies.valid("x").ageMa(Double.NaN, 5).build());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "Archelon", "archelon_ischyros", "-archelon", "archelon-", "a--b", "ñandú"})
    void malformedIdsAreRejected(String id) {
        assertThrows(InvalidScientificValueException.class, () -> TestSpecies.valid("x").id(id).build());
    }

    @Test
    void blankTextFieldsAreRejected() {
        assertThrows(InvalidScientificValueException.class, () -> TestSpecies.valid("x").commonName(" ").build());
        assertThrows(InvalidScientificValueException.class, () -> TestSpecies.valid("x").source(null).build());
        assertThrows(InvalidScientificValueException.class, () -> TestSpecies.valid("x").diet(null).build());
    }

    @Test
    void sessileAnimalsMustLiveOnTheFloor() {
        assertThrows(HabitatViolationException.class,
                () -> TestSpecies.valid("x").behavior(Behavior.SESSILE).zone(Zone.MIDWATER).build());
    }

    @Test
    void airBreathersCannotLiveOnTheFloor() {
        assertThrows(HabitatViolationException.class,
                () -> TestSpecies.valid("x").airBreather(true).zone(Zone.SEAFLOOR).build());
    }

    @Test
    void textIsTrimmedAndEditedCopyKeepsOtherFields() throws Exception {
        Species original = TestSpecies.valid("x").commonName("  Pez  ").build();
        assertEquals("Pez", original.commonName());
        Species edited = original.toBuilder().lengthMeters(3).build();
        assertEquals(3, edited.lengthMeters());
        assertEquals(original.commonName(), edited.commonName());
        assertEquals(original.ageMa(), edited.ageMa());
        assertEquals(original, edited, "identity is the id");
    }

    @Test
    void anAirBreatherThatTheGameCannotMakeBreatheIsRejected() {
        Species.Builder schoolingIchthyosaur = TestSpecies.valid("ictiosaurio").airBreather(true)
                .behavior(Behavior.SCHOOLING);

        assertThrows(HabitatViolationException.class, schoolingIchthyosaur::build);
    }
}
