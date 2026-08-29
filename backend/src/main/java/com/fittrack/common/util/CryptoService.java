package com.fittrack.common.util;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Authenticated symmetric encryption for third-party OAuth tokens at rest.
 *
 * <p>AES-GCM with a fresh random nonce per encryption, so an attacker with read access to the
 * database still cannot use a stored WHOOP token. Ciphertext is stored as
 * {@code base64(nonce || ciphertext || tag)}.
 */
@Service
public class CryptoService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int NONCE_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final int KEY_LENGTH_BYTES = 32;

    private final SecretKey key;
    private final SecureRandom secureRandom = new SecureRandom();

    public CryptoService(@Value("${fittrack.crypto.encryption-key}") String configuredKey) {
        this.key = new SecretKeySpec(decodeKey(configuredKey), "AES");
    }

    private static byte[] decodeKey(String configuredKey) {
        if (configuredKey == null || configuredKey.isBlank()) {
            throw new IllegalStateException(
                    "TOKEN_ENCRYPTION_KEY must be set: third-party tokens are never stored in plain text");
        }
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(configuredKey);
        } catch (IllegalArgumentException ignored) {
            decoded = configuredKey.getBytes(StandardCharsets.UTF_8);
        }
        if (decoded.length != KEY_LENGTH_BYTES) {
            throw new IllegalStateException(
                    "TOKEN_ENCRYPTION_KEY must decode to exactly "
                            + KEY_LENGTH_BYTES
                            + " bytes (generate one with: openssl rand -base64 32)");
        }
        return decoded;
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            byte[] nonce = new byte[NONCE_LENGTH_BYTES];
            secureRandom.nextBytes(nonce);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[nonce.length + ciphertext.length];
            System.arraycopy(nonce, 0, combined, 0, nonce.length);
            System.arraycopy(ciphertext, 0, combined, nonce.length, ciphertext.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (java.security.GeneralSecurityException ex) {
            // The message never contains the plaintext.
            throw new IllegalStateException("Could not encrypt a stored credential", ex);
        }
    }

    public String decrypt(String encoded) {
        if (encoded == null) {
            return null;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(encoded);
            if (combined.length <= NONCE_LENGTH_BYTES) {
                throw new IllegalStateException("Stored credential is malformed");
            }
            byte[] nonce = java.util.Arrays.copyOfRange(combined, 0, NONCE_LENGTH_BYTES);
            byte[] ciphertext = java.util.Arrays.copyOfRange(combined, NONCE_LENGTH_BYTES, combined.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (java.security.GeneralSecurityException | IllegalArgumentException ex) {
            throw new IllegalStateException("Could not decrypt a stored credential", ex);
        }
    }
}
