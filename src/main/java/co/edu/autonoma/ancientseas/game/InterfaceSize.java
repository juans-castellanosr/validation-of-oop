package co.edu.autonoma.ancientseas.game;

import co.edu.autonoma.ancientseas.catalog.DisplayNamed;

/** How large the interface is drawn. */
public enum InterfaceSize implements DisplayNamed {
    NORMAL("Normal", 1.0),
    LARGE("Grande (125 %)", 1.25),
    EXTRA_LARGE("Muy grande (150 %)", 1.5);

    private final String displayName;
    private final double scale;

    InterfaceSize(String displayName, double scale) {
        this.displayName = displayName;
        this.scale = scale;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    /**
     * Returns the scale factor applied to the interface.
     *
     * @return 1 for normal size
     */
    public double scale() {
        return scale;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
