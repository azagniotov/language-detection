package io.github.azagniotov.language.benchmark;

import io.github.azagniotov.language.annotations.GeneratedCodeClassCoverageExclusion;
import java.io.IOException;
import java.util.Set;
import org.apache.tika.langdetect.optimaize.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;

@GeneratedCodeClassCoverageExclusion
public class TikaOptimaizeDetector implements ThirdPartyDetector {

  private final LanguageDetector languageDetector;

  private TikaOptimaizeDetector(final LanguageDetector languageDetector) {
    this.languageDetector = languageDetector;
  }

  public static ThirdPartyDetector from(final String iso639_1CodesCsv) {
    final String[] codes = iso639_1CodesCsv.split(",");
    try {
      final LanguageDetector languageDetector =
          new OptimaizeLangDetector().loadModels(Set.of(codes));
      return new TikaOptimaizeDetector(languageDetector);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String detect(final String input) {
    final LanguageResult languageResult = this.languageDetector.detect(input);
    if (languageResult.isUnknown()) {
      return "none";
    }

    return languageResult.getLanguage();
  }
}
