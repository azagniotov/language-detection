package io.github.azagniotov.language;

import static io.github.azagniotov.language.GzipUtils.gzipString;
import static io.github.azagniotov.language.LanguageDetectionSettings.DEFAULT_SETTINGS_ALL_LANGUAGES;
import static io.github.azagniotov.language.StringConstants.BLANK_SPACE;
import static io.github.azagniotov.language.StringConstants.EMPTY_STRING;
import static io.github.azagniotov.language.TestDefaultConstants.MAX_NGRAM_LENGTH;
import static io.github.azagniotov.language.TestDefaultConstants.MIN_NGRAM_LENGTH;
import static io.github.azagniotov.language.TestHelper.resetLanguageDetectorFactoryInstance;
import static io.github.azagniotov.language.TestHelper.testLanguage;
import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.Arrays;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Sanity checks for {@link LanguageDetector#detectAll(String)}. */
public class LanguageDetectorTest {

  private static final String TRAINING_EN = "a a a b b c c d e";
  private static final String TRAINING_FR = "a b b c c c d d d";
  private static final String TRAINING_JA = "\u3042 \u3042 \u3042 \u3044 \u3046 \u3048 \u3048";

  private static Model MODEL;
  private static LanguageDetector DEFAULT_DETECTOR;

  @BeforeClass
  public static void beforeClass() throws Exception {
    MODEL =
        Model.fromJsonOrEnv(
            LanguageDetectorTest.class.getResourceAsStream("/model/testParameters.json"));

    final LanguageDetectorFactory factory =
        new LanguageDetectorFactory(DEFAULT_SETTINGS_ALL_LANGUAGES);
    DEFAULT_DETECTOR =
        new LanguageDetector(
            MODEL,
            factory.getSupportedIsoCodes639_1(),
            factory.getLanguageCorporaProbabilities(),
            MIN_NGRAM_LENGTH,
            MAX_NGRAM_LENGTH);
  }

  private LanguageDetector languageDetector;

  @Before
  public void setUp() throws Exception {
    resetLanguageDetectorFactoryInstance();

    final LanguageDetectionSettings emptySettings =
        LanguageDetectionSettings.fromIsoCodes639_1(EMPTY_STRING).build();
    final LanguageDetectorFactory factory = new LanguageDetectorFactory(emptySettings);

    final String profileTemplate = "{\"freq\":{},\"n_words\":[0.0, 0.0, 0.0],\"name\":\"%s\"}";
    final InputStream enTest = gzipString(String.format(profileTemplate, "en_test"));
    final LanguageProfile enProfile = LanguageProfile.fromGzippedJson(enTest);
    for (final String w : TRAINING_EN.split(BLANK_SPACE)) {
      enProfile.add(w, MIN_NGRAM_LENGTH, MAX_NGRAM_LENGTH);
    }
    factory.addProfile(enProfile, 0, 3);

    final InputStream frTest = gzipString(String.format(profileTemplate, "fr_test"));
    final LanguageProfile frProfile = LanguageProfile.fromGzippedJson(frTest);
    for (final String w : TRAINING_FR.split(BLANK_SPACE)) {
      frProfile.add(w, MIN_NGRAM_LENGTH, MAX_NGRAM_LENGTH);
    }
    factory.addProfile(frProfile, 1, 3);

    final InputStream jaTest = gzipString(String.format(profileTemplate, "ja_test"));
    final LanguageProfile jaProfile = LanguageProfile.fromGzippedJson(jaTest);
    for (final String w : TRAINING_JA.split(BLANK_SPACE)) {
      jaProfile.add(w, MIN_NGRAM_LENGTH, MAX_NGRAM_LENGTH);
    }
    factory.addProfile(jaProfile, 2, 3);

    languageDetector =
        new LanguageDetector(
            MODEL,
            factory.getSupportedIsoCodes639_1(),
            factory.getLanguageCorporaProbabilities(),
            MIN_NGRAM_LENGTH,
            MAX_NGRAM_LENGTH);
  }

  @Test
  public void shouldExtractExpectedNGrams() throws Exception {
    assertEquals(
        DEFAULT_DETECTOR.extractNGrams("alex"),
        Arrays.asList("a", " a", "l", "al", " al", "e", "le", "ale", "x", "ex", "lex"));

    assertEquals(
        DEFAULT_DETECTOR.extractNGrams("A\u06cc\u1ea0\u3044\u30a4\u3106\uac01\u2010a"),
        Arrays.asList("A", " A", "ي", "ể", "あ", "ア", "あア", "ㄅ", "가", "가 ", "a", " a"));
  }

  @Test
  public void shouldDetectEnglishDataset() throws Exception {
    assertEquals(languageDetector.detectAll("a").get(0).getIsoCode639_1(), "en_test");
  }

  @Test
  public void shouldDetectFrenchDataset() throws Exception {
    assertEquals(languageDetector.detectAll("b d").get(0).getIsoCode639_1(), "fr_test");
  }

  @Test
  public void shouldDetectEnglishDataset_v2() throws Exception {
    assertEquals(languageDetector.detectAll("d e").get(0).getIsoCode639_1(), "en_test");
  }

  @Test
  public void shouldDetectJapaneseDataset() throws Exception {
    assertEquals(
        languageDetector.detectAll("\u3042\u3042\u3042\u3042a").get(0).getIsoCode639_1(),
        "ja_test");
  }

  @Test
  public void languageDetectorShortStrings() throws Exception {
    final LanguageDetectionSettings supportedLanguages =
        LanguageDetectionSettings.fromIsoCodes639_1(
                "az,am,bo,br,cy,de,eu,ga,gv,he,hy,ka,kk,ky,lb,mn,ru,sr,tg,ti,yi")
            .build();
    final LanguageDetectorFactory factory = new LanguageDetectorFactory(supportedLanguages);
    final LanguageDetector detector =
        new LanguageDetector(
            MODEL,
            factory.getSupportedIsoCodes639_1(),
            factory.getLanguageCorporaProbabilities(),
            MIN_NGRAM_LENGTH,
            MAX_NGRAM_LENGTH);

    // "I am learning <LANGUAGE_NAME>" in various languages

    // Armenian
    assertEquals("hy", detector.detectAll("Սովորում եմ հայերեն").get(0).getIsoCode639_1());
    // Amharic
    assertEquals("am", detector.detectAll("አማርኛ እየተማርኩ ነው።").get(0).getIsoCode639_1());
    // Azerbaijani
    assertEquals("az", detector.detectAll("Azərbaycan dilini öyrənirəm").get(0).getIsoCode639_1());
    // Basque
    assertEquals("eu", detector.detectAll("Euskara ikasten ari naiz").get(0).getIsoCode639_1());
    // Breton
    assertEquals("br", detector.detectAll("Emaon o teskiñ brezhoneg").get(0).getIsoCode639_1());
    // Georgian
    assertEquals("ka", detector.detectAll("ვსწავლობ ქართულს").get(0).getIsoCode639_1());
    // German
    assertEquals("de", detector.detectAll("Ich lerne Deutsch").get(0).getIsoCode639_1());
    // Irish
    assertEquals("ga", detector.detectAll("Tá mé ag foghlaim Gaeilge").get(0).getIsoCode639_1());
    // Hebrew
    assertEquals("he", detector.detectAll("אני לומד עברית").get(0).getIsoCode639_1());
    // Kazakh
    assertEquals(
        "kk", detector.detectAll("Мен қазақ тілін үйреніп жатырмын").get(0).getIsoCode639_1());
    // Kyrgyz
    assertEquals(
        "ky", detector.detectAll("Мен кыргыз тилин үйрөнүп жатам").get(0).getIsoCode639_1());
    // Luxembourgish
    assertEquals("lb", detector.detectAll("Ech léiere Lëtzebuergesch").get(0).getIsoCode639_1());
    // Manx
    assertEquals(
        "gv", detector.detectAll("Ta mee gynsagh yn Ghaelg Manninagh").get(0).getIsoCode639_1());
    // Mongolian
    assertEquals("mn", detector.detectAll("Би монгол хэл сурч байна").get(0).getIsoCode639_1());
    // Russian
    assertEquals("ru", detector.detectAll("Я учу русский язык").get(0).getIsoCode639_1());
    // Serbian
    assertEquals("sr", detector.detectAll("Учим српски језик").get(0).getIsoCode639_1());
    // Tajik
    assertEquals("tg", detector.detectAll("Ман забони тоҷикиро меомӯзам").get(0).getIsoCode639_1());
    // Tibetan
    assertEquals("bo", detector.detectAll("ངས་བོད་ཡིག་སྦྱོང་གི་ཡོད།").get(0).getIsoCode639_1());
    // Tigrinya
    assertEquals("ti", detector.detectAll("ትግርኛ ይመሃር ኣለኹ").get(0).getIsoCode639_1());
    // Welsh
    assertEquals("cy", detector.detectAll("Dw i'n dysgu Cymraeg").get(0).getIsoCode639_1());
    // Yiddish
    assertEquals("yi", detector.detectAll("איך לערן זיך ייִדיש").get(0).getIsoCode639_1());
  }

  @Test
  public void testAmharic() throws Exception {
    testLanguage("amharic.txt", "am", DEFAULT_DETECTOR);
  }

  @Test
  public void testArmenian() throws Exception {
    testLanguage("armenian.txt", "hy", DEFAULT_DETECTOR);
  }

  @Test
  public void testAzerbaijani() throws Exception {
    testLanguage("azerbaijani.txt", "az", DEFAULT_DETECTOR);
  }

  @Test
  public void testBasque() throws Exception {
    testLanguage("basque.txt", "eu", DEFAULT_DETECTOR);
  }

  @Test
  public void testBreton() throws Exception {
    testLanguage("breton.txt", "br", DEFAULT_DETECTOR);
  }

  @Test
  public void testChinese() throws Exception {
    testLanguage("chinese.txt", "zh-cn", DEFAULT_DETECTOR);
  }

  @Test
  public void testEnglish() throws Exception {
    testLanguage("english.txt", "en", DEFAULT_DETECTOR);
  }

  @Test
  public void testGeorgian() throws Exception {
    testLanguage("georgian.txt", "ka", DEFAULT_DETECTOR);
  }

  @Test
  public void testGerman() throws Exception {
    testLanguage("german.txt", "de", DEFAULT_DETECTOR);
  }

  @Test
  public void testIrish() throws Exception {
    testLanguage("irish.txt", "ga", DEFAULT_DETECTOR);
  }

  @Test
  public void testJapanese() throws Exception {
    testLanguage("japanese.txt", "ja", DEFAULT_DETECTOR);
  }

  @Test
  public void testKazakh() throws Exception {
    testLanguage("kazakh.txt", "kk", DEFAULT_DETECTOR);
  }

  @Test
  public void testKorean() throws Exception {
    testLanguage("korean.txt", "ko", DEFAULT_DETECTOR);
  }

  @Test
  public void testKyrgyz() throws Exception {
    testLanguage("kyrgyz.txt", "ky", DEFAULT_DETECTOR);
  }

  @Test
  public void testLuxembourgish() throws Exception {
    testLanguage("luxembourgish.txt", "lb", DEFAULT_DETECTOR);
  }

  @Test
  public void testManx() throws Exception {
    testLanguage("manx.txt", "gv", DEFAULT_DETECTOR);
  }

  @Test
  public void testMongolian() throws Exception {
    testLanguage("mongolian.txt", "mn", DEFAULT_DETECTOR);
  }

  @Test
  public void testRussian() throws Exception {
    testLanguage("russian.txt", "ru", DEFAULT_DETECTOR);
  }

  @Test
  public void testSerbian() throws Exception {
    testLanguage("serbian.txt", "sr", DEFAULT_DETECTOR);
  }

  @Test
  public void testTajik() throws Exception {
    testLanguage("tajik.txt", "tg", DEFAULT_DETECTOR);
  }

  @Test
  public void testTibetan() throws Exception {
    testLanguage("tibetan.txt", "bo", DEFAULT_DETECTOR);
  }

  @Test
  public void testTigrinya() throws Exception {
    testLanguage("tigrinya.txt", "ti", DEFAULT_DETECTOR);
  }

  @Test
  public void testWelsh() throws Exception {
    testLanguage("welsh.txt", "cy", DEFAULT_DETECTOR);
  }

  @Test
  public void testYiddish() throws Exception {
    testLanguage("yiddish.txt", "yi", DEFAULT_DETECTOR);
  }

  @Test
  public final void languageDetectorShouldDetectShortStringsWithDefaultSanitization()
      throws Exception {

    assertEquals("ja", DEFAULT_DETECTOR.detectAll("ｼｰｻｲﾄﾞ_ﾗｲﾅｰ.pdf").get(0).getIsoCode639_1());

    assertEquals("ja", DEFAULT_DETECTOR.detectAll("東京に行き ABCDEF").get(0).getIsoCode639_1());

    assertEquals("ja", DEFAULT_DETECTOR.detectAll("\"ヨキ\" AND \"杉山\"").get(0).getIsoCode639_1());
    assertEquals(
        "ja", DEFAULT_DETECTOR.detectAll("\"ヨキ\" AND \"杉山\" OR \"分布\"").get(0).getIsoCode639_1());
    assertEquals(
        "ja", DEFAULT_DETECTOR.detectAll("#4_pj_23D002_HCMJ_デジ戦").get(0).getIsoCode639_1());
    assertEquals(
        "ja", DEFAULT_DETECTOR.detectAll("in1729x01_J-LCM刷新プロジェクト.docx").get(0).getIsoCode639_1());
    assertEquals("ja", DEFAULT_DETECTOR.detectAll("Ｃｕｌｔｕｒｅ　ｏｆ　Ｊａｐａｎ").get(0).getIsoCode639_1());
    assertEquals(
        "ja", DEFAULT_DETECTOR.detectAll("ﾊｰﾄﾞｶﾌﾟｾﾙ(ｻｲｽﾞ3号 NATURAL B／C)").get(0).getIsoCode639_1());
    assertEquals("ja", DEFAULT_DETECTOR.detectAll("Microsoft　インポート").get(0).getIsoCode639_1());

    assertEquals("ja", DEFAULT_DETECTOR.detectAll("Ｃｕｌｔｕｒｅ　ｏｆ　Ｊａｐａｎ.pdf").get(0).getIsoCode639_1());

    assertEquals(
        "ja",
        DEFAULT_DETECTOR.detectAll("ﾊｰﾄﾞｶﾌﾟｾﾙ(ｻｲｽﾞ3号 NATURAL B／C).pdf").get(0).getIsoCode639_1());
    assertEquals("ca", DEFAULT_DETECTOR.detectAll("刷新eclipseRCPExt.pdf").get(0).getIsoCode639_1());
    assertEquals("ca", DEFAULT_DETECTOR.detectAll("report.xls").get(0).getIsoCode639_1());

    assertEquals(
        "en", DEFAULT_DETECTOR.detectAll("This is a very small test").get(0).getIsoCode639_1());

    assertEquals(
        "de", DEFAULT_DETECTOR.detectAll("Das kann deutsch sein").get(0).getIsoCode639_1());
    assertEquals("de", DEFAULT_DETECTOR.detectAll("Das ist ein Text").get(0).getIsoCode639_1());

    assertEquals("zh-tw", DEFAULT_DETECTOR.detectAll("TOEIC 分布").get(0).getIsoCode639_1());
    assertEquals("ja", DEFAULT_DETECTOR.detectAll("１２３４５６７８９０.xls 報告").get(0).getIsoCode639_1());
    assertEquals("zh-tw", DEFAULT_DETECTOR.detectAll("富山県高岡市.txt").get(0).getIsoCode639_1());
    assertEquals(
        "zh-tw",
        DEFAULT_DETECTOR.detectAll("【12.21s】【流山運動公園：流山市市野谷】【中村P：3台】").get(0).getIsoCode639_1());
    assertEquals("zh-tw", DEFAULT_DETECTOR.detectAll("東京 ABCDEF").get(0).getIsoCode639_1());
    assertEquals("zh-tw", DEFAULT_DETECTOR.detectAll("東 ABCDE").get(0).getIsoCode639_1());
    assertEquals(
        "zh-tw", DEFAULT_DETECTOR.detectAll("AND \"杉山\" OR \"分布\"").get(0).getIsoCode639_1());

    assertEquals("zh-cn", DEFAULT_DETECTOR.detectAll("㈱_(株)_①②③_㈱㈲㈹").get(0).getIsoCode639_1());
    assertEquals(
        "zh-cn", DEFAULT_DETECTOR.detectAll("帮助他们以截然不同的方式探索和分析数据.pdf").get(0).getIsoCode639_1());
  }

  @Test
  public final void languageDetectorRespondsWithUndeterminedLanguage() throws Exception {
    final LanguageDetectionSettings supportedLanguages =
        LanguageDetectionSettings.fromIsoCodes639_1("en,de").build();
    final LanguageDetectorFactory factory = new LanguageDetectorFactory(supportedLanguages);
    final LanguageDetector detector =
        new LanguageDetector(
            MODEL,
            factory.getSupportedIsoCodes639_1(),
            factory.getLanguageCorporaProbabilities(),
            MIN_NGRAM_LENGTH,
            MAX_NGRAM_LENGTH);

    assertEquals("und", detector.detectAll("ｼｰｻｲﾄﾞ_ﾗｲﾅｰ").get(0).getIsoCode639_1());
    assertEquals("und", detector.detectAll("Ｃｕｌｔｕｒｅ　ｏｆ　Ｊａｐａｎ").get(0).getIsoCode639_1());
    assertEquals("und", detector.detectAll("㈱_(株)_①②③_㈱㈲㈹").get(0).getIsoCode639_1());
    assertEquals("und", detector.detectAll("...").get(0).getIsoCode639_1());
    assertEquals("und", detector.detectAll("1234567").get(0).getIsoCode639_1());
    assertEquals("und", detector.detectAll("한국어").get(0).getIsoCode639_1());
  }

  @Test
  public final void singleWords() throws Exception {
    final LanguageDetectionSettings supportedLanguages =
        LanguageDetectionSettings.fromIsoCodes639_1("en,de,fr,es,it").build();
    final LanguageDetectorFactory factory = new LanguageDetectorFactory(supportedLanguages);
    final LanguageDetector detector =
        new LanguageDetector(
            MODEL,
            factory.getSupportedIsoCodes639_1(),
            factory.getLanguageCorporaProbabilities(),
            MIN_NGRAM_LENGTH,
            MAX_NGRAM_LENGTH);

    assertEquals("fr", detector.detectAll("apple").get(0).getIsoCode639_1());
    assertEquals("es", detector.detectAll("report").get(0).getIsoCode639_1());
  }
}
