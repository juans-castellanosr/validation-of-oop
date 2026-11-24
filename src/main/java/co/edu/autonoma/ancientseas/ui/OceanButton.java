package co.edu.autonoma.ancientseas.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.KeyStroke;

final class OceanButton extends JButton {

    private static final long serialVersionUID = 1L;
    private static final int MIN_HEIGHT = Theme.px(44);
    private static final int ARC = 14;

    private final Color base;

    OceanButton(String text, Color color) {
        super(text);
        this.base = color;
        setFont(Theme.BODY_BOLD);
        setForeground(Theme.WHITE);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setBorder(BorderFactory.createEmptyBorder(Theme.px(10), Theme.px(20), Theme.px(10), Theme.px(20)));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        InputMap keys = getInputMap(WHEN_FOCUSED);
        keys.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), "pressed");
        keys.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), "released");
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        return new Dimension(size.width, Math.max(MIN_HEIGHT, size.height));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            ButtonModel model = getModel();
            Color fill = !isEnabled() ? Theme.DISABLED_FILL
                    : model.isPressed() ? Theme.darker(Theme.darker(base))
                    : model.isRollover() || isFocusOwner() ? Theme.darker(base) : base;
            double inset = 4;
            RoundRectangle2D body = new RoundRectangle2D.Double(inset, inset, getWidth() - 2 * inset,
                    getHeight() - 2 * inset, ARC, ARC);
            g.setColor(fill);
            g.fill(body);
            if (isFocusOwner()) {
                g.setStroke(new BasicStroke(3f));
                g.setColor(Theme.FOCUS);
                g.draw(new RoundRectangle2D.Double(1.5, 1.5, getWidth() - 3, getHeight() - 3, ARC + 4, ARC + 4));
                g.setColor(Theme.NAVY);
                g.setStroke(new BasicStroke(1f));
                g.draw(new RoundRectangle2D.Double(inset, inset, getWidth() - 2 * inset - 1, getHeight() - 2 * inset - 1,
                        ARC, ARC));
            }
            g.setFont(getFont());
            g.setColor(isEnabled() ? getForeground() : Theme.DISABLED_TEXT);
            FontMetrics fm = g.getFontMetrics();
            String text = getText();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(text, x, y);
        } finally {
            g.dispose();
        }
    }
}
