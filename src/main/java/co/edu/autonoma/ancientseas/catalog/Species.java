package co.edu.autonoma.ancientseas.catalog;

import co.edu.autonoma.ancientseas.error.HabitatViolationException;
import co.edu.autonoma.ancientseas.error.InvalidScientificValueException;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Immutable, always valid record of one species; obtained only through {@link Builder#build()},
 * and edited by deriving a copy with {@link #toBuilder()}.
 */
public final class Species {

    public static final double OLDEST_AGE_MA = 4000.0;
    public static final double MAX_LENGTH_METERS = 35.0;

    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*");

    private final String id;
    private final String scientificName;
    private final String commonName;
    private final String period;
    private final Range<Double> ageMa;
    private final double lengthMeters;
    private final Diet diet;
    private final Zone zone;
    private final boolean airBreather;
    private final Behavior behavior;
    private final BodyShape shape;
    private final String funFact;
    private final String source;

    private Species(Builder b) {
        this.id = b.id;
        this.scientificName = b.scientificName;
        this.commonName = b.commonName;
        this.period = b.period;
        this.ageMa = Range.of(b.minMa, b.maxMa);
        this.lengthMeters = b.lengthMeters;
        this.diet = b.diet;
        this.zone = b.zone;
        this.airBreather = b.airBreather;
        this.behavior = b.behavior;
        this.shape = b.shape;
        this.funFact = b.funFact;
        this.source = b.source;
    }

    /**
     * Starts building a new species.
     *
     * @return an empty builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Starts a builder pre-filled with this species, to derive an edited copy.
     *
     * @return a builder holding every field of this species
     */
    public Builder toBuilder() {
        return new Builder().id(id).scientificName(scientificName).commonName(commonName).period(period)
                .ageMa(ageMa.min(), ageMa.max()).lengthMeters(lengthMeters).diet(diet).zone(zone)
                .airBreather(airBreather).behavior(behavior).shape(shape).funFact(funFact).source(source);
    }

    /**
     * Returns the identifier.
     *
     * @return unique kebab-case id, e.g. {@code archelon-ischyros}
     */
    public String id() {
        return id;
    }

    /**
     * Returns the scientific name.
     *
     * @return the Latin binomial or genus name
     */
    public String scientificName() {
        return scientificName;
    }

    /**
     * Returns the common name.
     *
     * @return the Spanish name shown to visitors
     */
    public String commonName() {
        return commonName;
    }

    /**
     * Returns the geological period.
     *
     * @return the period, in Spanish
     */
    public String period() {
        return period;
    }

    /**
     * Returns the age range.
     *
     * @return millions of years before present
     */
    public Range<Double> ageMa() {
        return ageMa;
    }

    /**
     * Describes in one comparable text the values that shape how the species lives in the game;
     * saved games keep it to detect later catalog edits.
     *
     * @return for example {@code "HUNTER;true;MIDWATER;CARNIVORE;9.0;MOSASAUR"}
     */
    public String gameDefinition() {
        return String.join(";", behavior.name(), Boolean.toString(airBreather), zone.name(), diet.name(),
                Double.toString(lengthMeters), shape.name());
    }

    /**
     * Tells whether the species existed at a moment.
     *
     * @param millionYearsAgo the moment, in millions of years before present
     * @return {@code true} if the moment lies inside the age range
     */
    public boolean livedAt(double millionYearsAgo) {
        return ageMa.contains(millionYearsAgo);
    }

    /**
     * Tells whether the species existed at some point of a period.
     *
     * @param period the period, in millions of years before present
     * @return {@code true} if the age range and the period overlap
     */
    public boolean livedAt(Range<Double> period) {
        return ageMa.overlaps(period);
    }

    /**
     * Returns the adult length.
     *
     * @return approximate length in meters
     */
    public double lengthMeters() {
        return lengthMeters;
    }

    /**
     * Returns the diet.
     *
     * @return what the species eats
     */
    public Diet diet() {
        return diet;
    }

    /**
     * Returns the habitat zone.
     *
     * @return the ocean layer where it mostly lived
     */
    public Zone zone() {
        return zone;
    }

    /**
     * Tells whether it breathed air.
     *
     * @return {@code true} if it had to surface
     */
    public boolean airBreather() {
        return airBreather;
    }

    /**
     * Returns the simulation behavior.
     *
     * @return how it acts in the simulation
     */
    public Behavior behavior() {
        return behavior;
    }

    /**
     * Returns the drawing outline.
     *
     * @return the body shape
     */
    public BodyShape shape() {
        return shape;
    }

    /**
     * Returns the fun fact.
     *
     * @return a sourced fact for children, in Spanish
     */
    public String funFact() {
        return funFact;
    }

    /**
     * Returns the data source.
     *
     * @return the reference the data was taken from
     */
    public String source() {
        return source;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Species s && id.equals(s.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return commonName + " (" + scientificName + ")";
    }

    /** Collects the fields of a species and validates them all in {@link #build()}. */
    public static final class Builder {

        private String id;
        private String scientificName;
        private String commonName;
        private String period;
        private double minMa = Double.NaN;
        private double maxMa = Double.NaN;
        private double lengthMeters = Double.NaN;
        private Diet diet;
        private Zone zone;
        private boolean airBreather;
        private Behavior behavior;
        private BodyShape shape;
        private String funFact;
        private String source;

        private Builder() {
        }

        /**
         * Sets the identifier.
         *
         * @param value kebab-case id
         * @return this builder
         */
        public Builder id(String value) {
            this.id = value;
            return this;
        }

        /**
         * Sets the scientific name.
         *
         * @param value Latin name
         * @return this builder
         */
        public Builder scientificName(String value) {
            this.scientificName = value;
            return this;
        }

        /**
         * Sets the common name.
         *
         * @param value Spanish name
         * @return this builder
         */
        public Builder commonName(String value) {
            this.commonName = value;
            return this;
        }

        /**
         * Sets the geological period label.
         *
         * @param value period text
         * @return this builder
         */
        public Builder period(String value) {
            this.period = value;
            return this;
        }

        /**
         * Sets the age range.
         *
         * @param youngestMa most recent age, in millions of years
         * @param oldestMa   oldest age, in millions of years
         * @return this builder
         */
        public Builder ageMa(double youngestMa, double oldestMa) {
            this.minMa = youngestMa;
            this.maxMa = oldestMa;
            return this;
        }

        /**
         * Sets the adult length.
         *
         * @param value length in meters
         * @return this builder
         */
        public Builder lengthMeters(double value) {
            this.lengthMeters = value;
            return this;
        }

        /**
         * Sets the diet.
         *
         * @param value the diet
         * @return this builder
         */
        public Builder diet(Diet value) {
            this.diet = value;
            return this;
        }

        /**
         * Sets the habitat zone.
         *
         * @param value the zone
         * @return this builder
         */
        public Builder zone(Zone value) {
            this.zone = value;
            return this;
        }

        /**
         * Sets whether it breathed air.
         *
         * @param value {@code true} for air-breathers
         * @return this builder
         */
        public Builder airBreather(boolean value) {
            this.airBreather = value;
            return this;
        }

        /**
         * Sets the simulation behavior.
         *
         * @param value the behavior
         * @return this builder
         */
        public Builder behavior(Behavior value) {
            this.behavior = value;
            return this;
        }

        /**
         * Sets the drawing outline.
         *
         * @param value the body shape
         * @return this builder
         */
        public Builder shape(BodyShape value) {
            this.shape = value;
            return this;
        }

        /**
         * Sets the fun fact.
         *
         * @param value Spanish text for children
         * @return this builder
         */
        public Builder funFact(String value) {
            this.funFact = value;
            return this;
        }

        /**
         * Sets the source reference.
         *
         * @param value citation
         * @return this builder
         */
        public Builder source(String value) {
            this.source = value;
            return this;
        }

        /**
         * Validates every field and creates the species.
         *
         * @return the valid, immutable species
         * @throws InvalidScientificValueException if a value is missing or impossible
         * @throws HabitatViolationException       if zone, behavior and breathing do not fit together: a sessile
         *                                         animal off the seafloor, an air-breather on the seafloor or an
         *                                         air-breather that is not a hunter
         */
        public Species build() throws InvalidScientificValueException, HabitatViolationException {
            id = required("id", id);
            if (!ID_PATTERN.matcher(id).matches()) {
                throw new InvalidScientificValueException("id",
                        "use solo minúsculas, números y guiones, por ejemplo \"archelon-ischyros\"");
            }
            scientificName = required("nombre científico", scientificName);
            commonName = required("nombre común", commonName);
            period = required("periodo", period);
            funFact = required("dato curioso", funFact);
            source = required("fuente", source);
            requireSet("alimentación", diet);
            requireSet("zona", zone);
            requireSet("comportamiento", behavior);
            requireSet("forma", shape);
            validateAges();
            if (!(lengthMeters > 0) || lengthMeters > MAX_LENGTH_METERS) {
                throw new InvalidScientificValueException("tamaño",
                        "debe ser mayor que 0 y como máximo " + MAX_LENGTH_METERS + " metros");
            }
            if (behavior == Behavior.SESSILE && zone != Zone.SEAFLOOR) {
                throw new HabitatViolationException(id, "un animal fijo solo puede vivir en el fondo marino");
            }
            if (airBreather && zone == Zone.SEAFLOOR) {
                throw new HabitatViolationException(id, "un animal que respira aire no puede vivir en el fondo");
            }
            if (airBreather && behavior != Behavior.HUNTER) {
                throw new HabitatViolationException(id, "en el juego solo los cazadores suben a respirar; "
                        + "un animal que respira aire debe tener el comportamiento Cazador");
            }
            return new Species(this);
        }

        private void validateAges() throws InvalidScientificValueException {
            if (Double.isNaN(minMa) || Double.isNaN(maxMa)) {
                throw new InvalidScientificValueException("edad", "faltan los millones de años");
            }
            if (minMa < 0 || maxMa > OLDEST_AGE_MA) {
                throw new InvalidScientificValueException("edad",
                        "debe estar entre 0 y " + OLDEST_AGE_MA + " millones de años");
            }
            if (minMa >= maxMa) {
                throw new InvalidScientificValueException("edad",
                        "la edad más reciente debe ser menor que la más antigua");
            }
        }

        private static String required(String field, String value) throws InvalidScientificValueException {
            if (value == null || value.isBlank()) {
                throw new InvalidScientificValueException(field, "no puede estar vacío");
            }
            return value.strip();
        }

        private static void requireSet(String field, Object value) throws InvalidScientificValueException {
            if (Objects.isNull(value)) {
                throw new InvalidScientificValueException(field, "no puede estar vacío");
            }
        }
    }
}
