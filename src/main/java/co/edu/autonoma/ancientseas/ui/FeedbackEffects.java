package co.edu.autonoma.ancientseas.ui;

import co.edu.autonoma.ancientseas.game.EntityView;
import co.edu.autonoma.ancientseas.game.GameSnapshot;
import co.edu.autonoma.ancientseas.game.GameStatus;
import co.edu.autonoma.ancientseas.model.GrowthStage;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

final class FeedbackEffects {

    private static final double TEXT_SECONDS = 1.2;
    private static final double RING_SECONDS = 1.4;
    private static final double BUBBLE_SECONDS = 1.0;
    private static final double BITE_SECONDS = 0.7;
    private static final double BREATH_INTERVAL = 0.18;

    private static final class Effect {
        private final Kind kind;
        private final double x;
        private final double y;
        private final double duration;
        private final String text;
        private final double dx;
        private final double dy;
        private double age;

        Effect(Kind kind, double x, double y, double duration, String text) {
            this(kind, x, y, duration, text, 0, 0);
        }

        Effect(Kind kind, double x, double y, double duration, String text, double dx, double dy) {
            this.kind = kind;
            this.x = x;
            this.y = y;
            this.duration = duration;
            this.text = text;
            this.dx = dx;
            this.dy = dy;
        }

        double progress() {
            return Math.min(1, age / duration);
        }
    }

    private enum Kind {
        GAIN_TEXT, GROWTH_RING, BREATH_BUBBLE, CRUMB, SPARKLE
    }

    private static final int MAX_EFFECTS = 60;

    private final List<Effect> effects = new ArrayList<>();
    private GameSnapshot previous;
    private double biteSecondsLeft;
    private double breathTimer;
    private boolean reducedMotion;

    void setReducedMotion(boolean reduced) {
        this.reducedMotion = reduced;
    }

    void reset() {
        effects.clear();
        previous = null;
        biteSecondsLeft = 0;
    }

    void update(GameSnapshot current, double dt) {
        GameSnapshot before = previous;
        previous = current;
        biteSecondsLeft = Math.max(0, biteSecondsLeft - dt);
        for (Iterator<Effect> it = effects.iterator(); it.hasNext();) {
            Effect e = it.next();
            e.age += dt;
            if (e.age >= e.duration) {
                it.remove();
            }
        }
        if (before == null || current.status() != GameStatus.RUNNING) {
            return;
        }
        EntityView player = player(current);
        if (player == null) {
            return;
        }
        int gained = (int) Math.round((current.growth() - before.growth()) * GrowthStage.ADULT.pointsRequired());
        if (gained > 0) {
            effects.add(new Effect(Kind.GAIN_TEXT, player.x(), player.y() - player.radius() - 8, TEXT_SECONDS,
                    "+" + gained));
            double mouth = player.x() + (player.facingLeft() ? -1 : 1) * player.radius();
            for (int i = 0; i < 6; i++) {
                double angle = i * Math.PI / 3;
                add(new Effect(Kind.CRUMB, mouth, player.y(), 0.5, null, Math.cos(angle) * 40, Math.sin(angle) * 40));
            }
        }
        if (current.health() < before.health() - 0.01) {
            biteSecondsLeft = BITE_SECONDS;
        }
        if (current.stage() != before.stage()) {
            effects.add(new Effect(Kind.GROWTH_RING, player.x(), player.y(), RING_SECONDS, null));
            for (int i = 0; i < 8; i++) {
                double angle = i * Math.PI / 4;
                add(new Effect(Kind.SPARKLE, player.x() + Math.cos(angle) * player.radius(),
                        player.y() + Math.sin(angle) * player.radius(), 1.3, null, Math.cos(angle) * 12, -30));
            }
        }
        breathTimer -= dt;
        if (current.oxygen() > before.oxygen() && current.oxygen() < 1 && breathTimer <= 0) {
            breathTimer = BREATH_INTERVAL;
            effects.add(new Effect(Kind.BREATH_BUBBLE, player.x() + player.radius() * 0.8, player.y() - 4,
                    BUBBLE_SECONDS, null));
        }
    }

    private void add(Effect effect) {
        if (effects.size() < MAX_EFFECTS) {
            effects.add(effect);
        }
    }

    double playerHurt() {
        return biteSecondsLeft / BITE_SECONDS;
    }

    private static EntityView player(GameSnapshot snapshot) {
        for (EntityView view : snapshot.creatures()) {
            if (view.player()) {
                return view;
            }
        }
        return null;
    }

    void paintInOcean(Graphics2D g) {
        Composite saved = g.getComposite();
        for (Effect e : effects) {
            float alpha = (float) (1 - e.progress());
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, alpha)));
            double rise = reducedMotion ? 0 : e.progress();
            switch (e.kind) {
                case GAIN_TEXT -> {
                    g.setFont(Theme.BODY_BOLD.deriveFont(24f));
                    g.setColor(new Color(10, 38, 77, 170));
                    g.drawString(e.text, (float) e.x + 2, (float) (e.y - rise * 36) + 2);
                    g.setColor(Theme.FOCUS);
                    g.drawString(e.text, (float) e.x, (float) (e.y - rise * 36));
                }
                case GROWTH_RING -> {
                    double r = reducedMotion ? 60 : 30 + e.progress() * 90;
                    g.setColor(Theme.GROWTH);
                    g.setStroke(new BasicStroke(4f));
                    g.draw(new Ellipse2D.Double(e.x - r, e.y - r, 2 * r, 2 * r));
                }
                case CRUMB -> {
                    double travel = reducedMotion ? 0 : e.progress();
                    g.setColor(new Color(225, 255, 210));
                    g.fill(new Ellipse2D.Double(e.x + e.dx * travel - 2, e.y + e.dy * travel - 2, 4, 4));
                }
                case SPARKLE -> {
                    double travel = reducedMotion ? 0 : e.progress();
                    double sx = e.x + e.dx * travel;
                    double sy = e.y + e.dy * travel;
                    g.setColor(Theme.FOCUS);
                    g.setStroke(new BasicStroke(2f));
                    g.draw(new java.awt.geom.Line2D.Double(sx - 5, sy, sx + 5, sy));
                    g.draw(new java.awt.geom.Line2D.Double(sx, sy - 5, sx, sy + 5));
                }
                case BREATH_BUBBLE -> {
                    double size = 6 + 4 * e.progress();
                    g.setColor(Color.WHITE);
                    g.setStroke(new BasicStroke(1.6f));
                    g.draw(new Ellipse2D.Double(e.x - size / 2, e.y - rise * 40 - size / 2, size, size));
                }
            }
        }
        g.setComposite(saved);
    }

    void paintOnScreen(Graphics2D g, int width, int height) {
        if (biteSecondsLeft <= 0) {
            return;
        }
        int alpha = (int) (110 * biteSecondsLeft / BITE_SECONDS);
        float radius = Math.max(width, height) * 0.75f;
        g.setPaint(new RadialGradientPaint(new Point2D.Float(width / 2f, height / 2f), radius,
                new float[] {0.55f, 1f}, new Color[] {new Color(200, 30, 20, 0), new Color(200, 30, 20, alpha)},
                MultipleGradientPaint.CycleMethod.NO_CYCLE));
        g.fillRect(0, 0, width, height);
    }
}
