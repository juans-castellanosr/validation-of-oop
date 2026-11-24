package co.edu.autonoma.ancientseas.ui;

import co.edu.autonoma.ancientseas.catalog.Species;
import co.edu.autonoma.ancientseas.catalog.SpeciesCatalog;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

final class SpeciesBrowserDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    SpeciesBrowserDialog(Window owner, SpeciesCatalog catalog) {
        super(owner, "Conoce las especies", ModalityType.APPLICATION_MODAL);
        String note = CoexistenceNote.text(catalog);
        JList<Species> list = new JList<>(catalog.sorted(Comparator.comparingDouble(Species::lengthMeters).reversed())
                .toArray(Species[]::new));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFont(Theme.font(java.awt.Font.PLAIN, 17));
        list.setFixedCellHeight(Theme.px(34));
        list.setCellRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> l, Object value, int index,
                    boolean selected, boolean focus) {
                Species s = (Species) value;
                String text = String.format("<html><b>%s</b> &nbsp;<i>%s</i> (%.1f m)</html>", s.commonName(),
                        s.scientificName(), s.lengthMeters());
                return super.getListCellRendererComponent(l, text, index, selected, focus);
            }
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && list.getSelectedValue() != null) {
                    new SpeciesInfoDialog(SpeciesBrowserDialog.this, list.getSelectedValue(), note).setVisible(true);
                }
            }
        });
        JButton open = UiFactory.button("Ver ficha", Theme.SEA);
        JButton close = UiFactory.button("Cerrar", Theme.GREY);
        open.addActionListener(e -> {
            if (list.getSelectedValue() != null) {
                new SpeciesInfoDialog(this, list.getSelectedValue(), note).setVisible(true);
            }
        });
        close.addActionListener(e -> dispose());
        if (list.getModel().getSize() > 0) {
            list.setSelectedIndex(0);
        }
        list.getInputMap().put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "open-card");
        list.getActionMap().put("open-card", new javax.swing.AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                open.doClick();
            }
        });
        UiFactory.focusWhenShown(list);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(open);
        actions.add(close);
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(Theme.PALE);
        content.setBorder(BorderFactory.createEmptyBorder(18, 22, 14, 22));
        content.add(UiFactory.label("<html>Elige un animal y pulsa Ver ficha (o haz doble clic).<br><small>"
                + note + "</small></html>", Theme.BODY, Theme.INK), BorderLayout.NORTH);
        content.add(new JScrollPane(list), BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);
        setContentPane(content);
        UiFactory.closeOnEscape(this);
        setSize(560, 460);
        setLocationRelativeTo(owner);
    }
}
