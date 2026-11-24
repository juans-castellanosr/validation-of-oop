package co.edu.autonoma.ancientseas.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

final class TableStyle {

    private static final Color STRIPE = new Color(233, 241, 247);

    private TableStyle() {
    }

    static void apply(JTable table, Integer... rightAlignedColumns) {
        table.setFont(Theme.BODY);
        FontMetrics fm = table.getFontMetrics(Theme.BODY);
        table.setRowHeight(fm.getHeight() + Theme.px(12));
        table.getTableHeader().setFont(Theme.BODY_BOLD);
        table.getTableHeader().setBackground(Theme.NAVY);
        table.getTableHeader().setForeground(Theme.WHITE);
        table.setSelectionBackground(Theme.SEA);
        table.setSelectionForeground(Theme.WHITE);
        table.setGridColor(new Color(210, 220, 230));
        table.setFillsViewportHeight(true);
        Stripes renderer = new Stripes(Set.of(rightAlignedColumns));
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(Double.class, renderer);
        table.setDefaultRenderer(Integer.class, renderer);
    }

    static int fitColumnsToContent(JTable table) {
        int total = 0;
        int padding = Theme.px(20);
        for (int column = 0; column < table.getColumnCount(); column++) {
            javax.swing.table.TableColumn tc = table.getColumnModel().getColumn(column);
            Component header = table.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(table, tc.getHeaderValue(), false, false, -1, column);
            int width = header.getPreferredSize().width;
            for (int row = 0; row < table.getRowCount(); row++) {
                Component cell = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
                width = Math.max(width, cell.getPreferredSize().width);
            }
            tc.setPreferredWidth(width + padding);
            total += width + padding;
        }
        return total;
    }

    private static final class Stripes extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;
        private final transient Set<Integer> rightAligned;

        Stripes(Set<Integer> rightAligned) {
            this.rightAligned = rightAligned;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused,
                int row, int column) {
            Object shown = value instanceof Double d ? String.format("%.1f", d) : value;
            Component c = super.getTableCellRendererComponent(table, shown, selected, focused, row, column);
            if (!selected) {
                c.setBackground(row % 2 == 0 ? Theme.WHITE : STRIPE);
                c.setForeground(Theme.INK);
            }
            setBorder(BorderFactory.createEmptyBorder(0, Theme.px(8), 0, Theme.px(8)));
            int modelColumn = table.convertColumnIndexToModel(column);
            boolean number = value instanceof Number || rightAligned.contains(modelColumn);
            setHorizontalAlignment(number ? SwingConstants.RIGHT : SwingConstants.LEFT);
            return c;
        }
    }
}
