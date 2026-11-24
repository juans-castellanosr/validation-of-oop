package co.edu.autonoma.ancientseas.model;

/**
 * Immutable 2D vector in ocean coordinates (x grows right, y grows down to the floor).
 *
 * @param x horizontal component
 * @param y vertical component
 */
public record Vector2(double x, double y) {

    public static final Vector2 ZERO = new Vector2(0, 0);

    /**
     * Adds another vector.
     *
     * @param o the other vector
     * @return {@code this + o}
     */
    public Vector2 plus(Vector2 o) {
        return new Vector2(x + o.x, y + o.y);
    }

    /**
     * Subtracts another vector.
     *
     * @param o the other vector
     * @return {@code this - o}
     */
    public Vector2 minus(Vector2 o) {
        return new Vector2(x - o.x, y - o.y);
    }

    /**
     * Scales the vector.
     *
     * @param factor the scale factor
     * @return {@code this * factor}
     */
    public Vector2 times(double factor) {
        return new Vector2(x * factor, y * factor);
    }

    /**
     * Returns the Euclidean length.
     *
     * @return the length
     */
    public double length() {
        return Math.hypot(x, y);
    }

    /**
     * Returns the distance to another point.
     *
     * @param o the other point
     * @return the Euclidean distance
     */
    public double distanceTo(Vector2 o) {
        return Math.hypot(x - o.x, y - o.y);
    }

    /**
     * Returns a unit vector with the same direction.
     *
     * @return the normalised vector, or {@link #ZERO} for a zero-length vector (no direction to keep)
     */
    public Vector2 normalized() {
        double len = length();
        return len < 1e-9 ? ZERO : new Vector2(x / len, y / len);
    }

    /**
     * Shortens the vector if it is longer than a maximum.
     *
     * @param max the maximum length
     * @return this vector, or one with the same direction and length {@code max}
     */
    public Vector2 limit(double max) {
        double len = length();
        return len > max ? times(max / len) : this;
    }

    /**
     * Interpolates linearly towards another vector.
     *
     * @param target the vector reached when {@code t == 1}
     * @param t      the interpolation factor, clamped to [0, 1]
     * @return the interpolated vector
     */
    public Vector2 lerp(Vector2 target, double t) {
        double k = Math.max(0, Math.min(1, t));
        return new Vector2(x + (target.x - x) * k, y + (target.y - y) * k);
    }
}
