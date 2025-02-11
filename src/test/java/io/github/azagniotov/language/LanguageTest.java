package io.github.azagniotov.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class LanguageTest {

    @Test
    public final void shouldConstructLanguageAsExpected() {
        final Language lang = new Language(null, 0);
        assertNull(lang.getIsoCode639_1());
        assertEquals(lang.getProbability(), 0.0, 0.0001);
        assertNull(lang.getIsoCode639_1());

        final Language lang2 = new Language("en", 1.0);
        assertEquals(lang2.getIsoCode639_1(), "en");
        assertEquals(lang2.getProbability(), 1.0, 0.0001);
    }
}
