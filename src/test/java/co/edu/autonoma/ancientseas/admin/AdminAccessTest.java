package co.edu.autonoma.ancientseas.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import org.junit.jupiter.api.Test;

class AdminAccessTest {

    @Test
    void acceptsTheRightPinAndRejectsWrongAndMalformedOnes() {
        AdminAccess access = AdminAccess.fromPin("48151623".toCharArray());

        assertTrue(access.verify("48151623".toCharArray()));
        assertFalse(access.verify("48151624".toCharArray()));
        assertFalse(access.verify("4815162".toCharArray()));
        assertFalse(access.verify("".toCharArray()));
        assertFalse(access.verify("4815 1623".toCharArray()));
        assertFalse(access.verify("٤٨١٥١٦٢٣".toCharArray()));
        assertFalse(access.verify(null));
    }

    @Test
    void defaultPinWorks() {
        assertTrue(AdminAccess.withDefaultPin().verify(AdminAccess.DEFAULT_PIN.toCharArray()));
        assertEquals("1234", AdminAccess.DEFAULT_PIN);
    }

    @Test
    void rejectsPinsOutsideFourToTwelveDigits() {
        for (String bad : new String[] {"123", "1234567890123", "12a4", "", "١٢٣٤"}) {
            assertThrows(IllegalArgumentException.class, () -> AdminAccess.fromPin(bad.toCharArray()), bad);
        }
        assertThrows(IllegalArgumentException.class, () -> AdminAccess.fromPin(null));
        AdminAccess.fromPin("1234".toCharArray());
        AdminAccess.fromPin("123456789012".toCharArray());
    }

    @Test
    void encodingRoundTripsAndNeverContainsThePin() {
        AdminAccess access = AdminAccess.fromPin("987654".toCharArray());

        String encoded = access.encode();
        AdminAccess restored = AdminAccess.parse(encoded);

        assertTrue(restored.verify("987654".toCharArray()));
        assertFalse(restored.verify("987655".toCharArray()));
        assertEquals(encoded, restored.encode());
        assertFalse(encoded.contains("987654"));
    }

    @Test
    void sameSecretGetsDifferentSalts() {
        char[] pin = "1234".toCharArray();

        assertNotEquals(AdminAccess.fromPin(pin).encode(), AdminAccess.fromPin(pin).encode());
    }

    @Test
    void parseRejectsMalformedText() {
        String valid = AdminAccess.withDefaultPin().encode();
        String[] parts = valid.split("\\$");
        String shortSalt = Base64.getEncoder().encodeToString(new byte[4]);
        String[] bad = {"", "garbage", "a$b$c", valid + "$extra", "md5$1$" + parts[2] + "$" + parts[3],
            "pbkdf2-sha256$x$" + parts[2] + "$" + parts[3], "pbkdf2-sha256$0$" + parts[2] + "$" + parts[3],
            "pbkdf2-sha256$999999999$" + parts[2] + "$" + parts[3], "pbkdf2-sha256$1$!!!$" + parts[3],
            "pbkdf2-sha256$1$" + shortSalt + "$" + parts[3]};
        for (String text : bad) {
            assertThrows(IllegalArgumentException.class, () -> AdminAccess.parse(text), text);
        }
        assertThrows(IllegalArgumentException.class, () -> AdminAccess.parse(null));
    }
}
