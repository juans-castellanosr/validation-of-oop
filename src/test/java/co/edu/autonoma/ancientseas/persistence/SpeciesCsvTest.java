package co.edu.autonoma.ancientseas.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.autonoma.ancientseas.TestSpecies;
import co.edu.autonoma.ancientseas.catalog.Species;
import co.edu.autonoma.ancientseas.catalog.SpeciesCatalog;
import co.edu.autonoma.ancientseas.error.AncientSeasException;
import co.edu.autonoma.ancientseas.error.CatalogFormatException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SpeciesCsvTest {

    private static final String HEADER =
            "id,scientificName,commonName,period,minMa,maxMa,lengthMeters,diet,zone,airBreather,behavior,shape,funFact,source";
    private static final String GOOD_ROW = "tylosaurus,Tylosaurus,Tilosaurio,Cretácico,66,86,12,CARNIVORE,MIDWATER,true,HUNTER,MOSASAUR,Dato,Fuente";

    @TempDir
    Path dir;

    private final SpeciesCsv csv = new SpeciesCsv();

    private Path file(String content) throws IOException {
        Path file = dir.resolve("species.csv");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    private static Species build(Species.Builder builder) throws AncientSeasException {
        return builder.build();
    }

    @Test
    void roundTripReproducesEveryField() throws Exception {
        SpeciesCatalog original = TestSpecies.catalog();
        original.add(build(TestSpecies.valid("tricky").funFact("Dice \"hola\", y\nsigue; aquí").scientificName(" Odd, name ")
                .ageMa(0.0000001, 1234.5).lengthMeters(0.35)));
        Path file = dir.resolve("out.csv");

        csv.write(original, file);
        LoadResult<SpeciesCatalog> loaded = csv.read(file);

        assertFalse(loaded.hasProblems(), loaded.problems().toString());
        assertEquals(original.size(), loaded.value().size());
        for (Species expected : original.all()) {
            Species actual = loaded.value().find(expected.id()).orElseThrow();
            assertEquals(expected.scientificName(), actual.scientificName());
            assertEquals(expected.commonName(), actual.commonName());
            assertEquals(expected.period(), actual.period());
            assertEquals(expected.ageMa(), actual.ageMa());
            assertEquals(expected.lengthMeters(), actual.lengthMeters());
            assertEquals(expected.diet(), actual.diet());
            assertEquals(expected.zone(), actual.zone());
            assertEquals(expected.airBreather(), actual.airBreather());
            assertEquals(expected.behavior(), actual.behavior());
            assertEquals(expected.shape(), actual.shape());
            assertEquals(expected.funFact(), actual.funFact());
            assertEquals(expected.source(), actual.source());
        }
    }

    @Test
    void writeLeavesNoTemporaryFiles() throws Exception {
        csv.write(TestSpecies.catalog(), dir.resolve("a.csv"));
        csv.write(TestSpecies.catalog(), dir.resolve("a.csv"));
        try (Stream<Path> files = Files.list(dir)) {
            assertEquals(List.of("a.csv"), files.map(p -> p.getFileName().toString()).toList());
        }
    }

    @Test
    void semicolonFileAcceptsDecimalCommas() throws Exception {
        String row = "tylosaurus;Tylosaurus;Tilosaurio;Cretácico;66,5;86;0,35;CARNIVORE;MIDWATER;sí;HUNTER;MOSASAUR;Dato, con coma;Fuente";
        LoadResult<SpeciesCatalog> result = csv.read(file(HEADER.replace(',', ';') + "\n" + row + "\n"));

        assertFalse(result.hasProblems(), result.problems().toString());
        Species s = result.value().find("tylosaurus").orElseThrow();
        assertEquals(66.5, s.ageMa().min());
        assertEquals(0.35, s.lengthMeters());
        assertTrue(s.airBreather());
        assertEquals("Dato, con coma", s.funFact());
    }

    @Test
    void commaFileDoesNotAcceptDecimalCommas() throws Exception {
        String row = "tylosaurus,Tylosaurus,Tilosaurio,Cretácico,66,86,\"0,35\",CARNIVORE,MIDWATER,true,HUNTER,MOSASAUR,Dato,Fuente";
        LoadResult<SpeciesCatalog> result = csv.read(file(HEADER + "\n" + row + "\n"));

        assertEquals(0, result.value().size());
        assertTrue(result.problems().get(0).startsWith("Fila 2"));
    }

    @Test
    void quotedFieldsKeepCommasNewlinesAndQuotes() throws Exception {
        String row = "tylosaurus,Tylosaurus,\"Tilo, \"\"el grande\"\"\",Cretácico,66,86,12,carnivore,midwater,TRUE,hunter,mosasaur,\"línea 1\r\nlínea 2\",Fuente";
        LoadResult<SpeciesCatalog> result = csv.read(file(HEADER + "\r\n" + row + "\r\n\r\n"));

        assertFalse(result.hasProblems(), result.problems().toString());
        Species s = result.value().find("tylosaurus").orElseThrow();
        assertEquals("Tilo, \"el grande\"", s.commonName());
        assertEquals("línea 1\r\nlínea 2", s.funFact());
    }

    @Test
    void toleratesByteOrderMarkBlankLinesAndColumnOrder() throws Exception {
        String reordered = "\uFEFFSOURCE,Id,funFact,shape,behavior,airBreather,zone,diet,lengthMeters,maxMa,minMa,period,commonName,scientificName\n\n"
                + "Fuente,tylosaurus,Dato,MOSASAUR,HUNTER,true,MIDWATER,CARNIVORE,12,86,66,Cretácico,Tilosaurio,Tylosaurus\n\n";
        LoadResult<SpeciesCatalog> result = csv.read(file(reordered));

        assertFalse(result.hasProblems(), result.problems().toString());
        assertEquals(66, result.value().find("tylosaurus").orElseThrow().ageMa().min());
    }

    @Test
    void extraColumnIsIgnoredAndReportedOnce() throws Exception {
        String content = HEADER + ",notes\n" + GOOD_ROW + ",x\n" + GOOD_ROW.replace("tylosaurus", "other") + ",y\n";
        LoadResult<SpeciesCatalog> result = csv.read(file(content));

        assertEquals(2, result.value().size());
        assertEquals(1, result.problems().size());
        assertTrue(result.problems().get(0).contains("notes"));
    }

    @Test
    void badRowsAreReportedWithTheirRowAndNeverStopTheLoad() throws Exception {
        String content = String.join("\n", HEADER,
                GOOD_ROW,
                GOOD_ROW.replace("tylosaurus", "negative").replace(",12,", ",-3,"),
                GOOD_ROW.replace("tylosaurus", "short").replace(",Dato,Fuente", ""),
                GOOD_ROW.replace("tylosaurus", "badnumber").replace(",66,", ",abc,"),
                GOOD_ROW.replace("tylosaurus", "badenum").replace("CARNIVORE", "PLANTEATER"),
                GOOD_ROW.replace("tylosaurus", "badflag").replace(",true,", ",quizás,"),
                GOOD_ROW.replace("tylosaurus", "reversed").replace(",66,86,", ",90,10,"),
                GOOD_ROW.replace("tylosaurus", "nan").replace(",12,", ",NaN,"),
                GOOD_ROW,
                GOOD_ROW.replace("tylosaurus", "last"), "");
        LoadResult<SpeciesCatalog> result = csv.read(file(content));

        assertEquals(List.of("tylosaurus", "last"), result.value().all().stream().map(Species::id).toList());
        List<String> problems = result.problems();
        assertEquals(8, problems.size(), problems.toString());
        assertTrue(problems.get(0).startsWith("Fila 3"));
        assertTrue(problems.get(1).startsWith("Fila 4") && problems.get(1).contains("campos"));
        assertTrue(problems.get(2).contains("número"));
        assertTrue(problems.get(3).contains("PLANTEATER"));
        assertTrue(problems.get(7).startsWith("Fila 10") && problems.get(7).contains("tylosaurus"));
    }

    @Test
    void duplicateIdKeepsTheFirst() throws Exception {
        String content = HEADER + "\n" + GOOD_ROW + "\n" + GOOD_ROW.replace("Tilosaurio", "Copia") + "\n";
        LoadResult<SpeciesCatalog> result = csv.read(file(content));

        assertEquals("Tilosaurio", result.value().find("tylosaurus").orElseThrow().commonName());
        assertEquals(1, result.problems().size());
    }

    @Test
    void unusableFilesThrowACatalogFormatException() throws Exception {
        assertThrows(CatalogFormatException.class, () -> csv.read(file("")));
        assertThrows(CatalogFormatException.class, () -> csv.read(file("\n \n")));
        assertThrows(CatalogFormatException.class, () -> csv.read(file("id,commonName\nx,y\n")));
        assertThrows(CatalogFormatException.class, () -> csv.read(file(HEADER + "\n\"abierta,x\n")));
        assertThrows(CatalogFormatException.class, () -> csv.read(dir.resolve("missing.csv")));
        assertThrows(CatalogFormatException.class, () -> csv.read(dir));
        Path latin1 = dir.resolve("latin1.csv");
        Files.write(latin1, (HEADER + "\n" + GOOD_ROW.replace("Cretácico", "Cret\u00e1cico")).getBytes(StandardCharsets.ISO_8859_1));
        assertThrows(CatalogFormatException.class, () -> csv.read(latin1));
    }

    @Test
    void headerOnlyGivesAnEmptyCatalog() throws Exception {
        LoadResult<SpeciesCatalog> result = csv.read(file(HEADER + "\n"));

        assertEquals(0, result.value().size());
        assertFalse(result.hasProblems());
    }

    @Test
    void failedWriteKeepsTheOldFile() throws Exception {
        Path target = dir.resolve("kept.csv");
        csv.write(TestSpecies.catalog(), target);
        String before = Files.readString(target);
        Path blocked = dir.resolve("kept.csv").resolve("child.csv");

        assertThrows(IOException.class, () -> csv.write(TestSpecies.catalog(), blocked));
        assertEquals(before, Files.readString(target));
    }
}
