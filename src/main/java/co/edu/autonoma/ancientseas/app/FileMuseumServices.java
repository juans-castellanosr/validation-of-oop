package co.edu.autonoma.ancientseas.app;

import co.edu.autonoma.ancientseas.admin.AdminAccess;
import co.edu.autonoma.ancientseas.catalog.SpeciesCatalog;
import co.edu.autonoma.ancientseas.error.CatalogFormatException;
import co.edu.autonoma.ancientseas.error.SaveGameException;
import co.edu.autonoma.ancientseas.game.GameMemento;
import co.edu.autonoma.ancientseas.game.GameSession;
import co.edu.autonoma.ancientseas.game.GameSettings;
import co.edu.autonoma.ancientseas.game.Leaderboard;
import co.edu.autonoma.ancientseas.game.ScoreEntry;
import co.edu.autonoma.ancientseas.persistence.AdminAccessStore;
import co.edu.autonoma.ancientseas.persistence.DataDirectory;
import co.edu.autonoma.ancientseas.persistence.LoadResult;
import co.edu.autonoma.ancientseas.persistence.SaveGameXml;
import co.edu.autonoma.ancientseas.persistence.ScoreHistoryStore;
import co.edu.autonoma.ancientseas.persistence.SettingsStore;
import co.edu.autonoma.ancientseas.persistence.SpeciesCsv;
import co.edu.autonoma.ancientseas.persistence.XmlExporter;
import co.edu.autonoma.ancientseas.ui.MuseumServices;
import co.edu.autonoma.ancientseas.ui.ResumeAttempt;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

final class FileMuseumServices implements MuseumServices {

    private static final Logger LOG = Logger.getLogger(FileMuseumServices.class.getName());
    private static final int LEADERBOARD_PLACES = 10;
    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private final DataDirectory data;
    private final SpeciesCsv speciesCsv = new SpeciesCsv();
    private final SettingsStore settingsStore = new SettingsStore();
    private final AdminAccessStore adminStore = new AdminAccessStore();
    private final ScoreHistoryStore historyStore = new ScoreHistoryStore();
    private final SaveGameXml saves = new SaveGameXml();
    private final XmlExporter exporter = new XmlExporter();
    private volatile SpeciesCatalog catalog;
    private boolean catalogBackedUp;
    private final List<String> catalogProblems = new ArrayList<>();
    private final List<String> startupProblems = new ArrayList<>();
    private final List<ScoreEntry> history = new ArrayList<>();
    private volatile GameSettings settings;
    private volatile AdminAccess admin;

    FileMuseumServices(DataDirectory data) {
        this.data = data;
        this.catalog = loadCatalog();
        this.settings = keep(settingsStore.load(data.settingsFile()));
        this.admin = keep(adminStore.load(data.adminFile()));
        this.history.addAll(keep(historyStore.load(data.scoresFile())));
        LOG.info(() -> "Data loaded from " + data.root().toAbsolutePath() + ": " + catalog.size() + " species, "
                + history.size() + " past games");
    }

    private SpeciesCatalog loadCatalog() {
        try {
            LoadResult<SpeciesCatalog> loaded = speciesCsv.read(data.speciesFile());
            catalogProblems.addAll(loaded.problems());
            loaded.problems().forEach(problem -> LOG.warning(() -> "Catalog row skipped: " + problem));
            return loaded.value();
        } catch (CatalogFormatException e) {
            LOG.log(Level.SEVERE, "Catalog file unusable, using the bundled catalog", e);
            catalogProblems.add(e.getFriendlyMessage() + " Se está usando el catálogo original del juego.");
            return bundledCatalog();
        }
    }

    private SpeciesCatalog bundledCatalog() {
        try (InputStream in = FileMuseumServices.class.getResourceAsStream("/data/species.csv")) {
            if (in == null) {
                return new SpeciesCatalog();
            }
            Path copy = Files.createTempFile("ancient-seas-species", ".csv");
            try {
                Files.copy(in, copy, StandardCopyOption.REPLACE_EXISTING);
                return speciesCsv.read(copy).value();
            } finally {
                Files.deleteIfExists(copy);
            }
        } catch (IOException | CatalogFormatException e) {
            LOG.log(Level.SEVERE, "Bundled catalog unreadable; starting with an empty catalog", e);
            catalogProblems.add("Tampoco se pudo leer el catálogo original; el catálogo está vacío.");
            return new SpeciesCatalog();
        }
    }

    private <T> T keep(LoadResult<T> result) {
        startupProblems.addAll(result.problems());
        return result.value();
    }

    @Override
    public SpeciesCatalog catalog() {
        return catalog;
    }

    @Override
    public List<String> catalogProblems() {
        return Collections.unmodifiableList(catalogProblems);
    }

    @Override
    public List<String> startupProblems() {
        return Collections.unmodifiableList(startupProblems);
    }

    @Override
    public GameSettings settings() {
        return settings;
    }

    @Override
    public void saveSettings(GameSettings newSettings) throws IOException {
        this.settings = newSettings;
        settingsStore.save(newSettings, data.settingsFile());
    }

    @Override
    public boolean verifyAdminPin(char[] pin) {
        return admin.verify(pin);
    }

    @Override
    public void changeAdminPin(char[] pin) throws IOException {
        AdminAccess updated = AdminAccess.fromPin(pin);
        adminStore.save(updated, data.adminFile());
        this.admin = updated;
    }

    @Override
    public synchronized void saveCatalog(SpeciesCatalog edited) throws IOException {
        backUpCatalogWithSkippedRows();
        speciesCsv.write(edited, data.speciesFile());
        catalog = new SpeciesCatalog(edited);
        catalogProblems.clear();
    }

    private void backUpCatalogWithSkippedRows() throws IOException {
        Path file = data.speciesFile();
        if (catalogBackedUp || catalogProblems.isEmpty() || !Files.exists(file)) {
            return;
        }
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path backup = file.resolveSibling("species-" + stamp + ".csv.bak");
        Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING);
        catalogBackedUp = true;
        LOG.warning(() -> "Catalog had skipped rows; original file kept as " + backup.getFileName());
    }

    @Override
    public void recordEvent(String message) {
        LOG.info(message);
    }

    @Override
    public void recordError(String message, Throwable error) {
        LOG.log(Level.SEVERE, message, error);
    }

    @Override
    public synchronized Leaderboard leaderboard() {
        Leaderboard board = new Leaderboard(LEADERBOARD_PLACES);
        board.addAll(history);
        return board;
    }

    @Override
    public synchronized void recordScore(ScoreEntry entry) throws IOException {
        historyStore.append(entry, data.scoresFile());
        history.add(entry);
    }

    @Override
    public synchronized Path saveGame(GameMemento memento) throws IOException {
        Path file = data.savesDirectory().resolve("partida-" + FILE_STAMP.format(LocalDateTime.now()) + ".xml");
        saves.write(memento, file);
        return file;
    }

    @Override
    public ResumeAttempt resumeLatestGame() {
        List<Path> candidates;
        try (Stream<Path> files = Files.list(data.savesDirectory())) {
            candidates = files.filter(p -> p.getFileName().toString().endsWith(".xml"))
                    .sorted(Comparator.comparing(FileMuseumServices::modified).reversed()).toList();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Saves folder unreadable", e);
            return new ResumeAttempt(Optional.empty(), List.of(), false);
        }
        List<String> skipped = new ArrayList<>();
        for (Path file : candidates) {
            try {
                GameSession session = GameSession.restore(saves.read(file), catalog);
                return new ResumeAttempt(Optional.of(session), skipped, true);
            } catch (SaveGameException e) {
                LOG.log(Level.WARNING, "Skipping unusable save " + file, e);
                skipped.add(file.getFileName() + ": " + e.getFriendlyMessage());
            }
        }
        return new ResumeAttempt(Optional.empty(), skipped, !candidates.isEmpty());
    }

    private static FileTime modified(Path file) {
        try {
            return Files.getLastModifiedTime(file);
        } catch (IOException e) {
            return FileTime.fromMillis(0);
        }
    }

    @Override
    public synchronized Path exportScores() throws IOException {
        Path file = data.exportsDirectory().resolve("puntuaciones-" + FILE_STAMP.format(LocalDateTime.now()) + ".xml");
        exporter.exportScores(List.copyOf(history), file);
        return file;
    }

    @Override
    public Path exportReport(ScoreEntry result, Map<String, Integer> census) throws IOException {
        Path file = data.exportsDirectory().resolve("expedicion-" + FILE_STAMP.format(result.playedAt()) + ".xml");
        exporter.exportExpeditionReport(result, census, catalog, file);
        return file;
    }
}
