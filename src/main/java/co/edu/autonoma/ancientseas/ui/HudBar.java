package co.edu.autonoma.ancientseas.ui;

import co.edu.autonoma.ancientseas.game.GameSnapshot;
import co.edu.autonoma.ancientseas.model.GrowthStage;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JButton;
import javax.swing.JPanel;

final class HudBar extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int METER_WIDTH = 150;
    private static final int METER_GAP = 34;
    private static final double LOW = 0.3;
    private static final int CONTENT_WIDTH = 16 + 3 * (METER_WIDTH + METER_GAP) + 150;
    private static final java.awt.Font LABEL = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 14);
    private static final java.awt.Font SCORE = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 18);
    private static final java.awt.Font TIME = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 14);
    private static final java.awt.Font CAPTION = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 13);

    private transient GameSnapshot snapshot;

    HudBar(JButton pause, JButton save, JButton end) {
        super(null);
        setBackground(Theme.NAVY);
        for (JButton b : new JButton[] {pause, save, end}) {
            b.setFocusable(false);
            add(b);
        }
    }

    private static int rowHeight() {
        return Theme.px(70);
    }

    private int buttonsWidth() {
        int width = 0;
        for (java.awt.Component c : getComponents()) {
            width += c.getPreferredSize().width + Theme.px(8);
        }
        return width + Theme.px(8);
    }

    private boolean singleRow(int width) {
        return CONTENT_WIDTH * Theme.SCALE + buttonsWidth() <= width;
    }

    @Override
    public Dimension getPreferredSize() {
        int width = getParent() == null ? Theme.px(1280) : getParent().getWidth();
        int buttonsRow = getComponentCount() == 0 ? 0 : getComponent(0).getPreferredSize().height + Theme.px(12);
        return new Dimension(width, singleRow(width) ? rowHeight() : rowHeight() + buttonsRow);
    }

    @Override
    public void doLayout() {
        int gap = Theme.px(8);
        boolean oneRow = singleRow(getWidth());
        int x = getWidth() - gap;
        for (int i = getComponentCount() - 1; i >= 0; i--) {
            java.awt.Component c = getComponent(i);
            Dimension size = c.getPreferredSize();
            x -= size.width;
            int y = oneRow ? (rowHeight() - size.height) / 2 : rowHeight() + Theme.px(4);
            c.setBounds(x, y, size.width, size.height);
            x -= gap;
        }
    }

    void show(GameSnapshot latest) {
        this.snapshot = latest;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (snapshot == null) {
            return;
        }
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int available = singleRow(getWidth()) && getComponentCount() > 0 ? getComponent(0).getX() : getWidth();
            double scale = Math.min(Theme.SCALE, (available - 8.0) / CONTENT_WIDTH);
            g.scale(scale, scale);
            int x = 16;
            meter(g, x, Icon.HEART, "Vida", snapshot.health(), Theme.LIFE,
                    snapshot.health() < LOW ? new Caption("¡Cuidado!", true) : null);
            x += METER_WIDTH + METER_GAP;
            meter(g, x, Icon.BUBBLE, "Aire", snapshot.oxygen(), Theme.AIR,
                    snapshot.oxygen() < LOW ? new Caption("¡Sube a respirar!", true) : null);
            x += METER_WIDTH + METER_GAP;
            meter(g, x, Icon.SPROUT, snapshot.stage().displayName(), snapshot.growth(), Theme.GROWTH,
                    nextStage(snapshot.stage()));
            x += METER_WIDTH + METER_GAP;
            g.setColor(Color.WHITE);
            g.setFont(SCORE);
            g.drawString("Puntos: " + String.format("%,d", snapshot.score()), x, 30);
            g.setFont(TIME);
            int seconds = (int) snapshot.elapsedSeconds();
            g.drawString(String.format("Tiempo: %02d:%02d", seconds / 60, seconds % 60), x, 52);
        } finally {
            g.dispose();
        }
    }

    private static Caption nextStage(GrowthStage stage) {
        return stage == GrowthStage.ADULT ? new Caption("¡Adulto!", false)
                : new Caption("Siguiente: " + GrowthStage.values()[stage.ordinal() + 1].displayName(), false);
    }

    private record Caption(String text, boolean warning) {
    }

    private enum Icon {
        HEART, BUBBLE, SPROUT
    }

    private static void meter(Graphics2D g, int x, Icon icon, String label, double level, Color color,
            Caption caption) {
        drawIcon(g, icon, x + 9, 22, color);
        int percent = (int) Math.floor(Math.max(0, Math.min(1, level)) * 100 + 1e-9);
        g.setFont(LABEL);
        FontMetrics fm = g.getFontMetrics();
        String value = percent + " %";
        int valueWidth = fm.stringWidth(value);
        int labelSpace = METER_WIDTH - 24 - valueWidth - 8;
        g.setColor(Color.WHITE);
        g.drawString(fitted(label, fm, labelSpace), x + 24, 27);
        g.drawString(value, x + METER_WIDTH - valueWidth, 27);
        g.setColor(new Color(255, 255, 255, 60));
        g.fill(new RoundRectangle2D.Double(x, 36, METER_WIDTH, 12, 12, 12));
        g.setColor(color);
        g.fill(new RoundRectangle2D.Double(x, 36, METER_WIDTH * percent / 100.0, 12, 12, 12));
        if (caption == null) {
            return;
        }
        g.setFont(CAPTION);
        FontMetrics small = g.getFontMetrics();
        if (caption.warning()) {
            g.setColor(Theme.FOCUS);
            g.setStroke(new BasicStroke(2f));
            g.draw(new RoundRectangle2D.Double(x - 2, 34, METER_WIDTH + 4, 16, 14, 14));
            drawWarningShape(g, x + 6, 60);
            g.setFont(CAPTION);
            g.drawString(fitted(caption.text(), small, METER_WIDTH - 16), x + 16, 65);
        } else {
            g.setColor(Theme.SKY);
            g.drawString(fitted(caption.text(), small, METER_WIDTH), x, 65);
        }
    }

    private static String fitted(String text, FontMetrics fm, int width) {
        if (fm.stringWidth(text) <= width) {
            return text;
        }
        String shortened = text;
        while (shortened.length() > 1 && fm.stringWidth(shortened + "...") > width) {
            shortened = shortened.substring(0, shortened.length() - 1);
        }
        return shortened + "...";
    }

    private static void drawIcon(Graphics2D g, Icon icon, double cx, double cy, Color color) {
        g.setColor(color);
        switch (icon) {
            case HEART -> {
                Path2D heart = new Path2D.Double();
                heart.moveTo(cx, cy + 7);
                heart.curveTo(cx - 10, cy - 1, cx - 6, cy - 9, cx, cy - 4);
                heart.curveTo(cx + 6, cy - 9, cx + 10, cy - 1, cx, cy + 7);
                g.fill(heart);
            }
            case BUBBLE -> {
                g.setStroke(new BasicStroke(2.2f));
                g.draw(new Ellipse2D.Double(cx - 7, cy - 7, 14, 14));
                g.fill(new Ellipse2D.Double(cx - 3.5, cy - 4.5, 4, 4));
            }
            case SPROUT -> {
                g.setStroke(new BasicStroke(2.2f));
                g.draw(new java.awt.geom.Line2D.Double(cx, cy + 8, cx, cy - 2));
                g.fill(new Ellipse2D.Double(cx - 9, cy - 8, 9, 6));
                g.fill(new Ellipse2D.Double(cx, cy - 5, 9, 6));
            }
        }
    }

    private static void drawWarningShape(Graphics2D g, double cx, double cy) {
        Path2D triangle = new Path2D.Double();
        triangle.moveTo(cx, cy - 7);
        triangle.lineTo(cx + 7, cy + 6);
        triangle.lineTo(cx - 7, cy + 6);
        triangle.closePath();
        g.setColor(Theme.FOCUS);
        g.fill(triangle);
        g.setColor(Theme.NAVY);
        g.setFont(LABEL.deriveFont(10f));
        g.drawString("!", (float) cx - 1.5f, (float) cy + 5);
        g.setColor(Theme.FOCUS);
    }
}
