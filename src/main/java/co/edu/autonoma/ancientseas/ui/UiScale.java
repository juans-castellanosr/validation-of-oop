package co.edu.autonoma.ancientseas.ui;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/** Enlarges every Swing font and drawn size by the interface size the visitor chose. */
public final class UiScale {

    private static double factor = 1.0;

    private UiScale() {
    }

    /**
     * Applies a scale factor; call once, before any window is built.
     *
     * @param newFactor 1.0 to 2.0
     * @throws IllegalArgumentException if the factor is outside that range
     */
    public static void apply(double newFactor) {
        if (!(newFactor >= 1.0 && newFactor <= 2.0)) {
            throw new IllegalArgumentException("Interface scale must be between 1 and 2: " + newFactor);
        }
        factor = newFactor;
        if (newFactor == 1.0) {
            return;
        }
        List<Object> keys = new ArrayList<>(UIManager.getLookAndFeelDefaults().keySet());
        for (Object key : keys) {
            if (UIManager.get(key) instanceof Font font) {
                UIManager.put(key, new FontUIResource(font.deriveFont(font.getSize2D() * (float) newFactor)));
            }
        }
    }

    static double factor() {
        return factor;
    }
}
