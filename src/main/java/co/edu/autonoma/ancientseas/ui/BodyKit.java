package co.edu.autonoma.ancientseas.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.function.DoubleUnaryOperator;

final class BodyKit {

    private static final Color HURT = new Color(225, 60, 45);

    private BodyKit() {
    }

    record Palette(Color back, Color side, Color belly) {

        Palette hurt(double amount) {
            if (amount <= 0) {
                return this;
            }
            double k = Math.min(1, amount) * 0.35;
            return new Palette(blend(back, HURT, k), blend(side, HURT, k), blend(belly, HURT, k));
        }

        Color line() {
            return blend(back, new Color(10, 20, 30), 0.55);
        }
    }

    static Color blend(Color a, Color b, double k) {
        double t = Math.max(0, Math.min(1, k));
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    static void shade(Graphics2D g, Shape body, Palette palette, double radius) {
        Rectangle2D b = body.getBounds2D();
        if (b.getHeight() <= 0) {
            return;
        }
        g.setPaint(new LinearGradientPaint((float) b.getCenterX(), (float) b.getMinY(), (float) b.getCenterX(),
                (float) b.getMaxY(), new float[] {0f, 0.55f, 1f},
                new Color[] {palette.back(), palette.side(), palette.belly()}));
        g.fill(body);
        outline(g, body, palette.line(), radius);
    }

    static void outline(Graphics2D g, Shape shape, Color color, double radius) {
        g.setColor(color);
        g.setStroke(new BasicStroke((float) Math.max(0.8, radius * 0.045), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g.draw(shape);
    }

    static void fin(Graphics2D g, Shape fin, Color color, Color line, double radius) {
        g.setColor(color);
        g.fill(fin);
        outline(g, fin, line, radius);
    }

    static void eye(Graphics2D g, double x, double y, double size) {
        double s = Math.max(1.2, size);
        g.setColor(new Color(245, 245, 235));
        g.fill(new Ellipse2D.Double(x - s, y - s, 2 * s, 2 * s));
        g.setColor(new Color(15, 18, 22));
        g.fill(new Ellipse2D.Double(x - s * 0.62, y - s * 0.62, s * 1.24, s * 1.24));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(x - s * 0.15, y - s * 0.55, s * 0.45, s * 0.45));
    }

    static double spine(double t, double amplitude, double phase, double wavelength) {
        double tail = 1 - t;
        return amplitude * tail * tail * Math.sin(phase - t * wavelength);
    }

    static Path2D spineBody(double tailX, double headX, DoubleUnaryOperator thickness, double amplitude,
            double phase, double wavelength) {
        int segments = 28;
        double[] xs = new double[segments + 1];
        double[] ys = new double[segments + 1];
        double[] hs = new double[segments + 1];
        for (int i = 0; i <= segments; i++) {
            double t = (double) i / segments;
            xs[i] = tailX + (headX - tailX) * t;
            ys[i] = spine(t, amplitude, phase, wavelength);
            hs[i] = thickness.applyAsDouble(t);
        }
        Path2D path = new Path2D.Double();
        path.moveTo(xs[0], ys[0] - hs[0]);
        for (int i = 1; i <= segments; i++) {
            path.lineTo(xs[i], ys[i] - hs[i]);
        }
        for (int i = segments; i >= 0; i--) {
            path.lineTo(xs[i], ys[i] + hs[i]);
        }
        path.closePath();
        return path;
    }

    static DoubleUnaryOperator profile(double max, double peak, double snout) {
        return t -> {
            if (t <= peak) {
                double u = t / peak;
                return max * (0.12 + 0.88 * Math.sin(u * Math.PI / 2));
            }
            double u = (t - peak) / (1 - peak);
            return max * Math.sqrt(Math.max(0, 1 - u * u)) * (snout + (1 - snout) * (1 - u));
        };
    }
}
