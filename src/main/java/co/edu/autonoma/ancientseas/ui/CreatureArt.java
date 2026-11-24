package co.edu.autonoma.ancientseas.ui;

import co.edu.autonoma.ancientseas.catalog.BodyShape;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.EnumMap;
import java.util.Map;

final class CreatureArt {

    private static final Map<BodyShape, CreaturePainter> PAINTERS = new EnumMap<>(BodyShape.class);

    static {
        PAINTERS.put(BodyShape.TURTLE, CreatureArt::turtle);
        PAINTERS.put(BodyShape.MOSASAUR, CreatureArt::mosasaur);
        PAINTERS.put(BodyShape.SHARK, CreatureArt::shark);
        PAINTERS.put(BodyShape.PLESIOSAUR, CreatureArt::plesiosaur);
        PAINTERS.put(BodyShape.BONY_FISH, CreatureArt::slenderFish);
        PAINTERS.put(BodyShape.PREDATORY_FISH, CreatureArt::predatoryFish);
        PAINTERS.put(BodyShape.AMMONITE, CreatureArt::coiledAmmonite);
        PAINTERS.put(BodyShape.STRAIGHT_AMMONITE, CreatureArt::straightAmmonite);
        PAINTERS.put(BodyShape.CLAM, CreatureArt::clam);
    }

    private CreatureArt() {
    }

    static CreaturePainter painterFor(BodyShape shape) {
        return PAINTERS.get(shape);
    }

    private static final BodyKit.Palette SHELL = new BodyKit.Palette(new Color(58, 78, 64), new Color(92, 118, 92),
            new Color(128, 150, 112));
    private static final BodyKit.Palette TURTLE_SKIN = new BodyKit.Palette(new Color(84, 112, 104),
            new Color(116, 146, 132), new Color(206, 204, 170));

    private static void turtle(Graphics2D g, CreaturePose p) {
        double r = p.radius();
        BodyKit.Palette shell = SHELL.hurt(p.hurt());
        BodyKit.Palette skin = TURTLE_SKIN.hurt(p.hurt());
        double stroke = Math.sin(p.phase());
        Color far = BodyKit.blend(skin.back(), new Color(20, 40, 60), 0.35);
        flipper(g, r * 0.32, r * 0.12, r * 1.05, r * 0.24, 0.55 + stroke * 0.55, far, skin.line(), r);
        flipper(g, -r * 0.62, r * 0.2, r * 0.45, r * 0.16, 2.3 - stroke * 0.25, far, skin.line(), r);

        Path2D neck = new Path2D.Double();
        neck.moveTo(r * 0.55, -r * 0.12);
        neck.quadTo(r * 0.85, -r * 0.2, r * 1.0, -r * 0.12);
        neck.lineTo(r * 1.0, r * 0.16);
        neck.quadTo(r * 0.8, r * 0.22, r * 0.55, r * 0.2);
        neck.closePath();
        BodyKit.shade(g, neck, skin, r);
        Path2D head = new Path2D.Double();
        head.moveTo(r * 0.9, -r * 0.16);
        head.curveTo(r * 1.15, -r * 0.3, r * 1.42, -r * 0.18, r * 1.46, r * 0.0);
        head.quadTo(r * 1.48, r * 0.1, r * 1.38, r * 0.12);
        head.quadTo(r * 1.15, r * 0.24, r * 0.92, r * 0.17);
        head.closePath();
        BodyKit.shade(g, head, skin, r);
        g.setColor(skin.line());
        g.setStroke(new BasicStroke((float) Math.max(0.8, r * 0.035)));
        g.draw(new java.awt.geom.QuadCurve2D.Double(r * 1.44, r * 0.04, r * 1.25, r * 0.08, r * 1.08, r * 0.04));

        Path2D carapace = new Path2D.Double();
        carapace.moveTo(r * 0.72, r * 0.12);
        carapace.curveTo(r * 0.62, -r * 0.62, -r * 0.55, -r * 0.7, -r * 1.02, r * 0.05);
        carapace.quadTo(-r * 0.4, r * 0.3, r * 0.72, r * 0.12);
        carapace.closePath();
        BodyKit.shade(g, carapace, shell, r);
        g.setColor(BodyKit.blend(shell.side(), shell.belly(), 0.35));
        g.setStroke(new BasicStroke((float) Math.max(0.7, r * 0.035), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 3; i++) {
            double lift = 0.14 + i * 0.15;
            g.draw(new java.awt.geom.CubicCurve2D.Double(r * 0.6, r * (0.1 - lift * 0.2), r * 0.45,
                    -r * (0.55 - lift), -r * 0.45, -r * (0.6 - lift), -r * 0.92, r * 0.02));
        }
        Path2D plastron = new Path2D.Double();
        plastron.moveTo(r * 0.7, r * 0.12);
        plastron.quadTo(-r * 0.2, r * 0.34, -r * 0.98, r * 0.06);
        plastron.quadTo(-r * 0.2, r * 0.22, r * 0.7, r * 0.12);
        g.setColor(skin.belly());
        g.fill(plastron);

        Color near = skin.side();
        flipper(g, r * 0.38, r * 0.16, r * 1.12, r * 0.26, 0.75 + stroke * 0.6, near, skin.line(), r);
        flipper(g, -r * 0.55, r * 0.22, r * 0.48, r * 0.17, 2.45 - stroke * 0.25, near, skin.line(), r);
        BodyKit.eye(g, r * 1.2, -r * 0.06, r * 0.075);
    }

    private static void flipper(Graphics2D g, double x, double y, double length, double width, double angle,
            Color color, Color line, double r) {
        AffineTransform saved = g.getTransform();
        g.translate(x, y);
        g.rotate(angle);
        Path2D paddle = new Path2D.Double();
        paddle.moveTo(0, -width * 0.5);
        paddle.curveTo(length * 0.4, -width * 0.9, length * 0.85, -width * 0.45, length, 0);
        paddle.curveTo(length * 0.8, width * 0.35, length * 0.35, width * 0.6, 0, width * 0.5);
        paddle.closePath();
        BodyKit.fin(g, paddle, color, line, r);
        g.setTransform(saved);
    }

    private static final BodyKit.Palette MOSASAUR = new BodyKit.Palette(new Color(48, 62, 78),
            new Color(84, 100, 116), new Color(200, 204, 198));

    private static void mosasaur(Graphics2D g, CreaturePose p) {
        double r = p.radius();
        BodyKit.Palette c = MOSASAUR.hurt(p.hurt());
        double amp = r * 0.16;
        double wave = 2.4;
        double tailY = BodyKit.spine(0, amp, p.phase(), wave);
        Color finColor = BodyKit.blend(c.back(), c.side(), 0.3);
        Path2D fluke = new Path2D.Double();
        fluke.moveTo(-r * 1.45, tailY);
        fluke.quadTo(-r * 1.75, tailY - r * 0.12, -r * 1.85, tailY - r * 0.28);
        fluke.quadTo(-r * 1.72, tailY + r * 0.05, -r * 1.95, tailY + r * 0.5);
        fluke.quadTo(-r * 1.62, tailY + r * 0.3, -r * 1.45, tailY + r * 0.06);
        fluke.closePath();
        BodyKit.fin(g, fluke, finColor, c.line(), r);
        flipper(g, -r * 0.55, r * 0.18, r * 0.42, r * 0.14, 1.9 + Math.sin(p.phase()) * 0.2, finColor, c.line(), r);
        Path2D body = BodyKit.spineBody(-r * 1.55, r * 1.55, BodyKit.profile(r * 0.33, 0.55, 0.15), amp, p.phase(),
                wave);
        BodyKit.shade(g, body, c, r);
        flipper(g, r * 0.35, r * 0.2, r * 0.5, r * 0.16, 1.1 + Math.sin(p.phase() + 1) * 0.25, finColor, c.line(), r);
        g.setColor(c.line());
        g.setStroke(new BasicStroke((float) Math.max(0.8, r * 0.035), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new java.awt.geom.Line2D.Double(r * 1.0, r * 0.06, r * 1.52, r * 0.02));
        BodyKit.eye(g, r * 1.1, -r * 0.08, r * 0.06);
    }

    private static final BodyKit.Palette SHARK = new BodyKit.Palette(new Color(74, 92, 110), new Color(112, 130, 146),
            new Color(226, 230, 232));

    private static void shark(Graphics2D g, CreaturePose p) {
        double r = p.radius();
        BodyKit.Palette c = SHARK.hurt(p.hurt());
        double amp = r * 0.18;
        double wave = 2.0;
        double tailY = BodyKit.spine(0, amp, p.phase(), wave);
        Color finColor = BodyKit.blend(c.back(), c.side(), 0.2);
        Path2D tail = new Path2D.Double();
        tail.moveTo(-r * 1.15, tailY - r * 0.06);
        tail.quadTo(-r * 1.4, tailY - r * 0.35, -r * 1.55, tailY - r * 0.68);
        tail.quadTo(-r * 1.38, tailY - r * 0.1, -r * 1.42, tailY);
        tail.quadTo(-r * 1.38, tailY + r * 0.1, -r * 1.5, tailY + r * 0.55);
        tail.quadTo(-r * 1.32, tailY + r * 0.25, -r * 1.15, tailY + r * 0.06);
        tail.closePath();
        BodyKit.fin(g, tail, finColor, c.line(), r);
        Path2D dorsal = new Path2D.Double();
        dorsal.moveTo(r * 0.05, -r * 0.3);
        dorsal.quadTo(-r * 0.12, -r * 0.62, -r * 0.3, -r * 0.82);
        dorsal.quadTo(-r * 0.32, -r * 0.5, -r * 0.48, -r * 0.24);
        dorsal.closePath();
        BodyKit.fin(g, dorsal, finColor, c.line(), r);
        Path2D body = BodyKit.spineBody(-r * 1.2, r * 1.3, BodyKit.profile(r * 0.36, 0.55, 0.3), amp, p.phase(), wave);
        BodyKit.shade(g, body, c, r);
        Path2D pectoral = new Path2D.Double();
        pectoral.moveTo(r * 0.35, r * 0.18);
        pectoral.quadTo(r * 0.1, r * 0.48, -r * 0.18, r * 0.62);
        pectoral.quadTo(r * 0.02, r * 0.32, r * 0.1, r * 0.18);
        pectoral.closePath();
        BodyKit.fin(g, pectoral, finColor, c.line(), r);
        g.setColor(BodyKit.blend(c.side(), Color.BLACK, 0.35));
        g.setStroke(new BasicStroke((float) Math.max(0.6, r * 0.025)));
        for (int i = 0; i < 3; i++) {
            double x = r * (0.62 - i * 0.09);
            g.draw(new java.awt.geom.Line2D.Double(x, -r * 0.08, x - r * 0.03, r * 0.12));
        }
        BodyKit.eye(g, r * 0.92, -r * 0.1, r * 0.055);
    }

    private static final BodyKit.Palette PLESIO = new BodyKit.Palette(new Color(80, 92, 70), new Color(116, 128, 98),
            new Color(198, 194, 156));

    private static void plesiosaur(Graphics2D g, CreaturePose p) {
        double r = p.radius();
        BodyKit.Palette c = PLESIO.hurt(p.hurt());
        double stroke = Math.sin(p.phase());
        Color far = BodyKit.blend(c.side(), new Color(20, 40, 60), 0.35);
        flipper(g, r * 0.22, r * 0.12, r * 0.85, r * 0.2, 0.5 + stroke * 0.5, far, c.line(), r);
        flipper(g, -r * 0.5, r * 0.14, r * 0.75, r * 0.18, 2.2 - stroke * 0.5, far, c.line(), r);
        double sway = Math.sin(p.time() * 1.1) * r * 0.12;
        Path2D neck = new Path2D.Double();
        neck.moveTo(r * 0.35, -r * 0.18);
        neck.curveTo(r * 0.9, -r * 0.45 + sway, r * 1.3, -r * 0.55 + sway, r * 1.72, -r * 0.42 + sway);
        neck.lineTo(r * 1.74, -r * 0.3 + sway);
        neck.curveTo(r * 1.3, -r * 0.38 + sway, r * 0.9, -r * 0.24 + sway, r * 0.42, r * 0.08);
        neck.closePath();
        BodyKit.shade(g, neck, c, r);
        Path2D body = BodyKit.spineBody(-r * 1.05, r * 0.6, BodyKit.profile(r * 0.36, 0.6, 0.8), r * 0.04, p.phase(),
                1.0);
        BodyKit.shade(g, body, c, r);
        Path2D head = new Path2D.Double();
        head.moveTo(r * 1.66, -r * 0.46 + sway);
        head.quadTo(r * 1.85, -r * 0.52 + sway, r * 2.02, -r * 0.4 + sway);
        head.quadTo(r * 1.88, -r * 0.27 + sway, r * 1.68, -r * 0.28 + sway);
        head.closePath();
        BodyKit.shade(g, head, c, r);
        flipper(g, r * 0.3, r * 0.16, r * 0.9, r * 0.22, 0.7 + stroke * 0.55, c.side(), c.line(), r);
        flipper(g, -r * 0.45, r * 0.18, r * 0.8, r * 0.2, 2.35 - stroke * 0.55, c.side(), c.line(), r);
        BodyKit.eye(g, r * 1.86, -r * 0.42 + sway, r * 0.045);
    }

    private static final BodyKit.Palette SILVER = new BodyKit.Palette(new Color(84, 116, 146), new Color(150, 176, 196),
            new Color(232, 238, 240));
    private static final BodyKit.Palette DEEP_BLUE = new BodyKit.Palette(new Color(38, 62, 96), new Color(96, 128, 158),
            new Color(218, 226, 230));

    private static void slenderFish(Graphics2D g, CreaturePose p) {
        double r = p.radius();
        BodyKit.Palette c = SILVER.hurt(p.hurt());
        double amp = r * 0.2;
        double wave = 2.6;
        double tailY = BodyKit.spine(0, amp, p.phase(), wave);
        Color finColor = BodyKit.blend(c.back(), c.side(), 0.5);
        forkedTail(g, -r * 1.0, tailY, r * 0.55, finColor, c.line(), r);
        Path2D dorsal = new Path2D.Double();
        dorsal.moveTo(-r * 0.05, -r * 0.27);
        dorsal.quadTo(-r * 0.2, -r * 0.55, -r * 0.4, -r * 0.5);
        dorsal.quadTo(-r * 0.35, -r * 0.32, -r * 0.42, -r * 0.2);
        dorsal.closePath();
        BodyKit.fin(g, dorsal, finColor, c.line(), r);
        Path2D body = BodyKit.spineBody(-r * 1.05, r * 1.15, BodyKit.profile(r * 0.3, 0.55, 0.5), amp, p.phase(), wave);
        BodyKit.shade(g, body, c, r);
        g.setColor(new Color(255, 255, 255, 120));
        g.setStroke(new BasicStroke((float) Math.max(0.6, r * 0.025)));
        g.draw(new java.awt.geom.QuadCurve2D.Double(-r * 0.8, BodyKit.spine(0.1, amp, p.phase(), wave), 0, -r * 0.02,
                r * 0.8, 0));
        g.setColor(new Color(245, 245, 235));
        Path2D fang = new Path2D.Double();
        fang.moveTo(r * 1.0, r * 0.06);
        fang.lineTo(r * 1.06, r * 0.26);
        fang.lineTo(r * 1.11, r * 0.06);
        fang.closePath();
        g.fill(fang);
        BodyKit.eye(g, r * 0.82, -r * 0.06, r * 0.11);
    }

    private static void predatoryFish(Graphics2D g, CreaturePose p) {
        double r = p.radius();
        BodyKit.Palette c = DEEP_BLUE.hurt(p.hurt());
        double amp = r * 0.15;
        double wave = 2.2;
        double tailY = BodyKit.spine(0, amp, p.phase(), wave);
        Color finColor = BodyKit.blend(c.back(), c.side(), 0.3);
        forkedTail(g, -r * 1.12, tailY, r * 0.72, finColor, c.line(), r);
        Path2D body = BodyKit.spineBody(-r * 1.15, r * 1.25, BodyKit.profile(r * 0.46, 0.6, 0.75), amp, p.phase(),
                wave);
        BodyKit.shade(g, body, c, r);
        Path2D chin = new Path2D.Double();
        chin.moveTo(r * 0.9, r * 0.2);
        chin.quadTo(r * 1.18, r * 0.16, r * 1.3, -r * 0.06);
        chin.quadTo(r * 1.16, r * 0.04, r * 0.9, r * 0.12);
        chin.closePath();
        g.setColor(c.belly());
        g.fill(chin);
        g.setColor(c.line());
        g.setStroke(new BasicStroke((float) Math.max(0.8, r * 0.04), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new java.awt.geom.QuadCurve2D.Double(r * 0.92, r * 0.1, r * 1.12, r * 0.06, r * 1.3, -r * 0.08));
        Path2D pectoral = new Path2D.Double();
        pectoral.moveTo(r * 0.45, r * 0.18);
        pectoral.quadTo(r * 0.2, r * 0.48, -r * 0.05, r * 0.52);
        pectoral.quadTo(r * 0.12, r * 0.3, r * 0.25, r * 0.16);
        pectoral.closePath();
        BodyKit.fin(g, pectoral, finColor, c.line(), r);
        BodyKit.eye(g, r * 0.9, -r * 0.14, r * 0.09);
    }

    private static void forkedTail(Graphics2D g, double x, double y, double size, Color color, Color line, double r) {
        Path2D tail = new Path2D.Double();
        tail.moveTo(x + size * 0.15, y);
        tail.quadTo(x - size * 0.35, y - size * 0.25, x - size * 0.7, y - size * 0.85);
        tail.quadTo(x - size * 0.45, y, x - size * 0.7, y + size * 0.85);
        tail.quadTo(x - size * 0.35, y + size * 0.25, x + size * 0.15, y);
        tail.closePath();
        BodyKit.fin(g, tail, color, line, r);
    }

    private static final BodyKit.Palette AMMONITE = new BodyKit.Palette(new Color(150, 104, 62),
            new Color(206, 160, 104), new Color(236, 208, 160));

    private static void coiledAmmonite(Graphics2D g, CreaturePose p) {
        double r = p.radius();
        BodyKit.Palette c = AMMONITE.hurt(p.hurt());
        tentacles(g, r, p.time(), r * 0.55, c);
        Ellipse2D shell = new Ellipse2D.Double(-r * 0.75, -r * 0.75, r * 1.5, r * 1.5);
        BodyKit.shade(g, shell, c, r);
        g.setColor(BodyKit.blend(c.back(), Color.BLACK, 0.25));
        g.setStroke(new BasicStroke((float) Math.max(0.8, r * 0.06)));
        for (double k = 0.6; k > 0.1; k -= 0.17) {
            g.draw(new Arc2D.Double(-r * k, -r * k, 2 * r * k, 2 * r * k, 90, 300, Arc2D.OPEN));
        }
        BodyKit.eye(g, r * 0.68, 0, r * 0.07);
    }

    private static void straightAmmonite(Graphics2D g, CreaturePose p) {
        double r = p.radius();
        BodyKit.Palette c = AMMONITE.hurt(p.hurt());
        double bob = Math.sin(p.time() * 1.3) * r * 0.03;
        tentacles(g, r, p.time(), r * 0.82, c);
        Path2D shell = new Path2D.Double();
        shell.moveTo(r * 0.85, -r * 0.24 + bob);
        shell.lineTo(-r * 1.25, -r * 0.1 + bob);
        shell.quadTo(-r * 1.4, bob, -r * 1.25, r * 0.1 + bob);
        shell.lineTo(r * 0.85, r * 0.24 + bob);
        shell.quadTo(r * 0.95, bob, r * 0.85, -r * 0.24 + bob);
        shell.closePath();
        BodyKit.shade(g, shell, c, r);
        g.setColor(BodyKit.blend(c.back(), Color.BLACK, 0.25));
        g.setStroke(new BasicStroke((float) Math.max(0.6, r * 0.035), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (double x = -r * 1.05; x < r * 0.75; x += r * 0.22) {
            double half = r * (0.11 + 0.13 * (x + r * 1.25) / (r * 2.1));
            Path2D suture = new Path2D.Double();
            suture.moveTo(x, -half + bob);
            suture.quadTo(x + r * 0.06, bob, x, half + bob);
            g.draw(suture);
        }
        BodyKit.eye(g, r * 0.9, bob, r * 0.07);
    }

    private static void tentacles(Graphics2D g, double r, double time, double from, BodyKit.Palette c) {
        g.setColor(BodyKit.blend(c.belly(), new Color(220, 140, 130), 0.5));
        g.setStroke(new BasicStroke((float) Math.max(0.8, r * 0.07), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = -2; i <= 2; i++) {
            double sway = Math.sin(time * 2.2 + i * 0.9) * r * 0.12;
            g.draw(new java.awt.geom.QuadCurve2D.Double(from, i * r * 0.06, from + r * 0.3, i * r * 0.12 + sway,
                    from + r * 0.55, i * r * 0.17 - sway * 0.5));
        }
    }

    private static final BodyKit.Palette CLAM = new BodyKit.Palette(new Color(122, 110, 98), new Color(168, 154, 136),
            new Color(196, 184, 164));

    private static void clam(Graphics2D g, CreaturePose p) {
        double r = p.radius();
        BodyKit.Palette c = CLAM.hurt(p.hurt());
        g.setPaint(new RadialGradientPaint(new Point2D.Double(0, r * 0.35), (float) (r * 1.2), new float[] {0f, 1f},
                new Color[] {new Color(40, 30, 20, 90), new Color(40, 30, 20, 0)}));
        g.fill(new Ellipse2D.Double(-r * 1.2, r * 0.05, r * 2.4, r * 0.6));
        double gape = 0.04 + 0.05 * (1 + Math.sin(p.time() * 0.7));
        Arc2D lower = new Arc2D.Double(-r, -r * (0.25 + gape), 2 * r, 0.6 * r, 180, 180, Arc2D.CHORD);
        BodyKit.shade(g, lower, new BodyKit.Palette(c.side(), c.back(), c.back()), r);
        Arc2D upper = new Arc2D.Double(-r, -r * 0.72, 2 * r, 1.4 * r, 0, 180, Arc2D.CHORD);
        BodyKit.shade(g, upper, c, r);
        g.setColor(BodyKit.blend(c.back(), Color.BLACK, 0.25));
        g.setStroke(new BasicStroke((float) Math.max(0.7, r * 0.04)));
        for (double k = 0.3; k < 1.0; k += 0.16) {
            g.draw(new Arc2D.Double(-r * k, -r * 0.72 * k, 2 * r * k, 1.4 * r * k, 0, 180, Arc2D.OPEN));
        }
    }
}
