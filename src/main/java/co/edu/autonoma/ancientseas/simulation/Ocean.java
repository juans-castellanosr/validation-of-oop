package co.edu.autonoma.ancientseas.simulation;

import co.edu.autonoma.ancientseas.catalog.Behavior;
import co.edu.autonoma.ancientseas.catalog.Range;
import co.edu.autonoma.ancientseas.catalog.Species;
import co.edu.autonoma.ancientseas.catalog.Zone;
import co.edu.autonoma.ancientseas.error.HabitatViolationException;
import co.edu.autonoma.ancientseas.model.Creature;
import co.edu.autonoma.ancientseas.model.CreatureFactory;
import co.edu.autonoma.ancientseas.model.Edible;
import co.edu.autonoma.ancientseas.model.FoodParticle;
import co.edu.autonoma.ancientseas.model.FoodState;
import co.edu.autonoma.ancientseas.model.SeafloorDweller;
import co.edu.autonoma.ancientseas.model.Surroundings;
import co.edu.autonoma.ancientseas.model.Vector2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

/** The scene: the body of water and everything living in it. */
public final class Ocean implements Surroundings {

    public static final double DEFAULT_WIDTH = 1600;
    public static final double DEFAULT_HEIGHT = 900;
    public static final int MAX_FOOD = 160;
    private static final double PLANKTON_PER_SECOND = 2.5;
    private static final double NEWCOMERS_PER_SECOND = 0.1;
    private static final double FOOD_LIFETIME_SECONDS = 60;

    private final double width;
    private final double height;
    private final ResumableRandom random;
    private final double hunterSpeedFactor;
    private final Map<String, Integer> populationCaps;
    private final List<Species> residents;
    private final EventScheduler events;
    private final List<Creature> creatures = new ArrayList<>();
    private final List<Creature> births = new ArrayList<>();
    private final List<FoodParticle> food = new ArrayList<>();
    private long nextId = 1;
    private double planktonDue;
    private Vector2 current = Vector2.ZERO;
    private double activityFactor = 1.0;

    /**
     * Creates an empty ocean; creatures and food are added by the game session.
     *
     * @param width             ocean width
     * @param height            ocean height
     * @param random            source of randomness, kept so a saved game continues identically
     * @param hunterSpeedFactor speed multiplier for hunters, set by the difficulty
     * @param eventFrequency    multiplier of how often events happen
     * @param populationCaps    maximum individuals per species id
     * @param residents         species that live here and can be renewed
     * @param arrivingHunters   species that can arrive as predators
     */
    public Ocean(double width, double height, ResumableRandom random, double hunterSpeedFactor, double eventFrequency,
            Map<String, Integer> populationCaps, List<Species> residents, List<Species> arrivingHunters) {
        this.width = width;
        this.height = height;
        this.random = random;
        this.hunterSpeedFactor = hunterSpeedFactor;
        this.populationCaps = Map.copyOf(populationCaps);
        this.residents = residents.stream().filter(s -> populationCaps.containsKey(s.id()))
                .sorted(Comparator.comparing(Species::id)).toList();
        this.events = new EventScheduler(random, eventFrequency, arrivingHunters);
    }

    /**
     * Advances the whole ocean one step.
     *
     * @param dt elapsed simulation seconds
     * @return announcements of events that started in this step
     */
    public List<String> step(double dt) {
        current = Vector2.ZERO;
        activityFactor = 1.0;
        List<String> announcements = events.tick(this, dt).stream().map(EnvironmentEvent::announcement).toList();

        growPlankton(dt);
        bringNewcomers(dt);
        for (Creature creature : creatures) {
            creature.update(this, dt);
        }
        double floor = height;
        for (FoodParticle particle : food) {
            particle.update(floor, current, dt);
        }
        creatures.removeIf(c -> !c.isAlive());
        food.removeIf(f -> !f.isAlive());
        creatures.addAll(births);
        births.clear();
        return announcements;
    }

    private void growPlankton(double dt) {
        planktonDue += PLANKTON_PER_SECOND * dt;
        while (planktonDue >= 1) {
            planktonDue -= 1;
            addFoodInWater();
        }
    }

    /**
     * Adds a creature, checking that it is placed where its species can live.
     *
     * @param creature the creature
     * @throws HabitatViolationException if it lies outside the ocean, or a sea-floor species is
     *                                   not on the floor
     */
    public void add(Creature creature) throws HabitatViolationException {
        requireHabitat(creature.species(), creature.position());
        creatures.add(creature);
        nextId = Math.max(nextId, creature.id() + 1);
    }

    /**
     * Creates and adds a creature of a species at a random place inside its zone.
     *
     * @param species    the species
     * @param sizeMeters its body length
     * @param energy     its initial food reserve
     * @param acceptable extra condition on the position; after 20 failed draws the last one is used
     * @return the new creature
     */
    public Creature spawnInZone(Species species, double sizeMeters, double energy, Predicate<Vector2> acceptable) {
        Vector2 position = randomPointIn(species.zone());
        for (int attempt = 0; attempt < 20 && !acceptable.test(position); attempt++) {
            position = randomPointIn(species.zone());
        }
        Creature creature = CreatureFactory.create(nextId++, species, position, sizeMeters, energy);
        creatures.add(creature);
        return creature;
    }

    /**
     * Rebuilds a food particle from a saved game.
     *
     * @param state the saved particle
     */
    public void restoreFood(FoodState state) {
        food.add(new FoodParticle(state.id(), new Vector2(state.x(), state.y()), state.secondsLeft()));
        nextId = Math.max(nextId, state.id() + 1);
    }

    void addFoodNearSurface() {
        addFood(new Vector2(random.nextDouble() * width, random.nextDouble() * height * 0.15));
    }

    private void addFoodInWater() {
        addFood(new Vector2(random.nextDouble() * width, random.nextDouble() * height * Zone.MIDWATER.band().max()));
    }

    private void addFood(Vector2 at) {
        if (food.size() < MAX_FOOD) {
            food.add(new FoodParticle(nextId++, at, FOOD_LIFETIME_SECONDS * (0.7 + 0.6 * random.nextDouble())));
        }
    }

    private void bringNewcomers(double dt) {
        for (Species species : residents) {
            int cap = populationCaps.get(species.id());
            if (populationOf(species.id()) < Math.max(1, cap / 4) && random.nextDouble() < NEWCOMERS_PER_SECOND * dt) {
                double y = randomPointIn(species.zone()).y();
                double margin = species.lengthMeters();
                boolean leftIsFarther = creatures.stream().filter(Creature::isPlayer).findFirst()
                        .map(p -> p.position().x() > width / 2).orElseGet(random::nextBoolean);
                double x = leftIsFarther ? margin : width - margin;
                births.add(CreatureFactory.create(nextId++, species, new Vector2(x, y),
                        species.lengthMeters() * 0.6, 0.6));
            }
        }
    }

    void addArrivingPredator(Species species) {
        double x = random.nextBoolean() ? species.lengthMeters() : width - species.lengthMeters();
        Range<Double> band = species.zone().band();
        double y = (band.min() + band.max()) / 2 * height;
        creatures.add(CreatureFactory.create(nextId++, species, new Vector2(x, y), species.lengthMeters(), 0.6));
    }

    private void requireHabitat(Species species, Vector2 p) throws HabitatViolationException {
        if (p.x() < 0 || p.x() > width || p.y() < 0 || p.y() > height) {
            throw new HabitatViolationException(species.id(), "quedó fuera del océano");
        }
        if (species.behavior() == Behavior.SESSILE && Zone.at(p.y() / height) != Zone.SEAFLOOR) {
            throw new HabitatViolationException(species.id(), "debe estar en el fondo marino");
        }
    }

    private Vector2 randomPointIn(Zone zone) {
        Range<Double> band = zone == Zone.SEAFLOOR ? SeafloorDweller.RESTING_BAND : zone.band();
        double y = (band.min() + random.nextDouble() * (band.max() - band.min())) * height;
        return new Vector2(40 + random.nextDouble() * (width - 80), y);
    }

    @Override
    public boolean requestOffspring(Creature parent) {
        Species species = parent.species();
        int cap = populationCaps.getOrDefault(species.id(), 0);
        if (populationOf(species.id()) >= cap) {
            return false;
        }
        Vector2 offset = new Vector2(random.nextDouble() * 30 - 15, random.nextDouble() * 10 - 5);
        Vector2 at = parent.position().plus(offset);
        Vector2 inside = new Vector2(Math.max(0, Math.min(width, at.x())), Math.max(0, Math.min(height, at.y())));
        births.add(CreatureFactory.create(nextId++, species, inside, species.lengthMeters() * 0.35, 0.5));
        return true;
    }

    /**
     * Counts living individuals of a species, including those born in the current step.
     *
     * @param speciesId the species id
     * @return the population
     */
    public int populationOf(String speciesId) {
        int count = 0;
        for (Creature c : creatures) {
            if (c.isAlive() && c.species().id().equals(speciesId)) {
                count++;
            }
        }
        for (Creature c : births) {
            if (c.species().id().equals(speciesId)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Optional<Edible> nearestEdible(Creature eater, double radius) {
        Edible best = null;
        double bestDistance = radius;
        for (Creature c : creatures) {
            double d = eater.position().distanceTo(c.position());
            if (d <= bestDistance && eater.canEat(c)) {
                best = c;
                bestDistance = d;
            }
        }
        for (FoodParticle f : food) {
            double d = eater.position().distanceTo(f.position());
            if (d <= bestDistance && eater.canEat(f)) {
                best = f;
                bestDistance = d;
            }
        }
        return Optional.ofNullable(best);
    }

    @Override
    public Optional<Creature> nearestThreat(Creature target, double radius) {
        Creature best = null;
        double bestDistance = radius;
        for (Creature c : creatures) {
            double d = target.position().distanceTo(c.position());
            if (d <= bestDistance && c.isAlive() && c.canEat(target)) {
                best = c;
                bestDistance = d;
            }
        }
        return Optional.ofNullable(best);
    }

    @Override
    public List<Creature> sameSpeciesNear(Creature creature, double radius) {
        List<Creature> near = new ArrayList<>();
        for (Creature c : creatures) {
            if (c != creature && c.isAlive() && c.species().equals(creature.species())
                    && c.position().distanceTo(creature.position()) <= radius) {
                near.add(c);
            }
        }
        return near;
    }

    @Override
    public Vector2 currentAt(Vector2 point) {
        return current;
    }

    @Override
    public double activityFactor() {
        return activityFactor;
    }

    @Override
    public double hunterSpeedFactor() {
        return hunterSpeedFactor;
    }

    @Override
    public Random random() {
        return random;
    }

    @Override
    public double width() {
        return width;
    }

    @Override
    public double height() {
        return height;
    }

    void setCurrent(Vector2 drift) {
        this.current = drift;
    }

    void setActivityFactor(double factor) {
        this.activityFactor = factor;
    }

    /**
     * Returns the water current flowing in this step.
     *
     * @return the drift velocity
     */
    public Vector2 current() {
        return current;
    }

    /**
     * Returns the living creatures.
     *
     * @return an unmodifiable view
     */
    public List<Creature> creatures() {
        return Collections.unmodifiableList(creatures);
    }

    /**
     * Returns the food particles.
     *
     * @return an unmodifiable view
     */
    public List<FoodParticle> food() {
        return Collections.unmodifiableList(food);
    }

    /**
     * Returns the environmental event scheduler.
     *
     * @return the scheduler
     */
    public EventScheduler events() {
        return events;
    }

    /**
     * Returns the population limits.
     *
     * @return maximum individuals per species id
     */
    public Map<String, Integer> populationCaps() {
        return populationCaps;
    }

    /**
     * Returns the id the next entity will get.
     *
     * @return the next id
     */
    public long nextId() {
        return nextId;
    }

    /**
     * Returns the fraction of the next plankton particle already grown.
     *
     * @return a value in [0, 1)
     */
    public double planktonDue() {
        return planktonDue;
    }

    /**
     * Returns the position of the random sequence.
     *
     * @return the generator's state
     */
    public long randomState() {
        return random.state();
    }

    /**
     * Puts the random sequence back where a saved game left it; call it last when restoring.
     *
     * @param savedState the state read with {@link #randomState()}
     */
    public void restoreRandomState(long savedState) {
        random.resumeAt(savedState);
    }

    /**
     * Restores the counters of a saved game; the id allocator never goes back.
     *
     * @param savedNextId       the id the saved game would have used next
     * @param savedPlanktonDue  the saved plankton progress, clamped to [0, 1)
     */
    public void restoreCounters(long savedNextId, double savedPlanktonDue) {
        nextId = Math.max(nextId, savedNextId);
        planktonDue = Math.max(0, Math.min(Math.nextDown(1.0), savedPlanktonDue));
    }

    /**
     * Returns the population of every species, for reports.
     *
     * @return individuals per species id
     */
    public Map<String, Integer> census() {
        Map<String, Integer> census = new HashMap<>();
        creatures.forEach(c -> census.merge(c.species().id(), 1, Integer::sum));
        return census;
    }
}
