package co.edu.autonoma.ancientseas.catalog;

/** The way a species acts in the simulation. */
public enum Behavior implements DisplayNamed {
    HUNTER("Cazador"),
    SCHOOLING("Nada en grupo"),
    DRIFTER("Flota a la deriva"),
    SESSILE("Vive fijo en el fondo");

    private final String displayName;

    Behavior(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String displayName() {
        return displayName;
    }
}
