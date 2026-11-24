package co.edu.autonoma.ancientseas.ui;

import co.edu.autonoma.ancientseas.game.ScoreEntry;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.Collection;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

final class ScoresDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    ScoresDialog(Window owner, Collection<ScoreEntry> best, Runnable onExport) {
        super(owner, "Mejores puntuaciones", ModalityType.APPLICATION_MODAL);
        JButton export = UiFactory.button("Exportar historial (XML)", Theme.SEA);
        JButton close = UiFactory.button("Cerrar", Theme.GREY);
        export.addActionListener(e -> onExport.run());
        close.addActionListener(e -> dispose());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(export);
        actions.add(close);
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(Theme.PALE);
        content.setBorder(BorderFactory.createEmptyBorder(18, 22, 14, 22));
        content.add(UiFactory.label(best.isEmpty() ? "Todavía no hay puntuaciones. ¡Sé el primero!"
                : "Mejores puntuaciones", Theme.HEADING, Theme.NAVY), BorderLayout.NORTH);
        content.add(LeaderboardTable.of(best), BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);
        setContentPane(content);
        UiFactory.closeOnEscape(this);
        UiFactory.packWithinScreen(this, actions);
        setLocationRelativeTo(owner);
    }
}
