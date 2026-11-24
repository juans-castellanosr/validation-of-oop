package co.edu.autonoma.ancientseas.game;

import co.edu.autonoma.ancientseas.model.CreatureState;
import co.edu.autonoma.ancientseas.model.FoodState;
import co.edu.autonoma.ancientseas.simulation.EventState;
import java.util.List;
import java.util.Map;

/**
 * Everything needed to continue a game exactly where it was left.
 *
 * @param settings                the settings the game was created with
 * @param elapsedSeconds          simulation time played
 * @param nextId                  next entity id to hand out
 * @param creatures               every living creature, the player included
 * @param food                    every food particle
 * @param events                  active environmental events
 * @param secondsUntilNextEvent   countdown to the next event
 * @param populationCaps          maximum individuals per species id
 * @param planktonDue             fraction of the next plankton particle already grown
 * @param randomState             position of the game's random sequence
 * @param speciesDefinitions      per species id, its {@code Species#gameDefinition()} when saved
 */
public record GameMemento(GameSettings settings, double elapsedSeconds, long nextId, List<CreatureState> creatures,
        List<FoodState> food, List<EventState> events, double secondsUntilNextEvent,
        Map<String, Integer> populationCaps, double planktonDue, long randomState,
        Map<String, String> speciesDefinitions) {

    /** Copies the collections so the memento cannot change after it is created. */
    public GameMemento {
        creatures = List.copyOf(creatures);
        food = List.copyOf(food);
        events = List.copyOf(events);
        populationCaps = Map.copyOf(populationCaps);
        speciesDefinitions = Map.copyOf(speciesDefinitions);
    }

    /**
     * Creates a memento without random state or species definitions, as in older saves.
     *
     * @param settings              settings of the game
     * @param elapsedSeconds        simulation time played
     * @param nextId                next free entity id
     * @param creatures             every living creature
     * @param food                  every food particle
     * @param events                active events
     * @param secondsUntilNextEvent time until the next event
     * @param populationCaps        maximum individuals per species id
     * @param planktonDue           plankton waiting to appear
     */
    public GameMemento(GameSettings settings, double elapsedSeconds, long nextId, List<CreatureState> creatures,
            List<FoodState> food, List<EventState> events, double secondsUntilNextEvent,
            Map<String, Integer> populationCaps, double planktonDue) {
        this(settings, elapsedSeconds, nextId, creatures, food, events, secondsUntilNextEvent, populationCaps,
                planktonDue, 0L, Map.of());
    }
}
