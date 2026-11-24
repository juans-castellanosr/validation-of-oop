package co.edu.autonoma.ancientseas.error;

/** The catalog file as a whole cannot be read (missing header, unknown columns, unreadable file). */
public class CatalogFormatException extends AncientSeasException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception for an unusable catalog file.
     *
     * @param technicalMessage detail for the log
     * @param friendlyMessage  explanation in Spanish for the staff
     */
    public CatalogFormatException(String technicalMessage, String friendlyMessage) {
        super(technicalMessage, friendlyMessage);
    }

    /**
     * Creates the exception for an unusable catalog file, keeping the error that caused it.
     *
     * @param technicalMessage detail for the log
     * @param friendlyMessage  explanation in Spanish for the staff
     * @param cause            the original error
     */
    public CatalogFormatException(String technicalMessage, String friendlyMessage, Throwable cause) {
        super(technicalMessage, friendlyMessage, cause);
    }
}
