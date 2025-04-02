package io.github.azagniotov.language.benchmark;

import static com.github.pemistahl.lingua.api.Language.CHINESE;
import static com.github.pemistahl.lingua.api.Language.getByIsoCode639_1;

import com.github.pemistahl.lingua.api.IsoCode639_1;
import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import io.github.azagniotov.language.annotations.GeneratedCodeClassCoverageExclusion;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@GeneratedCodeClassCoverageExclusion
public class LinguaHighDetector implements ThirdPartyDetector {

  private static final Set<String> ISO_CODE_639_1_LOOKUP;

  static {
    final Set<String> langCodeStrings = new HashSet<>();
    for (IsoCode639_1 langCode : IsoCode639_1.values()) {
      langCodeStrings.add(langCode.toString().toUpperCase());
    }
    ISO_CODE_639_1_LOOKUP = Collections.unmodifiableSet(langCodeStrings);
  }

  private final LanguageDetector languageDetector;

  private LinguaHighDetector(final LanguageDetector languageDetector) {
    this.languageDetector = languageDetector;
  }

  public static ThirdPartyDetector from(final String iso639_1CodesCsv) {
    final Set<Language> languages = new HashSet<>();
    final String[] codes = iso639_1CodesCsv.split(",");
    for (final String langCode : codes) {
      if (langCode != null && !langCode.trim().isEmpty()) {
        final String isoCode639_1Code = langCode.trim().toUpperCase();
        if (isoCode639_1Code.equals("ZH-CN")) {
          // Lingua does not have distinction between ZH-CN and ZH-TW,
          // therefore Lingua's Language.CHINESE(ZH) means the ZH-CN
          languages.add(CHINESE);
        } else {
          if (ISO_CODE_639_1_LOOKUP.contains(isoCode639_1Code)) {
            languages.add(getByIsoCode639_1(IsoCode639_1.valueOf(isoCode639_1Code)));
          }
        }
      }
    }

    assert languages.size() == codes.length;

    final Language[] languageArgs = languages.toArray(new Language[0]);
    final LanguageDetector languageDetector =
        LanguageDetectorBuilder.fromLanguages(languageArgs).build();
    return new LinguaHighDetector(languageDetector);
  }

  @Override
  public String name() {
    return DetectorImpl.LINGUA_HIGH.name().toLowerCase();
  }

  @Override
  public String detect(final String input) {
    return this.languageDetector.detectLanguageOf(input).getIsoCode639_1().name().toLowerCase();
  }
}
