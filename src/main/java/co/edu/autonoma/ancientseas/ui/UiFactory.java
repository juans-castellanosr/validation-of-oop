package co.edu.autonoma.ancientseas.ui;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

final class UiFactory {

    private UiFactory() {
    }

    static JButton button(String text, Color background) {
        return new OceanButton(text, background);
    }

    static <T extends JComponent> T focusWhenShown(T field) {
        field.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                SwingUtilities.invokeLater(field::requestFocusInWindow);
                field.removeAncestorListener(this);
                java.awt.Window window = SwingUtilities.getWindowAncestor(field);
                if (window != null) {
                    window.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowActivated(java.awt.event.WindowEvent e) {
                            window.removeWindowListener(this);
                            SwingUtilities.invokeLater(field::requestFocusInWindow);
                        }
                    });
                }
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
        return field;
    }

    static void packWithinScreen(javax.swing.JDialog dialog, JComponent actions) {
        dialog.pack();
        java.awt.Rectangle usable = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int maxWidth = (int) (usable.width * 0.95);
        int maxHeight = (int) (usable.height * 0.92);
        if (dialog.getHeight() <= maxHeight && dialog.getWidth() <= maxWidth) {
            return;
        }
        java.awt.Container content = dialog.getContentPane();
        java.awt.Color background = content.getBackground();
        actions.getParent().remove(actions);
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(content);
        scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(Theme.px(24));
        int scrollbar = scroll.getVerticalScrollBar().getPreferredSize().width;
        boolean tooWide = content.getPreferredSize().width + scrollbar > maxWidth;
        scroll.setHorizontalScrollBarPolicy(tooWide ? javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
                : javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        javax.swing.JPanel frame = new javax.swing.JPanel(new java.awt.BorderLayout());
        frame.setBackground(background);
        actions.setBorder(javax.swing.BorderFactory.createEmptyBorder(Theme.px(6), Theme.px(16), Theme.px(10),
                Theme.px(16)));
        frame.add(scroll, java.awt.BorderLayout.CENTER);
        frame.add(actions, java.awt.BorderLayout.SOUTH);
        dialog.setContentPane(frame);
        java.awt.Insets insets = dialog.getInsets();
        int width = Math.min(content.getPreferredSize().width + scrollbar + insets.left + insets.right, maxWidth);
        dialog.setSize(width, maxHeight);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                SwingUtilities.invokeLater(() -> scroll.getViewport().setViewPosition(new java.awt.Point(0, 0)));
            }
        });
    }

    static void closeOnEscape(javax.swing.JDialog dialog) {
        if (dialog.getDefaultCloseOperation() == javax.swing.WindowConstants.HIDE_ON_CLOSE) {
            dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        }
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispatchEvent(
                new java.awt.event.WindowEvent(dialog, java.awt.event.WindowEvent.WINDOW_CLOSING)),
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    static boolean confirm(java.awt.Component parent, String title, String message, String doIt, String keep) {
        Object[] options = {doIt, keep};
        int answer = javax.swing.JOptionPane.showOptionDialog(parent, message, title,
                javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE, null, options, keep);
        return answer == 0;
    }

    static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }
}
