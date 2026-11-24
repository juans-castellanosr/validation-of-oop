package co.edu.autonoma.ancientseas.catalog;

import java.util.Objects;

/**
 * An immutable closed interval {@code [min, max]} of comparable values.
 *
 * @param <T> the bound type; it must be comparable with itself
 */
public final class Range<T extends Comparable<? super T>> {

    private final T min;
    private final T max;

    private Range(T min, T max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Creates a range.
     *
     * @param <T> the bound type
     * @param min lower bound, inclusive
     * @param max upper bound, inclusive
     * @return the range
     * @throws IllegalArgumentException if {@code min} is greater than {@code max}
     */
    public static <T extends Comparable<? super T>> Range<T> of(T min, T max) {
        Objects.requireNonNull(min, "min");
        Objects.requireNonNull(max, "max");
        if (min.compareTo(max) > 0) {
            throw new IllegalArgumentException("min " + min + " is greater than max " + max);
        }
        return new Range<>(min, max);
    }

    /**
     * Returns the lower bound.
     *
     * @return the inclusive minimum
     */
    public T min() {
        return min;
    }

    /**
     * Returns the upper bound.
     *
     * @return the inclusive maximum
     */
    public T max() {
        return max;
    }

    /**
     * Tells whether a value lies inside the range, bounds included.
     *
     * @param value the value to test
     * @return {@code true} if {@code min <= value <= max}
     */
    public boolean contains(T value) {
        return min.compareTo(value) <= 0 && value.compareTo(max) <= 0;
    }

    /**
     * Brings a value inside the range.
     *
     * @param value the value to clamp
     * @return {@code value} itself if inside, otherwise the nearest bound
     */
    public T clamp(T value) {
        if (value.compareTo(min) < 0) {
            return min;
        }
        return value.compareTo(max) > 0 ? max : value;
    }

    /**
     * Tells whether two ranges share at least one value.
     *
     * @param other the other range
     * @return {@code true} if they overlap, touching bounds included
     */
    public boolean overlaps(Range<T> other) {
        return min.compareTo(other.max) <= 0 && other.min.compareTo(max) <= 0;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Range<?> r && min.equals(r.min) && max.equals(r.max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }

    @Override
    public String toString() {
        return "[" + min + ", " + max + "]";
    }
}
