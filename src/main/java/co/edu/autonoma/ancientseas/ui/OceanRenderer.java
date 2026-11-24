package co.edu.autonoma.ancientseas.ui;

import co.edu.autonoma.ancientseas.catalog.BodyShape;
import co.edu.autonoma.ancientseas.game.EntityView;
import co.edu.autonoma.ancientseas.game.GameSnapshot;
import co.edu.autonoma.ancientseas.game.GameStatus;
import co.edu.autonoma.ancientseas.model.Vector2;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

final class OceanRenderer {

    private static final double MAX_TILT = Math.toRadians(25);
    private static final double TURN_RATE = 5.5;
    private static final double TILT_RATE = 4.0;
    private static final Color FOOD_GLOW_OUTER = new Color(220, 255, 200, 40);
    private static final Color FOOD_GLOW_INNER = new Color(220, 255, 200, 70);
    private static final Color FOOD_CORE = new Color(235, 255, 220, 240);

    private static final class Look {
        private double facing = 1;
        private double tilt;
        private double phase;
        private long seenFrame;
    }

    private final OceanScenery scenery = new OceanScenery();
    private final Map<Long, Look> looks = new HashMap<>();
    private boolean reducedMotion;
    private long frame;

    void setReducedMotion(boolean reduced) {
        this.reducedMotion = reduced;
    }

    void paint(Graphics2D g, int width, int height, GameSnapshot snapshot, double time, double dt,
            FeedbackEffects effects) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        scenery.paintBackground(g, width, height);
        double decorTime = reducedMotion ? 0 : time;
        double decorDt = reducedMotion ? 0 : dt;
        AffineTransform saved = g.getTransform();
        g.scale(width / OceanScenery.W, height / OceanScenery.H);
        scenery.paintBehind(g, decorTime);
        Vector2 current = snapshot == null ? Vector2.ZERO : snapshot.current();
        if (snapshot == null) {
            if (!reducedMotion) {
                paintPassingAnimals(g, time);
            }
        } else {
            if (!reducedMotion) {
                scenery.paintCurrent(g, current, time);
            }
            paintFood(g, snapshot.food(), time);
            frame++;
            double hurt = effects == null ? 0 : effects.playerHurt();
            for (EntityView view : snapshot.creatures()) {
                paintCreature(g, view, look(view, dt), time, view.player() ? hurt : 0);
            }
            forgetUnseen();
            if (effects != null) {
                effects.paintInOcean(g);
            }
            if (snapshot.cold()) {
                scenery.paintCold(g);
            }
        }
        scenery.paintFront(g, decorDt, current);
        g.setTransform(saved);
        if (effects != null) {
            effects.paintOnScreen(g, width, height);
        }
        if (snapshot != null && snapshot.status() == GameStatus.PAUSED) {
            paintPauseVeil(g, width, height);
        }
    }

    private Look look(EntityView view, double dt) {
        Look look = looks.computeIfAbsent(view.id(), id -> {
            Look fresh = new Look();
            fresh.facing = view.facingLeft() ? -1 : 1;
            fresh.phase = id * 1.7;
            return fresh;
        });
        look.seenFrame = frame;
        double speed = Math.hypot(view.vx(), view.vy());
        if (Math.abs(view.vx()) > 4) {
            double target = view.vx() < 0 ? -1 : 1;
            look.facing += Math.max(-TURN_RATE * dt, Math.min(TURN_RATE * dt, target - look.facing));
        }
        double targetTilt = speed < 4 ? 0
                : Math.max(-MAX_TILT, Math.min(MAX_TILT, Math.atan2(view.vy(), Math.abs(view.vx()) + 1e-6)));
        look.tilt += (targetTilt - look.tilt) * Math.min(1, TILT_RATE * dt);
        look.phase += dt * (2.2 + speed * 0.045);
        return look;
    }

    private void forgetUnseen() {
        for (Iterator<Look> it = looks.values().iterator(); it.hasNext();) {
            if (it.next().seenFrame != frame) {
                it.remove();
            }
        }
    }

    private static void paintFood(Graphics2D g, List<Vector2> food, double time) {
        for (Vector2 p : food) {
            double pulse = 1 + 0.15 * Math.sin(time * 3 + p.x() * 0.05);
            double glow = 7 * pulse;
            g.setColor(FOOD_GLOW_OUTER);
            g.fill(new Ellipse2D.Double(p.x() - glow, p.y() - glow, glow * 2, glow * 2));
            g.setColor(FOOD_GLOW_INNER);
            g.fill(new Ellipse2D.Double(p.x() - glow * 0.55, p.y() - glow * 0.55, glow * 1.1, glow * 1.1));
            g.setColor(FOOD_CORE);
            g.fill(new Ellipse2D.Double(p.x() - 2.5, p.y() - 2.5, 5, 5));
        }
    }

    private static void paintCreature(Graphics2D g, EntityView view, Look look, double time, double hurt) {
        AffineTransform saved = g.getTransform();
        g.translate(view.x(), view.y());
        double r = view.radius();
        Color halo = view.player() ? new Color(255, 214, 90, 150) : new Color(200, 235, 255, 36);
        g.setPaint(new RadialGradientPaint(new Point2D.Double(0, 0), (float) (r * 1.7), new float[] {0.35f, 1f},
                new Color[] {halo, new Color(halo.getRed(), halo.getGreen(), halo.getBlue(), 0)},
                MultipleGradientPaint.CycleMethod.NO_CYCLE));
        g.fill(new Ellipse2D.Double(-r * 1.7, -r * 1.7, r * 3.4, r * 3.4));
        if (view.player()) {
            paintPlayerRing(g, r, view.recovering());
        }
        Composite composite = g.getComposite();
        if (view.recovering()) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
        }
        double facing = Math.signum(look.facing) * Math.max(0.18, Math.abs(look.facing));
        g.scale(facing, 1);
        g.rotate(look.tilt);
        CreatureArt.painterFor(view.shape()).paint(g, new CreaturePose(r, look.phase, time + view.id(), hurt));
        g.setComposite(composite);
        if (view.hunting()) {
            g.setColor(new Color(255, 70, 50, 230));
            g.fill(new Ellipse2D.Double(r * 0.55, -r * 0.5, r * 0.16, r * 0.16));
        }
        g.setTransform(saved);
    }

    private static void paintPlayerRing(Graphics2D g, double r, boolean shielded) {
        double ring = r * 1.5;
        if (shielded) {
            g.setPaint(new RadialGradientPaint(new Point2D.Double(0, 0), (float) ring, new float[] {0.6f, 1f},
                    new Color[] {new Color(140, 217, 242, 0), new Color(140, 217, 242, 120)}));
            g.fill(new Ellipse2D.Double(-ring, -ring, ring * 2, ring * 2));
            g.setColor(new Color(170, 230, 250, 235));
            g.setStroke(new BasicStroke(4.5f));
        } else {
            g.setColor(new Color(255, 214, 90, 225));
            g.setStroke(new BasicStroke(3f));
        }
        g.draw(new Ellipse2D.Double(-ring, -ring, ring * 2, ring * 2));
    }

    private static void paintPassingAnimals(Graphics2D g, double time) {
        Composite saved = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
        passing(g, BodyShape.MOSASAUR, 34, 470, 26, time, 0, 0.6);
        passing(g, BodyShape.TURTLE, 30, 250, -34, time, 900, 0.9);
        for (int i = 0; i < 6; i++) {
            passing(g, BodyShape.BONY_FISH, 12, 330 + (i % 3) * 18 + i * 4, 48, time + i * 0.35, 300 + i * 22, 1.4);
        }
        passing(g, BodyShape.STRAIGHT_AMMONITE, 16, 600, -12, time, 500, 0.4);
        g.setComposite(saved);
    }

    private static void passing(Graphics2D g, BodyShape shape, double radius, double y, double speed, double time,
            double offset, double swim) {
        double span = OceanScenery.W + 400;
        double x = ((offset + time * speed) % span + span) % span - 200;
        AffineTransform saved = g.getTransform();
        g.translate(x, y + Math.sin(time * 0.7 + offset) * 12);
        if (speed < 0) {
            g.scale(-1, 1);
        }
        CreatureArt.painterFor(shape).paint(g, new CreaturePose(radius, time * (2 + swim * 2) + offset,
                time + offset, 0));
        g.setTransform(saved);
    }

    private static void paintPauseVeil(Graphics2D g, int width, int height) {
        g.setColor(new Color(0, 10, 30, 140));
        g.fillRect(0, 0, width, height);
        g.setColor(Color.WHITE);
        g.setFont(Theme.font(Font.BOLD, 40));
        String title = "Juego en pausa";
        int tw = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (width - tw) / 2, height / 2 - 10);
        g.setFont(Theme.BODY);
        String hint = "El océano te espera. Pulsa Espacio o el botón Seguir (P) para continuar.";
        int hw = g.getFontMetrics().stringWidth(hint);
        g.setColor(Theme.SKY);
        g.drawString(hint, (width - hw) / 2, height / 2 + Theme.px(26));
    }
}
