package io.github.azagniotov.language.benchmark;

import io.github.azagniotov.language.LanguageDetectionOrchestrator;
import io.github.azagniotov.language.LanguageDetectionSettings;
import io.github.azagniotov.language.annotations.GeneratedCodeClassCoverageExclusion;

@GeneratedCodeClassCoverageExclusion
public class DefaultDetector implements ThirdPartyDetector {

  private final LanguageDetectionOrchestrator languageDetectionOrchestrator;

  private DefaultDetector(final LanguageDetectionOrchestrator languageDetectionOrchestrator) {
    this.languageDetectionOrchestrator = languageDetectionOrchestrator;
  }

  public static ThirdPartyDetector from(final String iso639_1CodesCsv) {
    final LanguageDetectionSettings settings =
        LanguageDetectionSettings.fromIsoCodes639_1(iso639_1CodesCsv)
            .withMininumCertainty(0.1)
            .withMaxTextChars(2000)
            .build();
    final LanguageDetectionOrchestrator languageDetectionOrchestrator =
        LanguageDetectionOrchestrator.fromSettings(settings);
    return new DefaultDetector(languageDetectionOrchestrator);
  }

  @Override
  public String detect(final String input) {
    return this.languageDetectionOrchestrator.detect(input).getIsoCode639_1();
  }
}
