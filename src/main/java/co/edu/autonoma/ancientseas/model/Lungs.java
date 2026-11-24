package co.edu.autonoma.ancientseas.model;

/** Oxygen store shared by air-breathing animals that are not related by inheritance. */
public final class Lungs {

    private final double secondsOfAir;
    private double oxygen = 1.0;

    /**
     * Creates full lungs.
     *
     * @param secondsOfAir how long a full breath lasts underwater; must be positive
     */
    public Lungs(double secondsOfAir) {
        if (!(secondsOfAir > 0)) {
            throw new IllegalArgumentException("secondsOfAir must be positive: " + secondsOfAir);
        }
        this.secondsOfAir = secondsOfAir;
    }

    /**
     * Advances breathing: refills quickly at the surface and empties underwater.
     *
     * @param atSurface whether the animal is in the surface layer
     * @param dt        elapsed simulation seconds
     */
    public void breathe(boolean atSurface, double dt) {
        double delta = atSurface ? 4.0 * dt / secondsOfAir : -dt / secondsOfAir;
        oxygen = Math.max(0.0, Math.min(1.0, oxygen + delta));
    }

    /**
     * Returns the oxygen level.
     *
     * @return a value in [0, 1]
     */
    public double oxygen() {
        return oxygen;
    }

    /**
     * Restores a saved oxygen level.
     *
     * @param level the level, clamped to [0, 1]
     */
    public void restore(double level) {
        oxygen = Math.max(0.0, Math.min(1.0, level));
    }

    /**
     * Tells whether the animal has run out of air.
     *
     * @return {@code true} when oxygen is zero
     */
    public boolean isEmpty() {
        return oxygen <= 0.0;
    }
}
