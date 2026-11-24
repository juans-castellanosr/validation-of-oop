package co.edu.autonoma.ancientseas.model;

/** Life stages of the visitor's creature; reaching {@link #ADULT} wins the game. */
public enum GrowthStage {
    HATCHLING("Cría", 0.15, 0),
    JUVENILE("Juvenil", 0.4, 35),
    SUBADULT("Subadulto", 0.7, 100),
    ADULT("Adulto", 1.0, 200);

    private final String displayName;
    private final double lengthFraction;
    private final double pointsRequired;

    GrowthStage(String displayName, double lengthFraction, double pointsRequired) {
        this.displayName = displayName;
        this.lengthFraction = lengthFraction;
        this.pointsRequired = pointsRequired;
    }

    /**
     * Finds the stage reached with a number of food points.
     *
     * @param points accumulated food points
     * @return the highest stage whose requirement is met
     */
    public static GrowthStage forPoints(double points) {
        GrowthStage reached = HATCHLING;
        for (GrowthStage stage : values()) {
            if (points >= stage.pointsRequired) {
                reached = stage;
            }
        }
        return reached;
    }

    /**
     * Returns the body length at a number of food points, growing smoothly between stages.
     *
     * @param points            accumulated food points
     * @param adultLengthMeters the species' adult length
     * @return the current length in meters
     */
    public static double lengthAt(double points, double adultLengthMeters) {
        GrowthStage current = forPoints(points);
        if (current == ADULT) {
            return adultLengthMeters;
        }
        GrowthStage next = values()[current.ordinal() + 1];
        double progress = (points - current.pointsRequired) / (next.pointsRequired - current.pointsRequired);
        double fraction = current.lengthFraction + (next.lengthFraction - current.lengthFraction) * progress;
        return adultLengthMeters * fraction;
    }

    /**
     * Returns the fraction of the way from hatchling to adult, for the growth bar.
     *
     * @param points accumulated food points
     * @return a value in [0, 1]
     */
    public static double overallProgress(double points) {
        return Math.max(0, Math.min(1, points / ADULT.pointsRequired));
    }

    /**
     * Returns the Spanish name shown to visitors.
     *
     * @return the display name
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Returns the food points needed to reach this stage.
     *
     * @return the points
     */
    public double pointsRequired() {
        return pointsRequired;
    }
}
