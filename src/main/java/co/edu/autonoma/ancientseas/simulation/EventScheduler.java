package co.edu.autonoma.ancientseas.simulation;

import co.edu.autonoma.ancientseas.catalog.Species;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

/** Decides when environmental events happen and keeps the active ones running, in simulation time. */
public final class EventScheduler {

    public static final double MEAN_INTERVAL_SECONDS = 25;

    private final Random random;
    private final double frequency;
    private final List<Supplier<EnvironmentEvent>> factories = new ArrayList<>();
    private final List<EnvironmentEvent> active = new ArrayList<>();
    private double secondsUntilNext;

    /**
     * Creates the scheduler and picks the time until the first event.
     *
     * @param random    source of randomness shared with the ocean
     * @param frequency multiplier of how often events happen; must be positive
     * @param hunters   species that can arrive as predators; if empty, no predator arrives
     */
    public EventScheduler(Random random, double frequency, List<Species> hunters) {
        if (!(frequency > 0)) {
            throw new IllegalArgumentException("frequency must be positive: " + frequency);
        }
        this.random = random;
        this.frequency = frequency;
        List<Species> arriving = List.copyOf(hunters);
        factories.add(() -> new OceanCurrent(random.nextBoolean() ? 1 : -1, 14 + random.nextInt(8), 0));
        factories.add(() -> new ColdSnap(12 + random.nextInt(6), 0));
        factories.add(() -> new FoodShower(4, 0));
        if (!arriving.isEmpty()) {
            factories.add(() -> new PredatorArrival(arriving.get(random.nextInt(arriving.size())), 4, 0));
        }
        this.secondsUntilNext = nextInterval();
    }

    List<EnvironmentEvent> tick(Ocean ocean, double dt) {
        List<EnvironmentEvent> started = new ArrayList<>();
        secondsUntilNext -= dt;
        if (secondsUntilNext <= 0) {
            secondsUntilNext = nextInterval();
            pickNewEvent().ifPresent(event -> {
                active.add(event);
                started.add(event);
            });
        }
        for (EnvironmentEvent event : active) {
            event.tick(ocean, dt);
        }
        active.removeIf(EnvironmentEvent::isFinished);
        return started;
    }

    private Optional<EnvironmentEvent> pickNewEvent() {
        for (int attempt = 0; attempt < factories.size(); attempt++) {
            EnvironmentEvent candidate = factories.get(random.nextInt(factories.size())).get();
            boolean alreadyActive = active.stream().anyMatch(e -> e.type() == candidate.type());
            if (!alreadyActive) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    private double nextInterval() {
        return MEAN_INTERVAL_SECONDS * (0.6 + 0.8 * random.nextDouble()) / frequency;
    }

    /**
     * Returns the active events.
     *
     * @return an unmodifiable copy
     */
    public List<EnvironmentEvent> active() {
        return List.copyOf(active);
    }

    /**
     * Returns the countdown to the next event.
     *
     * @return simulation seconds
     */
    public double secondsUntilNext() {
        return secondsUntilNext;
    }

    /**
     * Replaces the scheduler state with a saved one.
     *
     * @param states         the saved active events
     * @param countdown      the saved countdown to the next event
     * @param speciesLookup  finds species by id in the current catalog
     */
    public void restore(List<EventState> states, double countdown, Function<String, Optional<Species>> speciesLookup) {
        active.clear();
        for (EventState s : states) {
            switch (s.type()) {
                case CURRENT -> active.add(new OceanCurrent(s.direction(), s.durationSeconds(), s.elapsedSeconds()));
                case COLD_SNAP -> active.add(new ColdSnap(s.durationSeconds(), s.elapsedSeconds()));
                case FOOD_SHOWER -> active.add(new FoodShower(s.durationSeconds(), s.elapsedSeconds()));
                case PREDATOR_ARRIVAL -> speciesLookup.apply(s.speciesId()).ifPresent(species ->
                        active.add(new PredatorArrival(species, s.durationSeconds(), s.elapsedSeconds())));
            }
        }
        secondsUntilNext = Math.max(0, countdown);
    }
}
