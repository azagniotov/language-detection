package io.github.azagniotov.language;

import static io.github.azagniotov.language.LanguageDetector.CHINESE_LANGUAGE_RESPONSE;
import static io.github.azagniotov.language.LanguageDetector.JAPANESE_LANGUAGE_RESPONSE;
import static io.github.azagniotov.language.LanguageDetector.PERFECT_PROBABILITY;
import static io.github.azagniotov.language.LanguageDetector.UNDETERMINED_LANGUAGE_RESPONSE;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LanguageDetectionOrchestrator {

  private final LanguageDetectionSettings settings;

  public LanguageDetectionOrchestrator(final LanguageDetectionSettings settings) {
    this.settings = settings;

    // Warm-up.
    // The warm-up sentence must be a non-CJK sentence in case if the config
    // settings.getCjkDetectionThreshold() > 0, as we need to ensure we are
    // calling the LanguageDetectorFactory.detector(this.settings) to load
    // the language profiles and init the LanguageDetectorFactory singleton.
    detect("Let's warm up, because languages are awesome!");
  }

  public Language detect(final String input) {
    return detectAll(input).get(0);
  }

  public List<Language> detectAll(final String input) {
    if (input == null || input.trim().isEmpty()) {
      return Collections.singletonList(UNDETERMINED_LANGUAGE_RESPONSE);
    } else {
      final int maxChars = Math.min(this.settings.getMaxTextChars(), input.length());
      final String truncatedInput = input.substring(0, maxChars);

      final String sanitizedInput = conditionallySanitizeInput(truncatedInput);
      if (sanitizedInput.trim().isEmpty()) {
        return Collections.singletonList(UNDETERMINED_LANGUAGE_RESPONSE);
      }

      // Do a quick heuristic to check if this is a Chinese / Japanese input
      if (this.settings.getCjkDetectionThreshold() > 0) {
        final CjkDecision decision =
            CjkDetector.decide(sanitizedInput, this.settings.getCjkDetectionThreshold());
        if (decision == CjkDecision.DECISION_JAPANESE) {
          return Collections.singletonList(JAPANESE_LANGUAGE_RESPONSE);
        } else if (decision == CjkDecision.DECISION_CHINESE) {
          if (this.settings.isClassifyChineseAsJapanese()) {
            return Collections.singletonList(JAPANESE_LANGUAGE_RESPONSE);
          } else {
            // If it is a Chinese input, then enforce
            // the input to be a Japanese string
            return Collections.singletonList(CHINESE_LANGUAGE_RESPONSE);
          }
        }
      }

      // For non-Chinese/Japanese decisions we are going through
      // Naive Bayes below (the original LangDetect flow)
      final LanguageDetector languageDetector;
      try {
        languageDetector = LanguageDetectorFactory.detector(this.settings);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }

      final List<Language> languages = languageDetector.detectAll(sanitizedInput);
      final Language topLanguage = languages.get(0);
      if (topLanguage.getIsoCode639_1().equals(UNDETERMINED_LANGUAGE_RESPONSE.getIsoCode639_1())) {
        // Return undetermined ISO code to the client,
        // so that client can make a decision what to do,
        // e.g.: cross-index into all languages or search through all language fields
        return languages;
      }

      if (this.settings.isTopLanguageCertaintyThresholdSet()) {
        if (topLanguage.getProbability() < this.settings.getTopLanguageCertaintyThreshold()) {
          return Collections.singletonList(
              new Language(
                  this.settings.getTopLanguageFallbackIsoCode639_1(), PERFECT_PROBABILITY));
        }
      } else if (this.settings.isMinimumCertaintyThresholdSet()) {
        final List<Language> aboveThreshold = new ArrayList<>();
        for (final Language language : languages) {
          if (language.getProbability() >= this.settings.getMinimumCertaintyThreshold()) {
            aboveThreshold.add(language);
          }
        }

        if (aboveThreshold.isEmpty()) {
          // Return undetermined ISO code to the client,
          // so that client can make a decision what to do,
          // e.g.: cross-index into all languages or search through all language fields
          return Collections.singletonList(UNDETERMINED_LANGUAGE_RESPONSE);
        } else {
          return aboveThreshold;
        }
      }

      return languages;
    }
  }

  private String conditionallySanitizeInput(final String truncatedInput) {
    if (this.settings.isSanitizeInput()) {
      return InputSanitizer.sanitize(truncatedInput);
    } else {
      return truncatedInput;
    }
  }
}
