package co.edu.autonoma.ancientseas.persistence;

import co.edu.autonoma.ancientseas.catalog.Species;
import co.edu.autonoma.ancientseas.catalog.SpeciesCatalog;
import co.edu.autonoma.ancientseas.game.ScoreEntry;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Writes the score table and a visitor's expedition report as XML, built as a DOM so text is always escaped. */
public final class XmlExporter {

    /** Creates the exporter; each method receives the file it writes. */
    public XmlExporter() {
    }

    /**
     * Exports a score table.
     *
     * @param scores the entries to export, in the order they should appear
     * @param file   the target file
     * @throws IOException if the file cannot be written
     */
    public void exportScores(Collection<ScoreEntry> scores, Path file) throws IOException {
        Document document = XmlDocuments.create();
        Element root = document.createElement("scores");
        document.appendChild(root);
        for (ScoreEntry score : scores) {
            addResult(root, "entry", score);
        }
        AtomicFile.write(file, XmlDocuments.toBytes(document));
    }

    /**
     * Exports one visitor's result together with what lived in the ocean at the end.
     *
     * @param result  the visitor's finished game
     * @param census  living individuals per species id at the end; absent ids count as zero
     * @param catalog the catalog whose species are listed, in catalog order
     * @param file    the target file
     * @throws IOException if the file cannot be written
     */
    public void exportExpeditionReport(ScoreEntry result, Map<String, Integer> census, SpeciesCatalog catalog,
            Path file) throws IOException {
        Document document = XmlDocuments.create();
        Element root = document.createElement("expeditionReport");
        document.appendChild(root);
        addResult(root, "result", result);
        Element speciesList = add(root, "species");
        for (Species species : catalog.all()) {
            Element element = add(speciesList, "species");
            element.setAttribute("id", species.id());
            addText(element, "scientificName", species.scientificName());
            addText(element, "commonName", species.commonName());
            addText(element, "period", species.period());
            Element age = add(element, "ageMa");
            age.setAttribute("youngest", Double.toString(species.ageMa().min()));
            age.setAttribute("oldest", Double.toString(species.ageMa().max()));
            addText(element, "lengthMeters", Double.toString(species.lengthMeters()));
            addText(element, "diet", species.diet().displayName());
            addText(element, "zone", species.zone().displayName());
            addText(element, "aliveAtEnd", Integer.toString(census.getOrDefault(species.id(), 0)));
        }
        AtomicFile.write(file, XmlDocuments.toBytes(document));
    }

    private static void addResult(Element parent, String tag, ScoreEntry score) {
        Element element = add(parent, tag);
        element.setAttribute("player", score.playerName());
        element.setAttribute("score", Integer.toString(score.score()));
        element.setAttribute("stage", score.stage().displayName());
        element.setAttribute("outcome", score.outcome().name());
        element.setAttribute("durationSeconds", Double.toString(score.durationSeconds()));
        element.setAttribute("difficulty", score.difficulty().displayName());
        element.setAttribute("playedAt", score.playedAt().toString());
    }

    private static Element add(Element parent, String tag) {
        Element child = parent.getOwnerDocument().createElement(tag);
        parent.appendChild(child);
        return child;
    }

    private static void addText(Element parent, String tag, String text) {
        add(parent, tag).setTextContent(text);
    }
}
