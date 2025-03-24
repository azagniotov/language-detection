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
  static final Language CHINESE_LANGUAGE_RESPONSE = new Language("zh-cn", PERFECT_PROBABILITY);

  // return up to top 10 detected language when calling detectAll(String)
  private static final int MAX_DETECTED_CLASSES = 10;

  // An empirically derived value
  private static final int CONVERGENCE_CHECK_FREQUENCY = 5;

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
  private final float convergenceThreshold;

  LanguageDetector(
      final Model model,
      final List<String> supportedIsoCodes639_1,
      final Map<String, float[]> languageCorporaProbabilities,
      final int minNGramLength,
      final int maxNGramLength) {
    this.supportedIsoCodes639_1 = supportedIsoCodes639_1;
    this.languageCorporaProbabilities = languageCorporaProbabilities;
    this.minNGramLength = minNGramLength;
    this.maxNGramLength = maxNGramLength;

    this.baseFreq = model.getBaseFrequency();
    this.iterationLimit = model.getIterationLimit();
    this.numberOfTrials = model.getNumberOfTrials();
    this.alpha = model.getAlpha();
    this.alphaWidth = model.getAlphaWidth();
    this.convergenceThreshold = model.getConvergenceThreshold();
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

  /**
   * Naive Bayes classification algorithm implementation.
   *
   * <p>The core principle of Naive Bayes is based on Bayes' Theorem, which calculates the
   * conditional probability of an event based on prior knowledge of related conditions.
   *
   * <p>The detectBlock() method calculates language probabilities for each supported language. This
   * is a core function of Naive Bayes, which aims to determine the probability of a document
   * belonging to each class (language).
   *
   * @param input input string
   * @return an array of probabilities.
   */
  private float[] detectBlock(final String input) {
    final List<String> extractedNGrams = extractNGrams(input);

    final float[] languageProbabilities = new float[supportedIsoCodes639_1.size()];
    if (extractedNGrams.isEmpty()) {
      return languageProbabilities;
    }

    final Random random = new Random();
    random.setSeed(0L);

    // 1. The random selection of n-grams in the following code, and the multiple trials, indicates
    //    that this is a stochastic approximation of the Naive Bayes calculations. This is useful
    //    when dealing with very large datasets.
    // 2. Due to numberOfTrials the algorithm runs multiple times with slightly different parameters
    //    (due to the random smoothing, see below), and the results are averaged. This helps improve
    //    the robustness of the predictions.
    for (int t = 0; t < numberOfTrials; ++t) {
      final float[] probabilities = initProbabilies();

      // 1. Smoothing is essential in Naive Bayes to prevent zero probabilities when encountering
      //    unseen n-grams. This is a form of smoothing, likely a variant of Laplace smoothing or
      //    Lidstone smoothing adapted for this specific application. The alpha and alphaWidth
      //    parameters control the degree of smoothing.
      // 2. Gaussian: the random gaussian addition to alpha, implies the alpha value is being
      //    varied slightly between trials. This random variation of the alpha smoothing parameter
      //    is a unique implementation detail and something which is not "normally" used by Naive
      //    Bayers. By introducing random variation, the algorithm becomes less sensitive to the
      //    specific value of alpha and alphaWidth, which provides more robust performance, even if
      //    the hyperparameters are not perfectly tuned. Because the algorithm runs multiple trials
      //    with different alpha values, it effectively creates an ensemble of models. Averaging the
      //    results from these trials can improve the overall accuracy of the predictions.
      //    Most standard Naive Bayes implementations use fixed smoothing constants (like in Laplace
      //    or Lidstone smoothing) or more deterministic techniques (like Kneser-Ney). Random
      //    variation adds complexity and computational overhead, which might not be necessary for
      //    many applications. It is harder to analyze the results of a stochastic application,
      //    than a deterministic one. In essence, the Gaussian variation is a more advanced and
      //    potentially more powerful approach to smoothing. It allows the algorithm to adapt to
      //    the data and find a more optimal smoothing parameter. However, it's not the "normal"
      //    approach because it adds complexity and is not always necessary.
      final float alphaSmoothing = (float) (this.alpha + random.nextGaussian() * alphaWidth);
      // Smoothing is essential in Naive Bayes to prevent
      // zero probabilities when encountering unseen n-grams.
      final float weight = alphaSmoothing / baseFreq;

      for (int iteration = 0; iteration <= iterationLimit; ++iteration) {
        final int randomIdx = random.nextInt(extractedNGrams.size());
        final String nGram = extractedNGrams.get(randomIdx);

        // Retrieving the probabilities for a specific n-gram appears in each language.
        final float[] wordProbabilities = languageCorporaProbabilities.get(nGram);
        float probSum = 0.0f;
        for (int probIdx = 0; probIdx < probabilities.length; ++probIdx) {

          // Multiplying the existing probability of a language by the probability of
          // the n-gram appearing in that language. This aligns strongly with the
          // multiplicative nature of Naive Bayes probability calculations.
          probabilities[probIdx] *= weight + wordProbabilities[probIdx];
          probSum += probabilities[probIdx];
        }

        // Probabilities are normalized and checked for convergence threshold
        // on every 5th iteration with the help of CONVERGENCE_CHECK_FREQUENCY,
        // which was probably an empirically derived value. Why 5th in particular?
        // Probably to reduce the computational overhead of the loop, and potentially
        // improving performance because checking for convergence can be computationally
        // expensive, especially if the probabilities array is large. I guess this was
        // the trade-off between the accuracy of the convergence check and the speed
        // of the algorithm chosen by Nakatani Shuyo, the original author.
        if (iteration % CONVERGENCE_CHECK_FREQUENCY == 0) {
          // Normalization is often used in probability calculations to ensure that
          // the probabilities sum to 1. This is a standard practice in Naive Bayes.
          if (normalizeProbabilitiesAndReturnMax(probSum, probabilities) > convergenceThreshold) {
            break;
          }
        }
      }

      // This loop averages the probability estimates obtained from multiple
      // trials (iterations of the outer loop, controlled by numberOfTrials).
      // This averaging helps to smooth out the variations in probability estimates
      // that might occur between the individual trials due to the stochastic nature
      // of the algorithm (random n-gram selection and alpha variation). In other
      // words, this averaging process inherently contributes to a more stable and
      // representative probability distribution by reducing the variance of the
      // probability estimates, making the results more stable and reliable.
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
   * The method has two functions:
   *
   * <p>1. Normalizes the input probability array to sum to 1. This is achieved by dividing each
   * element of the array by the total sum of all elements. This results in probability
   * distribution.
   *
   * <p>2. Returns the maximum value found within the normalized probability array.
   *
   * <p>This function is essential for ensuring that the probabilities returned by the {@link
   * #detectBlock(String)} represent a valid probability distribution across all supported
   * languages.
   *
   * @return the maximum value found within the normalized probability array. This maximum value is
   *     used as a convergence check, to determine if the probability distribution has stabilized.
   */
  private float normalizeProbabilitiesAndReturnMax(final float sump, final float[] prob) {
    if (prob.length == 0 || sump == 0) {
      return ZERO_PROBABILITY;
    }

    float maxp = ZERO_PROBABILITY;
    for (int i = 0; i < prob.length; i++) {
      prob[i] = prob[i] / sump;
      maxp = Math.max(maxp, prob[i]);
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
