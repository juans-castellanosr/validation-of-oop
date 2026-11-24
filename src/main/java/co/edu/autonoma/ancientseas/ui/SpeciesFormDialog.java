package co.edu.autonoma.ancientseas.ui;

import co.edu.autonoma.ancientseas.catalog.Behavior;
import co.edu.autonoma.ancientseas.catalog.BodyShape;
import co.edu.autonoma.ancientseas.catalog.Diet;
import co.edu.autonoma.ancientseas.catalog.DisplayNamed;
import co.edu.autonoma.ancientseas.catalog.Species;
import co.edu.autonoma.ancientseas.catalog.Zone;
import co.edu.autonoma.ancientseas.error.AncientSeasException;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

final class SpeciesFormDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JTextField id = new JTextField(18);
    private final JTextField scientificName = new JTextField(18);
    private final JTextField commonName = new JTextField(18);
    private final JTextField period = new JTextField(18);
    private final JTextField youngestMa = new JTextField(8);
    private final JTextField oldestMa = new JTextField(8);
    private final JTextField length = new JTextField(8);
    private final JComboBox<Diet> diet = combo(Diet.values());
    private final JComboBox<Zone> zone = combo(Zone.values());
    private final JComboBox<Behavior> behavior = combo(Behavior.values());
    private final JComboBox<BodyShape> shape = combo(BodyShape.values());
    private final JCheckBox airBreather = new JCheckBox("Respira aire");
    private final JTextArea funFact = new JTextArea(3, 36);
    private final JTextField source = new JTextField(36);
    private final JLabel error = UiFactory.label(" ", Theme.SMALL, Theme.CORAL);
    private transient Species result;

    SpeciesFormDialog(Window owner, Optional<Species> existing) {
        super(owner, existing.isPresent() ? "Editar especie" : "Nueva especie", ModalityType.APPLICATION_MODAL);
        existing.ifPresent(this::fill);
        funFact.setLineWrap(true);
        funFact.setFont(Theme.BODY);
        source.setFont(Theme.BODY);
        funFact.setWrapStyleWord(true);
        airBreather.setOpaque(false);

        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 4));
        grid.setOpaque(false);
        field(grid, "Id (minúsculas y guiones)", id);
        field(grid, "Tamaño adulto (metros)", length);
        field(grid, "Nombre científico", scientificName);
        field(grid, "Alimentación", diet);
        field(grid, "Nombre común", commonName);
        field(grid, "Zona", zone);
        field(grid, "Periodo", period);
        field(grid, "Comportamiento", behavior);
        JPanel ages = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        ages.setOpaque(false);
        ages.add(youngestMa);
        ages.add(new JLabel("a"));
        ages.add(oldestMa);
        field(grid, "Edad (millones de años): desde y hasta", ages);
        field(grid, "Forma del dibujo", shape);
        grid.add(airBreather);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.PALE);
        content.setBorder(BorderFactory.createEmptyBorder(18, 24, 14, 24));
        content.add(left(UiFactory.label(getTitle(), Theme.HEADING, Theme.NAVY)));
        content.add(left(grid));
        content.add(left(UiFactory.label("Dato curioso (para niños, verificado)", Theme.SMALL, Theme.GREY)));
        content.add(left(new JScrollPane(funFact)));
        content.add(left(UiFactory.label("Fuente científica", Theme.SMALL, Theme.GREY)));
        content.add(left(source));
        content.add(left(error));
        JButton cancel = UiFactory.button("Cancelar", Theme.GREY);
        JButton save = UiFactory.button("Guardar", Theme.GREEN);
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> trySave());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(cancel);
        actions.add(save);
        content.add(left(actions));
        UiFactory.focusWhenShown(id);
        setContentPane(content);
        UiFactory.closeOnEscape(this);
        UiFactory.packWithinScreen(this, actions);
        setLocationRelativeTo(owner);
    }

    Optional<Species> ask() {
        setVisible(true);
        return Optional.ofNullable(result);
    }

    private void fill(Species s) {
        id.setText(s.id());
        scientificName.setText(s.scientificName());
        commonName.setText(s.commonName());
        period.setText(s.period());
        youngestMa.setText(Double.toString(s.ageMa().min()));
        oldestMa.setText(Double.toString(s.ageMa().max()));
        length.setText(Double.toString(s.lengthMeters()));
        diet.setSelectedItem(s.diet());
        zone.setSelectedItem(s.zone());
        behavior.setSelectedItem(s.behavior());
        shape.setSelectedItem(s.shape());
        airBreather.setSelected(s.airBreather());
        funFact.setText(s.funFact());
        source.setText(s.source());
    }

    private void trySave() {
        try {
            result = Species.builder().id(id.getText()).scientificName(scientificName.getText())
                    .commonName(commonName.getText()).period(period.getText())
                    .ageMa(number(youngestMa, "edad más reciente"), number(oldestMa, "edad más antigua"))
                    .lengthMeters(number(length, "tamaño")).diet((Diet) diet.getSelectedItem())
                    .zone((Zone) zone.getSelectedItem()).behavior((Behavior) behavior.getSelectedItem())
                    .shape((BodyShape) shape.getSelectedItem()).airBreather(airBreather.isSelected())
                    .funFact(funFact.getText()).source(source.getText()).build();
            dispose();
        } catch (AncientSeasException e) {
            error.setText(e.getFriendlyMessage());
        } catch (NumberFormatException e) {
            error.setText(e.getMessage());
        }
    }

    private static double number(JTextField field, String name) {
        String text = field.getText().strip().replace(',', '.');
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("El dato \"" + name + "\" debe ser un número, por ejemplo 4.5");
        }
    }

    private static javax.swing.JComponent left(javax.swing.JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        return c;
    }

    private static void field(JPanel grid, String label, java.awt.Component input) {
        input.setFont(Theme.BODY);
        JPanel cell = new JPanel(new GridLayout(2, 1));
        cell.setOpaque(false);
        cell.add(UiFactory.label(label, Theme.SMALL, Theme.GREY));
        cell.add(input);
        grid.add(cell);
    }

    private static <E extends Enum<E>> JComboBox<E> combo(E[] values) {
        JComboBox<E> box = new JComboBox<>(values);
        box.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean selected, boolean focus) {
                return super.getListCellRendererComponent(list, value instanceof DisplayNamed named ? named.displayName() : value, index, selected, focus);
            }
        });
        return box;
    }
}
