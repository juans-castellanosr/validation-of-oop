package co.edu.autonoma.ancientseas.ui;

import co.edu.autonoma.ancientseas.game.Difficulty;
import co.edu.autonoma.ancientseas.game.EventFrequency;
import co.edu.autonoma.ancientseas.game.GameSettings;
import co.edu.autonoma.ancientseas.game.InterfaceSize;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

final class SettingsDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JTextField name;
    private final JComboBox<Difficulty> difficulty = new JComboBox<>(Difficulty.values());
    private final JComboBox<EventFrequency> events = new JComboBox<>(EventFrequency.values());
    private final JComboBox<InterfaceSize> size = new JComboBox<>(InterfaceSize.values());
    private final JCheckBox reducedMotion = new JCheckBox("Movimiento reducido");
    private final JSlider creatures;
    private final JLabel difficultyHelp = UiFactory.label(" ", Theme.SMALL, Theme.GREY);
    private final JLabel error = UiFactory.label(" ", Theme.SMALL, Theme.CORAL);
    private transient GameSettings result;

    SettingsDialog(Window owner, GameSettings current) {
        super(owner, "Ajustes", ModalityType.APPLICATION_MODAL);
        name = UiFactory.focusWhenShown(new JTextField(current.playerName(), 18));
        difficulty.setSelectedItem(current.difficulty());
        events.setSelectedItem(current.eventFrequency());
        size.setSelectedItem(current.interfaceSize());
        reducedMotion.setSelected(current.reducedMotion());
        reducedMotion.setOpaque(false);
        reducedMotion.setFont(Theme.BODY);
        creatures = new JSlider(GameSettings.CREATURE_COUNT.min(), GameSettings.CREATURE_COUNT.max(),
                current.creatureCount());
        creatures.setMajorTickSpacing(15);
        creatures.setPaintTicks(true);
        creatures.setPaintLabels(true);
        creatures.setOpaque(false);
        JLabel creaturesLabel = UiFactory.label("Número de criaturas al empezar: " + current.creatureCount(),
                Theme.BODY_BOLD, Theme.GREY);
        creaturesLabel.setLabelFor(creatures);
        creatures.addChangeListener(e -> creaturesLabel.setText("Número de criaturas al empezar: "
                + creatures.getValue()));
        difficulty.addActionListener(e -> explainDifficulty());
        explainDifficulty();
        for (JComponent input : new JComponent[] {name, difficulty, events, size, creatures}) {
            input.setFont(Theme.BODY);
        }

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.PALE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 28, 16, 28));
        add(content, UiFactory.label("Ajustes", Theme.HEADING, Theme.NAVY));

        section(content, "Jugador");
        add(content, labelFor("Tu nombre (aparece en las puntuaciones)", name));
        add(content, name);

        section(content, "Partida");
        add(content, labelFor("Dificultad", difficulty));
        add(content, difficulty);
        add(content, difficultyHelp);
        add(content, Box.createVerticalStrut(6));
        add(content, creaturesLabel);
        add(content, creatures);
        add(content, labelFor("Eventos del océano (corrientes, agua fría, comida, depredadores)", events));
        add(content, events);

        section(content, "Accesibilidad");
        add(content, reducedMotion);
        add(content, UiFactory.label("Quita los rayos de luz, las burbujas y otros movimientos decorativos.",
                Theme.SMALL, Theme.GREY));
        add(content, Box.createVerticalStrut(6));
        add(content, labelFor("Tamaño de la interfaz", size));
        add(content, size);
        add(content, UiFactory.label("Se aplica la próxima vez que abras el juego.", Theme.SMALL, Theme.GREY));

        add(content, Box.createVerticalStrut(8));
        add(content, error);
        JButton cancel = UiFactory.button("Cancelar", Theme.GREY);
        JButton save = UiFactory.button("Guardar", Theme.GREEN);
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> trySave());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(cancel);
        actions.add(save);
        add(content, actions);
        setContentPane(content);
        UiFactory.closeOnEscape(this);
        getRootPane().setDefaultButton(save);
        UiFactory.packWithinScreen(this, actions);
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void explainDifficulty() {
        Difficulty chosen = (Difficulty) difficulty.getSelectedItem();
        difficultyHelp.setText(switch (chosen) {
            case EASY -> "Menos cazadores y más lentos. Ideal para empezar.";
            case NORMAL -> "El equilibrio pensado para el museo.";
            case HARD -> "Más cazadores y más rápidos. Da más puntos.";
        });
    }

    private static void section(JPanel content, String title) {
        add(content, Box.createVerticalStrut(14));
        JLabel label = UiFactory.label(title, Theme.SECTION, Theme.SEA);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.SKY), BorderFactory.createEmptyBorder(0, 0, 4, 0)));
        add(content, label);
        add(content, Box.createVerticalStrut(6));
    }

    private static JLabel labelFor(String text, JComponent target) {
        JLabel label = UiFactory.label(text, Theme.BODY_BOLD, Theme.GREY);
        label.setLabelFor(target);
        return label;
    }

    private static void add(JPanel content, java.awt.Component c) {
        if (c instanceof JComponent jc) {
            jc.setAlignmentX(LEFT_ALIGNMENT);
        }
        content.add(c);
    }

    private void trySave() {
        String typed = name.getText().strip();
        if (typed.isEmpty() || typed.length() > GameSettings.MAX_NAME_LENGTH) {
            error.setText("Escribe un nombre de 1 a " + GameSettings.MAX_NAME_LENGTH + " letras.");
            name.requestFocusInWindow();
            return;
        }
        result = new GameSettings((Difficulty) difficulty.getSelectedItem(), creatures.getValue(),
                (EventFrequency) events.getSelectedItem(), typed, reducedMotion.isSelected(),
                (InterfaceSize) size.getSelectedItem());
        dispose();
    }

    Optional<GameSettings> ask() {
        setVisible(true);
        return Optional.ofNullable(result);
    }
}
