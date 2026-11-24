package co.edu.autonoma.ancientseas.game;

import co.edu.autonoma.ancientseas.catalog.Behavior;
import co.edu.autonoma.ancientseas.catalog.Species;
import co.edu.autonoma.ancientseas.catalog.SpeciesCatalog;
import co.edu.autonoma.ancientseas.error.HabitatViolationException;
import co.edu.autonoma.ancientseas.error.CorruptSaveException;
import co.edu.autonoma.ancientseas.error.IncompatibleSaveException;
import co.edu.autonoma.ancientseas.error.SaveGameException;
import co.edu.autonoma.ancientseas.model.Creature;
import co.edu.autonoma.ancientseas.model.CreatureFactory;
import co.edu.autonoma.ancientseas.model.CreatureState;
import co.edu.autonoma.ancientseas.model.FoodState;
import co.edu.autonoma.ancientseas.model.GrowthStage;
import co.edu.autonoma.ancientseas.model.PlayerCreature;
import co.edu.autonoma.ancientseas.model.Vector2;
import co.edu.autonoma.ancientseas.simulation.EventState;
import co.edu.autonoma.ancientseas.simulation.EnvironmentEvent;
import co.edu.autonoma.ancientseas.simulation.EventType;
import co.edu.autonoma.ancientseas.simulation.Ocean;
import co.edu.autonoma.ancientseas.simulation.ResumableRandom;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/** One game: the ocean, the visitor's creature and the rules that decide victory and defeat. */
public final class GameSession {

    public static final String PLAYER_SPECIES_ID = "archelon-ischyros";
    private static final double SAFE_SPAWN_DISTANCE = 380;
    private static final Map<Behavior, Double> POPULATION_WEIGHT = new EnumMap<>(Map.of(
            Behavior.HUNTER, 0.5, Behavior.SCHOOLING, 4.0, Behavior.DRIFTER, 3.0, Behavior.SESSILE, 1.5));

    private final Ocean ocean;
    private final PlayerCreature player;
    private final GameSettings settings;
    private GameStatus status = GameStatus.READY;
    private double elapsedSeconds;
    private GrowthStage lastStage;
    private double lastHealth;

    private GameSession(Ocean ocean, PlayerCreature player, GameSettings settings, double elapsedSeconds) {
        this.ocean = ocean;
        this.player = player;
        this.settings = settings;
        this.elapsedSeconds = elapsedSeconds;
        this.lastStage = player.stage();
        this.lastHealth = player.healthLevel();
    }

    /**
     * Creates a new game populated from the catalog.
     *
     * @param catalog  the species catalog; must contain {@link #PLAYER_SPECIES_ID}
     * @param settings the visitor's settings
     * @param source   where the game's random sequence starts; pass a seeded one for reproducible games
     * @return a game ready to start
     * @throws IllegalStateException if the catalog has no player species
     */
    public static GameSession newGame(SpeciesCatalog catalog, GameSettings settings, Random source) {
        ResumableRandom random = ResumableRandom.seeded(source.nextLong());
        Species playerSpecies = catalog.find(PLAYER_SPECIES_ID)
                .orElseThrow(() -> new IllegalStateException("Catalog lacks the player species " + PLAYER_SPECIES_ID));
        List<Species> others = catalog.filter(s -> !s.equals(playerSpecies));
        Map<String, Integer> initial = initialPopulation(others, settings);
        Map<String, Integer> caps = new HashMap<>();
        initial.forEach((id, count) -> caps.put(id, Math.max(2, count * 2)));
        List<Species> hunters = arrivingHunters(catalog);

        Ocean ocean = new Ocean(Ocean.DEFAULT_WIDTH, Ocean.DEFAULT_HEIGHT, random,
                settings.difficulty().hunterSpeedFactor(), settings.eventFrequency().factor(), caps, catalog.all(),
                hunters);
        Vector2 start = new Vector2(ocean.width() / 2, ocean.height() * 0.1);
        PlayerCreature player = new PlayerCreature(ocean.nextId(), playerSpecies, start);
        try {
            ocean.add(player);
        } catch (HabitatViolationException e) {
            throw new IllegalStateException("The fixed start point must be inside the ocean", e);
        }
        for (Species species : others) {
            boolean dangerous = species.behavior() == Behavior.HUNTER;
            for (int i = 0; i < initial.get(species.id()); i++) {
                double size = species.lengthMeters() * (0.55 + 0.45 * random.nextDouble());
                double energy = 0.6 + 0.3 * random.nextDouble();
                ocean.spawnInZone(species, size, energy,
                        at -> !dangerous || at.distanceTo(start) > SAFE_SPAWN_DISTANCE);
            }
        }
        return new GameSession(ocean, player, settings, 0);
    }

    private static List<Species> arrivingHunters(SpeciesCatalog catalog) {
        return catalog.filter(s -> s.behavior() == Behavior.HUNTER && !s.id().equals(PLAYER_SPECIES_ID));
    }

    private static Map<String, Integer> initialPopulation(List<Species> species, GameSettings settings) {
        double totalWeight = species.stream().mapToDouble(s -> weight(s, settings)).sum();
        Map<String, Integer> counts = new HashMap<>();
        for (Species s : species) {
            int count = (int) Math.round(settings.creatureCount() * weight(s, settings) / totalWeight);
            counts.put(s.id(), Math.max(1, count));
        }
        return counts;
    }

    private static double weight(Species s, GameSettings settings) {
        double base = POPULATION_WEIGHT.get(s.behavior());
        return s.behavior() == Behavior.HUNTER ? base * settings.difficulty().hunterCountFactor() : base;
    }

    private static void checkSpeciesUnchanged(GameMemento memento, SpeciesCatalog catalog)
            throws IncompatibleSaveException {
        for (Map.Entry<String, String> saved : memento.speciesDefinitions().entrySet()) {
            Optional<Species> now = catalog.find(saved.getKey());
            if (now.isPresent() && !now.get().gameDefinition().equals(saved.getValue())) {
                throw new IncompatibleSaveException("Species " + saved.getKey() + " changed since the save: "
                        + saved.getValue() + " -> " + now.get().gameDefinition(),
                        "Esta partida se guardó antes de un cambio en el catálogo de especies y no se puede continuar.");
            }
        }
    }

    private static void checkValues(GameMemento memento, SpeciesCatalog catalog) throws SaveGameException {
        if (memento.elapsedSeconds() < 0 || memento.secondsUntilNextEvent() < 0 || memento.planktonDue() < 0
                || memento.planktonDue() >= 1) {
            throw new CorruptSaveException("Impossible clock values in save", null);
        }
        for (FoodState particle : memento.food()) {
            if (particle.secondsLeft() < 0) {
                throw new CorruptSaveException("Food particle " + particle.id() + " is impossible", null);
            }
        }
        for (Map.Entry<String, Integer> cap : memento.populationCaps().entrySet()) {
            if (cap.getValue() < 0) {
                throw new CorruptSaveException("Negative population cap for " + cap.getKey(), null);
            }
            if (catalog.find(cap.getKey()).isEmpty()) {
                throw new IncompatibleSaveException("Population cap for species not in catalog: " + cap.getKey());
            }
        }
        Set<EventType> seen = EnumSet.noneOf(EventType.class);
        for (EventState event : memento.events()) {
            if (!seen.add(event.type())) {
                throw new CorruptSaveException("Event " + event.type() + " appears twice", null);
            }
            if (event.elapsedSeconds() < 0 || !(event.durationSeconds() > 0)
                    || event.elapsedSeconds() > event.durationSeconds()) {
                throw new CorruptSaveException("Event " + event.type() + " has impossible times", null);
            }
            if (event.type() == EventType.PREDATOR_ARRIVAL && catalog.find(event.speciesId()).isEmpty()) {
                throw new IncompatibleSaveException("Arriving predator not in catalog: " + event.speciesId());
            }
        }
    }

    /**
     * Rebuilds a saved game, paused, validating that the values make a possible game.
     *
     * @param memento the saved game
     * @param catalog the current catalog
     * @return the restored game
     * @throws IncompatibleSaveException if the save refers to species no longer in the catalog, or to a species
     *                                   whose game behavior changed since the save
     * @throws CorruptSaveException      if the data describes an impossible game: no player or two,
     *                                   a player of the wrong species, a finished game, repeated
     *                                   ids, invalid values or creatures outside their habitat
     */
    public static GameSession restore(GameMemento memento, SpeciesCatalog catalog) throws SaveGameException {
        GameSettings settings = memento.settings();
        checkSpeciesUnchanged(memento, catalog);
        checkValues(memento, catalog);
        List<Species> hunters = arrivingHunters(catalog);
        Ocean ocean = new Ocean(Ocean.DEFAULT_WIDTH, Ocean.DEFAULT_HEIGHT, ResumableRandom.seeded(memento.randomState()),
                settings.difficulty().hunterSpeedFactor(), settings.eventFrequency().factor(),
                memento.populationCaps(), catalog.all(), hunters);
        PlayerCreature player = null;
        Set<Long> ids = new HashSet<>();
        for (CreatureState state : memento.creatures()) {
            Species species = catalog.find(state.speciesId()).orElseThrow(() -> new IncompatibleSaveException(
                    "Saved species not in catalog: " + state.speciesId()));
            if (!ids.add(state.id())) {
                throw new CorruptSaveException("Repeated creature id " + state.id(), null);
            }
            Creature creature;
            try {
                if (state.player()) {
                    if (player != null) {
                        throw new CorruptSaveException("Save has more than one player creature", null);
                    }
                    if (!species.id().equals(PLAYER_SPECIES_ID)) {
                        throw new CorruptSaveException("Player creature has species " + species.id(), null);
                    }
                    player = CreatureFactory.restorePlayer(species, state);
                    creature = player;
                } else {
                    creature = CreatureFactory.restore(species, state);
                }
                ocean.add(creature);
            } catch (IllegalArgumentException e) {
                throw new CorruptSaveException("Invalid values for creature " + state.id() + ": " + e.getMessage(), e);
            } catch (HabitatViolationException e) {
                throw new CorruptSaveException("Saved creature " + state.id() + " is outside its habitat: "
                        + e.getMessage(), e);
            }
        }
        if (player == null || !player.isAlive()) {
            throw new CorruptSaveException("Save has no living player creature", null);
        }
        if (player.stage() == GrowthStage.ADULT) {
            throw new CorruptSaveException("Save holds a game that was already won", null);
        }
        for (FoodState particle : memento.food()) {
            if (!ids.add(particle.id())) {
                throw new CorruptSaveException("Repeated entity id " + particle.id(), null);
            }
        }
        if (ids.stream().anyMatch(id -> id >= memento.nextId())) {
            throw new CorruptSaveException("Next id " + memento.nextId() + " is already in use", null);
        }
        memento.food().forEach(ocean::restoreFood);
        ocean.events().restore(memento.events(), memento.secondsUntilNextEvent(), catalog::find);
        ocean.restoreCounters(memento.nextId(), memento.planktonDue());
        ocean.restoreRandomState(memento.randomState());
        GameSession session = new GameSession(ocean, player, settings, memento.elapsedSeconds());
        session.status = GameStatus.PAUSED;
        return session;
    }

    /**
     * Captures the game for saving, without changing it. Call it only from the thread that runs the game.
     *
     * @return the memento
     * @throws IllegalStateException if the game is over, because it could never be resumed
     */
    public GameMemento toMemento() {
        if (status.isFinal()) {
            throw new IllegalStateException("A finished game cannot be saved: " + status);
        }
        List<CreatureState> creatures = ocean.creatures().stream().map(Creature::toState).toList();
        List<FoodState> food = ocean.food().stream()
                .map(f -> new FoodState(f.id(), f.position().x(), f.position().y(), f.secondsLeft())).toList();
        List<EventState> events = ocean.events().active().stream().map(EnvironmentEvent::toState).toList();
        Map<String, String> definitions = new java.util.TreeMap<>();
        ocean.creatures().forEach(c -> definitions.put(c.species().id(), c.species().gameDefinition()));
        return new GameMemento(settings, elapsedSeconds, ocean.nextId(), creatures, food, events,
                ocean.events().secondsUntilNext(), ocean.populationCaps(), ocean.planktonDue(), ocean.randomState(),
                definitions);
    }

    /**
     * Starts a game that is ready.
     *
     * @throws IllegalStateException if it is not {@link GameStatus#READY}
     */
    public void start() {
        transition(GameStatus.READY, GameStatus.RUNNING);
    }

    /**
     * Pauses a running game.
     *
     * @throws IllegalStateException if it is not running
     */
    public void pause() {
        transition(GameStatus.RUNNING, GameStatus.PAUSED);
    }

    /**
     * Resumes a paused game.
     *
     * @throws IllegalStateException if it is not paused
     */
    public void resume() {
        transition(GameStatus.PAUSED, GameStatus.RUNNING);
    }

    /** Ends the game early; ending a game that is already over does nothing. */
    public void end() {
        if (!status.isFinal()) {
            status = GameStatus.ENDED;
        }
    }

    private void transition(GameStatus from, GameStatus to) {
        if (status != from) {
            throw new IllegalStateException("Cannot go from " + status + " to " + to);
        }
        status = to;
    }

    /**
     * Advances the game one step if it is running, then applies the rules.
     *
     * @param dt elapsed simulation seconds
     * @return messages for the visitor produced in this step (events, growth, bites)
     */
    public List<String> step(double dt) {
        if (status != GameStatus.RUNNING) {
            return List.of();
        }
        elapsedSeconds += dt;
        List<String> messages = new ArrayList<>(ocean.step(dt));
        if (player.healthLevel() < lastHealth - 0.2) {
            messages.add("¡Ay! Te mordieron. ¡Huye y busca un lugar seguro!");
        }
        lastHealth = player.healthLevel();
        if (player.stage() != lastStage) {
            lastStage = player.stage();
            messages.add("¡Creciste! Ahora eres " + lastStage.displayName().toLowerCase() + ".");
        }
        if (!player.isAlive()) {
            status = GameStatus.LOST;
        } else if (player.stage() == GrowthStage.ADULT) {
            status = GameStatus.WON;
        }
        return messages;
    }

    /**
     * Computes the score from food eaten, time survived, stages reached and a winning bonus, scaled
     * by difficulty.
     *
     * @return the score
     */
    public int score() {
        double points = player.foodPoints() * 10 + elapsedSeconds * 2 + player.stage().ordinal() * 250
                + (status == GameStatus.WON ? 1000 : 0);
        return (int) Math.round(points * settings.difficulty().scoreMultiplier());
    }

    /**
     * Captures the game for drawing.
     *
     * @return an immutable snapshot
     */
    public GameSnapshot snapshot() {
        List<EntityView> views = ocean.creatures().stream()
                .map(c -> new EntityView(c.id(), c.species().id(), c.species().shape(), c.position().x(),
                        c.position().y(), c.radius(), c.velocity().x() < 0, c.isPlayer(), c.isHunting(),
                        c == player && player.isRecovering(), c.velocity().x(), c.velocity().y()))
                .toList();
        List<Vector2> food = ocean.food().stream().map(f -> f.position()).toList();
        boolean cold = ocean.events().active().stream().anyMatch(e -> e.type() == EventType.COLD_SNAP);
        return new GameSnapshot(status, elapsedSeconds, score(), player.healthLevel(), player.oxygenLevel(),
                GrowthStage.overallProgress(player.foodPoints()), player.stage(), ocean.width(), ocean.height(),
                views, food, ocean.current(), cold, ocean.census());
    }

    /**
     * Builds the score entry for a finished game.
     *
     * @param playerName who played
     * @param playedAt   when it ended
     * @return the entry, if the game is over
     */
    public Optional<ScoreEntry> result(String playerName, java.time.LocalDateTime playedAt) {
        if (!status.isFinal()) {
            return Optional.empty();
        }
        return Optional.of(new ScoreEntry(playerName, score(), player.stage(), status, elapsedSeconds,
                settings.difficulty(), playedAt));
    }

    /**
     * Returns the game status.
     *
     * @return the status
     */
    public GameStatus status() {
        return status;
    }

    /**
     * Returns the visitor's creature.
     *
     * @return the player creature
     */
    public PlayerCreature player() {
        return player;
    }

    /**
     * Returns the scene.
     *
     * @return the scene
     */
    public Ocean ocean() {
        return ocean;
    }

    /**
     * Returns the game settings.
     *
     * @return the settings the game was created with
     */
    public GameSettings settings() {
        return settings;
    }

    /**
     * Returns the time played.
     *
     * @return simulation seconds
     */
    public double elapsedSeconds() {
        return elapsedSeconds;
    }
}
