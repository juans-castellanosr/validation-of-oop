package co.edu.autonoma.ancientseas.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.autonoma.ancientseas.TestSpecies;
import co.edu.autonoma.ancientseas.catalog.SpeciesCatalog;
import co.edu.autonoma.ancientseas.persistence.DataDirectory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileMuseumServicesTest {

    @TempDir
    Path dir;

    private FileMuseumServices open() throws Exception {
        DataDirectory data = new DataDirectory(dir);
        data.seedIfMissing();
        return new FileMuseumServices(data);
    }

    @Test
    void anEditedCopyReachesTheGameOnlyWhenSaved() throws Exception {
        FileMuseumServices services = open();
        SpeciesCatalog draft = new SpeciesCatalog(services.catalog());
        draft.add(TestSpecies.valid("nueva-especie").build());

        assertFalse(services.catalog().find("nueva-especie").isPresent(), "unsaved changes must not be live");

        services.saveCatalog(draft);

        assertTrue(services.catalog().find("nueva-especie").isPresent());
        assertTrue(open().catalog().find("nueva-especie").isPresent(), "the change must survive a restart");
    }

    @Test
    void savingAfterSkippedRowsKeepsABackupWithTheOriginalRows() throws Exception {
        open();
        Path csv = dir.resolve("species.csv");
        String badRow = "roto,Brokenus,Roto,Cretácico,70,60,-3,CARNIVORE,MIDWATER,false,HUNTER,SHARK,dato,fuente";
        Files.writeString(csv, Files.readString(csv, StandardCharsets.UTF_8).stripTrailing() + "\n" + badRow + "\n",
                StandardCharsets.UTF_8);
        FileMuseumServices services = open();
        assertFalse(services.catalogProblems().isEmpty(), "the bad row must be reported");

        services.saveCatalog(new SpeciesCatalog(services.catalog()));

        List<Path> backups;
        try (Stream<Path> files = Files.list(dir)) {
            backups = files.filter(p -> p.getFileName().toString().endsWith(".csv.bak")).toList();
        }
        assertEquals(1, backups.size());
        assertTrue(Files.readString(backups.get(0), StandardCharsets.UTF_8).contains(badRow));
        assertFalse(Files.readString(csv, StandardCharsets.UTF_8).contains(badRow));
        assertTrue(services.catalogProblems().isEmpty(), "the warnings described the old file");
    }

    @Test
    void savingACleanCatalogMakesNoBackup() throws Exception {
        FileMuseumServices services = open();

        services.saveCatalog(new SpeciesCatalog(services.catalog()));

        try (Stream<Path> files = Files.list(dir)) {
            assertEquals(0, files.filter(p -> p.getFileName().toString().endsWith(".bak")).count());
        }
    }
}
