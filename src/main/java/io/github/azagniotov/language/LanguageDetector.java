package io.github.azagniotov.language;

import static io.github.azagniotov.language.InputSanitizer.filterOutNonWords;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The {@link LanguageDetector} class identifies the language (ISO 639-1 code) of a given text. An
 * instance of this class is created through the factory class {@link LanguageDetectorFactory}.
 *
 * <p>The detector delivers language detection results for the provided text through {@link
 * #detectAll(String)}. The method {@link #detectAll(String)} provides a list of up to five
 * languages along with their respective probabilities.
 *
 * <p>The detector is equipped with certain parameters for the language detection model that, at
 * present, are fixed. These parameters reflect well-considered defaults that are optimized for the
 * majority of use cases.
 *
 * @see LanguageDetectorFactory
 */
class LanguageDetector {

  private static final String ISO_CODE_639_3_UND = "und";
  private static final float ZERO_PROBABILITY = 0f;
  static final float PERFECT_PROBABILITY = 1.0f;

  static final Language UNDETERMINED_LANGUAGE_RESPONSE =
      new Language(ISO_CODE_639_3_UND, ZERO_PROBABILITY);
  static final Language JAPANESE_LANGUAGE_RESPONSE = new Language("ja", PERFECT_PROBABILITY);

  // return up to top 10 detected language when calling detectAll(String)
  private static final int MAX_DETECTED_CLASSES = 10;

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

  private final int baseFreq;
  private final int numberOfTrials;
  private final int iterationLimit;
  private final float alpha;
  private final float alphaWidth;
  private final float convThreshold;

  LanguageDetector(
      final List<String> supportedIsoCodes639_1,
      final Map<String, float[]> languageCorporaProbabilities,
      final int minNGramLength,
      final int maxNGramLength) {
    this.supportedIsoCodes639_1 = supportedIsoCodes639_1;
    this.languageCorporaProbabilities = languageCorporaProbabilities;
    this.minNGramLength = minNGramLength;
    this.maxNGramLength = maxNGramLength;

    this.baseFreq = 10000;
    this.iterationLimit = 10000;
    this.numberOfTrials = 7;
    this.alpha = 0.5f;
    this.alphaWidth = 0.05f;
    this.convThreshold = 0.99999f;
  }

  /**
   * Get language candidates which have high probabilities
   *
   * @return possible languages list (whose probabilities are over probabilityThreshold, ordered by
   *     probabilities descendently
   */
  List<Language> detectAll(final String text) {
    // To filter based on "\\P{IsWord}" is the original filtering check by the original author
    // Do not .trim() the input nor the result, otherwise accuracy unit tests will fail
    final String sanitizedInput = filterOutNonWords(text);

    // Do not .trim() the input nor the result, otherwise accuracy unit tests will fail
    final String normalizedText = NGram.normalizeVietnamese(sanitizedInput);

    final float[] probabilities = detectBlock(normalizedText);
    final List<Language> languages = sortProbability(probabilities);

    return languages.subList(0, Math.min(languages.size(), MAX_DETECTED_CLASSES));
  }

  private float[] detectBlock(final String input) {
    final List<String> extractedNGrams = extractNGrams(input);

    final float[] languageProbabilities = new float[supportedIsoCodes639_1.size()];
    if (extractedNGrams.isEmpty()) {
      return languageProbabilities;
    }

    final Random random = new Random();
    random.setSeed(0L);

    for (int t = 0; t < numberOfTrials; ++t) {
      final float[] probabilities = initProbabilies();
      float alphaSmoothing = (float) (this.alpha + random.nextGaussian() * alphaWidth);

      for (int i = 0; i <= iterationLimit; ++i) {
        final int randomIdx = random.nextInt(extractedNGrams.size());
        final String nGram = extractedNGrams.get(randomIdx);
        updateLangProb(probabilities, nGram, alphaSmoothing);

        if (i % 5 == 0 && normalizeProb(probabilities) > convThreshold) {
          break;
        }
      }

      for (int j = 0; j < languageProbabilities.length; ++j) {
        languageProbabilities[j] += probabilities[j] / numberOfTrials;
      }
    }
    return languageProbabilities;
  }

  /**
   * Initialize an array of language probabilities.
   *
   * @return initialized array of language probabilities
   */
  private float[] initProbabilies() {
    final float[] probabilities = new float[supportedIsoCodes639_1.size()];
    Arrays.fill(probabilities, PERFECT_PROBABILITY / supportedIsoCodes639_1.size());

    return probabilities;
  }

  /**
   * Extract n-grams from target text
   *
   * @return n-grams list
   */
  List<String> extractNGrams(final String input) {
    final NGram ngram = new NGram(input, this.minNGramLength, this.maxNGramLength);

    return ngram.extractNGrams(languageCorporaProbabilities.keySet());
  }

  /**
   * Update language probabilities with N-gram string(N=1,2,3)
   *
   * @param word N-gram string
   */
  private void updateLangProb(final float[] prob, final String word, final float alpha) {
    float[] wordProbabilities = languageCorporaProbabilities.get(word);
    float weight = alpha / baseFreq;
    for (int i = 0; i < prob.length; ++i) {
      prob[i] *= weight + wordProbabilities[i];
    }
  }

  /**
   * Normalize probabilities and check convergence by the maximun probability
   *
   * @return maximum of probabilities
   */
  private float normalizeProb(final float[] prob) {
    if (prob.length == 0) {
      return ZERO_PROBABILITY;
    }
    float sump = prob[0];
    for (int i = 1; i < prob.length; i++) {
      sump += prob[i];
    }
    float maxp = ZERO_PROBABILITY;
    for (int i = 0; i < prob.length; i++) {
      float p = prob[i] / sump;
      if (maxp < p) {
        maxp = p;
      }
      prob[i] = p;
    }
    return maxp;
  }

  private List<Language> sortProbability(final float[] probabilities) {

    final List<Language> languages = new ArrayList<>();
    // Using final array as a holder of a counter
    final float[] probabilitiesSum = new float[1];

    for (int probIdx = 0; probIdx < probabilities.length; ++probIdx) {
      final float currentLanguageProbability = probabilities[probIdx];
      probabilitiesSum[0] += currentLanguageProbability;
      if (currentLanguageProbability > ZERO_PROBABILITY) {
        final String code = supportedIsoCodes639_1.get(probIdx);
        languages.add(new Language(code, currentLanguageProbability));
      }
    }
    Collections.sort(languages);

    if (probabilitiesSum[0] == 0 || languages.isEmpty()) {
      // In certain scenarios, the sum of probabilities could be so low
      // that it fails to reach a defined threshold. In such cases, the
      // probability sum might still be greater than zero, but remain
      // below the threshold. To handle both situations effectively, it
      // is essential to ensure that we return the ISO 639-3 code "und"
      // to signify an unknown or undetermined language in these cases.
      languages.add(new Language(ISO_CODE_639_3_UND, ZERO_PROBABILITY));
    }

    return languages;
  }
}
