package co.edu.autonoma.ancientseas.ui;

import co.edu.autonoma.ancientseas.catalog.Diet;
import co.edu.autonoma.ancientseas.catalog.DisplayNamed;
import co.edu.autonoma.ancientseas.catalog.Species;
import co.edu.autonoma.ancientseas.catalog.SpeciesCatalog;
import co.edu.autonoma.ancientseas.catalog.Zone;
import co.edu.autonoma.ancientseas.error.DuplicateSpeciesException;
import co.edu.autonoma.ancientseas.game.GameSession;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

final class CatalogAdminDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final String ALL = "Todas";

    private final transient SpeciesCatalog catalog;
    private final transient List<String> pendingChanges = new ArrayList<>();
    private final transient CatalogAdminActions actions;
    private final SpeciesTableModel model;
    private final JTable table;
    private final transient TableRowSorter<SpeciesTableModel> sorter;
    private final JTextField search = new JTextField(14);
    private final JComboBox<Object> zoneFilter = new JComboBox<>(withAll(Zone.values()));
    private final JComboBox<Object> dietFilter = new JComboBox<>(withAll(Diet.values()));
    private boolean unsaved;

    CatalogAdminDialog(Window owner, SpeciesCatalog catalog, List<String> loadProblems, CatalogAdminActions actions) {
        super(owner, "Catálogo de especies", ModalityType.APPLICATION_MODAL);
        this.catalog = catalog;
        this.actions = actions;
        this.model = new SpeciesTableModel(catalog);
        this.table = new JTable(model);
        this.sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableStyle.apply(table);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.setOpaque(false);
        filters.add(UiFactory.label("Buscar:", Theme.BODY_BOLD, Theme.GREY));
        filters.add(search);
        filters.add(UiFactory.label("Zona:", Theme.BODY_BOLD, Theme.GREY));
        filters.add(zoneFilter);
        filters.add(UiFactory.label("Alimentación:", Theme.BODY_BOLD, Theme.GREY));
        filters.add(dietFilter);
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilters();
            }
        });
        for (javax.swing.JComponent input : new javax.swing.JComponent[] {search, zoneFilter, dietFilter}) {
            input.setFont(Theme.BODY);
        }
        zoneFilter.addActionListener(e -> applyFilters());
        dietFilter.addActionListener(e -> applyFilters());
        javax.swing.ListCellRenderer<? super Object> base = zoneFilter.getRenderer();
        javax.swing.ListCellRenderer<Object> named = (list, value, index, selected, focus) ->
                base.getListCellRendererComponent(list,
                        value instanceof DisplayNamed n ? n.displayName() : value, index, selected, focus);
        zoneFilter.setRenderer(named);
        dietFilter.setRenderer(named);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(UiFactory.label("Catálogo de especies", Theme.HEADING, Theme.NAVY), BorderLayout.NORTH);
        north.add(filters, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        if (!loadProblems.isEmpty()) {
            JTextArea problems = new JTextArea(String.join("\n", loadProblems), Math.min(4, loadProblems.size()), 60);
            problems.setEditable(false);
            problems.setForeground(Theme.CORAL);
            problems.setFont(Theme.SMALL);
            JPanel box = new JPanel(new BorderLayout());
            box.setOpaque(false);
            box.add(UiFactory.label("Avisos al cargar el CSV. Se omitieron esas filas. Al guardar, primero se hace una copia del archivo (.bak):", Theme.SMALL,
                    Theme.CORAL), BorderLayout.NORTH);
            box.add(new JScrollPane(problems), BorderLayout.CENTER);
            south.add(box, BorderLayout.NORTH);
        }
        south.add(buttons(), BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(Theme.PALE);
        content.setBorder(BorderFactory.createEmptyBorder(18, 22, 14, 22));
        content.add(north, BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(south, BorderLayout.SOUTH);
        setContentPane(content);
        UiFactory.closeOnEscape(this);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
        java.awt.Rectangle usable = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int wanted = TableStyle.fitColumnsToContent(table) + Theme.px(60);
        setSize(Math.min(Math.max(wanted, Theme.px(1040)), (int) (usable.width * 0.95)),
                Math.min(Theme.px(600), (int) (usable.height * 0.92)));
        setLocationRelativeTo(owner);
    }

    private JPanel buttons() {
        JButton add = UiFactory.button("Nueva especie", Theme.GREEN);
        JButton edit = UiFactory.button("Editar", Theme.SEA);
        JButton remove = UiFactory.button("Eliminar", Theme.CORAL);
        JButton save = UiFactory.button("Guardar catálogo", Theme.SEA);
        JButton pin = UiFactory.button("Cambiar PIN", Theme.GREY);
        JButton close = UiFactory.button("Cerrar", Theme.GREY);
        add.addActionListener(e -> addSpecies());
        edit.addActionListener(e -> selected().ifPresent(this::editSpecies));
        remove.addActionListener(e -> selected().ifPresent(this::removeSpecies));
        save.addActionListener(e -> save());
        pin.addActionListener(e -> changePin());
        close.addActionListener(e -> close());
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setOpaque(false);
        for (JButton b : List.of(add, edit, remove, save, pin, close)) {
            panel.add(b);
        }
        return panel;
    }

    private void applyFilters() {
        String text = search.getText().strip().toLowerCase();
        Object zone = zoneFilter.getSelectedItem();
        Object diet = dietFilter.getSelectedItem();
        Predicate<Species> matches = s -> text.isEmpty() || s.commonName().toLowerCase().contains(text)
                || s.scientificName().toLowerCase().contains(text) || s.id().contains(text);
        Predicate<Species> inZone = s -> ALL.equals(zone) || s.zone() == zone;
        Predicate<Species> eats = s -> ALL.equals(diet) || s.diet() == diet;
        Predicate<Species> all = matches.and(inZone).and(eats);
        sorter.setRowFilter(new RowFilter<SpeciesTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends SpeciesTableModel, ? extends Integer> entry) {
                return all.test(entry.getModel().speciesAt(entry.getIdentifier()));
            }
        });
    }

    private Optional<Species> selected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Primero elija una especie de la tabla.", "Catálogo",
                    JOptionPane.INFORMATION_MESSAGE);
            return Optional.empty();
        }
        return Optional.of(model.speciesAt(table.convertRowIndexToModel(row)));
    }

    private void addSpecies() {
        Optional<Species> created = new SpeciesFormDialog(this, Optional.empty()).ask();
        while (created.isPresent()) {
            try {
                catalog.add(created.get());
                changed("Species added: " + created.get().id());
                warnIfFromAnotherTime(created.get());
                return;
            } catch (DuplicateSpeciesException e) {
                JOptionPane.showMessageDialog(this, e.getFriendlyMessage(), "Catálogo", JOptionPane.WARNING_MESSAGE);
                created = new SpeciesFormDialog(this, created).ask();
            }
        }
    }

    private void editSpecies(Species original) {
        Optional<Species> edited = new SpeciesFormDialog(this, Optional.of(original)).ask();
        if (edited.isEmpty()) {
            return;
        }
        if (original.id().equals(GameSession.PLAYER_SPECIES_ID) && !edited.get().id().equals(original.id())) {
            JOptionPane.showMessageDialog(this, "El id de la especie del jugador no se puede cambiar.", "Catálogo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            catalog.replace(original.id(), edited.get());
            changed("Species edited: " + original.id() + " -> " + edited.get().id());
            warnIfFromAnotherTime(edited.get());
        } catch (DuplicateSpeciesException e) {
            JOptionPane.showMessageDialog(this, e.getFriendlyMessage(), "Catálogo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void removeSpecies(Species species) {
        if (species.id().equals(GameSession.PLAYER_SPECIES_ID)) {
            JOptionPane.showMessageDialog(this, "No se puede eliminar la especie que controlan los visitantes.",
                    "Catálogo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean delete = UiFactory.confirm(this, "Eliminar especie", "¿Eliminar \"" + species.commonName()
                + "\" del catálogo? Podrá deshacerlo cerrando sin guardar.", "Eliminar", "Cancelar");
        if (delete && catalog.remove(species.id())) {
            changed("Species removed: " + species.id());
        }
    }

    private void changed(String description) {
        unsaved = true;
        pendingChanges.add(description);
        model.reload();
    }

    private void warnIfFromAnotherTime(Species species) {
        SpeciesCatalog others = new SpeciesCatalog(catalog);
        others.remove(species.id());
        String warning = CoexistenceNote.warningFor(others, species);
        if (!warning.isEmpty()) {
            JOptionPane.showMessageDialog(this, warning, "Especie de otra época", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private boolean save() {
        try {
            actions.saveCatalog(catalog);
            pendingChanges.forEach(actions::recordChange);
            pendingChanges.clear();
            unsaved = false;
            JOptionPane.showMessageDialog(this, "Catálogo guardado.", "Catálogo", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (IOException e) {
            actions.recordError("Could not save the catalog", e);
            JOptionPane.showMessageDialog(this, "No se pudo guardar el catálogo. Revise que el archivo no esté abierto "
                    + "en otro programa.", "Catálogo", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void changePin() {
        JPasswordField first = UiFactory.focusWhenShown(new JPasswordField(12));
        JPasswordField second = new JPasswordField(12);
        Object[] form = {"Nuevo PIN (4 a 12 números):", first, "Repítelo:", second};
        if (JOptionPane.showConfirmDialog(this, form, "Cambiar PIN", JOptionPane.OK_CANCEL_OPTION)
                != JOptionPane.OK_OPTION) {
            return;
        }
        char[] a = first.getPassword();
        char[] b = second.getPassword();
        try {
            if (!java.util.Arrays.equals(a, b)) {
                JOptionPane.showMessageDialog(this, "Los dos PIN no coinciden.", "Cambiar PIN",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            actions.changePin(a);
            JOptionPane.showMessageDialog(this, "PIN cambiado.", "Cambiar PIN", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "El PIN debe tener de 4 a 12 números.", "Cambiar PIN",
                    JOptionPane.WARNING_MESSAGE);
        } catch (IOException e) {
            actions.recordError("Could not save the new administration PIN", e);
            JOptionPane.showMessageDialog(this, "No se pudo guardar el nuevo PIN.", "Cambiar PIN",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            java.util.Arrays.fill(a, '\0');
            java.util.Arrays.fill(b, '\0');
        }
    }

    private void close() {
        if (unsaved) {
            Object[] options = {"Guardar y cerrar", "Cerrar sin guardar", "Seguir editando"};
            int answer = JOptionPane.showOptionDialog(this, "Hay cambios sin guardar en el catálogo.", "Catálogo",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (answer == 2 || answer == JOptionPane.CLOSED_OPTION) {
                return;
            }
            if (answer == 0 && !save()) {
                return;
            }
            if (answer == 1) {
                actions.recordChange("Catalog changes discarded without saving: " + pendingChanges.size());
            }
        }
        dispose();
    }

    private static Object[] withAll(Object[] values) {
        List<Object> items = new ArrayList<>();
        items.add(ALL);
        items.addAll(List.of(values));
        return items.toArray();
    }

    private static final class SpeciesTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 1L;
        private static final String[] COLUMNS = {"Id", "Nombre común", "Nombre científico", "Tamaño (m)", "Zona",
            "Alimentación", "Comportamiento"};

        private final transient SpeciesCatalog catalog;
        private transient List<Species> rows;

        SpeciesTableModel(SpeciesCatalog catalog) {
            this.catalog = catalog;
            this.rows = catalog.all();
        }

        void reload() {
            rows = catalog.all();
            fireTableDataChanged();
        }

        Species speciesAt(int row) {
            return rows.get(row);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == 3 ? Double.class : String.class;
        }

        @Override
        public Object getValueAt(int row, int column) {
            Species s = rows.get(row);
            return switch (column) {
                case 0 -> s.id();
                case 1 -> s.commonName();
                case 2 -> s.scientificName();
                case 3 -> s.lengthMeters();
                case 4 -> s.zone().displayName();
                case 5 -> s.diet().displayName();
                default -> s.behavior().displayName();
            };
        }
    }
}
