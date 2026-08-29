package com.fittrack.whoop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fittrack.common.util.CryptoService;
import org.junit.jupiter.api.Test;

/** Third-party tokens must never be recoverable from the database alone. */
class CryptoServiceTest {

    private static final String KEY = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    private final CryptoService cryptoService = new CryptoService(KEY);

    @Test
    void roundTripsAToken() {
        String token = "whoop-access-token-value";
        String encrypted = cryptoService.encrypt(token);

        assertThat(encrypted).isNotEqualTo(token).doesNotContain(token);
        assertThat(cryptoService.decrypt(encrypted)).isEqualTo(token);
    }

    @Test
    void producesADifferentCiphertextEachTime() {
        String token = "whoop-access-token-value";

        // A fresh nonce per encryption means identical tokens are not identifiable in the database.
        assertThat(cryptoService.encrypt(token)).isNotEqualTo(cryptoService.encrypt(token));
    }

    @Test
    void refusesToDecryptTamperedCiphertext() {
        String encrypted = cryptoService.encrypt("whoop-access-token-value");
        String tampered = encrypted.substring(0, encrypted.length() - 4) + "AAAA";

        assertThatThrownBy(() -> cryptoService.decrypt(tampered))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotDecryptWithADifferentKey() {
        String encrypted = cryptoService.encrypt("whoop-access-token-value");
        CryptoService other =
                new CryptoService("YWJjZGVmMDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODk=");

        assertThatThrownBy(() -> other.decrypt(encrypted)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void refusesAKeyOfTheWrongLength() {
        assertThatThrownBy(() -> new CryptoService("too-short"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");

        assertThatThrownBy(() -> new CryptoService(""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TOKEN_ENCRYPTION_KEY");
    }

    @Test
    void passesNullThrough() {
        assertThat(cryptoService.encrypt(null)).isNull();
        assertThat(cryptoService.decrypt(null)).isNull();
    }
}
