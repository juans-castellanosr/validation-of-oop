package co.edu.autonoma.ancientseas.game;

import co.edu.autonoma.ancientseas.catalog.DisplayNamed;

/** How often environmental events happen. */
public enum EventFrequency implements DisplayNamed {
    LOW("Pocos", 0.6),
    NORMAL("Normal", 1.0),
    HIGH("Muchos", 1.7);

    private final String displayName;
    private final double factor;

    EventFrequency(String displayName, double factor) {
        this.displayName = displayName;
        this.factor = factor;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    /**
     * Returns the event frequency multiplier.
     *
     * @return the multiplier applied by the event scheduler
     */
    public double factor() {
        return factor;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
