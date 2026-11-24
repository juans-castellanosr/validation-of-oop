package co.edu.autonoma.ancientseas.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RangeTest {

    @Test
    void boundsAreInclusive() {
        Range<Integer> r = Range.of(15, 90);
        assertTrue(r.contains(15));
        assertTrue(r.contains(90));
        assertFalse(r.contains(14));
        assertFalse(r.contains(91));
    }

    @Test
    void clampBringsValuesToNearestBound() {
        Range<Double> r = Range.of(0.2, 0.8);
        assertEquals(0.2, r.clamp(-5.0));
        assertEquals(0.8, r.clamp(9.0));
        assertEquals(0.5, r.clamp(0.5));
    }

    @Test
    void singlePointRangeIsValid() {
        Range<Integer> r = Range.of(3, 3);
        assertTrue(r.contains(3));
        assertEquals(3, r.clamp(100));
    }

    @Test
    void reversedBoundsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> Range.of(10, 1));
    }

    @Test
    void overlapIncludesTouchingRanges() {
        assertTrue(Range.of(72.0, 80.0).overlaps(Range.of(80.0, 86.0)));
        assertFalse(Range.of(72.0, 79.9).overlaps(Range.of(80.0, 86.0)));
    }

    @Test
    void zonesCoverTheWholeColumnWithoutGaps() {
        assertEquals(Zone.SURFACE, Zone.at(0));
        assertEquals(Zone.MIDWATER, Zone.at(Zone.SURFACE.band().max()));
        assertEquals(Zone.SEAFLOOR, Zone.at(Zone.MIDWATER.band().max()));
        assertEquals(Zone.SEAFLOOR, Zone.at(1));
        assertEquals(Zone.SURFACE, Zone.at(-3), "depths above the surface are clamped");
        assertEquals(Zone.SEAFLOOR, Zone.at(7), "depths below the floor are clamped");
    }
}
