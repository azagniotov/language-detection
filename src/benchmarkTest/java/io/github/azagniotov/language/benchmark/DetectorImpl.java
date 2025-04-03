package io.github.azagniotov.language.benchmark;

import io.github.azagniotov.language.annotations.GeneratedCodeClassCoverageExclusion;

@GeneratedCodeClassCoverageExclusion
public enum DetectorImpl {
  // The current libary, language-detection by azagniotov
  DEFAULT,

  // https://github.com/vinhkhuc/JFastText Java wrapper around Facebook's FastText
  JFASTTEXT,

  // https://github.com/pemistahl/lingua with a low accuracy mode on
  LINGUA_LOW,

  // https://github.com/pemistahl/lingua with the default high accuracy mode on
  LINGUA_HIGH,

  // https://github.com/optimaize/language-detector
  OPTIMAIZE,

  // https://opennlp.apache.org/
  OPENNLP,

  // https://tika.apache.org This is based on OpenNLP's language detector. However,
  // they've built their own ProbingLanguageDetector and their own language models.
  TIKA_OPENNLP,

  // https://tika.apache.org
  TIKA_OPTIMAIZE;

  public static String[] valuesLowerCased;

  static {
    valuesLowerCased = new String[DetectorImpl.values().length];
    for (final DetectorImpl member : DetectorImpl.values()) {
      valuesLowerCased[member.ordinal()] = member.name().toLowerCase();
    }
  }
}
