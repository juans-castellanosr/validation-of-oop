package co.edu.autonoma.ancientseas.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;
import org.junit.jupiter.api.Test;

class ResumableRandomTest {

    @Test
    void producesTheSameNumbersAsJavaUtilRandomForEveryMethodTheGameUses() {
        Random reference = new Random(2026);
        ResumableRandom resumable = ResumableRandom.seeded(2026);

        for (int i = 0; i < 1000; i++) {
            assertEquals(reference.nextDouble(), resumable.nextDouble());
            assertEquals(reference.nextBoolean(), resumable.nextBoolean());
            assertEquals(reference.nextInt(37), resumable.nextInt(37));
            assertEquals(reference.nextLong(), resumable.nextLong());
        }
    }

    @Test
    void resumingAtASavedStateRepeatsTheRestOfTheSequence() {
        ResumableRandom original = ResumableRandom.seeded(5);
        original.nextDouble();
        long saved = original.state();
        ResumableRandom restored = ResumableRandom.seeded(999);

        restored.resumeAt(saved);

        for (int i = 0; i < 100; i++) {
            assertEquals(original.nextDouble(), restored.nextDouble());
        }
    }
}
