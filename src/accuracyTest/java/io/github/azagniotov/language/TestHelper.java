package io.github.azagniotov.language;

import static io.github.azagniotov.language.StringConstants.TAB_CHAR;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class TestHelper {

  private TestHelper() {}

  static final float ACCURACY_DELTA = 1e-6f;

  // To disable a language from being evaluated, we need to set its
  // probability in the CSV as NaN. Then, it will be filtered out.
  static final String ALL_LANGUAGES =
      "af,ar,bg,bn,ca,cs,da,de,el,en,es,et,fa,fi,fr,gu,he,hi,hr,hu,id,it,ja,kn,ko,lt,lv,mk,ml,mr,ne,nl,no,pa,pl,pt,"
          + "ro,ru,si,sk,sl,so,sq,sv,sw,ta,te,th,tl,tr,uk,ur,vi,yi,zh-cn,zh-tw";

  /**
   * Read and parse a multi-language dataset from the given path.
   *
   * @param path resource path, where the file is in tab-separated format with two columns: language
   *     code and text
   * @return a mapping from each language code found in the file to the texts of this language
   */
  static Map<String, List<String>> readDataset(final String path) throws IOException {
    final Map<String, List<String>> languageToFullTexts = new HashMap<>();

    try (final BufferedReader bufferedReader = getResourceReader(path)) {
      while (bufferedReader.ready()) {
        final String[] csvLineChunks = bufferedReader.readLine().split(TAB_CHAR);
        final String language = csvLineChunks[0];
        final String textSubstring = csvLineChunks[1];

        languageToFullTexts.computeIfAbsent(language, k -> new ArrayList<>()).add(textSubstring);
      }
    }
    return languageToFullTexts;
  }

  /** Helper method to open a resource path and return it as a BufferedReader instance. */
  static BufferedReader getResourceReader(final String path) {
    return new BufferedReader(
        new InputStreamReader(
            Objects.requireNonNull(LanguageDetectorAccuracyTest.class.getResourceAsStream(path)),
            StandardCharsets.UTF_8));
  }

  static String getTopLanguageCode(final LanguageDetector languageDetector, final String text) {
    final List<Language> languages = languageDetector.detectAll(text);
    return languages.get(0).getIsoCode639_1();
  }

  static void resetLanguageDetectorFactoryInstance()
      throws NoSuchFieldException, IllegalAccessException {
    try {
      final Field field = LanguageDetectorFactory.class.getDeclaredField("instance");
      field.setAccessible(true);
      field.set(null, null); // Set to null
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw e; // Re-throw the exceptions
    }
  }
}
