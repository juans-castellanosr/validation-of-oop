package co.edu.autonoma.ancientseas.error;

/** The save file is well formed but belongs to another version of the game or catalog. */
public class IncompatibleSaveException extends SaveGameException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception with the default message for a save from another version.
     *
     * @param technicalMessage detail for the log
     */
    public IncompatibleSaveException(String technicalMessage) {
        this(technicalMessage, "Esta partida se guardó con otra versión del juego y no se puede continuar.");
    }

    /**
     * Creates the exception with a specific message for the visitor.
     *
     * @param technicalMessage detail for the log
     * @param friendlyMessage  explanation in Spanish for the visitor
     */
    public IncompatibleSaveException(String technicalMessage, String friendlyMessage) {
        super(technicalMessage, friendlyMessage, null);
    }
}
