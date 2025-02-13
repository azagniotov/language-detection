package io.github.azagniotov.language;

import static io.github.azagniotov.language.LanguageDetector.JAPANESE_LANGUAGE_RESPONSE;
import static io.github.azagniotov.language.LanguageDetector.UNDETERMINED_LANGUAGE_RESPONSE;

import java.util.Collections;
import java.util.List;

public class LanguageDetectionOrchestrator {

  private final LanguageDetectionSettings settings;

  public LanguageDetectionOrchestrator(final LanguageDetectionSettings settings) {
    this.settings = settings;
  }

  public Language detect(final String input) {
    return detectAll(input).get(0);
  }

  public List<Language> detectAll(final String input) {
    if (input == null || input.trim().isEmpty()) {
      return Collections.singletonList(UNDETERMINED_LANGUAGE_RESPONSE);
    } else {
      final String sanitizedInput = conditionallySanitizeForSearch(input);
      if (sanitizedInput.trim().isEmpty()) {
        return Collections.singletonList(UNDETERMINED_LANGUAGE_RESPONSE);
      }

      // Do a quick heuristic to check if this is a Chinese / Japanese input
      if (this.settings.isClassifyChineseAsJapanese()
          && this.settings.getClassifyChineseAsJapaneseThreshold() > 0) {
        final CjkDecision decision =
            CjkDetector.decide(
                sanitizedInput, this.settings.getClassifyChineseAsJapaneseThreshold());
        // If it is a Chinese / Japanese input, then detect/enforce the input to be a Japanese
        // string
        if (decision == CjkDecision.DECISION_CHINESE || decision == CjkDecision.DECISION_JAPANESE) {
          return Collections.singletonList(JAPANESE_LANGUAGE_RESPONSE);
        }
      }

      // Go through the original LangDetect flow otherwise
      final LanguageDetector languageDetector = LanguageDetectorFactory.detector(this.settings);

      final int maxChars = Math.min(this.settings.getMaxTextChars(), sanitizedInput.length());
      final List<Language> languages =
          languageDetector.detectAll(sanitizedInput.substring(0, maxChars));

      final Language topLanguage = languages.get(0);
      if (topLanguage.getIsoCode639_1().equals(UNDETERMINED_LANGUAGE_RESPONSE.getIsoCode639_1())) {
        // Return undetermined ISO code to the client,
        // so that client can make a decision what to do,
        // e.g.: cross-index into all languages or search through all language fields
        return languages;
      } else if (topLanguage.getProbability() < this.settings.getCertaintyThreshold()) {
        return Collections.singletonList(
            new Language(this.settings.getFallbackIsoCode639_1(), 1.0));
      } else {
        return languages;
      }
    }
  }

  private String conditionallySanitizeForSearch(final String input) {
    if (this.settings.isSanitizeForSearch()) {
      if (input.length() < this.settings.getSanitizeForSearchThreshold()) {
        return InputSanitizer.sanitizeForSearch(input);
      } else {
        return input;
      }
    } else {
      return input;
    }
  }
}
