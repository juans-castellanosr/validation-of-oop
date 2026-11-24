package co.edu.autonoma.ancientseas.admin;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Protects the administration mode with a numeric PIN. Only a salted PBKDF2 hash is stored, and
 * it is compared in constant time. Instances are immutable and never keep a PIN array; the caller
 * owns, and should wipe, the arrays it passes in.
 */
public final class AdminAccess {

    public static final String DEFAULT_PIN = "1234";
    public static final int MIN_DIGITS = 4;
    public static final int MAX_DIGITS = 12;

    private static final String SCHEME = "pbkdf2-sha256";
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int MAX_ITERATIONS = 2_000_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final int iterations;
    private final byte[] salt;
    private final byte[] hash;

    private AdminAccess(int iterations, byte[] salt, byte[] hash) {
        this.iterations = iterations;
        this.salt = salt;
        this.hash = hash;
    }

    /**
     * Creates the protection for a new PIN, with a fresh random salt.
     *
     * @param pin the digits chosen by staff
     * @return the protection that accepts exactly that PIN
     * @throws IllegalArgumentException if the PIN is {@code null} or not {@value #MIN_DIGITS} to
     *                                  {@value #MAX_DIGITS} digits
     */
    public static AdminAccess fromPin(char[] pin) {
        if (!isWellFormed(pin)) {
            throw new IllegalArgumentException(
                    "El PIN debe tener de " + MIN_DIGITS + " a " + MAX_DIGITS + " dígitos, solo números.");
        }
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return new AdminAccess(ITERATIONS, salt, derive(pin, salt, ITERATIONS));
    }

    /**
     * Creates the protection used when nothing was stored yet.
     *
     * @return protection for {@link #DEFAULT_PIN}
     */
    public static AdminAccess withDefaultPin() {
        return fromPin(DEFAULT_PIN.toCharArray());
    }

    /**
     * Rebuilds the protection from its {@linkplain #encode() stored form}.
     *
     * @param encoded text produced by {@link #encode()}
     * @return the equivalent protection
     * @throws IllegalArgumentException if the text is {@code null} or malformed
     */
    public static AdminAccess parse(String encoded) {
        if (encoded == null) {
            throw new IllegalArgumentException("Stored PIN is missing");
        }
        String[] parts = encoded.strip().split("\\$", -1);
        if (parts.length != 4 || !SCHEME.equals(parts[0])) {
            throw new IllegalArgumentException("Stored PIN has an unknown format");
        }
        int storedIterations;
        try {
            storedIterations = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Stored PIN has an unreadable iteration count", e);
        }
        if (storedIterations < 1 || storedIterations > MAX_ITERATIONS) {
            throw new IllegalArgumentException("Stored PIN has an unreasonable iteration count");
        }
        byte[] storedSalt = Base64.getDecoder().decode(parts[2]);
        byte[] storedHash = Base64.getDecoder().decode(parts[3]);
        if (storedSalt.length != SALT_BYTES || storedHash.length != HASH_BYTES) {
            throw new IllegalArgumentException("Stored PIN has the wrong salt or hash length");
        }
        return new AdminAccess(storedIterations, storedSalt, storedHash);
    }

    /**
     * Checks a PIN typed by someone.
     *
     * @param pin the digits typed; not retained and not modified
     * @return {@code true} only if it is the right PIN; malformed input is simply wrong
     */
    public boolean verify(char[] pin) {
        if (!isWellFormed(pin)) {
            return false;
        }
        return MessageDigest.isEqual(hash, derive(pin, salt, iterations));
    }

    /**
     * Returns the text to store: scheme, iterations, salt and hash, never the PIN.
     *
     * @return the encoded form accepted by {@link #parse(String)}
     */
    public String encode() {
        Base64.Encoder base64 = Base64.getEncoder();
        return SCHEME + "$" + iterations + "$" + base64.encodeToString(salt) + "$" + base64.encodeToString(hash);
    }

    private static boolean isWellFormed(char[] pin) {
        if (pin == null || pin.length < MIN_DIGITS || pin.length > MAX_DIGITS) {
            return false;
        }
        for (char c : pin) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private static byte[] derive(char[] pin, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(pin, salt, iterations, HASH_BYTES * 8);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 is not available", e);
        } finally {
            spec.clearPassword();
        }
    }
}
