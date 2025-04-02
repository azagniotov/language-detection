package io.github.azagniotov.language.benchmark;

import static java.util.Objects.requireNonNull;

import com.github.jfasttext.JFastText;
import io.github.azagniotov.language.annotations.GeneratedCodeClassCoverageExclusion;
import java.util.Set;

@GeneratedCodeClassCoverageExclusion
public class JFastTextDetector implements ThirdPartyDetector {

  // https://fasttext.cc/docs/en/language-identification.html#content
  private static final String MODEL_PATH =
      requireNonNull(JFastTextDetector.class.getResource("/lid.176.bin")).getPath();

  private static final String COMPRESSED_MODEL_PATH =
      requireNonNull(JFastTextDetector.class.getResource("/lid.176.ftz")).getPath();

  private final JFastText languageDetector;
  private final Set<String> iso6391Codes;

  private JFastTextDetector(final JFastText languageDetector, final Set<String> iso639_1Codes) {
    this.languageDetector = languageDetector;
    this.iso6391Codes = iso639_1Codes;
  }

  public static ThirdPartyDetector from(final String iso639_1CodesCsv) {
    final JFastText jFastText = new JFastText();
    jFastText.loadModel(COMPRESSED_MODEL_PATH);

    final String[] codes = iso639_1CodesCsv.split(",");

    return new JFastTextDetector(jFastText, Set.of(codes));
  }

  @Override
  public String name() {
    return DetectorImpl.JFASTTEXT.name().toLowerCase();
  }

  @Override
  public String detect(final String input) {
    final JFastText.ProbLabel probLabel = languageDetector.predictProba(input);

    final String detectedIso639_1Code =
        probLabel.label.replaceAll(languageDetector.getLabelPrefix(), "");
    if (this.iso6391Codes.contains(detectedIso639_1Code)) {
      return detectedIso639_1Code;
    }

    return LANGUAGE_CODE_NONE;
  }
}
