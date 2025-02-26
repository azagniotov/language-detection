package io.github.azagniotov.language;

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
 * <p>4. The created language detector factory singleton instance is then cached (via float-checked
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
  // is in the float[] in languageCorporaProbabilities.
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
  private final Map<String, float[]> languageCorporaProbabilities;

  private final int minNGramLength;
  private final int maxNGramLength;

  LanguageDetectorFactory(final LanguageDetectionSettings languageDetectionSettings) {
    this.languageDetectionSettings = languageDetectionSettings;
    this.supportedIsoCodes639_1 = new LinkedList<>();
    this.languageCorporaProbabilities = new HashMap<>();
    this.minNGramLength = this.languageDetectionSettings.getMinNGramLength();
    this.maxNGramLength = this.languageDetectionSettings.getMaxNGramLength();

    addProfiles();
  }

  List<String> getSupportedIsoCodes639_1() {
    return supportedIsoCodes639_1;
  }

  Map<String, float[]> getLanguageCorporaProbabilities() {
    return languageCorporaProbabilities;
  }

  int getMinNGramLength() {
    return minNGramLength;
  }

  int getMaxNGramLength() {
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
    // Fake call to the class to cause it to be loaded and the static initializers executed
    NGram.normalize('\u0000');

    final List<String> supportedIsoCodes = this.languageDetectionSettings.getIsoCodes639_1();
    final List<LanguageProfile> allLoadedProfiles = new ArrayList<>(supportedIsoCodes.size());
    for (final String isoCode639_1 : supportedIsoCodes) {
      if (isoCode639_1 == null || isoCode639_1.trim().isEmpty()) {
        continue;
      }
      final String profile = this.languageDetectionSettings.getProfile();
      final String languageResourcePath = "/profiles/" + profile + "/" + isoCode639_1;
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
        this.languageCorporaProbabilities.put(word, new float[totalProfiles]);
      }

      final int length = word.length();
      if (length >= this.minNGramLength && length <= this.maxNGramLength) {
        final long wordFrequency = profile.getWordFrequencies().get(word);

        // e.g.: "n_words":[260942223,308553243,224934017]
        final float nGramCount = profile.getNGramCounts().get(length - 1);
        final float probability = ((float) wordFrequency / nGramCount);

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
        instance.getMinNGramLength(),
        instance.getMaxNGramLength());
  }
}
