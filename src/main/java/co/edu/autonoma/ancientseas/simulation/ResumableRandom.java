package co.edu.autonoma.ancientseas.simulation;

import java.util.Random;

/**
 * A {@link Random} whose position in its sequence can be read and set again, so a saved game
 * continues with the same numbers.
 */
public final class ResumableRandom extends Random {

    private static final long serialVersionUID = 1L;
    private static final long MULTIPLIER = 0x5DEECE66DL;
    private static final long ADDEND = 0xBL;
    private static final long MASK = (1L << 48) - 1;

    private long state;

    private ResumableRandom(long state) {
        super(0L);
        this.state = state & MASK;
    }

    /**
     * Creates a generator that produces the same numbers as {@code new Random(seed)}.
     *
     * @param seed the seed
     * @return the generator
     */
    public static ResumableRandom seeded(long seed) {
        return new ResumableRandom(seed ^ MULTIPLIER);
    }

    /**
     * Returns the current position in the sequence, to save it.
     *
     * @return the 48-bit state
     */
    public long state() {
        return state;
    }

    /**
     * Moves the generator to a state previously read with {@link #state()}.
     *
     * @param savedState the saved state; only its lower 48 bits are used
     */
    public void resumeAt(long savedState) {
        this.state = savedState & MASK;
    }

    @Override
    protected int next(int bits) {
        state = (state * MULTIPLIER + ADDEND) & MASK;
        return (int) (state >>> (48 - bits));
    }
}
