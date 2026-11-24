package co.edu.autonoma.ancientseas.ui;

import co.edu.autonoma.ancientseas.catalog.BodyShape;
import co.edu.autonoma.ancientseas.game.GameStatus;
import co.edu.autonoma.ancientseas.game.ScoreEntry;
import co.edu.autonoma.ancientseas.model.GrowthStage;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.Timer;

final class ResultsDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    enum Choice {
        MENU,
        PLAY_AGAIN
    }

    private Choice choice = Choice.MENU;

    ResultsDialog(Window owner, ScoreEntry result, Collection<ScoreEntry> best, Runnable onExport,
            boolean reducedMotion) {
        super(owner, "Resultados", ModalityType.APPLICATION_MODAL);
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.PALE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 26, 16, 26));
        content.add(left(new Banner(result.outcome(), result.stage(), reducedMotion)));
        content.add(Box.createVerticalStrut(12));
        content.add(left(UiFactory.label(title(result.outcome()), Theme.font(java.awt.Font.BOLD, 26), Theme.NAVY)));
        content.add(left(UiFactory.label("<html><div style='width:" + Theme.px(560) + "px'>" + subtitle(result.outcome())
                + "</div></html>", Theme.BODY, Theme.INK)));
        content.add(Box.createVerticalStrut(10));
        JPanel facts = new JPanel(new GridLayout(0, 2, 10, 4));
        facts.setOpaque(false);
        int seconds = (int) result.durationSeconds();
        String[][] rows = {{"Puntos", String.format("%,d", result.score())},
            {"Etapa alcanzada", result.stage().displayName()},
            {"Tiempo", String.format("%02d:%02d", seconds / 60, seconds % 60)},
            {"Dificultad", result.difficulty().displayName()}};
        for (String[] row : rows) {
            facts.add(UiFactory.label(row[0], Theme.BODY_BOLD, Theme.GREY));
            facts.add(UiFactory.label(row[1], Theme.BODY, Theme.INK));
        }
        content.add(left(facts));
        content.add(Box.createVerticalStrut(12));
        content.add(left(UiFactory.label("Mejores puntuaciones", Theme.BODY_BOLD, Theme.NAVY)));
        content.add(left(LeaderboardTable.of(best)));

        JButton menu = UiFactory.button("Menú", Theme.GREY);
        JButton export = UiFactory.button("Exportar informe", Theme.SEA);
        JButton again = UiFactory.button("Jugar otra vez", Theme.GREEN);
        menu.addActionListener(e -> dispose());
        export.addActionListener(e -> onExport.run());
        again.addActionListener(e -> {
            choice = Choice.PLAY_AGAIN;
            dispose();
        });
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(menu);
        actions.add(export);
        actions.add(again);
        content.add(left(actions));
        setContentPane(content);
        UiFactory.closeOnEscape(this);
        getRootPane().setDefaultButton(again);
        UiFactory.packWithinScreen(this, actions);
        setLocationRelativeTo(owner);
    }

    Choice ask() {
        setVisible(true);
        return choice;
    }

    private static String title(GameStatus outcome) {
        return switch (outcome) {
            case WON -> "¡Lo lograste!";
            case LOST -> "¡Casi lo logras!";
            default -> "Partida terminada";
        };
    }

    private static String subtitle(GameStatus outcome) {
        return switch (outcome) {
            case WON -> "Tu Archelon creció hasta ser adulta. ¡Eres un gran explorador del Cretácico!";
            case LOST -> "Tu Archelon no sobrevivió esta vez. Consejo: vigila la barra de Aire y sube antes de que se "
                    + "acabe. Los cazadores que ya comieron no te persiguen.";
            default -> "Terminaste la partida antes de tiempo. ¡Vuelve cuando quieras!";
        };
    }

    private static final class Banner extends JComponent {

        private static final long serialVersionUID = 1L;
        private static final double CELEBRATION_SECONDS = 2.0;

        private final GameStatus outcome;
        private final GrowthStage stage;
        private final Timer timer;
        private double time;

        Banner(GameStatus outcome, GrowthStage stage, boolean reducedMotion) {
            this.outcome = outcome;
            this.stage = stage;
            Dimension size = new Dimension(Theme.px(640), Theme.px(120));
            setPreferredSize(size);
            setMaximumSize(size);
            timer = new Timer(33, e -> {
                time += 0.033;
                repaint();
                if (time >= CELEBRATION_SECONDS) {
                    ((Timer) e.getSource()).stop();
                }
            });
            if (outcome == GameStatus.WON && !reducedMotion) {
                timer.start();
            } else {
                time = CELEBRATION_SECONDS;
            }
        }

        @Override
        public void removeNotify() {
            timer.stop();
            super.removeNotify();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color top = outcome == GameStatus.WON ? Theme.WATER_TOP : new Color(60, 110, 150);
                g.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), Theme.NAVY));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                if (outcome == GameStatus.WON) {
                    Random seeded = new Random(7);
                    g.setColor(new Color(255, 255, 255, 170));
                    g.setStroke(new BasicStroke(2f));
                    for (int i = 0; i < 18; i++) {
                        double x = 20 + seeded.nextDouble() * (getWidth() - 40);
                        double startY = getHeight() + seeded.nextDouble() * 60;
                        double y = startY - (time / CELEBRATION_SECONDS) * (startY + 10) * (0.6 + seeded.nextDouble() * 0.4);
                        double r = 4 + seeded.nextDouble() * 6;
                        g.draw(new Ellipse2D.Double(x, y, r, r));
                    }
                }
                double radius = Theme.px(16) + Theme.px(26) * (stage.ordinal() + 1) / 4.0;
                g.translate(getWidth() / 2.0, getHeight() / 2.0);
                CreatureArt.painterFor(BodyShape.TURTLE).paint(g, new CreaturePose(radius, time * 4, time, 0));
            } finally {
                g.dispose();
            }
        }
    }

    private static JComponent left(JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        return c;
    }
}
