package co.edu.autonoma.ancientseas.ui;

import co.edu.autonoma.ancientseas.game.GameStatus;
import co.edu.autonoma.ancientseas.game.ScoreEntry;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

final class LeaderboardTable {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private LeaderboardTable() {
    }

    static JScrollPane of(Collection<ScoreEntry> entries) {
        List<ScoreEntry> rows = new ArrayList<>(entries);
        JTable table = new JTable(new AbstractTableModel() {
            private static final long serialVersionUID = 1L;
            private final String[] columns = {"#", "Nombre", "Puntos", "Etapa", "Resultado", "Fecha"};

            @Override
            public int getRowCount() {
                return rows.size();
            }

            @Override
            public int getColumnCount() {
                return columns.length;
            }

            @Override
            public String getColumnName(int column) {
                return columns[column];
            }

            @Override
            public Object getValueAt(int row, int column) {
                ScoreEntry e = rows.get(row);
                return switch (column) {
                    case 0 -> row + 1;
                    case 1 -> e.playerName();
                    case 2 -> String.format("%,d", e.score());
                    case 3 -> e.stage().displayName();
                    case 4 -> outcome(e.outcome());
                    default -> DATE.format(e.playedAt());
                };
            }
        });
        TableStyle.apply(table, 0, 2);
        int[] widths = {40, 170, 100, 110, 150, 170};
        int total = 0;
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(Theme.px(widths[i]));
            total += Theme.px(widths[i]);
        }
        JScrollPane scroll = new JScrollPane(table);
        int header = table.getTableHeader().getPreferredSize().height;
        int visibleRows = Math.min(8, Math.max(1, rows.size()));
        scroll.setPreferredSize(new java.awt.Dimension(total + Theme.px(4),
                header + visibleRows * table.getRowHeight() + Theme.px(4)));
        return scroll;
    }

    static String outcome(GameStatus outcome) {
        return switch (outcome) {
            case WON -> "¡Creció!";
            case LOST -> "No sobrevivió";
            default -> "Terminada antes";
        };
    }
}
