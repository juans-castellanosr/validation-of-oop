package co.edu.autonoma.ancientseas.catalog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Which species of a catalog lived at the same time, derived from their age ranges: the moment
 * shared by the most species defines the {@linkplain #together() together} group, and the rest
 * are {@linkplain #apart() apart}.
 */
public final class Coexistence {

    private final Range<Double> sharedPeriod;
    private final List<Species> together;
    private final List<Species> apart;

    private Coexistence(Range<Double> sharedPeriod, List<Species> together, List<Species> apart) {
        this.sharedPeriod = sharedPeriod;
        this.together = List.copyOf(together);
        this.apart = List.copyOf(apart);
    }

    /**
     * Analyzes a group of species, picking the most crowded moment among the range bounds
     * (ties go to the most recent one).
     *
     * @param species the species, for example {@code catalog.all()}
     * @return the analysis; with no species, nothing is together and nothing is apart
     */
    public static Coexistence of(Collection<Species> species) {
        List<Double> moments = new ArrayList<>();
        for (Species s : species) {
            moments.add(s.ageMa().min());
            moments.add(s.ageMa().max());
        }
        moments.sort(Comparator.naturalOrder());
        double best = Double.NaN;
        long bestCount = 0;
        for (double moment : moments) {
            long count = species.stream().filter(s -> s.livedAt(moment)).count();
            if (count > bestCount) {
                best = moment;
                bestCount = count;
            }
        }
        if (bestCount == 0) {
            return new Coexistence(null, List.of(), List.of());
        }
        double moment = best;
        List<Species> together = species.stream().filter(s -> s.livedAt(moment)).toList();
        List<Species> apart = species.stream().filter(s -> !s.livedAt(moment)).toList();
        double youngest = together.stream().mapToDouble(s -> s.ageMa().min()).max().orElseThrow();
        double oldest = together.stream().mapToDouble(s -> s.ageMa().max()).min().orElseThrow();
        return new Coexistence(Range.of(youngest, oldest), together, apart);
    }

    /**
     * Returns the period of the past in which all the species that are together were alive.
     *
     * @return the shared period in millions of years before present, or empty if there are no species
     */
    public Optional<Range<Double>> sharedPeriod() {
        return Optional.ofNullable(sharedPeriod);
    }

    /**
     * Returns the species alive during the {@linkplain #sharedPeriod() shared period}.
     *
     * @return the species that coexisted, in catalog order
     */
    public List<Species> together() {
        return together;
    }

    /**
     * Returns the species that never lived during the shared period.
     *
     * @return the species from another time, in catalog order; empty when no periods are mixed
     */
    public List<Species> apart() {
        return apart;
    }

    /**
     * Tells whether a species, for example one about to be added, lived during the shared period.
     *
     * @param candidate the species to check
     * @return {@code true} if it fits, or if there is no shared period yet
     */
    public boolean fits(Species candidate) {
        return sharedPeriod == null || candidate.livedAt(sharedPeriod);
    }
}
