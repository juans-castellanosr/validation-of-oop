package co.edu.autonoma.ancientseas.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.autonoma.ancientseas.TestSpecies;
import co.edu.autonoma.ancientseas.model.Vector2;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(20)
class GameEngineTest {

    private final AtomicInteger frames = new AtomicInteger();
    private final AtomicReference<Throwable> failure = new AtomicReference<>();
    private final CountDownLatch failed = new CountDownLatch(1);
    private GameEngine engine;

    private final GameListener listener = new GameListener() {
        @Override
        public void onFrame(GameSnapshot snapshot) {
            frames.incrementAndGet();
        }

        @Override
        public void onMessage(String message) {
        }

        @Override
        public void onFinished(GameSnapshot finalSnapshot) {
        }

        @Override
        public void onFailure(Throwable error) {
            failure.set(error);
            failed.countDown();
        }
    };

    private GameEngine start() {
        GameSession session = GameSession.newGame(TestSpecies.catalog(), GameSettings.defaults(), new Random(5));
        engine = new GameEngine(session, listener);
        engine.launch();
        engine.submit(GameSession::start);
        return engine;
    }

    @AfterEach
    void stop() throws InterruptedException {
        if (engine != null) {
            engine.shutdown();
        }
    }

    private static void waitFor(java.util.function.BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) {
                throw new AssertionError("condition not met in time");
            }
            Thread.sleep(10);
        }
    }

    @Test
    void simulationAdvancesOnItsOwnThread() throws Exception {
        GameEngine e = start();
        waitFor(() -> e.latest().elapsedSeconds() > 0.2);
        assertEquals(GameStatus.RUNNING, e.latest().status());
        assertTrue(frames.get() > 0);
    }

    @Test
    void pauseFreezesTheGameUntilResumed() throws Exception {
        GameEngine e = start();
        waitFor(() -> e.latest().elapsedSeconds() > 0.1);
        e.submit(GameSession::pause);
        waitFor(() -> e.latest().status() == GameStatus.PAUSED);
        double frozen = e.latest().elapsedSeconds();
        Thread.sleep(300);
        assertEquals(frozen, e.latest().elapsedSeconds());
        e.submit(GameSession::resume);
        waitFor(() -> e.latest().elapsedSeconds() > frozen);
    }

    @Test
    void commandsFromManyThreadsAreAllAppliedWithoutCorruptingTheState() throws Exception {
        GameEngine e = start();
        AtomicInteger applied = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        List<Future<?>> sent = new ArrayList<>();
        for (int t = 0; t < 8; t++) {
            int thread = t;
            sent.add(pool.submit(() -> {
                for (int i = 0; i < 200; i++) {
                    Vector2 dir = new Vector2(Math.cos(thread + i), Math.sin(thread + i));
                    e.submit(s -> {
                        s.player().steerTowards(dir);
                        applied.incrementAndGet();
                    });
                }
            }));
        }
        for (Future<?> f : sent) {
            f.get();
        }
        pool.shutdown();
        waitFor(() -> applied.get() == 1600);
        GameMemento memento = e.query(GameSession::toMemento).get(5, TimeUnit.SECONDS);
        assertEquals(1, memento.creatures().stream().filter(c -> c.player()).count());
        assertTrue(failure.get() == null, "no step failed");
    }

    @Test
    void savingWhileRunningCapturesAConsistentState() throws Exception {
        GameEngine e = start();
        waitFor(() -> e.latest().elapsedSeconds() > 0.2);
        GameMemento memento = e.query(GameSession::toMemento).get(5, TimeUnit.SECONDS);
        GameSession copy = GameSession.restore(memento, TestSpecies.catalog());
        assertEquals(memento, copy.toMemento());
    }

    @Test
    void unexpectedErrorStopsTheEngineAndIsReported() throws Exception {
        GameEngine e = start();
        e.submit(s -> {
            throw new IllegalStateException("boom");
        });
        assertTrue(failed.await(5, TimeUnit.SECONDS));
        assertInstanceOf(IllegalStateException.class, failure.get());
        waitFor(e::isStopped);
        ExecutionException afterStop = assertThrows(ExecutionException.class,
                () -> e.query(GameSession::score).get(5, TimeUnit.SECONDS));
        assertInstanceOf(IllegalStateException.class, afterStop.getCause());
    }

    @Test
    void anErrorIsReportedBeforeItStopsTheEngineInsteadOfFreezingSilently() throws Exception {
        GameEngine e = start();
        e.submit(s -> {
            throw new StackOverflowError("deep");
        });
        assertTrue(failed.await(5, TimeUnit.SECONDS));
        assertInstanceOf(StackOverflowError.class, failure.get());
        waitFor(e::isStopped);
    }

    @Test
    void shutdownStopsTheThread() throws Exception {
        GameEngine e = start();
        e.shutdown();
        assertTrue(e.isStopped());
        int after = frames.get();
        Thread.sleep(100);
        assertEquals(after, frames.get());
        assertFalse(failure.get() != null);
    }
}
