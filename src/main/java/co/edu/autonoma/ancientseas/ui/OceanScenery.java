package co.edu.autonoma.ancientseas.ui;

import co.edu.autonoma.ancientseas.catalog.Zone;
import co.edu.autonoma.ancientseas.model.Vector2;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

final class OceanScenery {

    static final double W = 1600;
    static final double H = 900;
    private static final double FLOOR = Zone.SEAFLOOR.band().min() * H;
    private static final double SURFACE = Zone.SURFACE.band().max() * H;
    private static final int SNOW = 70;
    private static final int BUBBLES = 36;
    private static final int KELP = 11;

    private final Random random = new Random(12);
    private final double[][] snow = new double[SNOW][3];
    private final double[][] bubbles = new double[BUBBLES][3];
    private BufferedImage cached;
    private BufferedImage raySprite;
    private double raySpriteScaleX;
    private double raySpriteScaleY;

    OceanScenery() {
        for (double[] s : snow) {
            s[0] = random.nextDouble() * W;
            s[1] = random.nextDouble() * FLOOR;
            s[2] = 0.6 + random.nextDouble() * 1.6;
        }
        for (double[] b : bubbles) {
            b[0] = random.nextDouble();
            b[1] = random.nextDouble();
            b[2] = 0.5 + random.nextDouble();
        }
    }

    void paintBackground(Graphics2D g, int width, int height) {
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        if (cached == null || cached.getWidth() != w || cached.getHeight() != h) {
            cached = g.getDeviceConfiguration().createCompatibleImage(w, h);
            Graphics2D c = cached.createGraphics();
            try {
                c.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                c.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                c.scale(w / W, h / H);
                paintStill(c);
            } finally {
                c.dispose();
            }
        }
        g.drawImage(cached, 0, 0, width, height, null);
    }

    void paintBehind(Graphics2D g, double time) {
        paintRays(g, time);
        paintCaustics(g, time);
        paintSurface(g, time);
        for (int i = 0; i < KELP; i++) {
            double x = 60 + i * (W - 120) / (KELP - 1) + (i % 2) * 30;
            paintKelp(g, x, FLOOR + 30 + Math.sin(x / 150) * 14, 110 + (i % 4) * 35, time + i * 0.9, i);
        }
    }

    void paintFront(Graphics2D g, double dt, Vector2 drift) {
        g.setColor(new Color(230, 240, 235, 110));
        for (double[] s : snow) {
            s[0] = wrap(s[0] + (Math.sin(s[1] * 0.02) * 4 + drift.x() * 0.6) * dt, W);
            s[1] += (5 + s[2] * 3) * dt;
            if (s[1] > FLOOR + 40) {
                s[1] = 0;
                s[0] = random.nextDouble() * W;
            }
            g.fill(new Ellipse2D.Double(s[0], s[1], s[2], s[2]));
        }
        g.setStroke(new BasicStroke(1.4f));
        for (double[] b : bubbles) {
            b[1] -= dt * 0.05 * b[2];
            b[0] += Math.sin(b[1] * 30) * 0.03 * dt;
            if (b[1] < 0) {
                b[0] = random.nextDouble();
                b[1] = 1;
            }
            double size = 4 + b[2] * 5;
            double x = b[0] * W;
            double y = b[1] * H * 0.9;
            g.setColor(new Color(255, 255, 255, 120));
            g.draw(new Ellipse2D.Double(x, y, size, size));
            g.setColor(new Color(255, 255, 255, 170));
            g.fill(new Ellipse2D.Double(x + size * 0.25, y + size * 0.2, size * 0.25, size * 0.25));
        }
    }

    void paintCurrent(Graphics2D g, Vector2 current, double time) {
        if (current.length() < 1e-6) {
            return;
        }
        double direction = Math.signum(current.x());
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 16; i++) {
            double x = wrap(i * 191 + time * current.x() * 2.5, W);
            double y = 110 + (i * 97) % (FLOOR - 160);
            double len = 70 + (i % 3) * 30;
            g.setPaint(new GradientPaint((float) x, (float) y, new Color(255, 255, 255, 0),
                    (float) (x + direction * len), (float) y, new Color(255, 255, 255, 90)));
            g.draw(new QuadCurve2D.Double(x, y, x + direction * len / 2, y - 8 * Math.sin(time + i), x + direction * len,
                    y));
        }
    }

    void paintCold(Graphics2D g) {
        g.setPaint(new GradientPaint(0, 0, new Color(230, 245, 255, 110), 0, (float) (H * 0.6),
                new Color(230, 245, 255, 25)));
        g.fill(new Rectangle2D.Double(0, 0, W, H));
    }

    private static void paintStill(Graphics2D g) {
        g.setPaint(new LinearGradientPaint(0, 0, 0, (float) H, new float[] {0f, 0.18f, 0.55f, 1f},
                new Color[] {new Color(78, 176, 214), new Color(42, 132, 186), new Color(16, 70, 122),
                    new Color(5, 24, 52)}));
        g.fill(new Rectangle2D.Double(0, 0, W, H));
        paintFarSilhouettes(g);
        paintSeabed(g);
        g.setPaint(new RadialGradientPaint(new Point2D.Double(W / 2, H * 0.45), (float) (W * 0.75),
                new float[] {0.55f, 1f}, new Color[] {new Color(0, 10, 30, 0), new Color(0, 10, 30, 120)},
                MultipleGradientPaint.CycleMethod.NO_CYCLE));
        g.fill(new Rectangle2D.Double(0, 0, W, H));
        g.setColor(new Color(255, 255, 255, 120));
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        g.drawString("Superficie", (float) (W - 160), 34f);
        g.drawString("Aguas intermedias", (float) (W - 205), (float) (SURFACE + 34));
        g.drawString("Fondo marino", (float) (W - 170), (float) (FLOOR + 46));
    }

    private static void paintFarSilhouettes(Graphics2D g) {
        Path2D ridge = new Path2D.Double();
        ridge.moveTo(0, H);
        for (double x = 0; x <= W; x += 50) {
            double y = FLOOR - 30 - 45 * Math.abs(Math.sin(x / 210)) - 25 * Math.sin(x / 77 + 1);
            ridge.lineTo(x, y);
        }
        ridge.lineTo(W, H);
        ridge.closePath();
        g.setColor(new Color(14, 52, 88, 150));
        g.fill(ridge);
        g.setColor(new Color(16, 60, 92, 120));
        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 18; i++) {
            double x = 30 + i * 93;
            double base = FLOOR - 20;
            g.draw(new QuadCurve2D.Double(x, base, x + 14, base - 90, x - 6, base - 150 - (i % 3) * 30));
        }
        g.setPaint(new GradientPaint(0, (float) (FLOOR - 160), new Color(10, 40, 70, 0), 0, (float) FLOOR,
                new Color(10, 40, 70, 110)));
        g.fill(new Rectangle2D.Double(0, FLOOR - 160, W, 160));
    }

    private static void paintSeabed(Graphics2D g) {
        Path2D sand = new Path2D.Double();
        sand.moveTo(0, H);
        for (double x = 0; x <= W; x += 30) {
            sand.lineTo(x, FLOOR + 25 + Math.sin(x / 150) * 14 + Math.sin(x / 41) * 3);
        }
        sand.lineTo(W, H);
        sand.closePath();
        g.setPaint(new LinearGradientPaint(0, (float) FLOOR, 0, (float) H, new float[] {0f, 0.35f, 1f},
                new Color[] {new Color(206, 186, 136), new Color(168, 146, 104), new Color(104, 88, 64)}));
        g.fill(sand);
        g.setColor(new Color(235, 220, 180, 90));
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int row = 0; row < 4; row++) {
            for (double x = 20 + row * 37; x < W; x += 130) {
                double y = FLOOR + 50 + row * 22 + Math.sin(x / 150) * 10;
                g.draw(new QuadCurve2D.Double(x, y, x + 25, y - 6, x + 55, y));
            }
        }
        Random r = new Random(5);
        for (int i = 0; i < 7; i++) {
            double x = 90 + i * 235 + r.nextDouble() * 60;
            double y = FLOOR + 40 + r.nextDouble() * 35;
            paintRock(g, x, y, 60 + r.nextDouble() * 50, 30 + r.nextDouble() * 18);
        }
        for (int i = 0; i < 6; i++) {
            double x = 200 + i * 260 + r.nextDouble() * 80;
            paintSponge(g, x, FLOOR + 48 + r.nextDouble() * 20, 18 + r.nextDouble() * 10);
        }
        for (int i = 0; i < 5; i++) {
            double x = 330 + i * 300 + r.nextDouble() * 70;
            paintCrinoid(g, x, FLOOR + 50 + r.nextDouble() * 20, 55 + r.nextDouble() * 25);
        }
        g.setColor(new Color(120, 104, 80));
        for (int i = 0; i < 60; i++) {
            double x = r.nextDouble() * W;
            double y = FLOOR + 45 + r.nextDouble() * (H - FLOOR - 50);
            double s = 2 + r.nextDouble() * 4;
            g.fill(new Ellipse2D.Double(x, y, s * 1.4, s));
        }
    }

    private static void paintRock(Graphics2D g, double x, double y, double w, double h) {
        g.setPaint(new RadialGradientPaint(new Point2D.Double(x + w / 2, y + h * 0.9), (float) (w * 0.7),
                new float[] {0f, 1f}, new Color[] {new Color(40, 30, 20, 110), new Color(40, 30, 20, 0)}));
        g.fill(new Ellipse2D.Double(x - w * 0.2, y + h * 0.5, w * 1.4, h * 0.8));
        Path2D rock = new Path2D.Double();
        rock.moveTo(x, y + h);
        rock.curveTo(x - w * 0.05, y + h * 0.3, x + w * 0.25, y - h * 0.15, x + w * 0.55, y);
        rock.curveTo(x + w * 0.85, y + h * 0.05, x + w * 1.05, y + h * 0.5, x + w, y + h);
        rock.closePath();
        g.setPaint(new RadialGradientPaint(new Point2D.Double(x + w * 0.4, y + h * 0.1), (float) w,
                new float[] {0f, 0.6f, 1f}, new Color[] {new Color(140, 136, 126), new Color(92, 88, 82),
                    new Color(52, 50, 48)}));
        g.fill(rock);
        g.setColor(new Color(40, 38, 36, 160));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(rock);
    }

    private static void paintSponge(Graphics2D g, double x, double y, double size) {
        Path2D vase = new Path2D.Double();
        vase.moveTo(x - size * 0.25, y);
        vase.quadTo(x - size * 0.55, y - size * 1.1, x - size * 0.35, y - size * 1.6);
        vase.lineTo(x + size * 0.35, y - size * 1.6);
        vase.quadTo(x + size * 0.55, y - size * 1.1, x + size * 0.25, y);
        vase.closePath();
        g.setPaint(new GradientPaint((float) x, (float) (y - size * 1.6), new Color(204, 150, 96), (float) x,
                (float) y, new Color(132, 86, 56)));
        g.fill(vase);
        g.setColor(new Color(80, 50, 30));
        g.fill(new Ellipse2D.Double(x - size * 0.3, y - size * 1.7, size * 0.6, size * 0.2));
    }

    private static void paintCrinoid(Graphics2D g, double x, double y, double height) {
        g.setColor(new Color(150, 110, 120));
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double topX = x + height * 0.1;
        double topY = y - height;
        g.draw(new QuadCurve2D.Double(x, y, x - height * 0.08, y - height * 0.5, topX, topY));
        g.setColor(new Color(186, 134, 146));
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = -3; i <= 3; i++) {
            double angle = -Math.PI / 2 + i * 0.32;
            double ex = topX + Math.cos(angle) * height * 0.35;
            double ey = topY + Math.sin(angle) * height * 0.35 + height * 0.06;
            g.draw(new QuadCurve2D.Double(topX, topY, (topX + ex) / 2 + i * 2, (topY + ey) / 2 - 6, ex, ey));
        }
    }

    private void paintRays(Graphics2D g, double time) {
        double sx = g.getTransform().getScaleX();
        double sy = g.getTransform().getScaleY();
        if (raySprite == null || raySpriteScaleX != sx || raySpriteScaleY != sy) {
            raySprite = createRaySprite(g, sx, sy);
            raySpriteScaleX = sx;
            raySpriteScaleY = sy;
        }
        AffineTransform saved = g.getTransform();
        Point2D origin = new Point2D.Double();
        for (int i = 0; i < 6; i++) {
            double x = (i * W / 6 + Math.sin(time * 0.25 + i) * 50) % W;
            saved.transform(new Point2D.Double(x, 0), origin);
            g.setTransform(AffineTransform.getTranslateInstance(Math.round(origin.getX()), Math.round(origin.getY())));
            g.drawImage(raySprite, 0, 0, null);
        }
        g.setTransform(saved);
    }

    private static BufferedImage createRaySprite(Graphics2D target, double sx, double sy) {
        int w = Math.max(1, (int) Math.ceil(280 * sx));
        int h = Math.max(1, (int) Math.ceil(H * 0.8 * sy));
        BufferedImage sprite = target.getDeviceConfiguration().createCompatibleImage(w, h,
                java.awt.Transparency.TRANSLUCENT);
        Graphics2D g = sprite.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.scale(sx, sy);
            Path2D ray = new Path2D.Double();
            ray.moveTo(0, 0);
            ray.lineTo(80, 0);
            ray.lineTo(280, H * 0.8);
            ray.lineTo(130, H * 0.8);
            ray.closePath();
            g.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 34), 0, (float) (H * 0.8),
                    new Color(255, 255, 255, 0)));
            g.fill(ray);
        } finally {
            g.dispose();
        }
        return sprite;
    }

    private static void paintCaustics(Graphics2D g, double time) {
        Composite saved = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.16f));
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 22; i++) {
            double x = wrap(i * 79 + Math.sin(time * 0.6 + i) * 30 + time * 6, W);
            double y = 14 + (i * 37) % (SURFACE - 20) + Math.sin(time * 0.9 + i * 2) * 5;
            double w = 26 + (i % 5) * 10;
            g.draw(new QuadCurve2D.Double(x, y, x + w / 2, y - 5 + Math.sin(time * 1.3 + i) * 3, x + w, y));
        }
        g.setComposite(saved);
    }

    private static void paintSurface(Graphics2D g, double time) {
        Path2D wave = new Path2D.Double();
        wave.moveTo(0, SURFACE);
        for (double x = 0; x <= W; x += 20) {
            wave.lineTo(x, SURFACE + Math.sin(x / 70 + time * 1.4) * 4);
        }
        g.setColor(new Color(255, 255, 255, 70));
        g.setStroke(new BasicStroke(2.2f));
        g.draw(wave);
        Path2D top = new Path2D.Double();
        top.moveTo(0, 6);
        for (double x = 0; x <= W; x += 20) {
            top.lineTo(x, 6 + Math.sin(x / 55 - time * 1.8) * 3);
        }
        g.setColor(new Color(255, 255, 255, 90));
        g.draw(top);
    }

    private static void paintKelp(Graphics2D g, double x, double base, double height, double time, int seed) {
        int segments = 9;
        double[] px = new double[segments + 1];
        double[] py = new double[segments + 1];
        for (int i = 0; i <= segments; i++) {
            double t = (double) i / segments;
            px[i] = x + Math.sin(time * 1.1 + t * 2) * 16 * t * t;
            py[i] = base - height * t;
        }
        Path2D stem = new Path2D.Double();
        stem.moveTo(px[0] - 4, py[0]);
        for (int i = 1; i <= segments; i++) {
            stem.lineTo(px[i] - 4 * (1 - (double) i / segments) - 0.5, py[i]);
        }
        for (int i = segments; i >= 0; i--) {
            stem.lineTo(px[i] + 4 * (1 - (double) i / segments) + 0.5, py[i]);
        }
        stem.closePath();
        Color dark = new Color(30, 96 + (seed % 3) * 12, 62);
        Color light = new Color(70, 150 + (seed % 3) * 10, 92);
        g.setPaint(new GradientPaint((float) x, (float) base, dark, (float) x, (float) (base - height), light));
        g.fill(stem);
        for (int i = 2; i < segments; i++) {
            double side = (i % 2 == 0) ? 1 : -1;
            double lx = px[i];
            double ly = py[i];
            Path2D leaf = new Path2D.Double();
            leaf.moveTo(lx, ly);
            leaf.quadTo(lx + side * 14, ly - 10, lx + side * 22, ly - 2 + Math.sin(time * 1.5 + i) * 3);
            leaf.quadTo(lx + side * 12, ly + 4, lx, ly);
            g.fill(leaf);
        }
    }

    private static double wrap(double value, double size) {
        return ((value % size) + size) % size;
    }
}
