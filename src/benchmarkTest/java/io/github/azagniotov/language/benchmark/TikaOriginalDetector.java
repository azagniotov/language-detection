package io.github.azagniotov.language.benchmark;

import io.github.azagniotov.language.annotations.GeneratedCodeClassCoverageExclusion;
import java.util.List;
import java.util.Set;
import org.apache.tika.langdetect.tika.TikaLanguageDetector;
import org.apache.tika.language.detect.LanguageResult;

@GeneratedCodeClassCoverageExclusion
public class TikaOriginalDetector implements ThirdPartyDetector {

  private final TikaLanguageDetector languageDetector;
  private final Set<String> iso6391Codes;

  private TikaOriginalDetector(
      final TikaLanguageDetector languageDetector, final Set<String> iso639_1Codes) {
    this.languageDetector = languageDetector;
    this.iso6391Codes = iso639_1Codes;
  }

  public static ThirdPartyDetector from(final String iso639_1CodesCsv) {
    final String[] codes = iso639_1CodesCsv.split(",");
    return new TikaOriginalDetector(new TikaLanguageDetector(), Set.of(codes));
  }

  @Override
  public String detect(final String input) {
    final List<LanguageResult> languageResults = this.languageDetector.detectAll(input);
    if (languageResults.isEmpty()) {
      return LANGUAGE_CODE_NONE;
    }

    if (languageResults.get(0).isUnknown()) {
      return LANGUAGE_CODE_NONE;
    }

    final LanguageResult languageResult = languageResults.get(0);
    if (this.iso6391Codes.contains(languageResult.getLanguage())) {
      return languageResult.getLanguage();
    }

    return LANGUAGE_CODE_NONE;
  }
}
