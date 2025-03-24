package io.github.azagniotov.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;

public class LanguageDetectionSettingsTest {

  @Test
  public void testDefaultValues() {
    final LanguageDetectionSettings settings =
        LanguageDetectionSettings.fromIsoCodes639_1("ja,en").build();

    assertEquals(settings.getProfilesHome(), "profiles");
    assertEquals(settings.getMinNGramLength(), 1);
    assertEquals(settings.getMaxNGramLength(), 3);
    assertEquals(settings.getMaxTextChars(), 2000);

    assertEquals(settings.getTopLanguageCertaintyThreshold() + "", "0.65");
    assertEquals(settings.getTopLanguageFallbackIsoCode639_1(), "en");
    assertFalse(settings.isTopLanguageCertaintyThresholdSet());

    assertEquals(settings.getMinimumCertaintyThreshold() + "", "0.1");
    assertTrue(settings.isMinimumCertaintyThresholdSet());

    assertEquals(settings.getCjkDetectionThreshold() + "", "0.1");
    assertEquals(settings.getIsoCodes639_1(), Arrays.asList("ja", "en"));

    assertFalse(settings.isClassifyChineseAsJapanese());
    assertTrue(settings.isSanitizeInput());
  }

  @Test
  public void testMutuallyExclusiveCertaintyThresholdSettings() {
    final LanguageDetectionSettings settingsWithMininumCertainty =
        LanguageDetectionSettings.fromIsoCodes639_1("ja,en").withMininumCertainty(0.70f).build();

    assertTrue(settingsWithMininumCertainty.isMinimumCertaintyThresholdSet());
    assertFalse(settingsWithMininumCertainty.isTopLanguageCertaintyThresholdSet());

    // Unrelated, but sanity checking the defaults
    assertFalse(settingsWithMininumCertainty.isClassifyChineseAsJapanese());
    assertTrue(settingsWithMininumCertainty.isSanitizeInput());

    final LanguageDetectionSettings settingsWithTopMininumCertainty =
        LanguageDetectionSettings.fromIsoCodes639_1("ja,en")
            .withTopLanguageMininumCertainty(0.70f, "en")
            .build();

    assertFalse(settingsWithTopMininumCertainty.isMinimumCertaintyThresholdSet());
    assertTrue(settingsWithTopMininumCertainty.isTopLanguageCertaintyThresholdSet());

    // Unrelated, but sanity checking the defaults
    assertFalse(settingsWithTopMininumCertainty.isClassifyChineseAsJapanese());
    assertTrue(settingsWithTopMininumCertainty.isSanitizeInput());

    final LanguageDetectionSettings settingsWithBoth =
        LanguageDetectionSettings.fromIsoCodes639_1("ja,en")
            .withTopLanguageMininumCertainty(0.70f, "en")
            .withMininumCertainty(0.70f)
            .build();

    assertTrue(settingsWithBoth.isMinimumCertaintyThresholdSet());
    assertFalse(settingsWithBoth.isTopLanguageCertaintyThresholdSet());

    // Unrelated, but sanity checking the defaults
    assertFalse(settingsWithBoth.isClassifyChineseAsJapanese());
    assertTrue(settingsWithBoth.isSanitizeInput());
  }

  @Test
  public void testUnsetSanitizeForSearch() {
    final LanguageDetectionSettings settings =
        LanguageDetectionSettings.fromIsoCodes639_1("ja,en").build();
    assertTrue(settings.isSanitizeInput());

    final LanguageDetectionSettings settingsWithoutSanitize =
        LanguageDetectionSettings.fromIsoCodes639_1("ja,en").withoutInputSanitize().build();
    assertFalse(settingsWithoutSanitize.isSanitizeInput());

    // Unrelated, but sanity checking the defaults
    assertFalse(settings.isClassifyChineseAsJapanese());
    assertFalse(settingsWithoutSanitize.isClassifyChineseAsJapanese());
  }

  @Test
  public void testSetClassifyChineseAsJapanese() {
    final LanguageDetectionSettings settings =
        LanguageDetectionSettings.fromIsoCodes639_1("ja,en").build();
    assertFalse(settings.isClassifyChineseAsJapanese());

    final LanguageDetectionSettings settingsWithClassify =
        LanguageDetectionSettings.fromIsoCodes639_1("ja,en")
            .withClassifyChineseAsJapanese()
            .build();
    assertTrue(settingsWithClassify.isClassifyChineseAsJapanese());

    // Unrelated, but sanity checking the defaults
    assertTrue(settings.isSanitizeInput());
    assertTrue(settingsWithClassify.isSanitizeInput());
  }
}
