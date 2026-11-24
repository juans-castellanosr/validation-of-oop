package co.edu.autonoma.ancientseas.game;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * The best scores, kept sorted and limited to a fixed number of places. Iteration goes from best
 * to worst and cannot modify the board; only {@link #add} keeps it ordered.
 */
public final class Leaderboard extends AbstractCollection<ScoreEntry> {

    public static final Comparator<ScoreEntry> RANKING = Comparator.comparingInt(ScoreEntry::score).reversed()
            .thenComparing(ScoreEntry::playedAt);

    private final int capacity;
    private final List<ScoreEntry> entries = new ArrayList<>();

    /**
     * Creates an empty board.
     *
     * @param capacity how many entries it keeps; must be positive
     */
    public Leaderboard(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive: " + capacity);
        }
        this.capacity = capacity;
    }

    @Override
    public boolean add(ScoreEntry entry) {
        int index = Collections.binarySearch(entries, entry, RANKING);
        int position = index >= 0 ? index + 1 : -index - 1;
        if (position >= capacity) {
            return false;
        }
        entries.add(position, entry);
        if (entries.size() > capacity) {
            entries.remove(entries.size() - 1);
        }
        return true;
    }

    @Override
    public Iterator<ScoreEntry> iterator() {
        return Collections.unmodifiableList(entries).iterator();
    }

    @Override
    public int size() {
        return entries.size();
    }

    /**
     * Tells whether a score would enter the board.
     *
     * @param score the score
     * @return {@code true} if there is room or it beats the last place
     */
    public boolean qualifies(int score) {
        return entries.size() < capacity || score > entries.get(entries.size() - 1).score();
    }

    /**
     * Returns the number of places.
     *
     * @return the capacity
     */
    public int capacity() {
        return capacity;
    }
}
