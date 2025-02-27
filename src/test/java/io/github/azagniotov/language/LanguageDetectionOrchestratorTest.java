package io.github.azagniotov.language;

import static io.github.azagniotov.language.TestDefaultConstants.MAX_NGRAM_LENGTH;
import static io.github.azagniotov.language.TestDefaultConstants.MIN_NGRAM_LENGTH;
import static io.github.azagniotov.language.TestHelper.resetLanguageDetectorFactoryInstance;
import static io.github.azagniotov.language.TestHelper.testLanguage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Warning. Heads up. Attention.
 *
 * <p>Tests in the current class invoke the public API of {@link LanguageDetectionOrchestrator},
 * which internally calls the {@link LanguageDetectorFactory#detector(LanguageDetectionSettings)},
 * which caches the singleton instance of the created {@link LanguageDetectorFactory} which caches
 * the loaded ISO codes and profiles.
 *
 * <p>With that in mind, please make sure to invoke {@link
 * TestHelper#resetLanguageDetectorFactoryInstance()} in setUp() to avoid flaky tests due to the
 * cached singleton instance of the {@link LanguageDetectorFactory}.
 */
public class LanguageDetectionOrchestratorTest {

  private static final String ISO_CODES = "en, ja, es, fr, de, zh-cn, af, nl, ko";
  private static final LanguageDetectionSettings SETTINGS =
      LanguageDetectionSettings.fromIsoCodes639_1(ISO_CODES).build();

  private static LanguageDetector DEFAULT_DETECTOR;

  @BeforeClass
  public static void beforeClass() throws Exception {
    final LanguageDetectorFactory factory = new LanguageDetectorFactory(SETTINGS);
    DEFAULT_DETECTOR =
        new LanguageDetector(
            factory.getSupportedIsoCodes639_1(),
            factory.getLanguageCorporaProbabilities(),
            MIN_NGRAM_LENGTH,
            MAX_NGRAM_LENGTH);
  }

  @Before
  public void setUp() throws Exception {
    resetLanguageDetectorFactoryInstance();
  }

  @Test
  public void detectsNullInput() throws Exception {
    final LanguageDetectionOrchestrator orchestrator = new LanguageDetectionOrchestrator(SETTINGS);
    final Language language = orchestrator.detect(null);

    assertEquals("und", language.getIsoCode639_1());
    assertEquals("0.0", String.valueOf(language.getProbability()));
  }

  @Test
  public void detectsEmptyInput() throws Exception {
    final LanguageDetectionOrchestrator orchestrator = new LanguageDetectionOrchestrator(SETTINGS);
    final Language language = orchestrator.detect("");

    assertEquals("und", language.getIsoCode639_1());
    assertEquals("0.0", String.valueOf(language.getProbability()));
  }

  @Test
  public void detectsBlankInput() throws Exception {
    final LanguageDetectionOrchestrator orchestrator = new LanguageDetectionOrchestrator(SETTINGS);
    final Language language = orchestrator.detect(" ");

    assertEquals("und", language.getIsoCode639_1());
    assertEquals("0.0", String.valueOf(language.getProbability()));
  }

  @Test
  public void detectsAnyCjkInputAsJapanese() throws Exception {
    final LanguageDetectionSettings settings =
        LanguageDetectionSettings.fromIsoCodes639_1(ISO_CODES)
            .withClassifyChineseAsJapanese()
            .build();

    final LanguageDetectionOrchestrator orchestrator = new LanguageDetectionOrchestrator(settings);
    final List<String> inputs =
        Arrays.asList(
            "QRコード",
            "in1729x01_J-LCM刷新プロジェクト",
            "\"ヨキ\" AND \"杉山\"",
            "RG-12　仕様書",
            "発注-営推契-オ-第24-01905号",
            "西条寺家Ⅲ１３号地ＳＲ８６０００３２",
            "【12.21s】【流山運動公園：流山市市野谷】【中村P：3台】",
            "ﾊｰﾄﾞｶﾌﾟｾﾙ(ｻｲｽﾞ3号 NATURAL B／C)",
            "CAD　キー",
            "自動dys",
            "SKS活動",
            "CHAINON上熊本",
            "T-LINK判定書",
            "RV  20UB　膜厚",
            "【MANGO】_当月_9161_[CI][I]",
            "#4_pj_23D002_HCMJ_デジ戦",
            "Microsoft　インポート",
            "\"BH34\"AND\"PCT\"紹介",
            "TOEIC 分布",
            "BP3棟3階 FCU-302",
            "(Ts-Hf3 OR Hf-PAH) AND (気化器",
            "ミニWebinar 最新FE-SEM",
            "爱云发见",
            "爱云发见 ABCD",
            "愛雲髮見 爱云发见",
            "麵最终报告 1234567890.pdf",
            "麵",
            "郁",
            "臟髒",
            "脏",
            "懞濛矇",
            "蒙");

    for (final String input : inputs) {
      final Language language = orchestrator.detect(input);

      assertEquals("ja", language.getIsoCode639_1());
      assertEquals("1.0", String.valueOf(language.getProbability()));
    }
  }

  @Test
  public void doesNotDetectsLatinInputAsJapanese() throws Exception {
    final LanguageDetectionSettings settings =
        LanguageDetectionSettings.fromIsoCodes639_1(ISO_CODES)
            .withClassifyChineseAsJapanese()
            .build();

    final LanguageDetectionOrchestrator orchestrator = new LanguageDetectionOrchestrator(settings);
    final List<String> inputs = Arrays.asList("report.xls", "山 ABCDEFGHJKL");

    for (final String input : inputs) {
      final Language language = orchestrator.detect(input);

      assertNotEquals("ja", language.getIsoCode639_1());
    }
  }

  @Test
  public void detectsLanguage() throws Exception {
    final LanguageDetectionOrchestrator orchestrator = new LanguageDetectionOrchestrator(SETTINGS);

    assertEquals("ja", orchestrator.detect("ｼｰｻｲﾄﾞ_ﾗｲﾅｰ.pdf").getIsoCode639_1());
    assertEquals("ja", orchestrator.detect("東京に行き ABCDEF").getIsoCode639_1());
    assertEquals("ja", orchestrator.detect("ABCDEF 東京に行き").getIsoCode639_1());
    assertEquals("ja", orchestrator.detect("\"ヨキ\" AND \"杉山\"").getIsoCode639_1());
    assertEquals("ja", orchestrator.detect("\"ヨキ\" AND \"杉山\" OR \"分布\"").getIsoCode639_1());
    assertEquals("ja", orchestrator.detect("#4_pj_23D002_HCMJ_デジ戦").getIsoCode639_1());
    assertEquals("ja", orchestrator.detect("in1729x01_J-LCM刷新プロジェクト.docx").getIsoCode639_1());
    assertEquals("ja", orchestrator.detect("Ｃｕｌｔｕｒｅ　ｏｆ　Ｊａｐａｎ").getIsoCode639_1());
    assertEquals("ja", orchestrator.detect("ﾊｰﾄﾞｶﾌﾟｾﾙ(ｻｲｽﾞ3号 NATURAL B／C)").getIsoCode639_1());
    assertEquals("ja", orchestrator.detect("Microsoft　インポート").getIsoCode639_1());
    assertEquals("ja", orchestrator.detect("Ｃｕｌｔｕｒｅ　ｏｆ　Ｊａｐａｎ.pdf").getIsoCode639_1());
    assertEquals("ja", orchestrator.detect("１２３４５６７８９０.xls 報告").getIsoCode639_1());
    assertEquals("ja", orchestrator.detect("富山県高岡市.txt").getIsoCode639_1());
    assertEquals("ja", orchestrator.detect("東 ABCDE").getIsoCode639_1());
    assertEquals("ja", orchestrator.detect("東京 ABCDEF").getIsoCode639_1());

    assertEquals("ja", orchestrator.detect("ﾊｰﾄﾞｶﾌﾟｾﾙ(ｻｲｽﾞ3号 NATURAL B／C).pdf").getIsoCode639_1());
    assertEquals("en", orchestrator.detect("刷新eclipseRCPExt.pdf").getIsoCode639_1());

    assertEquals("en", orchestrator.detect("This is a very small test").getIsoCode639_1());

    assertEquals("de", orchestrator.detect("Das kann deutsch sein").getIsoCode639_1());
    assertEquals("de", orchestrator.detect("Das ist ein Text").getIsoCode639_1());

    assertEquals("zh-cn", orchestrator.detect("TOEIC 分布").getIsoCode639_1());

    assertEquals("zh-cn", orchestrator.detect("【12.21s】【流山運動公園：流山市市野谷】【中村P：3台】").getIsoCode639_1());
    assertEquals("zh-cn", orchestrator.detect("AND \"杉山\" OR \"分布\"").getIsoCode639_1());

    assertEquals("zh-cn", orchestrator.detect("㈱_(株)_①②③_㈱㈲㈹").getIsoCode639_1());
    assertEquals("zh-cn", orchestrator.detect("帮助他们以截然不同的方式探索和分析数据.pdf").getIsoCode639_1());

    assertEquals(
        "zh-cn",
        orchestrator
            .detect(
                "位于美国首都华盛顿都会圈的希望中文学校５日晚举办活动庆祝建立２０周年。"
                    + "从中国大陆留学生为子女学中文而自发建立的学习班，"
                    + "到学生规模在全美名列前茅的中文学校，这个平台的发展也折射出美国的中文教育热度逐步提升。"
                    + "希望中文学校是大华盛顿地区最大中文学校，现有７个校区逾４０００名学生，"
                    + "规模在美国东部数一数二。不过，见证了希望中文学校２０年发展的人们起初根本无法想象这个小小的中文教育平台能发展到今日之规模。")
            .getIsoCode639_1());
  }

  @Test
  public void detectsAll() throws Exception {
    final LanguageDetectionOrchestrator orchestrator = new LanguageDetectionOrchestrator(SETTINGS);

    final List<Language> languages =
        orchestrator.detectAll(" deel te neem, om die kunste te geniet en in weten");
    assertEquals(2, languages.size());
    assertEquals("af", languages.get(0).getIsoCode639_1());
    assertEquals("0.7142819", String.valueOf(languages.get(0).getProbability()));

    assertEquals("nl", languages.get(1).getIsoCode639_1());
    assertEquals("0.28571808", String.valueOf(languages.get(1).getProbability()));
  }

  @Test
  public void detectsLanguageWithMaxTextCharsLimit() throws Exception {
    final LanguageDetectionSettings settings =
        LanguageDetectionSettings.fromIsoCodes639_1(ISO_CODES).withMaxTextChars(5).build();

    final LanguageDetectionOrchestrator orchestrator = new LanguageDetectionOrchestrator(settings);

    assertEquals("es", orchestrator.detect("abcdef 東京に行き").getIsoCode639_1());
    assertEquals("ja", orchestrator.detect("東京に行き abcdef").getIsoCode639_1());
    assertEquals("en", orchestrator.detect("aBCDEF").getIsoCode639_1());
    assertEquals("ja", orchestrator.detect("東京に行きA").getIsoCode639_1());
  }

  @Test
  public void detectsLanguageWithAndWithoutInputSanitizationForSearch() throws Exception {
    final LanguageDetectionSettings settingsWithoutSearchSanitize =
        LanguageDetectionSettings.fromIsoCodes639_1(ISO_CODES).withoutSanitizeForSearch().build();

    LanguageDetectionOrchestrator orchestrator =
        new LanguageDetectionOrchestrator(settingsWithoutSearchSanitize);
    assertEquals("ja", orchestrator.detect("Ｃｕｌｔｕｒｅ　ｏｆ　Ｊａｐａｎ.pdf").getIsoCode639_1());
    assertEquals("ja", orchestrator.detect("ﾊｰﾄﾞｶﾌﾟｾﾙ(ｻｲｽﾞ3号 NATURAL B／C).pdf").getIsoCode639_1());
    assertEquals("fr", orchestrator.detect("report.xls").getIsoCode639_1());

    final LanguageDetectionSettings settingsWithSearchSanitize =
        LanguageDetectionSettings.fromIsoCodes639_1(ISO_CODES).build();

    orchestrator = new LanguageDetectionOrchestrator(settingsWithSearchSanitize);
    assertEquals("ja", orchestrator.detect("Ｃｕｌｔｕｒｅ　ｏｆ　Ｊａｐａｎ.pdf").getIsoCode639_1());
    assertEquals("ja", orchestrator.detect("ﾊｰﾄﾞｶﾌﾟｾﾙ(ｻｲｽﾞ3号 NATURAL B／C).pdf").getIsoCode639_1());
    assertEquals("es", orchestrator.detect("report.xls").getIsoCode639_1());
  }

  @Test
  public void respondsWithFallbackLanguageForTopDetectedLanguage() throws Exception {
    final LanguageDetectionSettings settings =
        LanguageDetectionSettings.fromIsoCodes639_1(ISO_CODES)
            .withTopLanguageMininumCertainty(0.95f, "ru")
            .build();

    final LanguageDetectionOrchestrator orchestrator = new LanguageDetectionOrchestrator(settings);

    assertEquals("und", orchestrator.detect("...").getIsoCode639_1());
    assertEquals("und", orchestrator.detect("2025").getIsoCode639_1());
    assertEquals("und", orchestrator.detect("1234567").getIsoCode639_1());
    assertEquals("ru", orchestrator.detect("apples yaba").getIsoCode639_1());
    assertEquals("ru", orchestrator.detect("ourney mi casa").getIsoCode639_1());
  }

  @Test
  public void respondsWithUndeterminedLanguageWhenNoDetectedAboveMinimumCertaintyThreshold()
      throws Exception {
    final LanguageDetectionSettings settings =
        LanguageDetectionSettings.fromIsoCodes639_1(ISO_CODES).withMininumCertainty(0.72f).build();

    final LanguageDetectionOrchestrator orchestrator = new LanguageDetectionOrchestrator(settings);

    // Would return: [ES=0.7142833, EN=0.14285952, FR=0.14285715]
    final List<Language> languages = orchestrator.detectAll("ourney mi casa sen");

    assertEquals(1, languages.size());
    assertEquals("und", languages.get(0).getIsoCode639_1());
    assertEquals("0.0", languages.get(0).getProbability() + "");
  }

  @Test
  public void respondsWithLanguageAboveMinimumCertaintyThreshold() throws Exception {
    final LanguageDetectionSettings settings =
        LanguageDetectionSettings.fromIsoCodes639_1(ISO_CODES).withMininumCertainty(0.7f).build();

    final LanguageDetectionOrchestrator orchestrator = new LanguageDetectionOrchestrator(settings);

    // Would return: [ES=0.7142833, EN=0.14285952, FR=0.14285715]
    final List<Language> languages = orchestrator.detectAll("ourney mi casa sen");

    assertEquals(1, languages.size());
    assertEquals("es", languages.get(0).getIsoCode639_1());
    assertEquals("0.7142833", languages.get(0).getProbability() + "");
  }

  @Test
  public void testChinese() throws Exception {
    testLanguage("chinese.txt", "zh-cn", DEFAULT_DETECTOR);
  }

  @Test
  public void testJapanese() throws Exception {
    testLanguage("japanese.txt", "ja", DEFAULT_DETECTOR);
  }

  @Test
  public void testKorean() throws Exception {
    testLanguage("korean.txt", "ko", DEFAULT_DETECTOR);
  }

  @Test
  public void testGerman() throws Exception {
    testLanguage("german.txt", "de", DEFAULT_DETECTOR);
  }
}
