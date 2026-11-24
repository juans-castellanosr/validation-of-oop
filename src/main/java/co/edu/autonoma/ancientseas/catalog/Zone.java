package co.edu.autonoma.ancientseas.catalog;

/** The three layers of the ocean column, as bands of the ocean height (0 is the surface, 1 the sea floor). */
public enum Zone implements DisplayNamed {
    SURFACE("Superficie", Range.of(0.0, 0.2)),
    MIDWATER("Aguas intermedias", Range.of(0.2, 0.82)),
    SEAFLOOR("Fondo marino", Range.of(0.82, 1.0));

    private final String displayName;
    private final Range<Double> band;

    Zone(String displayName, Range<Double> band) {
        this.displayName = displayName;
        this.band = band;
    }

    /**
     * Returns the vertical band of this zone as fractions of the ocean height.
     *
     * @return the band, 0 being the surface
     */
    public Range<Double> band() {
        return band;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    /**
     * Finds the zone that contains a relative depth.
     *
     * @param depthFraction depth as a fraction of the ocean height; values outside [0, 1] are clamped
     * @return the zone at that depth
     */
    public static Zone at(double depthFraction) {
        double clamped = Math.max(0.0, Math.min(1.0, depthFraction));
        if (clamped < SURFACE.band.max()) {
            return SURFACE;
        }
        return clamped < MIDWATER.band.max() ? MIDWATER : SEAFLOOR;
    }
}
