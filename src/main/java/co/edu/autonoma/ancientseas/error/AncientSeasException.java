package co.edu.autonoma.ancientseas.error;

/**
 * Root of the checked domain errors; each carries a Spanish
 * {@linkplain #getFriendlyMessage() friendly message} besides the technical one.
 */
public abstract class AncientSeasException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String friendlyMessage;

    protected AncientSeasException(String technicalMessage, String friendlyMessage) {
        super(technicalMessage);
        this.friendlyMessage = friendlyMessage;
    }

    protected AncientSeasException(String technicalMessage, String friendlyMessage, Throwable cause) {
        super(technicalMessage, cause);
        this.friendlyMessage = friendlyMessage;
    }

    /**
     * Returns the explanation meant for people using the application.
     *
     * @return a short, non-technical Spanish message
     */
    public String getFriendlyMessage() {
        return friendlyMessage;
    }
}
