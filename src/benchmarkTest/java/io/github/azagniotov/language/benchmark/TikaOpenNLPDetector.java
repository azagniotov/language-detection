package io.github.azagniotov.language.benchmark;

import io.github.azagniotov.language.annotations.GeneratedCodeClassCoverageExclusion;
import java.util.Set;
import org.apache.tika.langdetect.opennlp.OpenNLPDetector;
import org.apache.tika.language.detect.LanguageResult;

@GeneratedCodeClassCoverageExclusion
public class TikaOpenNLPDetector implements ThirdPartyDetector {

  private final OpenNLPDetector languageDetector;
  private final Set<String> iso6391Codes;

  private TikaOpenNLPDetector(
      final OpenNLPDetector languageDetector, final Set<String> iso639_1Codes) {
    this.languageDetector = languageDetector;
    this.iso6391Codes = iso639_1Codes;
  }

  public static ThirdPartyDetector from(final String iso639_1CodesCsv) {
    final String[] codes = iso639_1CodesCsv.split(",");
    return new TikaOpenNLPDetector(new OpenNLPDetector(), Set.of(codes));
  }

  @Override
  public String detect(final String input) {
    final LanguageResult languageResult = this.languageDetector.detect(input);
    final String detectedLanguage = languageResult.getLanguage();
    if (!ISO_639_3_TO_ISO_639_1.containsKey(detectedLanguage)) {
      return LANGUAGE_CODE_NONE;
    }

    final String detectedIso639_1Code = ISO_639_3_TO_ISO_639_1.get(detectedLanguage);
    if (this.iso6391Codes.contains(detectedIso639_1Code)) {
      return detectedIso639_1Code;
    }

    return LANGUAGE_CODE_NONE;
  }
}
