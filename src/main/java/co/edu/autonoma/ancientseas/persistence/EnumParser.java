package co.edu.autonoma.ancientseas.persistence;

import java.util.Arrays;

final class EnumParser {

    private EnumParser() {
    }

    static <E extends Enum<E>> E parse(Class<E> type, String text) {
        String wanted = text.strip();
        E[] constants = type.getEnumConstants();
        for (E constant : constants) {
            if (constant.name().equalsIgnoreCase(wanted)) {
                return constant;
            }
        }
        throw new IllegalArgumentException(
                "\"" + text + "\" no es un valor válido; las opciones son " + Arrays.toString(constants));
    }
}
