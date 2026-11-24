package co.edu.autonoma.ancientseas.model;

import co.edu.autonoma.ancientseas.catalog.Species;

/** Creates the right creature class for a species, from its catalog behavior. */
public final class CreatureFactory {

    private CreatureFactory() {
    }

    /**
     * Creates an autonomous creature.
     *
     * @param id         unique id
     * @param species    its catalog data
     * @param position   initial position
     * @param sizeMeters initial body length
     * @param energy     initial food reserve
     * @return the creature
     */
    public static Creature create(long id, Species species, Vector2 position, double sizeMeters, double energy) {
        return switch (species.behavior()) {
            case HUNTER -> species.airBreather()
                    ? new MarineReptile(id, species, position, sizeMeters, energy)
                    : new Hunter(id, species, position, sizeMeters, energy);
            case SCHOOLING -> new SchoolingFish(id, species, position, sizeMeters, energy);
            case DRIFTER -> new Drifter(id, species, position, sizeMeters, energy);
            case SESSILE -> new SeafloorDweller(id, species, position, sizeMeters, energy);
        };
    }

    /**
     * Rebuilds an autonomous creature exactly as it was saved.
     *
     * @param species its catalog data
     * @param state   the saved state
     * @return the restored creature
     */
    public static Creature restore(Species species, CreatureState state) {
        Vector2 position = new Vector2(state.x(), state.y());
        Creature creature = create(state.id(), species, position, state.sizeMeters(), state.energy());
        creature.restore(state);
        return creature;
    }

    /**
     * Rebuilds the visitor's creature exactly as it was saved.
     *
     * @param species its catalog data
     * @param state   the saved state; it must be the player's
     * @return the restored player creature
     * @throws IllegalArgumentException if the state does not belong to a player
     */
    public static PlayerCreature restorePlayer(Species species, CreatureState state) {
        if (!state.player()) {
            throw new IllegalArgumentException("State " + state.id() + " is not the player's");
        }
        PlayerCreature player = new PlayerCreature(state.id(), species, new Vector2(state.x(), state.y()));
        player.restore(state);
        return player;
    }
}
