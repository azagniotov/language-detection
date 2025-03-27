package io.github.azagniotov.language;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

final class TestHelper {

  private TestHelper() {}

  /**
   * Test that the contents of the file at the provided path are correctly detected as being in
   * language languageCode.
   */
  static void testLanguage(
      final String path, final String languageCode, final LanguageDetector languageDetector)
      throws Exception {
    try (final InputStream datasetInputStream = TestHelper.class.getResourceAsStream("/" + path)) {
      assert datasetInputStream != null;

      final String dataset = new String(datasetInputStream.readAllBytes(), StandardCharsets.UTF_8);
      assertEquals(languageCode, getTopLanguageCode(languageDetector, dataset));
    }
  }

  /**
   * Return the text's language as detected by the given service object (may be null if no languages
   * are returned).
   */
  static String getTopLanguageCode(final LanguageDetector languageDetector, final String text) {
    final List<Language> languages = languageDetector.detectAll(text);
    System.out.println("\n\t" + languages + "\n");
    final Language language = languages.get(0);
    return language.getIsoCode639_1();
  }
}
