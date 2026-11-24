package co.edu.autonoma.ancientseas.ui;

import co.edu.autonoma.ancientseas.catalog.Species;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;

final class SpeciesInfoDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final int TEXT_WIDTH = Theme.px(560);

    SpeciesInfoDialog(Window owner, Species species, String coexistenceNote) {
        super(owner, species.commonName(), ModalityType.APPLICATION_MODAL);
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.PALE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 26, 20, 26));

        content.add(left(UiFactory.label(species.commonName(), Theme.font(java.awt.Font.BOLD, 24), Theme.NAVY)));
        content.add(left(UiFactory.label("<html><i>" + species.scientificName() + "</i> ("
                + species.shape().displayName() + ")</html>", Theme.BODY, Theme.SEA)));
        content.add(Box.createVerticalStrut(10));
        content.add(left(new Portrait(species)));
        content.add(left(section("Datos")));

        JPanel facts = new JPanel(new GridLayout(0, 2, 10, 6));
        facts.setOpaque(false);
        addRow(facts, "Periodo", species.period());
        addRow(facts, "Hace", String.format("%.1f a %.1f millones de años", species.ageMa().max(), species.ageMa().min()));
        addRow(facts, "Tamaño adulto", String.format("unos %.1f metros", species.lengthMeters()));
        addRow(facts, "Alimentación", species.diet().displayName());
        addRow(facts, "Respira aire", species.airBreather() ? "Sí, subía a la superficie" : "No");
        content.add(left(facts));
        content.add(left(section("En el juego")));
        JPanel game = new JPanel(new GridLayout(0, 2, 10, 6));
        game.setOpaque(false);
        addRow(game, "Nada en", species.zone().displayName());
        addRow(game, "Comportamiento", species.behavior().displayName());
        content.add(left(game));
        content.add(left(wrapped("Estos dos datos se eligieron para el juego; los fósiles no siempre permiten "
                + "saberlos con certeza.", Theme.SMALL, Theme.PALE, 0)));
        content.add(left(section("¿Sabías que...?")));

        JTextArea fact = wrapped(species.funFact(), Theme.BODY, Theme.SAND, 10);
        content.add(left(fact));
        content.add(left(section("Fuente y contexto")));
        content.add(left(wrapped(species.source(), Theme.SMALL, Theme.PALE, 0)));
        content.add(Box.createVerticalStrut(6));
        content.add(left(wrapped(coexistenceNote, Theme.SMALL, Theme.PALE, 0)));

        JButton close = UiFactory.button("Cerrar", Theme.SEA);
        close.addActionListener(e -> dispose());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(close);
        content.add(left(actions));

        setContentPane(content);
        UiFactory.closeOnEscape(this);
        getRootPane().setDefaultButton(close);
        UiFactory.packWithinScreen(this, actions);
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private static JComponent section(String title) {
        javax.swing.JLabel label = UiFactory.label(title, Theme.SECTION, Theme.SEA);
        label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(12, 0, 6, 0),
                BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.SKY),
                        BorderFactory.createEmptyBorder(0, 0, 3, 0))));
        label.setMaximumSize(new java.awt.Dimension(TEXT_WIDTH, 60));
        label.setPreferredSize(new java.awt.Dimension(TEXT_WIDTH, label.getPreferredSize().height));
        return label;
    }

    private static void addRow(JPanel grid, String label, String value) {
        grid.add(UiFactory.label(label, Theme.BODY_BOLD, Theme.GREY));
        grid.add(UiFactory.label(value, Theme.BODY, Theme.INK));
    }

    private static JTextArea wrapped(String text, java.awt.Font font, java.awt.Color background, int padding) {
        JTextArea area = new JTextArea(text);
        area.setFont(font);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(true);
        area.setBackground(background);
        area.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        area.setForeground(Theme.INK);
        area.setSize(TEXT_WIDTH, Short.MAX_VALUE);
        area.setPreferredSize(new java.awt.Dimension(TEXT_WIDTH, area.getPreferredSize().height));
        return area;
    }

    private static JComponent left(JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        return c;
    }

    private static final class Portrait extends JComponent {

        private static final long serialVersionUID = 1L;
        private final transient Species species;

        Portrait(Species species) {
            this.species = species;
            Dimension size = new Dimension(TEXT_WIDTH, Theme.px(130));
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setPaint(new GradientPaint(0, 0, Theme.WATER_TOP, 0, getHeight(), Theme.SEA.darker()));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g.translate(getWidth() / 2.0, getHeight() / 2.0);
                CreatureArt.painterFor(species.shape()).paint(g, CreaturePose.still(Theme.px(44)));
            } finally {
                g.dispose();
            }
        }
    }
}
