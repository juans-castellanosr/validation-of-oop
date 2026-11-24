package co.edu.autonoma.ancientseas.ui;

import co.edu.autonoma.ancientseas.catalog.SpeciesCatalog;
import co.edu.autonoma.ancientseas.game.GameEngine;
import co.edu.autonoma.ancientseas.game.GameMemento;
import co.edu.autonoma.ancientseas.game.GameSession;
import co.edu.autonoma.ancientseas.game.GameSettings;
import co.edu.autonoma.ancientseas.game.GameSnapshot;
import co.edu.autonoma.ancientseas.game.GameStatus;
import co.edu.autonoma.ancientseas.game.ScoreEntry;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

/**
 * The application window: switches between the menu and the game and opens every dialog.
 * Saving, loading and exporting run in background workers; all Swing calls stay on the Event Dispatch Thread.
 */
public final class MainWindow extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final String MENU = "menu";
    private static final String GAME = "game";

    private final transient MuseumServices services;
    private final CardLayout cards = new CardLayout();
    private final JPanel screens = new JPanel(cards);
    private final MenuScreen menu;
    private final GameScreen game;
    private transient GameEngine engine;
    private transient SwingWorker<Optional<Path>, Void> saving;

    /**
     * Builds the window with the menu; it is shown by {@link #open()}.
     *
     * @param services data, files and log used by the interface
     */
    public MainWindow(MuseumServices services) {
        super("Ancient Seas: un océano prehistórico para explorar");
        this.services = services;
        menu = new MenuScreen(menuButton("Jugar", Theme.GREEN, this::play),
                menuButton("Continuar partida guardada", Theme.SEA, this::continueSavedGame),
                menuButton("Conoce las especies", Theme.SEA,
                        () -> new SpeciesBrowserDialog(this, services.catalog()).setVisible(true)),
                menuButton("Mejores puntuaciones", Theme.SEA, this::showScores),
                menuButton("Ajustes", Theme.SEA, this::openSettings),
                menuButton("Administración del museo", Theme.SEA, this::openAdministration),
                menuButton("Salir", Theme.GREY, this::quit));
        game = new GameScreen(new GameActions());
        screens.add(menu, MENU);
        screens.add(game, GAME);
        setContentPane(screens);
        fitToScreen();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quit();
            }
        });
    }

    private void fitToScreen() {
        java.awt.Rectangle usable = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        int width = Math.min(1440, (int) (usable.width * 0.92));
        int height = Math.min((int) (usable.height * 0.92), width * 9 / 16 + 60);
        setMinimumSize(new Dimension(Math.min(960, usable.width), Math.min(600, usable.height)));
        setSize(width, height);
    }

    /** Shows the window with the menu and reports start-up problems to staff. */
    public void open() {
        applyReducedMotion(services.settings().reducedMotion());
        showMenu();
        setVisible(true);
        int catalogCount = services.catalogProblems().size();
        int otherCount = services.startupProblems().size();
        if (catalogCount + otherCount > 0) {
            services.startupProblems().forEach(p -> services.recordEvent("Startup problem: " + p));
            String where = otherCount == 0 ? "el personal puede ver el detalle en Administración del museo."
                    : catalogCount == 0 ? "el detalle quedó en el archivo ancient-seas.log de la carpeta de datos."
                    : "los del catálogo se ven en Administración del museo y los demás en el archivo "
                            + "ancient-seas.log de la carpeta de datos.";
            int count = catalogCount + otherCount;
            String found = count == 1 ? "Se encontró 1 dato con problemas"
                    : "Se encontraron " + count + " datos con problemas";
            JOptionPane.showMessageDialog(this, found + " al iniciar. El juego funciona igual; " + where,
                    "Aviso para el personal", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JButton menuButton(String text, java.awt.Color color, Runnable action) {
        JButton b = UiFactory.button(text, color);
        b.addActionListener(e -> action.run());
        return b;
    }

    private int gameRequests;

    private void play() {
        if (services.catalog().find(GameSession.PLAYER_SPECIES_ID).isEmpty()) {
            JOptionPane.showMessageDialog(this, "Falta la Archelon en el catálogo, así que no se puede jugar. "
                    + "Pide ayuda al personal del museo.", "No se puede jugar", JOptionPane.WARNING_MESSAGE);
            return;
        }
        gameRequests++;
        GameSession session = GameSession.newGame(services.catalog(), services.settings(), new Random());
        services.recordEvent("Game started by " + services.settings().playerName() + " ("
                + services.settings().difficulty() + ")");
        startEngine(session);
        game.showMessage("¡Come plancton, escapa de los grandes y sube a respirar!");
    }

    private void continueSavedGame() {
        int ticket = ++gameRequests;
        new SwingWorker<ResumeAttempt, Void>() {
            @Override
            protected ResumeAttempt doInBackground() {
                return services.resumeLatestGame();
            }

            @Override
            protected void done() {
                if (ticket != gameRequests || !isDisplayable()) {
                    return;
                }
                try {
                    showResumed(get());
                } catch (ExecutionException e) {
                    services.recordError("Could not resume saved game", e.getCause());
                    JOptionPane.showMessageDialog(MainWindow.this, "No se pudo abrir la partida guardada. "
                            + "Puedes empezar una partida nueva.", "Continuar", JOptionPane.WARNING_MESSAGE);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void showResumed(ResumeAttempt attempt) {
        if (attempt.session().isEmpty()) {
            String text = attempt.anySaveFound()
                    ? "Las partidas guardadas están dañadas o son de otra versión y no se pueden abrir. "
                            + "Puedes empezar una partida nueva."
                    : "Todavía no hay partidas guardadas.";
            JOptionPane.showMessageDialog(this, text, "Continuar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        services.recordEvent("Saved game resumed" + (attempt.skippedSaves().isEmpty() ? ""
                : "; skipped unusable saves: " + attempt.skippedSaves()));
        startEngine(attempt.session().get());
        game.showMessage(attempt.skippedSaves().isEmpty()
                ? "Partida recuperada. Pulsa Espacio o Seguir para continuar."
                : "La última partida estaba dañada; se abrió la anterior. Pulsa Espacio para seguir.");
    }

    private void startEngine(GameSession session) {
        stopEngine();
        engine = new GameEngine(session, game.listener());
        engine.launch();
        engine.submit(s -> {
            if (s.status() == GameStatus.READY) {
                s.start();
            }
        });
        menu.stopAnimation();
        cards.show(screens, GAME);
        game.attach(engine);
    }

    private void stopEngine() {
        if (engine == null) {
            return;
        }
        game.detach();
        try {
            engine.shutdown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        engine = null;
    }

    private void finishPendingSave() {
        if (saving == null) {
            return;
        }
        try {
            saving.get(5, java.util.concurrent.TimeUnit.SECONDS);
        } catch (ExecutionException | java.util.concurrent.TimeoutException e) {
            services.recordError("Save did not finish before closing", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void showMenu() {
        stopEngine();
        cards.show(screens, MENU);
        menu.startAnimation();
    }

    private record DialogPause(GameEngine engine, CompletableFuture<Boolean> pausedByUs) {
    }

    private DialogPause pauseForDialog() {
        if (engine == null) {
            return new DialogPause(null, CompletableFuture.completedFuture(false));
        }
        return new DialogPause(engine, engine.query(s -> {
            if (s.status() == GameStatus.RUNNING) {
                s.pause();
                return true;
            }
            return false;
        }));
    }

    private void resumeAfterDialog(DialogPause pause) {
        if (pause.engine() == null || pause.engine() != engine) {
            return;
        }
        GameEngine paused = pause.engine();
        pause.pausedByUs().thenAccept(didPause -> {
            if (didPause) {
                paused.submit(s -> {
                    if (s.status() == GameStatus.PAUSED) {
                        s.resume();
                    }
                });
            }
        });
    }

    private void showScores() {
        new ScoresDialog(this, services.leaderboard(), () -> export("historial de puntuaciones",
                services::exportScores)).setVisible(true);
    }

    private void openSettings() {
        Optional<GameSettings> chosen = new SettingsDialog(this, services.settings()).ask();
        chosen.ifPresent(settings -> {
            boolean sizeChanged = settings.interfaceSize() != services.settings().interfaceSize();
            applyReducedMotion(settings.reducedMotion());
            try {
                services.saveSettings(settings);
                if (sizeChanged) {
                    JOptionPane.showMessageDialog(this, "El nuevo tamaño de la interfaz se verá la próxima vez que "
                            + "abras el juego.", "Ajustes", JOptionPane.INFORMATION_MESSAGE);
                }
                services.recordEvent("Settings changed: " + settings);
            } catch (IOException e) {
                services.recordError("Could not save settings", e);
                JOptionPane.showMessageDialog(this, "Los ajustes se usarán ahora, pero no se pudieron guardar "
                        + "para la próxima vez.", "Ajustes", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private void applyReducedMotion(boolean reduced) {
        menu.setReducedMotion(reduced);
        game.setReducedMotion(reduced);
    }

    private void openAdministration() {
        JPasswordField field = UiFactory.focusWhenShown(new JPasswordField(12));
        Object[] form = {"Esta zona es para el personal del museo.", "PIN:", field};
        int answer = JOptionPane.showConfirmDialog(this, form, "Administración del museo",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }
        char[] pin = field.getPassword();
        boolean allowed;
        try {
            allowed = services.verifyAdminPin(pin);
        } finally {
            Arrays.fill(pin, '\0');
        }
        if (!allowed) {
            services.recordEvent("Wrong administration PIN entered");
            JOptionPane.showMessageDialog(this, "El PIN no es correcto.", "Administración del museo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        services.recordEvent("Administration mode opened");
        new CatalogAdminDialog(this, new SpeciesCatalog(services.catalog()), services.catalogProblems(), new AdminActions())
                .setVisible(true);
    }

    private void quit() {
        if (engine != null && !engine.latest().status().isFinal()) {
            DialogPause pause = pauseForDialog();
            boolean leave = UiFactory.confirm(this, "Salir", "Hay una partida en curso. Si quieres continuarla "
                    + "otro día, primero pulsa Guardar.", "Salir sin guardar", "Volver al juego");
            if (!leave) {
                resumeAfterDialog(pause);
                return;
            }
        }
        finishPendingSave();
        stopEngine();
        menu.stopAnimation();
        game.detach();
        services.recordEvent("Application closed");
        dispose();
    }

    @FunctionalInterface
    private interface Export {
        Path run() throws IOException;
    }

    private void export(String what, Export export) {
        new SwingWorker<Path, Void>() {
            @Override
            protected Path doInBackground() throws IOException {
                return export.run();
            }

            @Override
            protected void done() {
                try {
                    Path file = get();
                    services.recordEvent("Exported " + what + " to " + file);
                    JOptionPane.showMessageDialog(MainWindow.this, "Listo. Se guardó el " + what + " en:\n"
                            + file.toAbsolutePath(), "Exportar", JOptionPane.INFORMATION_MESSAGE);
                } catch (ExecutionException e) {
                    services.recordError("Export failed: " + what, e.getCause());
                    JOptionPane.showMessageDialog(MainWindow.this, "No se pudo exportar el " + what + ".",
                            "Exportar", JOptionPane.ERROR_MESSAGE);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private final class GameActions implements GameScreenActions {

        @Override
        public void saveRequested() {
            if (engine == null) {
                return;
            }
            if (saving != null) {
                game.showMessage("Ya se está guardando la partida, espera un momento.");
                return;
            }
            CompletableFuture<Optional<GameMemento>> memento = engine.query(
                    s -> s.status().isFinal() ? Optional.empty() : Optional.of(s.toMemento()));
            saving = new SwingWorker<Optional<Path>, Void>() {
                @Override
                protected Optional<Path> doInBackground() throws Exception {
                    Optional<GameMemento> state = memento.get();
                    return state.isPresent() ? Optional.of(services.saveGame(state.get())) : Optional.empty();
                }

                @Override
                protected void done() {
                    saving = null;
                    try {
                        Optional<Path> file = get();
                        if (file.isEmpty()) {
                            game.showMessage("La partida ya terminó, así que no hace falta guardarla.");
                            return;
                        }
                        services.recordEvent("Game saved to " + file.get());
                        game.showMessage("¡Partida guardada! Puedes continuarla otro día desde el menú.");
                    } catch (ExecutionException e) {
                        services.recordError("Could not save game", e.getCause());
                        game.showMessage("No se pudo guardar la partida. Avísale al personal del museo.");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            };
            saving.execute();
        }

        @Override
        public void endRequested() {
            if (engine == null || engine.latest().status().isFinal()) {
                return;
            }
            DialogPause pause = pauseForDialog();
            boolean end = UiFactory.confirm(MainWindow.this, "¿Terminar la partida?", "Si terminas ahora, se "
                    + "guardará tu puntuación, pero no podrás seguir esta partida.", "Sí, terminar", "No, seguir jugando");
            if (end && pause.engine() == engine) {
                pause.engine().submit(GameSession::end);
            } else {
                resumeAfterDialog(pause);
            }
        }

        @Override
        public void speciesInfoRequested(String speciesId) {
            services.catalog().find(speciesId).ifPresent(species -> {
                DialogPause pause = pauseForDialog();
                new SpeciesInfoDialog(MainWindow.this, species, CoexistenceNote.text(services.catalog())).setVisible(true);
                resumeAfterDialog(pause);
            });
        }

        @Override
        public void gameFinished(GameSnapshot finalSnapshot) {
            if (engine == null) {
                return;
            }
            String player = services.settings().playerName();
            engine.query(s -> s.result(player, LocalDateTime.now())).whenComplete((result, error) ->
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        if (error != null || result.isEmpty()) {
                            gameFailed(error != null ? error : new IllegalStateException("No result for final game"));
                            return;
                        }
                        showResults(result.get(), finalSnapshot);
                    }));
        }

        private void showResults(ScoreEntry result, GameSnapshot finalSnapshot) {
            stopEngine();
            services.recordEvent("Game finished: " + result.outcome() + ", " + result.score() + " points, stage "
                    + result.stage());
            try {
                services.recordScore(result);
            } catch (IOException e) {
                services.recordError("Could not store score", e);
                JOptionPane.showMessageDialog(MainWindow.this, "Tu puntuación no se pudo guardar en el historial, "
                        + "así que no aparecerá en la tabla. Avisa al personal del museo.", "Puntuación",
                        JOptionPane.WARNING_MESSAGE);
            }
            ResultsDialog.Choice choice = new ResultsDialog(MainWindow.this, result, services.leaderboard(),
                    () -> export("informe de la expedición",
                            () -> services.exportReport(result, finalSnapshot.populations())),
                    services.settings().reducedMotion()).ask();
            if (choice == ResultsDialog.Choice.PLAY_AGAIN) {
                play();
            } else {
                showMenu();
            }
        }

        @Override
        public void gameFailed(Throwable error) {
            services.recordError("The simulation stopped unexpectedly", error);
            showMenu();
            JOptionPane.showMessageDialog(MainWindow.this, "¡Ups! Algo salió mal en el océano y la partida se "
                    + "detuvo. Vuelve al menú e inténtalo otra vez.", "Algo salió mal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private final class AdminActions implements CatalogAdminActions {

        @Override
        public void saveCatalog(SpeciesCatalog catalog) throws IOException {
            services.saveCatalog(catalog);
            services.recordEvent("Catalog saved with " + catalog.size() + " species");
        }

        @Override
        public void recordChange(String description) {
            services.recordEvent(description);
        }

        @Override
        public void recordError(String message, Throwable error) {
            services.recordError(message, error);
        }

        @Override
        public void changePin(char[] newPin) throws IOException {
            services.changeAdminPin(newPin);
            services.recordEvent("Administration PIN changed");
        }
    }
}
