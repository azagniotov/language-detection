package io.github.azagniotov.language.benchmark;

import io.github.azagniotov.language.annotations.GeneratedCodeClassCoverageExclusion;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import opennlp.tools.langdetect.Language;
import opennlp.tools.langdetect.LanguageDetectorME;
import opennlp.tools.langdetect.LanguageDetectorModel;

@GeneratedCodeClassCoverageExclusion
public class OpenNLPDetector implements ThirdPartyDetector {

  private final LanguageDetectorME languageDetector;
  private final Set<String> iso6391Codes;

  private OpenNLPDetector(
      final LanguageDetectorME languageDetector, final Set<String> iso639_1Codes) {
    this.languageDetector = languageDetector;
    this.iso6391Codes = iso639_1Codes;
  }

  public static ThirdPartyDetector from(final String iso639_1CodesCsv) {
    try {
      final InputStream modelAsStream =
          OpenNLPDetector.class.getResourceAsStream("/langdetect-183.bin");
      assert modelAsStream != null;
      final LanguageDetectorModel languageDetectorModel = new LanguageDetectorModel(modelAsStream);

      final String[] codes = iso639_1CodesCsv.split(",");

      return new OpenNLPDetector(new LanguageDetectorME(languageDetectorModel), Set.of(codes));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String name() {
    return DetectorImpl.OPENNLP.name().toLowerCase();
  }

  @Override
  public String detect(final String input) {
    final Language language = this.languageDetector.predictLanguage(input);
    final String detectedLanguage = language.getLang();
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
