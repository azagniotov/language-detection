package io.github.azagniotov.language;

import static io.github.azagniotov.language.InputSanitizer.filterOutNonWords;

import java.util.ArrayList;
import java.util.Arrays;
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

    static final Language UNDETERMINED_LANGUAGE_RESPONSE = new Language(ISO_CODE_639_3_UND, 0.0);
    static final Language JAPANESE_LANGUAGE_RESPONSE = new Language("ja", 1.0);

    // return top 5 detected language when calling detectAll(String)
    private static final int MAX_DETECTED_CLASSES = 5;

    // The ISO 639-1 codes that have been configured by the user, e.g.: en, ja, es, fr, de, it, zh-cn,
    // af, nl
    private final List<String> supportedIsoCodes639_1;

    // This contains a mapping of all words from the language profiles (the profiles
    // which correspond to the configured ISO 639-1 code for detection), along with
    // their associated probabilities. These probabilities are calculated as the ratio
    // between the word's frequency and the frequency of its N-grams.
    private final Map<String, double[]> languageCorporaProbabilities;

    private final int baseFreq;
    private final int numberOfTrials;
    private final int iterationLimit;
    private final double alpha;
    private final double alphaWidth;
    private final double probabilityThreshold;
    private final double convThreshold;

    LanguageDetector(
            final List<String> supportedIsoCodes639_1, final Map<String, double[]> languageCorporaProbabilities) {
        this.supportedIsoCodes639_1 = supportedIsoCodes639_1;
        this.languageCorporaProbabilities = languageCorporaProbabilities;

        this.baseFreq = 10000;
        this.iterationLimit = 10000;
        this.numberOfTrials = 7;
        this.alpha = 0.5;
        this.alphaWidth = 0.05;
        this.probabilityThreshold = 0.1;
        this.convThreshold = 0.99999;
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

        final double[] probabilities = detectBlock(normalizedText);
        final List<Language> languages = sortProbability(probabilities);

        return languages.subList(0, Math.min(languages.size(), MAX_DETECTED_CLASSES));
    }

    private double[] detectBlock(final String input) {
        final List<String> extractedNGrams = extractNGrams(input);

        final double[] languageProbabilities = new double[supportedIsoCodes639_1.size()];
        if (extractedNGrams.isEmpty()) {
            return languageProbabilities;
        }

        final Random random = new Random();
        random.setSeed(0L);

        for (int t = 0; t < numberOfTrials; ++t) {
            final double[] probabilities = initProbabilies();
            double a = this.alpha + random.nextGaussian() * alphaWidth;
            for (int i = 0; ; ++i) {
                final int randomIdx = random.nextInt(extractedNGrams.size());
                updateLangProb(probabilities, extractedNGrams.get(randomIdx), a);
                if (i % 5 == 0 && normalizeProb(probabilities) > convThreshold || i >= iterationLimit) {
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
    private double[] initProbabilies() {
        final double[] probabilities = new double[supportedIsoCodes639_1.size()];
        Arrays.fill(probabilities, 1.0 / supportedIsoCodes639_1.size());

        return probabilities;
    }

    /**
     * Extract n-grams from target text
     *
     * @return n-grams list
     */
    private List<String> extractNGrams(final String input) {
        final NGram ngram = new NGram();
        final List<String> extractedNWords = new ArrayList<>();

        for (int i = 0; i < input.length(); ++i) {
            ngram.addChar(input.charAt(i));

            for (int n = 1; n <= NGram.TRI_GRAM_LENGTH; ++n) {
                final String word = ngram.get(n);
                if (word != null && languageCorporaProbabilities.containsKey(word)) {
                    extractedNWords.add(word);
                }
            }
        }

        return extractedNWords;
    }

    /**
     * Update language probabilities with N-gram string(N=1,2,3)
     *
     * @param word N-gram string
     */
    private void updateLangProb(final double[] prob, final String word, final double alpha) {
        if (word == null || !languageCorporaProbabilities.containsKey(word)) {
            return;
        }
        double[] wordProbabilities = languageCorporaProbabilities.get(word);
        double weight = alpha / baseFreq;
        for (int i = 0; i < prob.length; ++i) {
            prob[i] *= weight + wordProbabilities[i];
        }
    }

    /**
     * Normalize probabilities and check convergence by the maximun probability
     *
     * @return maximum of probabilities
     */
    private double normalizeProb(final double[] prob) {
        if (prob.length == 0) {
            return 0d;
        }
        double sump = prob[0];
        for (int i = 1; i < prob.length; i++) {
            sump += prob[i];
        }
        double maxp = 0d;
        for (int i = 0; i < prob.length; i++) {
            double p = prob[i] / sump;
            if (maxp < p) {
                maxp = p;
            }
            prob[i] = p;
        }
        return maxp;
    }

    private List<Language> sortProbability(final double[] probabilities) {

        final List<Language> languages = new ArrayList<>();
        // Using final array as a holder of a counter
        final double[] probabilitiesSum = new double[1];

        for (int probIdx = 0; probIdx < probabilities.length; ++probIdx) {
            final double currentProbability = probabilities[probIdx];
            probabilitiesSum[0] += currentProbability;

            if (currentProbability > probabilityThreshold) {
                for (int langIdx = 0; langIdx <= languages.size(); ++langIdx) {
                    if (langIdx == languages.size() || languages.get(langIdx).getProbability() < currentProbability) {
                        final String code = supportedIsoCodes639_1.get(probIdx);
                        languages.add(langIdx, new Language(code, currentProbability));
                        break;
                    }
                }
            }
        }

        if (probabilitiesSum[0] == 0) {
            languages.add(new Language(ISO_CODE_639_3_UND, 0.0));
        }

        return languages;
    }
}
