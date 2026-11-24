package co.edu.autonoma.ancientseas.game;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runs a game on its own thread at a fixed rate. That thread is the only one that touches the
 * {@link GameSession}; other threads {@linkplain #submit submit commands} and read the latest
 * immutable {@link GameSnapshot}, so the model needs no locks.
 */
public final class GameEngine {

    public static final int STEPS_PER_SECOND = 60;
    public static final long QUERY_TIMEOUT_SECONDS = 3;
    private static final double DT = 1.0 / STEPS_PER_SECOND;
    private static final Logger LOG = Logger.getLogger(GameEngine.class.getName());

    private final GameSession session;
    private final GameListener listener;
    private final Queue<Consumer<GameSession>> commands = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService executor;
    private volatile GameSnapshot latest;
    private boolean finishedReported;

    /**
     * Creates a stopped engine for the session; nothing runs until {@link #launch()}.
     *
     * @param session  the game this engine owns from now on
     * @param listener receives snapshots and the end of the game
     */
    public GameEngine(GameSession session, GameListener listener) {
        this.session = session;
        this.listener = listener;
        this.latest = session.snapshot();
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ancient-seas-simulation");
            t.setDaemon(true);
            return t;
        });
    }

    /** Starts the simulation thread. */
    public void launch() {
        executor.scheduleAtFixedRate(this::tick, 0, 1_000_000 / STEPS_PER_SECOND, TimeUnit.MICROSECONDS);
    }

    private void tick() {
        try {
            Consumer<GameSession> command;
            while ((command = commands.poll()) != null) {
                command.accept(session);
            }
            session.step(DT).forEach(listener::onMessage);
            GameSnapshot snapshot = session.snapshot();
            latest = snapshot;
            listener.onFrame(snapshot);
            if (session.status().isFinal() && !finishedReported) {
                finishedReported = true;
                listener.onFinished(snapshot);
            }
        } catch (RuntimeException e) {
            fail(e);
        } catch (Error e) {
            fail(e);
            throw e;
        }
    }

    private void fail(Throwable error) {
        LOG.log(Level.SEVERE, "Simulation step failed; stopping the game", error);
        executor.shutdown();
        listener.onFailure(error);
    }

    /**
     * Queues an action to run on the simulation thread before the next step.
     *
     * @param command the action, e.g. {@code s -> s.player().steerTowards(direction)}
     */
    public void submit(Consumer<GameSession> command) {
        commands.add(command);
    }

    /**
     * Runs a query on the simulation thread and returns its result asynchronously.
     *
     * @param <T>   the result type
     * @param query the computation, e.g. {@code GameSession::toMemento}
     * @return a future with the result, computed on the simulation thread; it fails if the engine is
     *         stopped or the query does not finish within the timeout
     */
    public <T> CompletableFuture<T> query(Function<GameSession, T> query) {
        CompletableFuture<T> result = new CompletableFuture<>();
        submit(s -> {
            try {
                result.complete(query.apply(s));
            } catch (RuntimeException e) {
                result.completeExceptionally(e);
            }
        });
        if (executor.isShutdown()) {
            result.completeExceptionally(new IllegalStateException("The game engine is stopped"));
        }
        return result.orTimeout(QUERY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Returns the latest published snapshot; safe to call from any thread.
     *
     * @return the snapshot
     */
    public GameSnapshot latest() {
        return latest;
    }

    /**
     * Stops the simulation thread and waits briefly for the current step to finish.
     *
     * @throws InterruptedException if interrupted while waiting
     */
    public void shutdown() throws InterruptedException {
        executor.shutdown();
        if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
    }

    /**
     * Tells whether the simulation thread has been stopped.
     *
     * @return {@code true} after {@link #shutdown()} or a failure
     */
    public boolean isStopped() {
        return executor.isShutdown();
    }
}
