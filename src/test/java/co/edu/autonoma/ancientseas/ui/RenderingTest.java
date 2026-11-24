package co.edu.autonoma.ancientseas.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.autonoma.ancientseas.TestSpecies;
import co.edu.autonoma.ancientseas.catalog.BodyShape;
import co.edu.autonoma.ancientseas.game.GameMemento;
import co.edu.autonoma.ancientseas.game.GameSession;
import co.edu.autonoma.ancientseas.game.GameSettings;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class RenderingTest {

    @ParameterizedTest
    @EnumSource(BodyShape.class)
    void everyBodyShapeHasAPainterThatDrawsSomethingAtAnySize(BodyShape shape) {
        CreaturePainter painter = CreatureArt.painterFor(shape);
        assertNotNull(painter, "no painter for " + shape);
        for (double radius : new double[] {4, 14, 60}) {
            BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.translate(200, 200);
            painter.paint(g, new CreaturePose(radius, 1.3, 2.0, 0.5));
            g.dispose();
            assertTrue(paintedPixels(image) > 0, shape + " drew nothing at radius " + radius);
        }
    }

    @Test
    void aFullGameFrameRendersWithEffectsAndPause() {
        GameSession game = GameSession.newGame(TestSpecies.catalog(), GameSettings.defaults(), new Random(3));
        game.start();
        OceanRenderer renderer = new OceanRenderer();
        FeedbackEffects effects = new FeedbackEffects();
        BufferedImage image = new BufferedImage(800, 450, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < 30; i++) {
            game.step(1.0 / 60);
            Graphics2D g = image.createGraphics();
            effects.update(game.snapshot(), 1.0 / 60);
            renderer.paint(g, image.getWidth(), image.getHeight(), game.snapshot(), i / 60.0, 1.0 / 60, effects);
            g.dispose();
        }
        game.pause();
        Graphics2D g = image.createGraphics();
        renderer.paint(g, image.getWidth(), image.getHeight(), game.snapshot(), 1, 0, effects);
        g.dispose();
    }

    @Test
    void drawingNeverChangesTheGame() {
        GameSession game = GameSession.newGame(TestSpecies.catalog(), GameSettings.defaults(), new Random(8));
        game.start();
        game.step(1.0 / 60);
        GameMemento before = game.toMemento();
        BufferedImage image = new BufferedImage(640, 360, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        new OceanRenderer().paint(g, 640, 360, game.snapshot(), 0.5, 1.0 / 60, new FeedbackEffects());
        g.dispose();
        assertEquals(before, game.toMemento());
    }

    @Test
    void menuOceanRendersWithAndWithoutReducedMotion() {
        for (boolean reduced : new boolean[] {false, true}) {
            OceanRenderer renderer = new OceanRenderer();
            renderer.setReducedMotion(reduced);
            BufferedImage image = new BufferedImage(640, 360, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            renderer.paint(g, 640, 360, null, 3.0, 1.0 / 30, null);
            g.dispose();
            assertTrue(paintedPixels(image) > 0);
        }
    }

    private static int paintedPixels(BufferedImage image) {
        int count = 0;
        for (int y = 0; y < image.getHeight(); y += 2) {
            for (int x = 0; x < image.getWidth(); x += 2) {
                if (image.getRGB(x, y) != 0) {
                    count++;
                }
            }
        }
        return count;
    }
}
