package ch.qligier.heicofawallpaper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for the {@link Utils} class.
 *
 * @author Quentin Ligier
 */
class UtilsTest {

    @Test
    void getLogo() {
        final var logo = Utils.getLogo();
        assertNotNull(logo);
        assertFalse(logo.isError());
    }
}
