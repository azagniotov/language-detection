package io.github.azagniotov.language.benchmark;

import io.github.azagniotov.language.annotations.GeneratedCodeClassCoverageExclusion;

@GeneratedCodeClassCoverageExclusion
public interface ThirdPartyDetector {

  static ThirdPartyDetector get(final String supportedDetector, final String iso639_1CodesCsv) {
    final DetectorImpl detector = DetectorImpl.valueOf(supportedDetector.toUpperCase());
    switch (detector) {
      case LINGUA_LOW:
        return LinguaLowDetector.from(iso639_1CodesCsv);
      case LINGUA_HIGH:
        return LinguaHighDetector.from(iso639_1CodesCsv);
      case OPTIMAIZE:
        return OptimaizeDetector.from(iso639_1CodesCsv);
      case TIKA_OPTIMAIZE:
        return TikaOptimaizeDetector.from(iso639_1CodesCsv);
      default:
        return DefaultDetector.from(iso639_1CodesCsv);
    }
  }

  String detect(final String input);
}
