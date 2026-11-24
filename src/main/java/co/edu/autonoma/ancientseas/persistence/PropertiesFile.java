package co.edu.autonoma.ancientseas.persistence;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

final class PropertiesFile {

    private PropertiesFile() {
    }

    static Properties read(Path file) throws IOException {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IllegalArgumentException e) {
            throw new IOException("Malformed properties file " + file, e);
        }
        return properties;
    }

    static void write(Properties properties, Path file, String comment) throws IOException {
        StringWriter out = new StringWriter();
        properties.store(out, comment);
        AtomicFile.write(file, out.toString().getBytes(StandardCharsets.UTF_8));
    }
}
