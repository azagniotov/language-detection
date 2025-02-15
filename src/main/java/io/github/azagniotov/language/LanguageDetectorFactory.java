package io.github.azagniotov.language;

import static io.github.azagniotov.language.NGram.UNI_GRAM_LENGTH;
import static io.github.azagniotov.language.StringConstants.EMPTY_STRING;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Language Detector Factory Class
 *
 * <p>This class manages an initialization and constructions of {@link LanguageDetector}.
 *
 * <p>Upon calling the only public API {@link
 * LanguageDetectorFactory#detector(LanguageDetectionSettings)}, the following happens:
 *
 * <p>1. LanguageDetectorFactory instance is created.
 *
 * <p>2. Language JSON profiles are loaded (once only) which contain N-Gram words and probabilities.
 *
 * <p>3. A mapping between N-Gram words and probabilities is getting computed.
 *
 * <p>4. The created language detector factory singleton instance is then cached (via double-checked
 * locking pattern)
 *
 * <p>5. The {@link LanguageDetector} instance is created with the computed N-Gram words and
 * probabilities mapping and configured language codes (ISO 639-1 codes)
 *
 * @see LanguageDetector
 * @see LanguageDetectionSettings
 */
class LanguageDetectorFactory {

  private static volatile LanguageDetectorFactory instance;

  private final LanguageDetectionSettings languageDetectionSettings;

  // All the loaded ISO 639-1 codes that have been configured by the user,
  // e.g.: en, ja, es. The codes are in exactly the same order as the data
  // is in the double[] in languageCorporaProbabilities.
  //
  // Example:
  // If languageCorporaProbabilities has an entry for the n-gram "foo", then for
  // each ISO code in the supportedIsoCodes639_1 here it has a probability value there.
  // Language codes that don't know the n-gram have the value 0d (zero probability).
  private final List<String> supportedIsoCodes639_1;

  // This contains a mapping of all words from the language profiles (the profiles
  // which correspond to the configured ISO 639-1 code for detection), along with
  // their associated probabilities. These probabilities are calculated as the ratio
  // between the word's frequency and the frequency of its N-grams.
  private final Map<String, double[]> languageCorporaProbabilities;
  private final int maxNGramLength;

  LanguageDetectorFactory(final LanguageDetectionSettings languageDetectionSettings) {
    this.languageDetectionSettings = languageDetectionSettings;
    this.supportedIsoCodes639_1 = new LinkedList<>();
    this.languageCorporaProbabilities = new HashMap<>();

    // TODO: Will come from the settings
    this.maxNGramLength = 3;

    addProfiles();
  }

  List<String> getSupportedIsoCodes639_1() {
    return supportedIsoCodes639_1;
  }

  Map<String, double[]> getLanguageCorporaProbabilities() {
    return languageCorporaProbabilities;
  }

  public int getMaxNGramLength() {
    return maxNGramLength;
  }

  /**
   * This method retrieves JSON language corpus profiles from the resourcse directory, using the ISO
   * 639-1 language codes specified by the user.
   *
   * <p>Once the language profiles are successfully loaded and deserialized, it computes a mapping
   * of word probabilities. These probabilities are determined by the ratio of each word's frequency
   * to the total N-Grams counts within the language corpus.
   *
   * <p>Since this operation is computationally intensive, it must be executed once prior to
   * performing language detection.
   */
  private void addProfiles() {
    final List<String> supportedIsoCodes = this.languageDetectionSettings.getIsoCodes639_1();
    final List<LanguageProfile> allLoadedProfiles = new ArrayList<>(supportedIsoCodes.size());
    for (final String isoCode639_1 : supportedIsoCodes) {
      if (isoCode639_1 == null || isoCode639_1.trim().isEmpty()) {
        continue;
      }
      final String profile = this.languageDetectionSettings.getProfile();
      final String languageResourcePath =
          "/langdetect/" + (profile == null ? EMPTY_STRING : profile + "/") + isoCode639_1;
      final InputStream in = getClass().getResourceAsStream(languageResourcePath);
      if (in == null) {
        throw new UncheckedIOException(
            new IOException("Could not load language profile from: " + languageResourcePath));
      }
      allLoadedProfiles.add(LanguageProfile.fromJson(in));
    }
    for (int idx = 0; idx < allLoadedProfiles.size(); idx++) {
      addProfile(allLoadedProfiles.get(idx), idx, allLoadedProfiles.size());
    }
  }

  void addProfile(final LanguageProfile profile, final int index, final int totalProfiles) {
    final String languageCode = profile.getIsoCode639_1();
    if (this.supportedIsoCodes639_1.contains(languageCode)) {
      throw new UncheckedIOException(
          new IOException("Duplicate of the same language profile: " + languageCode));
    }
    this.supportedIsoCodes639_1.add(languageCode);
    for (final String word : profile.getWordFrequencies().keySet()) {

      if (!this.languageCorporaProbabilities.containsKey(word)) {
        this.languageCorporaProbabilities.put(word, new double[totalProfiles]);
      }

      final int length = word.length();
      if (length >= UNI_GRAM_LENGTH && length <= this.maxNGramLength) {
        final long wordFrequency = profile.getWordFrequencies().get(word);

        // e.g.: "n_words":[260942223,308553243,224934017]
        final double nGramCount = profile.getNGramCounts().get(length - 1);
        final double probability = ((double) wordFrequency / nGramCount);

        this.languageCorporaProbabilities.get(word)[index] = probability;
      }
    }
  }

  public static LanguageDetector detector(
      final LanguageDetectionSettings languageDetectionSettings) {
    if (instance == null) {
      synchronized (LanguageDetectorFactory.class) {
        if (instance == null) {
          instance = new LanguageDetectorFactory(languageDetectionSettings);
        }
      }
    }
    return new LanguageDetector(
        instance.getSupportedIsoCodes639_1(),
        instance.getLanguageCorporaProbabilities(),
        instance.getMaxNGramLength());
  }
}
