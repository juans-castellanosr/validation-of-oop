package co.edu.autonoma.ancientseas.ui;

import co.edu.autonoma.ancientseas.game.EntityView;
import co.edu.autonoma.ancientseas.game.GameEngine;
import co.edu.autonoma.ancientseas.game.GameListener;
import co.edu.autonoma.ancientseas.game.GameSession;
import co.edu.autonoma.ancientseas.game.GameSnapshot;
import co.edu.autonoma.ancientseas.game.GameStatus;
import co.edu.autonoma.ancientseas.model.Vector2;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

final class GameScreen extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int FRAME_MILLIS = 16;
    private static final double MESSAGE_SECONDS = 4.0;
    private static final double FADE_SECONDS = 0.35;
    private static final int MAX_QUEUED_MESSAGES = 3;

    private enum Direction {
        UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);

        private final Vector2 vector;

        Direction(double x, double y) {
            this.vector = new Vector2(x, y);
        }
    }

    private final transient GameScreenActions actions;
    private final transient OceanRenderer renderer = new OceanRenderer();
    private final transient Map<Integer, Direction> held = new HashMap<>();
    private final JButton pauseButton = UiFactory.button("Pausa (P)", Theme.SEA);
    private final transient FeedbackEffects effects = new FeedbackEffects();
    private final transient java.util.Deque<String> messages = new java.util.ArrayDeque<>();
    private final HudBar hud;
    private final OceanView ocean = new OceanView();
    private final Timer frameTimer;
    private transient GameEngine engine;
    private int generation;
    private String message;
    private double messageAge;
    private long lastFrameNanos = System.nanoTime();
    private double animationTime;

    GameScreen(GameScreenActions actions) {
        super(new BorderLayout());
        this.actions = actions;
        JButton save = UiFactory.button("Guardar (G)", Theme.SEA);
        JButton end = UiFactory.button("Terminar (Esc)", Theme.CORAL);
        pauseButton.addActionListener(e -> togglePause());
        save.addActionListener(e -> actions.saveRequested());
        end.addActionListener(e -> actions.endRequested());
        hud = new HudBar(pauseButton, save, end);
        add(hud, BorderLayout.NORTH);
        add(ocean, BorderLayout.CENTER);
        installKeys();
        installMouse();
        installFocusLoss();
        frameTimer = new Timer(FRAME_MILLIS, e -> refresh());
    }

    GameListener listener() {
        int owner = ++generation;
        return new GameListener() {
            @Override
            public void onFrame(GameSnapshot snapshot) {
            }

            @Override
            public void onMessage(String text) {
                SwingUtilities.invokeLater(() -> {
                    if (owner == generation) {
                        showMessage(text);
                    }
                });
            }

            @Override
            public void onFinished(GameSnapshot finalSnapshot) {
                SwingUtilities.invokeLater(() -> {
                    if (owner == generation) {
                        actions.gameFinished(finalSnapshot);
                    }
                });
            }

            @Override
            public void onFailure(Throwable error) {
                SwingUtilities.invokeLater(() -> {
                    if (owner == generation) {
                        actions.gameFailed(error);
                    }
                });
            }
        };
    }

    void attach(GameEngine newEngine) {
        this.engine = newEngine;
        held.clear();
        message = null;
        messages.clear();
        effects.reset();
        frameTimer.start();
        ocean.requestFocusInWindow();
    }

    void detach() {
        frameTimer.stop();
        engine = null;
        generation++;
    }

    void showMessage(String text) {
        if (text.equals(message) || messages.contains(text)) {
            return;
        }
        if (messages.size() >= MAX_QUEUED_MESSAGES) {
            messages.pollFirst();
        }
        messages.addLast(text);
    }

    void setReducedMotion(boolean reduced) {
        renderer.setReducedMotion(reduced);
        effects.setReducedMotion(reduced);
    }

    private void advanceMessages(double dt) {
        if (message != null) {
            messageAge += dt;
            if (messageAge >= MESSAGE_SECONDS) {
                message = null;
            }
        }
        if (message == null && !messages.isEmpty()) {
            message = messages.pollFirst();
            messageAge = 0;
        }
    }

    void togglePause() {
        submit(s -> {
            if (s.status() == GameStatus.RUNNING) {
                s.pause();
            } else if (s.status() == GameStatus.PAUSED) {
                s.resume();
            }
        });
    }

    private void submit(java.util.function.Consumer<GameSession> command) {
        if (engine != null) {
            engine.submit(command);
        }
    }

    private void refresh() {
        if (engine == null) {
            return;
        }
        long now = System.nanoTime();
        double dt = Math.min(0.1, (now - lastFrameNanos) / 1e9);
        lastFrameNanos = now;
        GameSnapshot latest = engine.latest();
        if (latest.status() == GameStatus.RUNNING) {
            animationTime += dt;
        }
        pauseButton.setText(latest.status() == GameStatus.PAUSED ? "Seguir (P)" : "Pausa (P)");
        hud.show(latest);
        effects.update(latest, latest.status() == GameStatus.RUNNING ? dt : 0);
        advanceMessages(dt);
        ocean.frameDt = latest.status() == GameStatus.RUNNING ? dt : 0;
        ocean.repaint();
    }

    private void installKeys() {
        InputMap keys = ocean.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        bindDirection(keys, KeyEvent.VK_UP, Direction.UP);
        bindDirection(keys, KeyEvent.VK_W, Direction.UP);
        bindDirection(keys, KeyEvent.VK_DOWN, Direction.DOWN);
        bindDirection(keys, KeyEvent.VK_S, Direction.DOWN);
        bindDirection(keys, KeyEvent.VK_LEFT, Direction.LEFT);
        bindDirection(keys, KeyEvent.VK_A, Direction.LEFT);
        bindDirection(keys, KeyEvent.VK_RIGHT, Direction.RIGHT);
        bindDirection(keys, KeyEvent.VK_D, Direction.RIGHT);
        bind(keys, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "pause", this::togglePause);
        bind(keys, KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "pause", this::togglePause);
        bind(keys, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "end", actions::endRequested);
        bind(keys, KeyStroke.getKeyStroke(KeyEvent.VK_G, 0), "save", actions::saveRequested);
    }

    private void bindDirection(InputMap keys, int keyCode, Direction direction) {
        String name = direction.name() + keyCode;
        bind(keys, KeyStroke.getKeyStroke(keyCode, 0, false), name + "-press", () -> {
            if (held.put(keyCode, direction) == null) {
                sendSteering();
            }
        });
        bind(keys, KeyStroke.getKeyStroke(keyCode, 0, true), name + "-release", () -> {
            if (held.remove(keyCode) != null) {
                sendSteering();
            }
        });
    }

    private void bind(InputMap keys, KeyStroke stroke, String name, Runnable action) {
        keys.put(stroke, name);
        ocean.getActionMap().put(name, new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private void sendSteering() {
        Vector2 direction = held.values().stream().distinct().map(d -> d.vector).reduce(Vector2.ZERO, Vector2::plus);
        submit(s -> s.player().steerTowards(direction));
    }

    private void installFocusLoss() {
        ocean.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (!held.isEmpty()) {
                    held.clear();
                    sendSteering();
                }
            }
        });
    }

    private void installMouse() {
        ocean.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                ocean.requestFocusInWindow();
                if (engine == null) {
                    return;
                }
                Vector2 point = toOcean(e.getX(), e.getY());
                if (SwingUtilities.isRightMouseButton(e)) {
                    creatureAt(point).ifPresent(view -> actions.speciesInfoRequested(view.speciesId()));
                } else {
                    submit(s -> s.player().swimTo(point));
                }
            }
        });
    }

    private Vector2 toOcean(int x, int y) {
        GameSnapshot s = engine.latest();
        return new Vector2(x * s.width() / ocean.getWidth(), y * s.height() / ocean.getHeight());
    }

    private Optional<EntityView> creatureAt(Vector2 point) {
        return engine.latest().creatures().stream()
                .filter(v -> point.distanceTo(new Vector2(v.x(), v.y())) <= v.radius() * 1.3)
                .min(java.util.Comparator.comparingDouble(EntityView::radius));
    }

    private final class OceanView extends JComponent {

        private static final long serialVersionUID = 1L;
        private double frameDt;

        OceanView() {
            setFocusable(true);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                renderer.paint(g, getWidth(), getHeight(), engine == null ? null : engine.latest(), animationTime,
                        frameDt, effects);
                paintOverlayTexts(g);
            } finally {
                g.dispose();
            }
        }

        private void paintOverlayTexts(Graphics2D g) {
            if (message != null) {
                double fadeIn = Math.min(1, messageAge / FADE_SECONDS);
                double fadeOut = Math.min(1, (MESSAGE_SECONDS - messageAge) / FADE_SECONDS);
                float alpha = (float) Math.max(0, Math.min(fadeIn, fadeOut));
                java.awt.Composite saved = g.getComposite();
                g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
                g.setFont(Theme.font(java.awt.Font.BOLD, 19));
                int w = g.getFontMetrics().stringWidth(message) + Theme.px(44);
                int x = (getWidth() - w) / 2;
                g.setColor(Theme.SAND);
                g.fillRoundRect(x, Theme.px(18), w, Theme.px(42), Theme.px(38), Theme.px(38));
                g.setColor(Theme.INK);
                g.drawString(message, x + Theme.px(22), Theme.px(45));
                g.setComposite(saved);
            }
            paintKeyHints(g);
        }

        private void paintKeyHints(Graphics2D g) {
            String[][] hints = {{"Flechas / WASD", "nadar"}, {"Clic", "ir ahí"}, {"Espacio", "pausa"},
                {"Clic derecho", "ficha del animal"}, {"G", "guardar"}};
            g.setFont(Theme.font(java.awt.Font.BOLD, 14));
            java.awt.FontMetrics fm = g.getFontMetrics();
            int pad = Theme.px(6);
            int total = 0;
            for (String[] hint : hints) {
                total += fm.stringWidth(hint[0]) + Theme.px(14) + Theme.px(6) + fm.stringWidth(hint[1]) + Theme.px(18);
            }
            int baseline = getHeight() - Theme.px(14);
            int top = baseline - fm.getAscent() - pad - Theme.px(4);
            g.setColor(Theme.NAVY);
            g.fillRoundRect(Theme.px(8), top, total + pad, fm.getHeight() + 2 * pad + Theme.px(4), Theme.px(14),
                    Theme.px(14));
            int x = Theme.px(14);
            for (String[] hint : hints) {
                int keyWidth = fm.stringWidth(hint[0]) + Theme.px(14);
                g.setColor(Theme.SEA);
                g.fillRoundRect(x, baseline - fm.getAscent() - Theme.px(4), keyWidth, fm.getHeight() + Theme.px(4),
                        Theme.px(10), Theme.px(10));
                g.setColor(Color.WHITE);
                g.drawString(hint[0], x + Theme.px(7), baseline);
                x += keyWidth + Theme.px(6);
                g.drawString(hint[1], x, baseline);
                x += fm.stringWidth(hint[1]) + Theme.px(18);
            }
        }
    }
}
