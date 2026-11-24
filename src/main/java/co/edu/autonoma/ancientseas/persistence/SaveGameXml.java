package co.edu.autonoma.ancientseas.persistence;

import co.edu.autonoma.ancientseas.error.CorruptSaveException;
import co.edu.autonoma.ancientseas.error.IncompatibleSaveException;
import co.edu.autonoma.ancientseas.game.Difficulty;
import co.edu.autonoma.ancientseas.game.EventFrequency;
import co.edu.autonoma.ancientseas.game.GameMemento;
import co.edu.autonoma.ancientseas.game.GameSettings;
import co.edu.autonoma.ancientseas.game.InterfaceSize;
import co.edu.autonoma.ancientseas.model.CreatureState;
import co.edu.autonoma.ancientseas.model.FoodState;
import co.edu.autonoma.ancientseas.model.Vector2;
import co.edu.autonoma.ancientseas.simulation.EventState;
import co.edu.autonoma.ancientseas.simulation.EventType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Saves and restores a game as inspectable XML. The {@code formatVersion} root attribute detects layout
 * changes, and numbers are written so they read back bit-for-bit identical.
 */
public final class SaveGameXml {

    public static final int FORMAT_VERSION = 1;

    private static final String ROOT = "ancientSeasSave";

    /** Creates the reader and writer; each method receives the file it works on. */
    public SaveGameXml() {
    }

    /**
     * Writes a memento atomically.
     *
     * @param memento the game state
     * @param file    the target file
     * @throws IOException              if the file cannot be written
     * @throws IllegalArgumentException if the state holds a non-finite number, which could never be read back
     */
    public void write(GameMemento memento, Path file) throws IOException {
        Document document = XmlDocuments.create();
        Element root = document.createElement(ROOT);
        root.setAttribute("formatVersion", Integer.toString(FORMAT_VERSION));
        document.appendChild(root);

        GameSettings settings = memento.settings();
        Element settingsElement = add(root, "settings");
        settingsElement.setAttribute("difficulty", settings.difficulty().name());
        settingsElement.setAttribute("creatureCount", Integer.toString(settings.creatureCount()));
        settingsElement.setAttribute("eventFrequency", settings.eventFrequency().name());
        settingsElement.setAttribute("reducedMotion", Boolean.toString(settings.reducedMotion()));
        settingsElement.setAttribute("interfaceSize", settings.interfaceSize().name());
        add(settingsElement, "playerName").setTextContent(settings.playerName());

        Element state = add(root, "state");
        setNumber(state, "elapsedSeconds", memento.elapsedSeconds());
        state.setAttribute("nextId", Long.toString(memento.nextId()));
        setNumber(state, "secondsUntilNextEvent", memento.secondsUntilNextEvent());
        setNumber(state, "planktonDue", memento.planktonDue());
        state.setAttribute("randomState", Long.toString(memento.randomState()));
        Element definitions = add(root, "speciesDefinitions");
        memento.speciesDefinitions().forEach((speciesId, definition) -> {
            Element element = add(definitions, "species");
            element.setAttribute("id", speciesId);
            element.setAttribute("definition", definition);
        });

        Element creatures = add(root, "creatures");
        memento.creatures().forEach(c -> writeCreature(add(creatures, "creature"), c));
        Element food = add(root, "food");
        memento.food().forEach(f -> writeFood(add(food, "particle"), f));
        Element events = add(root, "events");
        memento.events().forEach(e -> writeEvent(add(events, "event"), e));
        Element caps = add(root, "populationCaps");
        memento.populationCaps().forEach((speciesId, cap) -> {
            Element element = add(caps, "cap");
            element.setAttribute("speciesId", speciesId);
            element.setAttribute("max", Integer.toString(cap));
        });

        AtomicFile.write(file, XmlDocuments.toBytes(document));
    }

    /**
     * Reads a saved game.
     *
     * @param file the save file
     * @return the stored game state
     * @throws CorruptSaveException      if the file is unreadable, not XML, truncated or has missing or invalid data
     * @throws IncompatibleSaveException if the file is XML of another kind or of another format version
     */
    public GameMemento read(Path file) throws CorruptSaveException, IncompatibleSaveException {
        Document document;
        try (InputStream in = Files.newInputStream(file)) {
            document = XmlDocuments.parse(in);
        } catch (IOException | SAXException e) {
            throw new CorruptSaveException("Cannot parse save file " + file, e);
        }
        Element rootElement = document.getDocumentElement();
        if (!ROOT.equals(rootElement.getTagName())) {
            throw new IncompatibleSaveException("Unexpected root element <" + rootElement.getTagName() + ">");
        }
        SaveElement root = new SaveElement(rootElement);
        long version = root.integer("formatVersion");
        if (version != FORMAT_VERSION) {
            throw new IncompatibleSaveException("Save format version " + version + ", expected " + FORMAT_VERSION);
        }
        try {
            return readMemento(root);
        } catch (IllegalArgumentException e) {
            throw new CorruptSaveException("Save holds an invalid value: " + e.getMessage(), e);
        }
    }

    private static GameMemento readMemento(SaveElement root) throws CorruptSaveException {
        SaveElement settingsElement = root.child("settings");
        GameSettings settings = new GameSettings(settingsElement.constant("difficulty", Difficulty.class),
                toInt(settingsElement.integer("creatureCount")),
                settingsElement.constant("eventFrequency", EventFrequency.class),
                settingsElement.child("playerName").content(),
                settingsElement.has("reducedMotion") && settingsElement.flag("reducedMotion"),
                settingsElement.has("interfaceSize")
                        ? settingsElement.constant("interfaceSize", InterfaceSize.class) : InterfaceSize.NORMAL);
        SaveElement state = root.child("state");

        List<CreatureState> creatures = new ArrayList<>();
        for (SaveElement e : root.child("creatures").children("creature")) {
            creatures.add(new CreatureState(e.integer("id"), e.text("speciesId"), e.flag("player"), e.number("x"),
                    e.number("y"), e.number("vx"), e.number("vy"), e.number("sizeMeters"), e.number("energy"),
                    e.number("ageSeconds"), e.number("digestSeconds"), e.number("reproductionSeconds"),
                    e.number("oxygen"), e.number("health"), e.number("growthPoints"), e.number("protectionSeconds"),
                    e.number("wanderAngle"), e.number("burstSeconds"), e.number("burstRecoverySeconds"),
                    optionalNumber(e, "burstDirectionX"), optionalNumber(e, "burstDirectionY"),
                    e.has("destinationX") && e.has("destinationY")
                            ? new Vector2(e.number("destinationX"), e.number("destinationY")) : null));
        }
        List<FoodState> food = new ArrayList<>();
        for (SaveElement e : root.child("food").children("particle")) {
            food.add(new FoodState(e.integer("id"), e.number("x"), e.number("y"), e.number("secondsLeft")));
        }
        List<EventState> events = new ArrayList<>();
        for (SaveElement e : root.child("events").children("event")) {
            events.add(new EventState(e.constant("type", EventType.class), e.number("elapsedSeconds"),
                    e.number("durationSeconds"), e.number("direction"), e.text("speciesId")));
        }
        Map<String, Integer> caps = new HashMap<>();
        for (SaveElement e : root.child("populationCaps").children("cap")) {
            if (caps.put(e.text("speciesId"), toInt(e.integer("max"))) != null) {
                throw new CorruptSaveException("Repeated population cap for " + e.text("speciesId"), null);
            }
        }
        long randomState = state.has("randomState") ? state.integer("randomState") : new java.util.Random().nextLong();
        Map<String, String> definitions = new HashMap<>();
        for (SaveElement e : root.children("speciesDefinitions").stream()
                .flatMap(list -> list.children("species").stream()).toList()) {
            definitions.put(e.text("id"), e.text("definition"));
        }
        return new GameMemento(settings, state.number("elapsedSeconds"), state.integer("nextId"), creatures, food,
                events, state.number("secondsUntilNextEvent"), caps, state.number("planktonDue"), randomState,
                definitions);
    }

    private static double optionalNumber(SaveElement e, String attribute) throws CorruptSaveException {
        return e.has(attribute) ? e.number(attribute) : 0.0;
    }

    private static int toInt(long value) throws CorruptSaveException {
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new CorruptSaveException("Integer out of range: " + value, null);
        }
        return (int) value;
    }

    private static void writeCreature(Element e, CreatureState c) {
        e.setAttribute("id", Long.toString(c.id()));
        e.setAttribute("speciesId", c.speciesId());
        e.setAttribute("player", Boolean.toString(c.player()));
        setNumber(e, "x", c.x());
        setNumber(e, "y", c.y());
        setNumber(e, "vx", c.vx());
        setNumber(e, "vy", c.vy());
        setNumber(e, "sizeMeters", c.sizeMeters());
        setNumber(e, "energy", c.energy());
        setNumber(e, "ageSeconds", c.ageSeconds());
        setNumber(e, "digestSeconds", c.digestSeconds());
        setNumber(e, "reproductionSeconds", c.reproductionSeconds());
        setNumber(e, "oxygen", c.oxygen());
        setNumber(e, "health", c.health());
        setNumber(e, "growthPoints", c.growthPoints());
        setNumber(e, "protectionSeconds", c.protectionSeconds());
        setNumber(e, "wanderAngle", c.wanderAngle());
        setNumber(e, "burstSeconds", c.burstSeconds());
        setNumber(e, "burstRecoverySeconds", c.burstRecoverySeconds());
        setNumber(e, "burstDirectionX", c.burstDirectionX());
        setNumber(e, "burstDirectionY", c.burstDirectionY());
        if (c.destination() != null) {
            setNumber(e, "destinationX", c.destination().x());
            setNumber(e, "destinationY", c.destination().y());
        }
    }

    private static void writeFood(Element e, FoodState f) {
        e.setAttribute("id", Long.toString(f.id()));
        setNumber(e, "x", f.x());
        setNumber(e, "y", f.y());
        setNumber(e, "secondsLeft", f.secondsLeft());
    }

    private static void writeEvent(Element e, EventState s) {
        e.setAttribute("type", s.type().name());
        setNumber(e, "elapsedSeconds", s.elapsedSeconds());
        setNumber(e, "durationSeconds", s.durationSeconds());
        setNumber(e, "direction", s.direction());
        e.setAttribute("speciesId", s.speciesId());
    }

    private static Element add(Element parent, String tag) {
        Element child = parent.getOwnerDocument().createElement(tag);
        parent.appendChild(child);
        return child;
    }

    private static void setNumber(Element element, String attribute, double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Cannot save non-finite number in '" + attribute + "': " + value);
        }
        element.setAttribute(attribute, Double.toString(value));
    }
}
