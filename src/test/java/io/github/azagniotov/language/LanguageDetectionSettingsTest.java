package io.github.azagniotov.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;

public class LanguageDetectionSettingsTest {

  @Test
  public final void testDefaultValues() {
    final LanguageDetectionSettings settings =
        LanguageDetectionSettings.fromIsoCodes639_1("ja,en").build();

    assertEquals(settings.getProfile(), "merged-average");
    assertEquals(settings.getMinNGramLength(), 1);
    assertEquals(settings.getMaxNGramLength(), 3);
    assertEquals(settings.getMaxTextChars(), 3000);
    assertEquals(settings.getCertaintyThreshold() + "", "0.65");
    assertEquals(settings.getClassifyChineseAsJapaneseThreshold() + "", "0.1");
    assertEquals(settings.getFallbackIsoCode639_1(), "en");
    assertEquals(settings.getIsoCodes639_1(), Arrays.asList("ja", "en"));

    assertFalse(settings.isClassifyChineseAsJapanese());
    assertTrue(settings.isSanitizeForSearch());
  }
}
