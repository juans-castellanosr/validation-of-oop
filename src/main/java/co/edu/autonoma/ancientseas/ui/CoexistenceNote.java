package co.edu.autonoma.ancientseas.ui;

import co.edu.autonoma.ancientseas.catalog.Coexistence;
import co.edu.autonoma.ancientseas.catalog.Range;
import co.edu.autonoma.ancientseas.catalog.Species;
import co.edu.autonoma.ancientseas.catalog.SpeciesCatalog;
import java.util.stream.Collectors;

final class CoexistenceNote {

    private CoexistenceNote() {
    }

    static String text(SpeciesCatalog catalog) {
        Coexistence coexistence = Coexistence.of(catalog.all());
        if (coexistence.sharedPeriod().isEmpty()) {
            return "";
        }
        StringBuilder note = new StringBuilder();
        if (coexistence.apart().isEmpty()) {
            note.append("Todos estos animales vivieron en la misma época: sus fósiles coinciden ");
        } else {
            note.append("La mayoría de estos animales vivieron en la misma época: sus fósiles coinciden ");
        }
        note.append(when(coexistence.sharedPeriod().orElseThrow())).append(". Algunos se conocen de capas de roca ")
                .append("un poco más antiguas o más recientes, así que en la realidad no todos nadaron juntos el ")
                .append("mismo día. El juego los reúne para contar la historia de ese mar.");
        if (!coexistence.apart().isEmpty()) {
            note.append(" Atención: ").append(coexistence.apart().stream()
                    .map(s -> s.commonName() + " (" + when(s.ageMa()) + ")").collect(Collectors.joining(", ")))
                    .append(coexistence.apart().size() == 1
                            ? " vivió en otra época y nunca coincidió con los demás. Se incluye solo para comparar."
                            : " vivieron en otra época y nunca coincidieron con los demás. "
                                    + "Se incluyen solo para comparar.");
        }
        return note.toString();
    }

    static String warningFor(SpeciesCatalog catalog, Species species) {
        Coexistence others = Coexistence.of(catalog.all());
        if (others.fits(species)) {
            return "";
        }
        return species.commonName() + " vivió " + when(species.ageMa()) + ", y las demás especies "
                + when(others.sharedPeriod().orElseThrow()) + ". Se puede guardar: la ficha y la lista de especies "
                + "explicarán a los visitantes que vivió en otra época.";
    }

    private static String when(Range<Double> ageMa) {
        long youngest = Math.round(ageMa.min());
        long oldest = Math.round(ageMa.max());
        return youngest == oldest ? "hace unos " + youngest + " millones de años"
                : "hace entre " + youngest + " y " + oldest + " millones de años";
    }
}
