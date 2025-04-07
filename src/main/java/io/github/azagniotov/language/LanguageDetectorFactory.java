package io.github.azagniotov.language;

import static io.github.azagniotov.language.StringConstants.GZIP_EXTENSION;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Singleton factory responsible for loading, processing, and caching language model data required
 * by {@link LanguageDetector}.
 *
 * <p>Ensures that language profiles (N-Gram probabilities) and model parameters are loaded from
 * resources and processed only once during the first initialization, triggered by {@link
 * #detector(LanguageDetectionSettings)}. It then provides configured {@code LanguageDetector}
 * instances that share this pre-processed data.
 *
 * <p>Initialization uses a lazy, volatile singleton pattern. Thread safety during the very first
 * initialization in concurrent environments relies on external mechanisms like warm-up calls (see
 * {@link LanguageDetectionOrchestrator#fromSettings(LanguageDetectionSettings)}).
 *
 * @see LanguageDetector
 * @see LanguageDetectionSettings
 * @see LanguageProfile
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

  private Model model;
  private PrimitiveTrie charPrefixLookup;

  /**
   * Private constructor for the singleton pattern. Initializes internal collections and settings.
   *
   * @param languageDetectionSettings Configuration settings provided by the user.
   */
  private LanguageDetectorFactory(final LanguageDetectionSettings languageDetectionSettings) {
    this.languageDetectionSettings = languageDetectionSettings;
    this.supportedIsoCodes639_1 = new LinkedList<>();
    this.languageCorporaProbabilities = new HashMap<>();
    this.minNGramLength = this.languageDetectionSettings.getMinNGramLength();
    this.maxNGramLength = this.languageDetectionSettings.getMaxNGramLength();
  }

  // Internal factory method to perform actual initialization
  static LanguageDetectorFactory fromSettings(final LanguageDetectionSettings settings)
      throws IOException {
    final LanguageDetectorFactory languageDetectorFactory = new LanguageDetectorFactory(settings);

    languageDetectorFactory.model = languageDetectorFactory.loadModelParameters();
    languageDetectorFactory.addProfiles();

    final Set<String> nGrams = languageDetectorFactory.languageCorporaProbabilities.keySet();
    languageDetectorFactory.charPrefixLookup = PrimitiveTrie.buildFromSet(nGrams);

    return languageDetectorFactory;
  }

  Model model() {
    return model;
  }

  List<String> supportedIsoCodes639_1() {
    return supportedIsoCodes639_1;
  }

  Map<String, float[]> languageCorporaProbabilities() {
    return languageCorporaProbabilities;
  }

  PrimitiveTrie charPrefixLookup() {
    return charPrefixLookup;
  }

  int minNGramLength() {
    return minNGramLength;
  }

  int maxNGramLength() {
    return maxNGramLength;
  }

  /**
   * Loads language profiles from Gzipped JSON files located in the classpath resources.
   *
   * <p>This method iterates through the ISO 639-1 language codes specified in the {@link
   * LanguageDetectionSettings}. For each valid code, it constructs the expected resource path
   * (within the configured 'profilesHome' directory), loads the corresponding Gzip-compressed JSON
   * file, and deserializes it into a {@link LanguageProfile} object using {@link
   * LanguageProfile#fromGzippedJson(InputStream)}.
   *
   * <p>After loading all specified profiles, it calls {@link #addProfile(LanguageProfile, int,
   * int)} for each loaded profile to populate the factory's internal data structures ({@code
   * supportedIsoCodes639_1} and {@code languageCorporaProbabilities}).
   *
   * <p>Since loading and processing profiles involves I/O and computation (especially if many
   * profiles or large profiles are used), this operation is typically performed only once during
   * the factory's initialization.
   *
   * @throws IOException If an I/O error occurs while reading from the Gzipped resource stream or
   *     during JSON deserialization within {@code LanguageProfile.fromGzippedJson}.
   * @throws UncheckedIOException If a specified language profile Gzip resource file cannot be found
   *     or accessed via {@code getClass().getResourceAsStream()}, wrapping the underlying
   *     IOException.
   */
  private void addProfiles() throws IOException {
    final List<String> supportedIsoCodes = this.languageDetectionSettings.getIsoCodes639_1();
    final List<LanguageProfile> allLoadedProfiles = new ArrayList<>(supportedIsoCodes.size());
    for (final String isoCode639_1 : supportedIsoCodes) {
      if (isoCode639_1 == null || isoCode639_1.trim().isEmpty()) {
        continue;
      }
      final String profilesHome = this.languageDetectionSettings.getProfilesHome();
      final String profileGzipArchive =
          String.format("/%s/%s%s", profilesHome, isoCode639_1, GZIP_EXTENSION);
      try (final InputStream in = getClass().getResourceAsStream(profileGzipArchive)) {
        if (in == null) {
          throw new UncheckedIOException(
              new IOException(
                  "Could not load language profile Gzip-compressed from: " + profileGzipArchive));
        }
        allLoadedProfiles.add(LanguageProfile.fromGzippedJson(in));
      }
    }
    for (int idx = 0; idx < allLoadedProfiles.size(); idx++) {
      addProfile(allLoadedProfiles.get(idx), idx, allLoadedProfiles.size());
    }
  }

  /** Loads model parameters from a fixed resource path ("/model/parameters.json"). */
  private Model loadModelParameters() throws IOException {
    final String modelParametersPath = "/model/parameters.json";
    try (final InputStream in = getClass().getResourceAsStream(modelParametersPath)) {
      if (in == null) {
        throw new UncheckedIOException(
            new IOException("Could not load model parameters from: " + modelParametersPath));
      }

      return Model.fromJsonOrEnv(in);
    }
  }

  /**
   * Processes a single loaded {@link LanguageProfile} and incorporates its data into the factory's
   * internal state.
   *
   * <p>Specifically, this method:
   *
   * <ol>
   *   <li>Adds the profile's language code (ISO 639-1) to the {@code supportedIsoCodes639_1} list,
   *       checking for duplicates.
   *   <li>Iterates through the word (N-Gram) frequencies contained in the profile.
   *   <li>For each word within the configured N-Gram length limits ({@code minNGramLength} to
   *       {@code maxNGramLength}):
   *       <ul>
   *         <li>Ensures an entry exists for the word in the {@code languageCorporaProbabilities}
   *             map.
   *         <li>Calculates the word's probability within this language profile (word frequency /
   *             total N-Gram count for its length).
   *         <li>Stores this calculated probability in the float array associated with the word, at
   *             the specified {@code index} corresponding to this language profile.
   *       </ul>
   * </ol>
   *
   * @param profile The {@link LanguageProfile} object containing data for one language.
   * @param index The zero-based index representing this language's position in the list of all
   *     loaded profiles. This index is used to place the calculated probability correctly within
   *     the float arrays in {@code languageCorporaProbabilities}.
   * @param totalProfiles The total number of language profiles being loaded. This is used to
   *     initialize the size of the float arrays when a new word is encountered.
   * @throws UncheckedIOException If a profile with the same language code has already been added,
   *     indicating a duplicate configuration.
   */
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

  void rebuildPrefixLookup() {
    final Set<String> nGrams = this.languageCorporaProbabilities.keySet();
    this.charPrefixLookup = PrimitiveTrie.buildFromSet(nGrams);
  }

  /**
   * Gets a configured {@link LanguageDetector} instance.
   *
   * <p>Initializes the singleton factory instance on the first call by loading and processing
   * language profiles and model parameters based on the provided settings. Subsequent calls reuse
   * the cached factory data to configure new detector instances.
   *
   * <p><b>Threading Note:</b> The lazy initialization of the factory instance is not inherently
   * thread-safe for the very first concurrent calls. It relies on external synchronization or
   * warm-up calls (like those in {@link
   * LanguageDetectionOrchestrator#fromSettings(LanguageDetectionSettings)}) to ensure safe
   * initialization in multi-threaded environments.
   *
   * @param languageDetectionSettings settings for the language detector.
   * @return A configured {@link LanguageDetector} instance.
   * @throws IOException if loading profiles or model parameters fails during first initialization.
   * @throws UncheckedIOException wraps IOException from profile/model loading.
   * @see LanguageDetector
   * @see LanguageDetectionOrchestrator
   */
  public static LanguageDetector detector(final LanguageDetectionSettings languageDetectionSettings)
      throws IOException {
    if (instance == null) {
      instance = LanguageDetectorFactory.fromSettings(languageDetectionSettings);
    }
    return new LanguageDetector(
        instance.model(),
        instance.supportedIsoCodes639_1(),
        instance.languageCorporaProbabilities(),
        instance.charPrefixLookup(),
        instance.minNGramLength(),
        instance.maxNGramLength());
  }
}
