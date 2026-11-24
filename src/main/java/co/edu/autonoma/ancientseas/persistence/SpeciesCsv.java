package co.edu.autonoma.ancientseas.persistence;

import co.edu.autonoma.ancientseas.catalog.Behavior;
import co.edu.autonoma.ancientseas.catalog.BodyShape;
import co.edu.autonoma.ancientseas.catalog.Diet;
import co.edu.autonoma.ancientseas.catalog.Species;
import co.edu.autonoma.ancientseas.catalog.SpeciesCatalog;
import co.edu.autonoma.ancientseas.catalog.Zone;
import co.edu.autonoma.ancientseas.error.AncientSeasException;
import co.edu.autonoma.ancientseas.error.CatalogFormatException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Reads and writes the species catalog as CSV that staff can edit in a spreadsheet. Reading is forgiving (locale
 * separators, column order, bad rows); writing always produces the same canonical form.
 */
public final class SpeciesCsv {

    private static final String ID = "id";
    private static final String SCIENTIFIC_NAME = "scientificName";
    private static final String COMMON_NAME = "commonName";
    private static final String PERIOD = "period";
    private static final String MIN_MA = "minMa";
    private static final String MAX_MA = "maxMa";
    private static final String LENGTH_METERS = "lengthMeters";
    private static final String DIET = "diet";
    private static final String ZONE = "zone";
    private static final String AIR_BREATHER = "airBreather";
    private static final String BEHAVIOR = "behavior";
    private static final String SHAPE = "shape";
    private static final String FUN_FACT = "funFact";
    private static final String SOURCE = "source";

    private static final List<String> COLUMNS = List.of(ID, SCIENTIFIC_NAME, COMMON_NAME, PERIOD, MIN_MA, MAX_MA,
            LENGTH_METERS, DIET, ZONE, AIR_BREATHER, BEHAVIOR, SHAPE, FUN_FACT, SOURCE);

    private static final Pattern NUMBER = Pattern.compile("[+-]?\\d+(\\.\\d+)?([eE][+-]?\\d+)?");
    private static final String BOM = "\uFEFF";
    private static final String UNUSABLE_FILE =
            "No se pudo abrir el catálogo de especies. Revise que el archivo exista y que esté guardado como CSV UTF-8.";

    /** Creates the reader and writer; each method receives the file it works on. */
    public SpeciesCsv() {
    }

    /**
     * Reads a catalog, recovering from bad rows.
     *
     * @param file the CSV file
     * @return the catalog of valid rows and one Spanish problem per row or column that was left out
     * @throws CatalogFormatException if the file cannot be read, is empty or lacks required columns
     */
    public LoadResult<SpeciesCatalog> read(Path file) throws CatalogFormatException {
        String text = readText(file);
        char delimiter = detectDelimiter(text);
        List<CsvRecord> records = parseRecords(text, delimiter);
        if (records.isEmpty()) {
            throw new CatalogFormatException("Catalog file is empty: " + file,
                    "El archivo del catálogo está vacío. Debe tener al menos la fila de encabezados.");
        }
        List<String> problems = new ArrayList<>();
        CsvRecord header = records.get(0);
        Map<String, Integer> columns = indexColumns(header, problems);
        boolean decimalComma = delimiter == ';';
        SpeciesCatalog catalog = new SpeciesCatalog();
        for (CsvRecord row : records.subList(1, records.size())) {
            if (row.fields().size() != header.fields().size()) {
                problems.add(rowProblem(row, "tiene " + row.fields().size() + " campos y se esperaban "
                        + header.fields().size() + ". Se omitió."));
                continue;
            }
            try {
                catalog.add(parseSpecies(row, columns, decimalComma));
            } catch (AncientSeasException e) {
                problems.add(rowProblem(row, e.getFriendlyMessage() + " Se omitió."));
            } catch (IllegalArgumentException e) {
                problems.add(rowProblem(row, e.getMessage() + " Se omitió."));
            }
        }
        return LoadResult.withProblems(catalog, problems);
    }

    /**
     * Writes the catalog in canonical form, replacing the file atomically.
     *
     * @param catalog the species to write
     * @param file    the target file
     * @throws IOException if the file cannot be written
     */
    public void write(SpeciesCatalog catalog, Path file) throws IOException {
        StringBuilder text = new StringBuilder(CsvWriter.line(COLUMNS));
        for (Species species : catalog.all()) {
            text.append(CsvWriter.line(toFields(species)));
        }
        AtomicFile.write(file, text.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String readText(Path file) throws CatalogFormatException {
        try {
            String text = Files.readString(file, StandardCharsets.UTF_8);
            return text.startsWith(BOM) ? text.substring(1) : text;
        } catch (IOException e) {
            throw new CatalogFormatException("Cannot read catalog file " + file, UNUSABLE_FILE, e);
        }
    }

    private static char detectDelimiter(String text) {
        String header = text.stripLeading().lines().findFirst().orElse("");
        return header.indexOf(';') >= 0 ? ';' : ',';
    }

    private static List<CsvRecord> parseRecords(String text, char delimiter) throws CatalogFormatException {
        try {
            return CsvParser.parse(text, delimiter);
        } catch (IllegalArgumentException e) {
            throw new CatalogFormatException("Unterminated quote in catalog: " + e.getMessage(), e.getMessage(), e);
        }
    }

    private static Map<String, Integer> indexColumns(CsvRecord header, List<String> problems)
            throws CatalogFormatException {
        Map<String, String> canonicalByLowerCase = new HashMap<>();
        COLUMNS.forEach(name -> canonicalByLowerCase.put(name.toLowerCase(Locale.ROOT), name));
        Map<String, Integer> positions = new HashMap<>();
        Set<String> unknown = new LinkedHashSet<>();
        List<String> names = header.fields();
        for (int i = 0; i < names.size(); i++) {
            String canonical = canonicalByLowerCase.get(names.get(i).strip().toLowerCase(Locale.ROOT));
            if (canonical == null) {
                unknown.add(names.get(i).strip());
            } else {
                positions.putIfAbsent(canonical, i);
            }
        }
        List<String> missing = COLUMNS.stream().filter(name -> !positions.containsKey(name)).toList();
        if (!missing.isEmpty()) {
            throw new CatalogFormatException("Missing catalog columns: " + missing,
                    "Al archivo del catálogo le faltan estas columnas: " + String.join(", ", missing) + ".");
        }
        if (!unknown.isEmpty()) {
            problems.add("Se ignoraron las columnas desconocidas: " + String.join(", ", unknown) + ".");
        }
        return positions;
    }

    private static Species parseSpecies(CsvRecord row, Map<String, Integer> columns, boolean decimalComma)
            throws AncientSeasException {
        Species.Builder builder = Species.builder()
                .id(cell(row, columns, ID))
                .scientificName(cell(row, columns, SCIENTIFIC_NAME))
                .commonName(cell(row, columns, COMMON_NAME))
                .period(cell(row, columns, PERIOD))
                .ageMa(number(row, columns, MIN_MA, decimalComma), number(row, columns, MAX_MA, decimalComma))
                .lengthMeters(number(row, columns, LENGTH_METERS, decimalComma))
                .diet(constant(row, columns, DIET, Diet.class))
                .zone(constant(row, columns, ZONE, Zone.class))
                .airBreather(flag(row, columns, AIR_BREATHER))
                .behavior(constant(row, columns, BEHAVIOR, Behavior.class))
                .shape(constant(row, columns, SHAPE, BodyShape.class))
                .funFact(cell(row, columns, FUN_FACT))
                .source(cell(row, columns, SOURCE));
        return builder.build();
    }

    private static String cell(CsvRecord row, Map<String, Integer> columns, String column) {
        return row.fields().get(columns.get(column));
    }

    private static double number(CsvRecord row, Map<String, Integer> columns, String column, boolean decimalComma) {
        String raw = cell(row, columns, column);
        String text = raw.strip();
        if (decimalComma) {
            text = text.replace(',', '.');
        }
        if (!NUMBER.matcher(text).matches()) {
            throw new IllegalArgumentException("El dato \"" + column + "\" debe ser un número, pero dice \"" + raw + "\".");
        }
        double value = Double.parseDouble(text);
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("El dato \"" + column + "\" es demasiado grande: \"" + raw + "\".");
        }
        return value;
    }

    private static boolean flag(CsvRecord row, Map<String, Integer> columns, String column) {
        String raw = cell(row, columns, column);
        return switch (raw.strip().toLowerCase(Locale.ROOT)) {
            case "true", "sí", "si" -> true;
            case "false", "no" -> false;
            default -> throw new IllegalArgumentException(
                    "El dato \"" + column + "\" debe ser true o false (también sí o no), pero dice \"" + raw + "\".");
        };
    }

    private static <E extends Enum<E>> E constant(CsvRecord row, Map<String, Integer> columns, String column,
            Class<E> type) {
        try {
            return EnumParser.parse(type, cell(row, columns, column));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El dato \"" + column + "\": " + e.getMessage(), e);
        }
    }

    private static String rowProblem(CsvRecord row, String detail) {
        return "Fila " + row.line() + ": " + detail;
    }

    private static List<String> toFields(Species s) {
        return List.of(s.id(), s.scientificName(), s.commonName(), s.period(), Double.toString(s.ageMa().min()),
                Double.toString(s.ageMa().max()), Double.toString(s.lengthMeters()), s.diet().name(),
                s.zone().name(), Boolean.toString(s.airBreather()), s.behavior().name(), s.shape().name(),
                s.funFact(), s.source());
    }
}
