package co.edu.autonoma.ancientseas.error;

/** A species is described, or placed, outside the part of the ocean where it can live. */
public class HabitatViolationException extends AncientSeasException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception for a species placed where it cannot live in the game.
     *
     * @param speciesId the species id
     * @param detail    which rule was broken, in Spanish
     */
    public HabitatViolationException(String speciesId, String detail) {
        super("Habitat violation for " + speciesId + ": " + detail,
                "\"" + speciesId + "\" no puede vivir ahí: " + detail);
    }
}
