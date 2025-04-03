package io.github.azagniotov.language.benchmark;

import io.github.azagniotov.language.annotations.GeneratedCodeClassCoverageExclusion;
import java.util.HashMap;
import java.util.Map;

@GeneratedCodeClassCoverageExclusion
public interface ThirdPartyDetector {

  String LANGUAGE_CODE_NONE = "none";

  Map<String, String> ISO_639_3_TO_ISO_639_1 =
      new HashMap<>() {
        {
          put("eng", "en"); // English
          put("spa", "es"); // Spanish
          put("fra", "fr"); // French
          put("deu", "de"); // German
          put("ita", "it"); // Italian
          put("jpn", "ja"); // Japanese
        }
      };

  static ThirdPartyDetector detectorFor(
      final String supportedDetector, final String iso639_1CodesCsv) {
    final DetectorImpl detector = DetectorImpl.valueOf(supportedDetector.toUpperCase());
    switch (detector) {
      case LINGUA_LOW:
        return LinguaLowDetector.from(iso639_1CodesCsv);
      case LINGUA_HIGH:
        return LinguaHighDetector.from(iso639_1CodesCsv);
      case OPTIMAIZE:
        return OptimaizeDetector.from(iso639_1CodesCsv);
      case TIKA_OPENNLP:
        return TikaOpenNLPDetector.from(iso639_1CodesCsv);
      case TIKA_OPTIMAIZE:
        return TikaOptimaizeDetector.from(iso639_1CodesCsv);
      case OPENNLP:
        return OpenNLPDetector.from(iso639_1CodesCsv);
      case JFASTTEXT:
        return JFastTextDetector.from(iso639_1CodesCsv);
      default:
        return DefaultDetector.from(iso639_1CodesCsv);
    }
  }

  String name();

  String detect(final String input);
}
