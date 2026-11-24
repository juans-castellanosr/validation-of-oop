package co.edu.autonoma.ancientseas.catalog;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/** What a species eats, expressed as the kinds of food it accepts. */
public enum Diet implements DisplayNamed {
    CARNIVORE("Carnívoro", EnumSet.of(PreyKind.SMALL_PREY, PreyKind.FISH, PreyKind.CEPHALOPOD, PreyKind.SHELLFISH, PreyKind.REPTILE)),
    PISCIVORE("Come peces", EnumSet.of(PreyKind.SMALL_PREY, PreyKind.FISH, PreyKind.CEPHALOPOD)),
    OMNIVORE("Omnívoro", EnumSet.of(PreyKind.SMALL_PREY, PreyKind.FISH, PreyKind.CEPHALOPOD, PreyKind.SHELLFISH)),
    PLANKTIVORE("Come plancton", EnumSet.of(PreyKind.SMALL_PREY)),
    FILTER_FEEDER("Filtrador", EnumSet.of(PreyKind.SMALL_PREY)),
    DETRITIVORE("Come restos", EnumSet.of(PreyKind.SMALL_PREY));

    private final String displayName;
    private final Set<PreyKind> accepted;

    Diet(String displayName, EnumSet<PreyKind> accepted) {
        this.displayName = displayName;
        this.accepted = Collections.unmodifiableSet(accepted);
    }

    /**
     * Tells whether this diet includes a kind of food.
     *
     * @param kind the food kind
     * @return {@code true} if a species with this diet can eat it
     */
    public boolean accepts(PreyKind kind) {
        return accepted.contains(kind);
    }

    @Override
    public String displayName() {
        return displayName;
    }
}
