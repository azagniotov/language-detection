package io.github.azagniotov.language.benchmark;

import com.optimaize.langdetect.DetectedLanguage;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import io.github.azagniotov.language.annotations.GeneratedCodeClassCoverageExclusion;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@GeneratedCodeClassCoverageExclusion
public class OptimaizeDetector implements ThirdPartyDetector {

  private final LanguageDetector languageDetector;

  private OptimaizeDetector(final LanguageDetector languageDetector) {
    this.languageDetector = languageDetector;
  }

  public static ThirdPartyDetector from(final String iso639_1CodesCsv) {
    final Set<LdLocale> ldLocales = new HashSet<>();
    final String[] codes = iso639_1CodesCsv.split(",");
    for (final String langCode : codes) {
      if (langCode != null && !langCode.trim().isEmpty()) {
        ldLocales.add(LdLocale.fromString(langCode));
      }
    }
    assert ldLocales.size() == codes.length;

    try {
      final List<LanguageProfile> languageProfiles =
          new LanguageProfileReader().readBuiltIn(ldLocales);
      final LanguageDetector languageDetector =
          LanguageDetectorBuilder.create(NgramExtractors.standard())
              .withProfiles(languageProfiles)
              .build();

      return new OptimaizeDetector(languageDetector);
    } catch (final IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  @Override
  public String name() {
    return DetectorImpl.OPTIMAIZE.name().toLowerCase();
  }

  @Override
  public String detect(final String input) {
    final List<DetectedLanguage> probabilities = this.languageDetector.getProbabilities(input);
    if (probabilities.isEmpty()) {
      return LANGUAGE_CODE_NONE;
    }
    final DetectedLanguage detectedLanguage = probabilities.get(0);
    final LdLocale locale = detectedLanguage.getLocale();

    return locale.getLanguage();
  }
}
