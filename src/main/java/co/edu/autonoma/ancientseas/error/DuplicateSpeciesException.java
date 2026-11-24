package co.edu.autonoma.ancientseas.error;

/** A species with the same identifier already exists in the catalog. */
public class DuplicateSpeciesException extends AncientSeasException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception for a species id that is already in the catalog.
     *
     * @param speciesId the repeated id
     */
    public DuplicateSpeciesException(String speciesId) {
        super("Duplicate species id: " + speciesId,
                "La especie \"" + speciesId + "\" ya está en el catálogo.");
    }
}
