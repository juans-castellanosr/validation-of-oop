package co.edu.autonoma.ancientseas.catalog;

/** The body outline used to draw a species, which also decides what kind of food it is for others. */
public enum BodyShape implements DisplayNamed {
    TURTLE("Tortuga", PreyKind.REPTILE),
    MOSASAUR("Mosasaurio", PreyKind.REPTILE),
    SHARK("Tiburón", PreyKind.FISH),
    PLESIOSAUR("Plesiosaurio", PreyKind.REPTILE),
    BONY_FISH("Pez óseo", PreyKind.FISH),
    PREDATORY_FISH("Pez depredador grande", PreyKind.FISH),
    AMMONITE("Amonite enrollado", PreyKind.CEPHALOPOD),
    STRAIGHT_AMMONITE("Amonite de concha recta", PreyKind.CEPHALOPOD),
    CLAM("Almeja", PreyKind.SHELLFISH);

    private final String displayName;
    private final PreyKind preyKind;

    BodyShape(String displayName, PreyKind preyKind) {
        this.displayName = displayName;
        this.preyKind = preyKind;
    }

    /**
     * Returns what kind of food an animal with this body is for a predator.
     *
     * @return the prey kind
     */
    public PreyKind preyKind() {
        return preyKind;
    }

    @Override
    public String displayName() {
        return displayName;
    }
}
