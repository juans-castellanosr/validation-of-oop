package co.edu.autonoma.ancientseas.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.autonoma.ancientseas.TestSpecies;
import co.edu.autonoma.ancientseas.error.CorruptSaveException;
import co.edu.autonoma.ancientseas.error.IncompatibleSaveException;
import co.edu.autonoma.ancientseas.game.GameMemento;
import co.edu.autonoma.ancientseas.game.GameSession;
import co.edu.autonoma.ancientseas.game.GameSettings;
import co.edu.autonoma.ancientseas.model.Vector2;
import co.edu.autonoma.ancientseas.simulation.EventState;
import co.edu.autonoma.ancientseas.simulation.EventType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SaveGameXmlTest {

    @TempDir
    Path dir;

    private final SaveGameXml xml = new SaveGameXml();
    private GameMemento memento;
    private Path valid;

    @BeforeEach
    void playAndSave() throws IOException {
        GameSession session = GameSession.newGame(TestSpecies.catalog(), GameSettings.defaults(), new Random(1));
        session.start();
        for (int i = 0; i < 400; i++) {
            session.step(0.1);
        }
        memento = session.toMemento();
        valid = dir.resolve("valid.xml");
        xml.write(memento, valid);
    }

    private Path text(String content) throws IOException {
        Path file = dir.resolve("case.xml");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    private String validText() throws IOException {
        return Files.readString(valid, StandardCharsets.UTF_8);
    }

    @Test
    void roundTripGivesAnEqualMemento() throws Exception {
        assertFalse(memento.creatures().isEmpty());
        assertEquals(memento, xml.read(valid));
    }

    @Test
    void roundTripKeepsTheClickDestination() throws Exception {
        GameSession session = GameSession.newGame(TestSpecies.catalog(), GameSettings.defaults(), new Random(2));
        session.start();
        session.player().swimTo(new Vector2(1234.5, 678.25));
        session.step(0.1);
        GameMemento heading = session.toMemento();
        Path file = dir.resolve("heading.xml");

        xml.write(heading, file);

        assertEquals(heading, xml.read(file));
        assertEquals(new Vector2(1234.5, 678.25),
                heading.creatures().stream().filter(c -> c.player()).findFirst().orElseThrow().destination());
    }

    @Test
    void roundTripKeepsAwkwardDoublesAndTextExactly() throws Exception {
        GameMemento odd = new GameMemento(
                new GameSettings(memento.settings().difficulty(), 20, memento.settings().eventFrequency(), "Ana <&> \"Z\""),
                Math.nextUp(0.1), Long.MAX_VALUE, memento.creatures(), memento.food(),
                List.of(new EventState(EventType.CURRENT, 1e-300, 1.7976931348623157E308, -1, "")),
                -0.0, memento.populationCaps(), Math.nextDown(1.0));
        Path file = dir.resolve("odd.xml");

        xml.write(odd, file);

        assertEquals(odd, xml.read(file));
    }

    @Test
    void writeLeavesNoTemporaryFiles() throws Exception {
        xml.write(memento, valid);
        try (Stream<Path> files = Files.list(dir)) {
            assertEquals(List.of("valid.xml"), files.map(p -> p.getFileName().toString()).toList());
        }
    }

    @Test
    void refusesToWriteANonFiniteNumber() {
        GameMemento broken = new GameMemento(memento.settings(), Double.NaN, 1, List.of(), List.of(), List.of(), 1,
                memento.populationCaps(), 0);

        assertThrows(IllegalArgumentException.class, () -> xml.write(broken, dir.resolve("never.xml")));
        assertFalse(Files.exists(dir.resolve("never.xml")));
    }

    @Test
    void truncatedFileIsCorrupt() throws Exception {
        String full = validText();
        assertThrows(CorruptSaveException.class, () -> xml.read(text(full.substring(0, full.length() / 2))));
    }

    @Test
    void plainTextEmptyAndMissingFilesAreCorrupt() throws Exception {
        assertThrows(CorruptSaveException.class, () -> xml.read(text("esto no es XML")));
        assertThrows(CorruptSaveException.class, () -> xml.read(text("")));
        assertThrows(CorruptSaveException.class, () -> xml.read(dir.resolve("absent.xml")));
    }

    @Test
    void doctypeAndExternalEntitiesAreRefused() throws Exception {
        Path secret = dir.resolve("secret.txt");
        Files.writeString(secret, "TOP-SECRET");
        String attack = "<?xml version=\"1.0\"?><!DOCTYPE s [<!ENTITY x SYSTEM \"" + secret.toUri()
                + "\">]><ancientSeasSave formatVersion=\"1\"><settings difficulty=\"&x;\"/></ancientSeasSave>";

        CorruptSaveException e = assertThrows(CorruptSaveException.class, () -> xml.read(text(attack)));

        assertFalse(String.valueOf(e.getMessage()).contains("TOP-SECRET"));
        assertFalse(String.valueOf(e.getCause()).contains("TOP-SECRET"));
    }

    @Test
    void billionLaughsIsRefused() {
        String bomb = "<?xml version=\"1.0\"?><!DOCTYPE l [<!ENTITY a \"aaaaaaaaaa\"><!ENTITY b \"&a;&a;&a;&a;\">]>"
                + "<ancientSeasSave formatVersion=\"1\">&b;</ancientSeasSave>";

        assertThrows(CorruptSaveException.class, () -> xml.read(text(bomb)));
    }

    @Test
    void nonFiniteAndUnreadableNumbersAreCorrupt() throws Exception {
        String full = validText();
        String elapsed = "elapsedSeconds=\"";
        int at = full.indexOf(elapsed) + elapsed.length();
        int end = full.indexOf('"', at);
        for (String bad : List.of("NaN", "Infinity", "-Infinity", "abc", "")) {
            String edited = full.substring(0, at) + bad + full.substring(end);
            assertThrows(CorruptSaveException.class, () -> xml.read(text(edited)), bad);
        }
    }

    @Test
    void missingAttributeElementAndUnknownEnumAreCorrupt() throws Exception {
        String full = validText();
        assertThrows(CorruptSaveException.class, () -> xml.read(text(full.replaceFirst(" nextId=\"[^\"]*\"", ""))));
        assertThrows(CorruptSaveException.class, () -> xml.read(text(full.replaceFirst(" planktonDue=\"[^\"]*\"", ""))));
        assertThrows(CorruptSaveException.class, () -> xml.read(text(full.replaceFirst(" burstRecoverySeconds=\"[^\"]*\"", ""))));
        assertThrows(CorruptSaveException.class, () -> xml.read(text(full.replaceFirst("wanderAngle=\"[^\"]*\"", "wanderAngle=\"NaN\""))));
        assertThrows(CorruptSaveException.class, () -> xml.read(text(full.replaceAll("(?s)<food>.*</food>", ""))));
        assertThrows(CorruptSaveException.class, () -> xml.read(text(full.replaceFirst("difficulty=\"NORMAL\"", "difficulty=\"IMPOSSIBLE\""))));
        assertThrows(CorruptSaveException.class, () -> xml.read(text(full.replaceFirst("creatureCount=\"45\"", "creatureCount=\"5000\""))));
        assertThrows(CorruptSaveException.class, () -> xml.read(text(full.replaceFirst("player=\"true\"", "player=\"yes\""))));
    }

    @Test
    void otherVersionsAndOtherDocumentsAreIncompatible() throws Exception {
        String full = validText();
        assertThrows(IncompatibleSaveException.class, () -> xml.read(text(full.replace("formatVersion=\"1\"", "formatVersion=\"99\""))));
        assertThrows(IncompatibleSaveException.class, () -> xml.read(text("<recipe formatVersion=\"1\"/>")));
        assertTrue(assertThrows(CorruptSaveException.class,
                () -> xml.read(text(full.replace("formatVersion=\"1\"", "formatVersion=\"uno\"")))).getMessage()
                .contains("formatVersion"));
    }
}
