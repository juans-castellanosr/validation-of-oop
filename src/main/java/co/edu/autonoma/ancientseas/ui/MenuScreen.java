package co.edu.autonoma.ancientseas.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

final class MenuScreen extends JPanel {

    private static final long serialVersionUID = 1L;

    private final transient OceanRenderer renderer = new OceanRenderer();
    private final Timer animation;
    private final JButton first;
    private final JPanel card;
    private final JPanel intro;
    private final JPanel actions;
    private double time;

    MenuScreen(JButton... buttons) {
        super(new GridBagLayout());
        this.first = buttons[0];
        JPanel panel = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(Theme.NAVY.getRed(), Theme.NAVY.getGreen(), Theme.NAVY.getBlue(),
                        Theme.MENU_PANEL_ALPHA));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 32, 32);
                g2.setColor(new Color(140, 217, 242, 90));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 32, 32);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(Theme.px(20), Theme.px(40), Theme.px(22), Theme.px(40)));
        JPanel intro = column();
        intro.add(centered(new Emblem()));
        intro.add(centered(new Title()));
        intro.add(centered(UiFactory.label("Un océano prehistórico del Cretácico tardío para explorar", Theme.BODY,
                Theme.SKY)));
        intro.add(Box.createVerticalStrut(Theme.px(14)));
        intro.add(centered(new HowToPlay()));
        JPanel actions = column();
        for (int i = 0; i < buttons.length; i++) {
            JButton b = buttons[i];
            b.setMaximumSize(new Dimension(Theme.px(340), Theme.px(48)));
            b.setPreferredSize(new Dimension(Theme.px(340), Theme.px(48)));
            actions.add(centered(b));
            actions.add(Box.createVerticalStrut(Theme.px(6)));
            int previous = (i - 1 + buttons.length) % buttons.length;
            int next = (i + 1) % buttons.length;
            moveFocusWith(b, KeyEvent.VK_UP, buttons[previous]);
            moveFocusWith(b, KeyEvent.VK_DOWN, buttons[next]);
        }
        panel.add(intro);
        panel.add(Box.createRigidArea(new Dimension(Theme.px(16), Theme.px(16))));
        panel.add(actions);
        arrange(panel, intro, actions, true);
        add(panel);
        this.card = panel;
        this.intro = intro;
        this.actions = actions;
        animation = new Timer(33, e -> {
            time += 0.033;
            repaint();
        });
    }

    private static JPanel column() {
        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        return column;
    }

    private static void arrange(JPanel panel, JPanel intro, JPanel actions, boolean stacked) {
        int axis = stacked ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS;
        if (panel.getLayout() instanceof BoxLayout current && current.getAxis() == axis) {
            return;
        }
        panel.setLayout(new BoxLayout(panel, axis));
        intro.setAlignmentY(CENTER_ALIGNMENT);
        actions.setAlignmentY(CENTER_ALIGNMENT);
        panel.revalidate();
    }

    private static void moveFocusWith(JButton from, int key, JButton to) {
        String name = "focus-" + key;
        from.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(key, 0), name);
        from.getActionMap().put(name, new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                to.requestFocusInWindow();
            }
        });
    }

    @Override
    public void doLayout() {
        java.awt.Insets insets = card.getInsets();
        int stacked = insets.top + insets.bottom + intro.getPreferredSize().height + Theme.px(16)
                + actions.getPreferredSize().height;
        arrange(card, intro, actions, stacked <= getHeight() - Theme.px(24));
        super.doLayout();
    }

    void startAnimation() {
        animation.start();
        SwingUtilities.invokeLater(first::requestFocusInWindow);
    }

    void stopAnimation() {
        animation.stop();
    }

    void setReducedMotion(boolean reduced) {
        renderer.setReducedMotion(reduced);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            renderer.paint(g2, getWidth(), getHeight(), null, time, 0.033, null);
        } finally {
            g2.dispose();
        }
    }

    private static JComponent centered(JComponent c) {
        c.setAlignmentX(CENTER_ALIGNMENT);
        return c;
    }

    private static final class Title extends JPanel {

        private static final long serialVersionUID = 1L;
        private static final String TEXT = "Ancient Seas";

        Title() {
            setOpaque(false);
            Dimension size = new Dimension(Theme.px(460), Theme.px(66));
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
            getAccessibleContext().setAccessibleName(TEXT);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setFont(Theme.TITLE);
                int x = (getWidth() - g.getFontMetrics().stringWidth(TEXT)) / 2;
                int y = g.getFontMetrics().getAscent();
                g.setColor(new Color(0, 0, 0, 120));
                g.drawString(TEXT, x + 3, y + 3);
                g.setColor(Color.WHITE);
                g.drawString(TEXT, x, y);
            } finally {
                g.dispose();
            }
        }
    }

    private static final class Emblem extends JComponent {

        private static final long serialVersionUID = 1L;

        Emblem() {
            Dimension size = new Dimension(Theme.px(70), Theme.px(64));
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                double cx = getWidth() / 2.0;
                double cy = getHeight() / 2.0;
                g.setStroke(new BasicStroke((float) (3 * Theme.SCALE)));
                double unit = Theme.SCALE;
                g.setColor(Theme.SAND);
                g.fill(new Ellipse2D.Double(cx - 28 * unit, cy - 28 * unit, 56 * unit, 56 * unit));
                g.setColor(new Color(140, 90, 50));
                for (double k = 24 * unit; k > 4 * unit; k -= 6 * unit) {
                    g.draw(new Arc2D.Double(cx - k, cy - k, 2 * k, 2 * k, 90, 300, Arc2D.OPEN));
                }
            } finally {
                g.dispose();
            }
        }
    }

    private static final class HowToPlay extends JPanel {

        private static final long serialVersionUID = 1L;
        private static final String[] STEPS = {"Come plancton y animales pequeños", "Escapa de los grandes",
            "Sube a respirar"};

        HowToPlay() {
            setOpaque(false);
            Dimension size = new Dimension(Theme.px(560), Theme.px(80));
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
            getAccessibleContext().setAccessibleName("Cómo jugar: " + String.join(", ", STEPS)
                    + ". Crece hasta ser adulta para ganar.");
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setFont(Theme.font(java.awt.Font.BOLD, 14));
                int column = getWidth() / STEPS.length;
                for (int i = 0; i < STEPS.length; i++) {
                    double cx = column * i + column / 2.0;
                    drawSymbol(g, i, cx, Theme.px(16));
                    g.setColor(Color.WHITE);
                    String[] lines = wrap(STEPS[i]);
                    for (int l = 0; l < lines.length; l++) {
                        int w = g.getFontMetrics().stringWidth(lines[l]);
                        g.drawString(lines[l], (float) (cx - w / 2.0), Theme.px(46) + l * Theme.px(18));
                    }
                }
            } finally {
                g.dispose();
            }
        }

        private static String[] wrap(String text) {
            if (text.length() <= 22) {
                return new String[] {text};
            }
            int cut = text.lastIndexOf(' ', 22);
            return new String[] {text.substring(0, cut), text.substring(cut + 1)};
        }

        private static void drawSymbol(Graphics2D g, int step, double cx, double cy) {
            switch (step) {
                case 0 -> {
                    g.setColor(Theme.GROWTH);
                    for (int i = -1; i <= 1; i++) {
                        g.fill(new Ellipse2D.Double(cx + i * 11 - 4, cy - 4 + Math.abs(i) * 4, 8, 8));
                    }
                }
                case 1 -> {
                    g.setColor(Theme.LIFE);
                    Path2D arrow = new Path2D.Double();
                    arrow.moveTo(cx - 14, cy - 3);
                    arrow.lineTo(cx + 4, cy - 3);
                    arrow.lineTo(cx + 4, cy - 10);
                    arrow.lineTo(cx + 16, cy);
                    arrow.lineTo(cx + 4, cy + 10);
                    arrow.lineTo(cx + 4, cy + 3);
                    arrow.lineTo(cx - 14, cy + 3);
                    arrow.closePath();
                    g.fill(arrow);
                }
                default -> {
                    g.setColor(Theme.AIR);
                    g.setStroke(new BasicStroke(2.2f));
                    g.draw(new Ellipse2D.Double(cx - 12, cy - 4, 10, 10));
                    g.draw(new Ellipse2D.Double(cx, cy - 12, 13, 13));
                    g.draw(new Ellipse2D.Double(cx + 6, cy + 4, 7, 7));
                }
            }
        }
    }
}
