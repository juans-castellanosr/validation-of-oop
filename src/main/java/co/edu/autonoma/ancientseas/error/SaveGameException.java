package co.edu.autonoma.ancientseas.error;

/** A saved game cannot be restored, either because it is damaged or because it is incompatible. */
public abstract class SaveGameException extends AncientSeasException {

    private static final long serialVersionUID = 1L;

    protected SaveGameException(String technicalMessage, String friendlyMessage, Throwable cause) {
        super(technicalMessage, friendlyMessage, cause);
    }
}
