package co.edu.autonoma.ancientseas.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.autonoma.ancientseas.TestSpecies;
import co.edu.autonoma.ancientseas.catalog.SpeciesCatalog;
import co.edu.autonoma.ancientseas.game.Difficulty;
import co.edu.autonoma.ancientseas.game.GameStatus;
import co.edu.autonoma.ancientseas.game.ScoreEntry;
import co.edu.autonoma.ancientseas.model.GrowthStage;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class XmlExporterTest {

    private static final ScoreEntry RESULT = new ScoreEntry("A<b>&\"c\" Ñu", 42, GrowthStage.ADULT, GameStatus.WON,
            61.5, Difficulty.NORMAL, LocalDateTime.of(2026, 11, 21, 9, 0));

    @TempDir
    Path dir;

    private final XmlExporter exporter = new XmlExporter();

    private static Document parse(Path file) throws Exception {
        try (InputStream in = Files.newInputStream(file)) {
            return XmlDocuments.parse(in);
        }
    }

    @Test
    void scoresAreEscapedIndentedAndParseBack() throws Exception {
        Path file = dir.resolve("scores.xml");

        exporter.exportScores(List.of(RESULT, RESULT), file);

        String raw = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(raw.startsWith("<?xml"));
        assertTrue(raw.contains("&lt;b&gt;&amp;"), raw);
        assertTrue(raw.contains("\n  <entry"), raw);
        NodeList entries = parse(file).getElementsByTagName("entry");
        assertEquals(2, entries.getLength());
        Element first = (Element) entries.item(0);
        assertEquals(RESULT.playerName(), first.getAttribute("player"));
        assertEquals("Adulto", first.getAttribute("stage"));
        assertEquals("2026-11-21T09:00", first.getAttribute("playedAt"));
    }

    @Test
    void expeditionReportListsEverySpeciesWithItsCensus() throws Exception {
        SpeciesCatalog catalog = TestSpecies.catalog();
        Path file = dir.resolve("report.xml");

        exporter.exportExpeditionReport(RESULT, Map.of("tylosaurus", 3), catalog, file);

        Document doc = parse(file);
        assertEquals(RESULT.playerName(), ((Element) doc.getElementsByTagName("result").item(0)).getAttribute("player"));
        NodeList species = ((Element) doc.getElementsByTagName("species").item(0)).getElementsByTagName("species");
        assertEquals(catalog.size(), species.getLength());
        for (int i = 0; i < species.getLength(); i++) {
            Element e = (Element) species.item(i);
            String expected = e.getAttribute("id").equals("tylosaurus") ? "3" : "0";
            assertEquals(expected, e.getElementsByTagName("aliveAtEnd").item(0).getTextContent());
            assertEquals(1, e.getElementsByTagName("ageMa").getLength());
        }
    }

    @Test
    void failureToWriteIsReportedNotSwallowed() throws Exception {
        Path blocker = dir.resolve("file");
        Files.writeString(blocker, "x");

        org.junit.jupiter.api.Assertions.assertThrows(java.io.IOException.class,
                () -> exporter.exportScores(List.of(), blocker.resolve("scores.xml")));
    }
}
