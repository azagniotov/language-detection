package io.github.azagniotov.language;

import static io.github.azagniotov.language.CjkDecision.DECISION_CHINESE;
import static io.github.azagniotov.language.CjkDecision.DECISION_JAPANESE;
import static io.github.azagniotov.language.CjkDecision.DECISION_NONE;
import static io.github.azagniotov.language.StringConstants.BLANK_SPACE;
import static io.github.azagniotov.language.StringConstants.EMPTY_STRING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class CjkDetectorTest {

  private static final double DEFAULT_THRESHOLD = 0.5;

  @Test
  public void shouldDetectJapaneseInput() throws Exception {
    assertEquals(CjkDetector.decide(sz("カタカナ"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("ひらがな"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("関西国際空港"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("愛雲髮見"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertEquals(
        CjkDetector.decide(sz("      東      A     京   "), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("東A京"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("東京 AB"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("東京に行きま ABCD"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertEquals(
        CjkDetector.decide(sz("東 A 京 B に C 行 D き E ま F し G た "), DEFAULT_THRESHOLD),
        DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("東京に行き ABCDEF"), 0.2), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("東 ABCDEF"), 0.1), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("東京に行き"), 1.0), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("東京に行き"), 1.1), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("報告 123.xls"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertEquals(
        CjkDetector.decide(sz("七月の報告 1234567890.pptx"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertEquals(
        CjkDetector.decide(sz("七月の報告 １２３４５６７８９０.pptx"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("２０２４七月　報告.pptx"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
  }

  @Test
  public void shouldNotDetectInputAsJapanese() throws Exception {
    assertNotEquals(CjkDetector.decide(sz(null), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertNotEquals(CjkDetector.decide(sz(EMPTY_STRING), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertNotEquals(CjkDetector.decide(sz(BLANK_SPACE), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertNotEquals(
        CjkDetector.decide(sz("               "), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertNotEquals(
        CjkDetector.decide(sz("     7         "), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertNotEquals(CjkDetector.decide(sz("東京に行き"), 0), DECISION_JAPANESE);
    assertNotEquals(CjkDetector.decide(sz("東京に行き"), -0.1), DECISION_JAPANESE);
    assertNotEquals(CjkDetector.decide(sz("report.xls"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertNotEquals(CjkDetector.decide(sz("123.xls"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertNotEquals(CjkDetector.decide(sz("12345"), DEFAULT_THRESHOLD), DECISION_JAPANESE);

    // Not in Japanese Han. Simplified Chinese random characters
    assertNotEquals(CjkDetector.decide(sz("爱云发见"), DEFAULT_THRESHOLD), DECISION_JAPANESE);

    // Only three(3) out of all Kanji are in Japanese Han
    assertNotEquals(CjkDetector.decide(sz("愛雲髮見 爱云发见"), DEFAULT_THRESHOLD), DECISION_JAPANESE);

    // Not in Japanese Han. Traditional Chinese: noodles
    assertNotEquals(CjkDetector.decide(sz("麵"), DEFAULT_THRESHOLD), DECISION_JAPANESE);

    // Not in Japanese Han. Simplified Chinese: depressed
    assertNotEquals(CjkDetector.decide(sz("郁"), DEFAULT_THRESHOLD), DECISION_JAPANESE);

    // Not in Japanese Han. Traditional Chinese: dirty
    assertNotEquals(CjkDetector.decide(sz("臟髒"), DEFAULT_THRESHOLD), DECISION_JAPANESE);

    // Not in Japanese Han. Simplified Chinese: dirty
    assertNotEquals(CjkDetector.decide(sz("脏"), DEFAULT_THRESHOLD), DECISION_JAPANESE);

    // Not in Japanese Han. Traditional Chinese: mongolian
    assertNotEquals(CjkDetector.decide(sz("懞濛矇"), DEFAULT_THRESHOLD), DECISION_JAPANESE);

    // Not in Japanese Han. Simplified Chinese: mongolian
    assertNotEquals(CjkDetector.decide(sz("蒙"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
  }

  @Test
  public void shouldDetectChineseInput() throws Exception {
    // Not in Japanese Han. Simplified Chinese random characters
    assertEquals(CjkDetector.decide(sz("爱云发见"), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertEquals(CjkDetector.decide(sz("爱云发见 ABCD"), DEFAULT_THRESHOLD), DECISION_CHINESE);

    // Only three(3) out of all Kanji are in Japanese Han
    assertEquals(CjkDetector.decide(sz("愛雲髮見 爱云发见"), DEFAULT_THRESHOLD), DECISION_CHINESE);

    assertEquals(
        CjkDetector.decide(sz("麵最终报告 1234567890.pdf"), DEFAULT_THRESHOLD), DECISION_CHINESE);

    // Not in Japanese Han. Traditional Chinese: noodles
    assertEquals(CjkDetector.decide(sz("麵"), DEFAULT_THRESHOLD), DECISION_CHINESE);

    // Not in Japanese Han. Simplified Chinese: depressed
    assertEquals(CjkDetector.decide(sz("郁"), DEFAULT_THRESHOLD), DECISION_CHINESE);

    // Not in Japanese Han. Traditional Chinese: dirty
    assertEquals(CjkDetector.decide(sz("臟髒"), DEFAULT_THRESHOLD), DECISION_CHINESE);

    // Not in Japanese Han. Simplified Chinese: dirty
    assertEquals(CjkDetector.decide(sz("脏"), DEFAULT_THRESHOLD), DECISION_CHINESE);

    // Not in Japanese Han. Traditional Chinese: mongolian
    assertEquals(CjkDetector.decide(sz("懞濛矇"), DEFAULT_THRESHOLD), DECISION_CHINESE);

    // Not in Japanese Han. Simplified Chinese: mongolian
    assertEquals(CjkDetector.decide(sz("蒙"), DEFAULT_THRESHOLD), DECISION_CHINESE);
  }

  @Test
  public void shouldNotDetectInputAsChinese() throws Exception {
    assertNotEquals(CjkDetector.decide(sz(null), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(sz(EMPTY_STRING), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(sz(BLANK_SPACE), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(sz("爱云发见"), 0), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(sz("爱云发见"), -0.1), DECISION_CHINESE);

    assertNotEquals(CjkDetector.decide(sz("カタカナ"), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(sz("ひらがな"), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(sz("関西国際空港"), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(sz("愛雲髮見"), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(
        CjkDetector.decide(sz("      東      A     京   "), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(sz("東A京"), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(sz("東京 AB"), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(sz("東京に行きま ABCD"), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(
        CjkDetector.decide(sz("東 A 京 B に C 行 D き E ま F し G た "), DEFAULT_THRESHOLD),
        DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(sz("東京に行き ABCDEF"), 0.2), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(sz("東 ABCDEF"), 0.1), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(sz("東京に行き"), 1.0), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(sz("東京に行き"), 1.1), DECISION_CHINESE);

    assertNotEquals(CjkDetector.decide(sz("report.xls"), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(sz("123.xls"), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(sz("12345"), DEFAULT_THRESHOLD), DECISION_CHINESE);
  }

  @Test
  public void shouldDetectJapaneseInput_Threshold_0_5() throws Exception {
    final double threshold = 0.5;
    assertEquals(CjkDetector.decide(sz("QRコード"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("in1729x01_J-LCM刷新プロジェクト"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("\"ヨキ\" AND \"杉山\""), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("RG-12　仕様書"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("発注-営推契-オ-第24-01905号"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("西条寺家Ⅲ１３号地ＳＲ８６０００３２"), threshold), DECISION_JAPANESE);
    assertEquals(
        CjkDetector.decide(sz("【12.21s】【流山運動公園：流山市市野谷】【中村P：3台】"), threshold), DECISION_JAPANESE);
    assertEquals(
        CjkDetector.decide(sz("ﾊｰﾄﾞｶﾌﾟｾﾙ(ｻｲｽﾞ3号 NATURAL B／C)"), threshold), DECISION_JAPANESE);
  }

  @Test
  public void shouldDetectJapaneseInput_Threshold_0_4() throws Exception {
    final double threshold = 0.4;
    assertEquals(CjkDetector.decide(sz("CAD　キー"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("自動dys"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("SKS活動"), threshold), DECISION_JAPANESE);
  }

  @Test
  public void shouldDetectJapaneseInput_Threshold_0_3() throws Exception {
    final double threshold = 0.3;
    assertEquals(CjkDetector.decide(sz("CHAINON上熊本"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("T-LINK判定書"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("RV  20UB　膜厚"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("【MANGO】_当月_9161_[CI][I]"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("#4_pj_23D002_HCMJ_デジ戦"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("Microsoft　インポート"), threshold), DECISION_JAPANESE);
  }

  @Test
  public void shouldDetectJapaneseInput_Threshold_0_2() throws Exception {
    final double threshold = 0.2;
    assertEquals(CjkDetector.decide(sz("\"BH34\"AND\"PCT\"紹介"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("TOEIC 分布"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("BP3棟3階 FCU-302"), threshold), DECISION_JAPANESE);
    assertEquals(
        CjkDetector.decide(sz("(Ts-Hf3 OR Hf-PAH) AND (気化器"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("ミニWebinar 最新FE-SEM"), threshold), DECISION_JAPANESE);
  }

  @Test
  public void shouldDetectJapaneseInput_Threshold_0_1() throws Exception {
    final double threshold = 0.1;
    assertEquals(
        CjkDetector.decide(sz("TAB-Therapeutic Procedure、All Acronyms、2017年3月27日閲覧"), threshold),
        DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(sz("㈱_(株)_①②③_㈱㈲㈹"), threshold), DECISION_JAPANESE);
  }

  @Test
  public void shouldNotDetectInputAsJapanese_Threshold_0_1() throws Exception {
    final double threshold = 0.1;
    assertEquals(
        CjkDetector.decide(
            sz(
                "dominar el verbo (TENER CONTROL). B2 [I o T] para tener ÃƒÂ ¢ Ã‚â‚¬Ã‚â € ¹ control sobre un ÃƒÂ ¢ Ã‚â‚¬Ã‚â € ¹place o ÃƒÂ ¢ Ã‚â‚¬Ã‚â € ¹: He ÃƒÂ ¢ Ã‚â‚¬Ã‚â € ¹ se niega a ÃƒÂ ¢ Ã‚â‚¬Ã‚â € ¹ dejar que ÃƒÂ ¢ Ã‚â‚¬Ã‚â € ¹ otros ÃƒÂ ¢ Ã‚â‚¬Ã‚â € ¹habla y domina cada reunión de ÃƒÂ ¢ Ã‚â‚¬Ã‚â €. Ellos ÃƒÂ ¢ Ã‚â‚¬Ã‚â € ¹work como ÃƒÂ ¢ Ã‚â‚¬Ã‚â € ¹group-nadie ÃƒÂ ¢ Ã‚â‚¬Ã‚â € ¹person ÃƒÂ ¢ Ã‚â‚¬ Ã‚â € ¹permitido dominar."),
            threshold),
        DECISION_NONE);
  }

  private String sz(final String input) {
    if (input == null || input.trim().isEmpty()) {
      return EMPTY_STRING;
    } else {
      return InputSanitizer.sanitize(input);
    }
  }
}
