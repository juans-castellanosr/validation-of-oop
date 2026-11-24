package co.edu.autonoma.ancientseas.model;

import co.edu.autonoma.ancientseas.catalog.PreyKind;
import co.edu.autonoma.ancientseas.catalog.Species;

/** Something that can be eaten, whether a creature or a food particle. */
public interface Edible {

    /**
     * Returns where the food is.
     *
     * @return the position
     */
    Vector2 position();

    /**
     * Returns the collision radius used to decide contact.
     *
     * @return the radius in ocean units
     */
    double radius();

    /**
     * Tells whether this food is an animal of the given species.
     *
     * @param species the species to compare with
     * @return {@code true} only for a creature of that species
     */
    default boolean isOfSpecies(Species species) {
        return false;
    }

    /**
     * Returns what kind of food this is.
     *
     * @return the prey kind
     */
    PreyKind preyKind();

    /**
     * Returns the body size.
     *
     * @return the size in meters
     */
    double sizeMeters();

    /**
     * Returns how much food this is worth.
     *
     * @return nutrition in biomass units (meters of body length as a proxy)
     */
    double nutrition();

    /**
     * Tells whether it can still be eaten.
     *
     * @return {@code false} once it was consumed or died
     */
    boolean isAvailable();

    /**
     * Tries to be eaten.
     *
     * @return {@code true} if the eater actually got the food
     */
    boolean consume();
}
