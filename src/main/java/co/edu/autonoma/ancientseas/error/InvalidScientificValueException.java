package co.edu.autonoma.ancientseas.error;

/** A species field holds a value that cannot be scientifically true (negative size, reversed age range). */
public class InvalidScientificValueException extends AncientSeasException {

    private static final long serialVersionUID = 1L;

    private final String field;

    /**
     * Creates the exception for a missing or impossible value in a species field.
     *
     * @param field  the field name, in Spanish
     * @param reason why the value is not valid, in Spanish
     */
    public InvalidScientificValueException(String field, String reason) {
        super("Invalid value for field '" + field + "': " + reason,
                "El dato \"" + field + "\" no es válido: " + reason);
        this.field = field;
    }

    /**
     * Returns the catalog field that failed validation.
     *
     * @return the field name
     */
    public String getField() {
        return field;
    }
}
