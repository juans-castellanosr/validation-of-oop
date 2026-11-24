package co.edu.autonoma.ancientseas.app;

import co.edu.autonoma.ancientseas.persistence.ApplicationLog;
import co.edu.autonoma.ancientseas.persistence.DataDirectory;
import co.edu.autonoma.ancientseas.ui.MainWindow;
import co.edu.autonoma.ancientseas.ui.UiScale;
import java.awt.AWTError;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Entry point: prepares the data folder and the log, loads the data and opens the window.
 * Usage: {@code java -jar ancient-seas.jar [data-folder]}; the default folder is {@code data}
 * next to where the program is started.
 */
public final class AncientSeasApp {

    private static final Logger LOG = Logger.getLogger(AncientSeasApp.class.getName());
    private static final Locale SPANISH = Locale.forLanguageTag("es-CO");
    private static final String AA_PROPERTY = "awt.useSystemAAFontSettings";

    private AncientSeasApp() {
    }

    /**
     * Starts the application.
     *
     * @param args optional data folder path
     */
    public static void main(String[] args) {
        enableTextAntialiasingWhereTheDesktopGivesNoHints();
        Locale.setDefault(SPANISH);
        DataDirectory data = args.length > 0 ? new DataDirectory(Path.of(args[0])) : DataDirectory.defaultLocation();
        try {
            List<Path> seeded = data.seedIfMissing();
            ApplicationLog.install(data.logFile());
            LOG.info(() -> "Ancient Seas started; data folder " + data.root().toAbsolutePath()
                    + (seeded.isEmpty() ? "" : "; sample files created: " + seeded));
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Cannot prepare the data folder", e);
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "No se pudo preparar la carpeta de datos \"" + data.root().toAbsolutePath() + "\".\n"
                            + "Revise que exista y que se pueda escribir en ella.",
                    "Ancient Seas", JOptionPane.ERROR_MESSAGE));
            return;
        }
        Thread.setDefaultUncaughtExceptionHandler((thread, error) ->
                LOG.log(Level.SEVERE, "Uncaught error in thread " + thread.getName(), error));
        if (GraphicsEnvironment.isHeadless()) {
            LOG.severe("No graphical display available; Ancient Seas needs a screen");
            System.err.println("Ancient Seas necesita una pantalla gráfica para funcionar.");
            System.exit(1);
        }
        FileMuseumServices services = new FileMuseumServices(data);
        JComponent.setDefaultLocale(SPANISH);
        try {
            SwingUtilities.invokeLater(() -> {
                useCrossPlatformLook();
                try {
                    UiScale.apply(services.settings().interfaceSize().scale());
                    new MainWindow(services).open();
                } catch (RuntimeException e) {
                    LOG.log(Level.SEVERE, "The main window could not be created", e);
                    JOptionPane.showMessageDialog(null, "Ancient Seas no pudo abrirse. Avisa al personal del "
                            + "museo; el detalle quedó en el registro.", "Ancient Seas", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            });
        } catch (AWTError e) {
            LOG.log(Level.SEVERE, "Cannot connect to the graphical display", e);
            System.err.println("Ancient Seas no pudo conectarse a la pantalla: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void enableTextAntialiasingWhereTheDesktopGivesNoHints() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        boolean unix = !os.contains("win") && !os.contains("mac");
        if (unix && System.getProperty(AA_PROPERTY) == null) {
            System.setProperty(AA_PROPERTY, "on");
        }
    }

    private static void useCrossPlatformLook() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            LOG.log(Level.WARNING, "Look and feel unavailable; using the default", e);
        }
    }
}
