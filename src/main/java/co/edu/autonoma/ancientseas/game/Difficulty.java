package co.edu.autonoma.ancientseas.game;

import co.edu.autonoma.ancientseas.catalog.DisplayNamed;

/** How hard the ocean is for the visitor. */
public enum Difficulty implements DisplayNamed {
    EASY("Fácil", 0.8, 0.6, 1.0),
    NORMAL("Normal", 1.0, 1.0, 1.5),
    HARD("Difícil", 1.2, 1.5, 2.0);

    private final String displayName;
    private final double hunterSpeedFactor;
    private final double hunterCountFactor;
    private final double scoreMultiplier;

    Difficulty(String displayName, double hunterSpeedFactor, double hunterCountFactor, double scoreMultiplier) {
        this.displayName = displayName;
        this.hunterSpeedFactor = hunterSpeedFactor;
        this.hunterCountFactor = hunterCountFactor;
        this.scoreMultiplier = scoreMultiplier;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    /**
     * Returns the hunters' speed multiplier.
     *
     * @return multiplier for hunters' sprint speed
     */
    public double hunterSpeedFactor() {
        return hunterSpeedFactor;
    }

    /**
     * Returns the hunter count multiplier.
     *
     * @return multiplier for the number of hunters
     */
    public double hunterCountFactor() {
        return hunterCountFactor;
    }

    /**
     * Returns the score multiplier.
     *
     * @return multiplier rewarding harder games with more points
     */
    public double scoreMultiplier() {
        return scoreMultiplier;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
