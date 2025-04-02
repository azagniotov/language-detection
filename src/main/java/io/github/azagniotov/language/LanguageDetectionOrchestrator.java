package io.github.azagniotov.language;

import static io.github.azagniotov.language.LanguageDetector.CHINESE_LANGUAGE_RESPONSE;
import static io.github.azagniotov.language.LanguageDetector.JAPANESE_LANGUAGE_RESPONSE;
import static io.github.azagniotov.language.LanguageDetector.PERFECT_PROBABILITY;
import static io.github.azagniotov.language.LanguageDetector.UNDETERMINED_LANGUAGE_RESPONSE;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LanguageDetectionOrchestrator {

  private final LanguageDetectionSettings settings;
  private static final List<Language> EMPTY_RESULTS = Collections.emptyList();

  public static LanguageDetectionOrchestrator fromSettings(
      final LanguageDetectionSettings settings) {
    // Fake call to the class to cause it to be loaded and the static initializer executed
    NGram.normalize('\u0000');
    NGram.normalize('高');
    JapaneseHan.of('高');
    UnicodeCache.scriptOf('高');
    UnicodeCache.blockOf('高');

    try {
      // Will load and unzip GZipped JSON language profiles from the resource directory
      LanguageDetectorFactory.detector(settings);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    final LanguageDetectionOrchestrator orchestrator = new LanguageDetectionOrchestrator(settings);

    final String udhrJapanese =
        "世界人権宣言 人類社会の全部の構成員の固有の尊厳と平等で譲れない権利とを承認することは、"
            + "世界における自由、正義と平和の基礎だから、 人権の無視と軽侮が、人類の良心を踏みにじった野蛮行為をもたらし、"
            + "言論と信仰の自由が受けられ、恐怖と欠乏のない世界の到来が、一般の人々の最高の願望として宣言されたから、 "
            + "人間が専制と圧迫とに対する最後の手段として反逆に訴えることがないようにするためには、"
            + "法の支配によって人権を保護することが肝要だから、 "
            + "諸国間の友好関係の発展を促進することが肝要だから、 国際連合の諸国民は、国連憲章において、基本的人権、"
            + "人間の尊厳と価値並びに男女の同権についての信念を再確認し、かつ、"
            + "一層大きな自由のうちで社会的進歩と生活水準の向上とを促進することを決意したから、 加盟国は、国際連合と協力して、"
            + "人権と基本的自由の普遍的な尊重と遵守の促進を達成することを誓約したから、 これらの権利と自由に対する共通の理解は、"
            + "この誓約を完全にするためにもっとも重要だから、 よって、ここに、国連総会は、 社会の各個人と各機関が、"
            + "この世界人権宣言を常に念頭に置きながら、加盟国自身の人民の間にも、また、加盟国の管轄下にある地域の人民の間にも、"
            + "これらの権利と自由との尊重を指導と教育によって促進すること並びにそれらの普遍的措置によって確保することに努力するように、"
            + "全部の人民と全部の国とが達成すべき共通の基準として、この人権宣言を公布する。";
    orchestrator.doCjkHeuristic(udhrJapanese);
    orchestrator.doStatisticalDetection(udhrJapanese);
    orchestrator.detect(udhrJapanese);

    final String udhrEnglish =
        "Universal Declaration of Human Rights Whereas recognition of the inherent dignity and "
            + "of the equal and inalienable rights of all members of the human family is the foundation of "
            + "freedom, justice and peace in the world, Whereas disregard and contempt for human rights "
            + "have resulted in barbarous acts which have outraged the conscience of mankind, and the advent "
            + "of a world in which human beings shall enjoy freedom of speech and belief and freedom from fear "
            + "and want has been proclaimed as the highest aspiration of the common people, Whereas it is "
            + "essential, if man is not to be compelled to have recourse, as a last resort, to rebellion against "
            + "tyranny and oppression, that human rights should be protected by the rule of law, Whereas it "
            + "is essential to promote the development of friendly relations between nations, Whereas the "
            + "peoples of the United Nations have in the Charter reaffirmed their faith in fundamental human "
            + "rights, in the dignity and worth of the human person and in the equal rights of men and women "
            + "and have determined to promote social progress and better standards of life in larger freedom, "
            + "Whereas Member States have pledged themselves to achieve, in co‐operation with the United Nations, "
            + "the promotion of universal respect for and observance of human rights and fundamental freedoms, "
            + "Whereas a common understanding of these rights and freedoms is of the greatest importance for the "
            + "full realization of this pledge, Now, therefore, The General Assembly Proclaims this Universal "
            + "Declaration of Human Rights as a common standard of achievement for all peoples and all nations, "
            + "to the end that every individual and every organ of society, keeping this Declaration constantly "
            + "in mind, shall strive by teaching and education to promote respect for these rights and freedoms "
            + "and by progressive measures, national and international, to secure their universal and effective "
            + "recognition and observance, both among the peoples of Member States themselves and among the peoples "
            + "of territories under their jurisdiction.";
    orchestrator.doCjkHeuristic(udhrEnglish);
    orchestrator.doStatisticalDetection(udhrEnglish);
    orchestrator.detect(udhrEnglish);

    return orchestrator;
  }

  private LanguageDetectionOrchestrator(final LanguageDetectionSettings settings) {
    this.settings = settings;
  }

  public Language detect(final String input) {
    return detectAll(input).get(0);
  }

  public List<Language> detectAll(final String input) {
    if (input == null || input.trim().isEmpty()) {
      return Collections.singletonList(UNDETERMINED_LANGUAGE_RESPONSE);
    } else {
      final int maxChars = Math.min(this.settings.getMaxTextChars(), input.length());
      final String truncatedInput = input.substring(0, maxChars);

      final String sanitizedInput = conditionallySanitizeInput(truncatedInput);
      if (sanitizedInput.trim().isEmpty()) {
        return Collections.singletonList(UNDETERMINED_LANGUAGE_RESPONSE);
      }

      final List<Language> cjkLanguages = doCjkHeuristic(sanitizedInput);
      if (cjkLanguages.isEmpty()) {
        return doStatisticalDetection(sanitizedInput);
      } else {
        return cjkLanguages;
      }
    }
  }

  private List<Language> doCjkHeuristic(final String sanitizedInput) {
    // Do a quick heuristic to check if this is a Chinese / Japanese input
    if (this.settings.getCjkDetectionThreshold() > 0) {
      final CjkDecision decision =
          CjkDetector.decide(sanitizedInput, this.settings.getCjkDetectionThreshold());
      if (decision == CjkDecision.DECISION_JAPANESE) {
        return Collections.singletonList(JAPANESE_LANGUAGE_RESPONSE);
      } else if (decision == CjkDecision.DECISION_CHINESE) {
        if (this.settings.isClassifyChineseAsJapanese()) {
          return Collections.singletonList(JAPANESE_LANGUAGE_RESPONSE);
        } else {
          // If it is a Chinese input, then enforce
          // the input to be a Japanese string
          return Collections.singletonList(CHINESE_LANGUAGE_RESPONSE);
        }
      }
    }
    return EMPTY_RESULTS;
  }

  private List<Language> doStatisticalDetection(final String sanitizedInput) {
    // For non-Chinese/Japanese decisions we are going through
    // Naive Bayes below (the original LangDetect flow)
    final LanguageDetector languageDetector;
    try {
      languageDetector = LanguageDetectorFactory.detector(this.settings);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    final List<Language> languages = languageDetector.detectAll(sanitizedInput);
    final Language topLanguage = languages.get(0);
    if (topLanguage.getIsoCode639_1().equals(UNDETERMINED_LANGUAGE_RESPONSE.getIsoCode639_1())) {
      // Return undetermined ISO code to the client,
      // so that client can make a decision what to do,
      // e.g.: cross-index into all languages or search through all language fields
      return languages;
    }

    if (this.settings.isTopLanguageCertaintyThresholdSet()) {
      if (topLanguage.getProbability() < this.settings.getTopLanguageCertaintyThreshold()) {
        return Collections.singletonList(
            new Language(this.settings.getTopLanguageFallbackIsoCode639_1(), PERFECT_PROBABILITY));
      }
    } else if (this.settings.isMinimumCertaintyThresholdSet()) {
      final List<Language> aboveThreshold = new ArrayList<>();
      for (final Language language : languages) {
        if (language.getProbability() >= this.settings.getMinimumCertaintyThreshold()) {
          aboveThreshold.add(language);
        }
      }

      if (aboveThreshold.isEmpty()) {
        // Return undetermined ISO code to the client,
        // so that client can make a decision what to do,
        // e.g.: cross-index into all languages or search through all language fields
        return Collections.singletonList(UNDETERMINED_LANGUAGE_RESPONSE);
      } else {
        return aboveThreshold;
      }
    }

    return languages;
  }

  private String conditionallySanitizeInput(final String truncatedInput) {
    if (this.settings.isSanitizeInput()) {
      return InputSanitizer.sanitize(truncatedInput);
    } else {
      return truncatedInput;
    }
  }
}
