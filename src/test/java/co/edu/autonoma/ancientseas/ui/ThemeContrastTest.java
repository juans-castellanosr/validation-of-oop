package co.edu.autonoma.ancientseas.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import org.junit.jupiter.api.Test;

class ThemeContrastTest {

    @Test
    void everyTextPairReachesAaContrast() {
        for (Color[] pair : Theme.TEXT_PAIRS) {
            double ratio = Theme.contrast(pair[0], pair[1]);
            assertTrue(ratio >= Theme.MIN_TEXT_CONTRAST,
                    "contrast " + String.format("%.2f", ratio) + " for " + pair[0] + " on " + pair[1]);
        }
    }

    @Test
    void textOnTranslucentSurfacesReachesAaContrastEvenOverWhite() {
        for (Color[] pair : Theme.TRANSLUCENT_TEXT_PAIRS) {
            Color worstSurface = Theme.composite(pair[1], Color.WHITE);
            double ratio = Theme.contrast(pair[0], worstSurface);
            assertTrue(ratio >= Theme.MIN_TEXT_CONTRAST,
                    "contrast " + String.format("%.2f", ratio) + " for " + pair[0] + " on " + worstSurface);
        }
    }

    @Test
    void compositingFullyOpaqueOrFullyTransparentColorsIsExact() {
        assertEquals(Theme.NAVY, Theme.composite(Theme.NAVY, Color.WHITE));
        assertEquals(Color.WHITE, Theme.composite(new Color(10, 38, 77, 0), Color.WHITE));
    }

    @Test
    void contrastFormulaMatchesKnownReferenceValues() {
        assertEquals(21.0, Theme.contrast(Color.BLACK, Color.WHITE), 1e-9);
        assertEquals(1.0, Theme.contrast(Theme.SEA, Theme.SEA), 1e-9);
        assertEquals(Theme.contrast(Color.WHITE, Theme.SEA), Theme.contrast(Theme.SEA, Color.WHITE), 1e-12);
    }

    @Test
    void hoverAndPressedShadesNeverLoseContrastWithWhiteText() {
        for (Color base : new Color[] {Theme.SEA, Theme.GREEN, Theme.CORAL, Theme.GREY}) {
            assertTrue(Theme.contrast(Color.WHITE, Theme.darker(base)) >= Theme.contrast(Color.WHITE, base));
        }
    }
}
