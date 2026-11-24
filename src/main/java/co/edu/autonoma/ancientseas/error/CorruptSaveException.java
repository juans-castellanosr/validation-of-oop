package co.edu.autonoma.ancientseas.error;

/** The save file is damaged: truncated, not XML, or missing required data. */
public class CorruptSaveException extends SaveGameException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception for a damaged save, with a fixed message for the visitor.
     *
     * @param technicalMessage detail for the log
     * @param cause            the original error, or {@code null}
     */
    public CorruptSaveException(String technicalMessage, Throwable cause) {
        super(technicalMessage, "La partida guardada está dañada y no se puede abrir.", cause);
    }
}
