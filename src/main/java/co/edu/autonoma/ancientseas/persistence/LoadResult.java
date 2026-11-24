package co.edu.autonoma.ancientseas.persistence;

import java.util.List;
import java.util.Objects;

/**
 * A loaded value plus the problems found while recovering it, so nothing is dropped silently.
 *
 * @param value    the loaded value; never {@code null}
 * @param problems human-readable problems found while loading, in Spanish; copied on creation
 * @param <T>      type of the loaded value
 */
public record LoadResult<T>(T value, List<String> problems) {

    /** Validates the value and copies the problems. */
    public LoadResult {
        Objects.requireNonNull(value, "value");
        problems = List.copyOf(problems);
    }

    /**
     * Creates the result of a load that found nothing wrong.
     *
     * @param value the loaded value
     * @param <T>   type of the value
     * @return a result without problems
     */
    public static <T> LoadResult<T> clean(T value) {
        return new LoadResult<>(value, List.of());
    }

    /**
     * Creates the result of a load that recovered with problems.
     *
     * @param value    the value built from what could be used
     * @param problems what was skipped or replaced, in Spanish
     * @param <T>      type of the value
     * @return a result carrying the problems
     */
    public static <T> LoadResult<T> withProblems(T value, List<String> problems) {
        return new LoadResult<>(value, problems);
    }

    /** {@return {@code true} if there is at least one problem} */
    public boolean hasProblems() {
        return !problems.isEmpty();
    }
}
