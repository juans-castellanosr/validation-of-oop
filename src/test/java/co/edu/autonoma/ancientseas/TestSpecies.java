package co.edu.autonoma.ancientseas;

import co.edu.autonoma.ancientseas.catalog.Behavior;
import co.edu.autonoma.ancientseas.catalog.BodyShape;
import co.edu.autonoma.ancientseas.catalog.Diet;
import co.edu.autonoma.ancientseas.catalog.Species;
import co.edu.autonoma.ancientseas.catalog.SpeciesCatalog;
import co.edu.autonoma.ancientseas.catalog.Zone;
import co.edu.autonoma.ancientseas.error.AncientSeasException;

public final class TestSpecies {

    private TestSpecies() {
    }

    public static Species.Builder valid(String id) {
        return Species.builder().id(id).scientificName("Testus " + id).commonName("Prueba " + id)
                .period("Cretácico tardío").ageMa(75, 80).lengthMeters(2).diet(Diet.CARNIVORE).zone(Zone.MIDWATER)
                .airBreather(false).behavior(Behavior.HUNTER).shape(BodyShape.BONY_FISH)
                .funFact("Un dato.").source("Fuente de prueba");
    }

    public static Species archelon() {
        return build(valid("archelon-ischyros").lengthMeters(4).diet(Diet.OMNIVORE).airBreather(true)
                .shape(BodyShape.TURTLE).zone(Zone.MIDWATER));
    }

    public static Species mosasaur() {
        return build(valid("tylosaurus").lengthMeters(12).airBreather(true).shape(BodyShape.MOSASAUR));
    }

    public static Species bigFish() {
        return build(valid("xiphactinus").lengthMeters(5).diet(Diet.PISCIVORE));
    }

    public static Species smallFish() {
        return build(valid("enchodus").lengthMeters(1).diet(Diet.PISCIVORE).behavior(Behavior.SCHOOLING));
    }

    public static Species ammonite() {
        return build(valid("baculites").lengthMeters(0.6).diet(Diet.OMNIVORE).behavior(Behavior.DRIFTER)
                .shape(BodyShape.AMMONITE));
    }

    public static Species clam() {
        return build(valid("inoceramus").lengthMeters(1).diet(Diet.FILTER_FEEDER).behavior(Behavior.SESSILE)
                .zone(Zone.SEAFLOOR).shape(BodyShape.CLAM));
    }

    public static SpeciesCatalog catalog() {
        SpeciesCatalog catalog = new SpeciesCatalog();
        try {
            for (Species s : new Species[] {archelon(), mosasaur(), bigFish(), smallFish(), ammonite(), clam()}) {
                catalog.add(s);
            }
        } catch (AncientSeasException e) {
            throw new AssertionError(e);
        }
        return catalog;
    }

    private static Species build(Species.Builder b) {
        try {
            return b.build();
        } catch (AncientSeasException e) {
            throw new AssertionError(e);
        }
    }
}
