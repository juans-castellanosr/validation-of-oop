package co.edu.autonoma.ancientseas.ui;

import co.edu.autonoma.ancientseas.catalog.SpeciesCatalog;
import java.io.IOException;

interface CatalogAdminActions {

    void saveCatalog(SpeciesCatalog catalog) throws IOException;

    void recordError(String message, Throwable error);

    void recordChange(String description);

    void changePin(char[] newPin) throws IOException;
}
