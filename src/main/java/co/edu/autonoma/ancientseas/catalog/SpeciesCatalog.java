package co.edu.autonoma.ancientseas.catalog;

import co.edu.autonoma.ancientseas.error.DuplicateSpeciesException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * The species catalog, keeping insertion order. Not thread-safe: it is never edited while the
 * simulation runs.
 */
public final class SpeciesCatalog {

    private final Map<String, Species> byId = new LinkedHashMap<>();

    /** Creates an empty catalog. */
    public SpeciesCatalog() {
    }

    /**
     * Creates an independent copy that keeps the original order; species are shared because they are immutable.
     *
     * @param original the catalog to copy
     */
    public SpeciesCatalog(SpeciesCatalog original) {
        byId.putAll(original.byId);
    }

    /**
     * Adds a new species.
     *
     * @param species the species to add
     * @throws DuplicateSpeciesException if a species with the same id exists
     */
    public void add(Species species) throws DuplicateSpeciesException {
        if (byId.containsKey(species.id())) {
            throw new DuplicateSpeciesException(species.id());
        }
        byId.put(species.id(), species);
    }

    /**
     * Replaces an existing species with an edited version, keeping its position in the catalog.
     *
     * @param originalId id of the species being edited
     * @param edited     the new data; its id may differ from {@code originalId}
     * @throws NoSuchElementException     if {@code originalId} is not in the catalog
     * @throws DuplicateSpeciesException if the id was changed to one that already exists
     */
    public void replace(String originalId, Species edited) throws DuplicateSpeciesException {
        if (!byId.containsKey(originalId)) {
            throw new NoSuchElementException("No species with id " + originalId);
        }
        if (!originalId.equals(edited.id()) && byId.containsKey(edited.id())) {
            throw new DuplicateSpeciesException(edited.id());
        }
        Map<String, Species> reordered = new LinkedHashMap<>();
        byId.forEach((id, species) -> {
            if (id.equals(originalId)) {
                reordered.put(edited.id(), edited);
            } else {
                reordered.put(id, species);
            }
        });
        byId.clear();
        byId.putAll(reordered);
    }

    /**
     * Removes a species.
     *
     * @param id the species id
     * @return {@code true} if it was present
     */
    public boolean remove(String id) {
        return byId.remove(id) != null;
    }

    /**
     * Looks a species up by id.
     *
     * @param id the species id
     * @return the species, or empty if unknown
     */
    public Optional<Species> find(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    /**
     * Returns every species in catalog order.
     *
     * @return an unmodifiable snapshot
     */
    public List<Species> all() {
        return List.copyOf(byId.values());
    }

    /**
     * Returns the species that satisfy a condition, in catalog order.
     *
     * @param condition the filter, e.g. {@code s -> s.zone() == Zone.SURFACE}
     * @return an unmodifiable list of matches
     */
    public List<Species> filter(Predicate<? super Species> condition) {
        return byId.values().stream().filter(condition).toList();
    }

    /**
     * Returns every species sorted by a criterion.
     *
     * @param order the comparator, e.g. {@code Comparator.comparing(Species::commonName)}
     * @return an unmodifiable sorted list
     */
    public List<Species> sorted(Comparator<? super Species> order) {
        List<Species> copy = new ArrayList<>(byId.values());
        copy.sort(order);
        return Collections.unmodifiableList(copy);
    }

    /**
     * Returns the number of species.
     *
     * @return the catalog size
     */
    public int size() {
        return byId.size();
    }
}
