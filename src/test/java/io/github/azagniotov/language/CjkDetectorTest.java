package io.github.azagniotov.language;

import static io.github.azagniotov.language.CjkDecision.DECISION_CHINESE;
import static io.github.azagniotov.language.CjkDecision.DECISION_JAPANESE;
import static io.github.azagniotov.language.StringConstants.BLANK_SPACE;
import static io.github.azagniotov.language.StringConstants.EMPTY_STRING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class CjkDetectorTest {

  private static final double DEFAULT_THRESHOLD = 0.5;

  @Test
  public void shouldDetectJapaneseInput() throws Exception {
    assertEquals(CjkDetector.decide(s("カタカナ"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("ひらがな"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("関西国際空港"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("愛雲髮見"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertEquals(
        CjkDetector.decide(s("      東      A     京   "), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("東A京"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("東京 AB"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("東京に行きま ABCD"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertEquals(
        CjkDetector.decide(s("東 A 京 B に C 行 D き E ま F し G た "), DEFAULT_THRESHOLD),
        DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("東京に行き ABCDEF"), 0.2), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("東 ABCDEF"), 0.1), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("東京に行き"), 1.0), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("東京に行き"), 1.1), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("報告 123.xls"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertEquals(
        CjkDetector.decide(s("七月の報告 1234567890.pptx"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertEquals(
        CjkDetector.decide(s("七月の報告 １２３４５６７８９０.pptx"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("２０２４七月　報告.pptx"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
  }

  @Test
  public void shouldNotDetectInputAsJapanese() throws Exception {
    assertNotEquals(CjkDetector.decide(s(null), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertNotEquals(CjkDetector.decide(s(EMPTY_STRING), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertNotEquals(CjkDetector.decide(s(BLANK_SPACE), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertNotEquals(CjkDetector.decide(s("               "), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertNotEquals(CjkDetector.decide(s("     7         "), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertNotEquals(CjkDetector.decide(s("東京に行き"), 0), DECISION_JAPANESE);
    assertNotEquals(CjkDetector.decide(s("東京に行き"), -0.1), DECISION_JAPANESE);
    assertNotEquals(CjkDetector.decide(s("report.xls"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertNotEquals(CjkDetector.decide(s("123.xls"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
    assertNotEquals(CjkDetector.decide(s("12345"), DEFAULT_THRESHOLD), DECISION_JAPANESE);

    // Not in Japanese Han. Simplified Chinese random characters
    assertNotEquals(CjkDetector.decide(s("爱云发见"), DEFAULT_THRESHOLD), DECISION_JAPANESE);

    // Only three(3) out of all Kanji are in Japanese Han
    assertNotEquals(CjkDetector.decide(s("愛雲髮見 爱云发见"), DEFAULT_THRESHOLD), DECISION_JAPANESE);

    // Not in Japanese Han. Traditional Chinese: noodles
    assertNotEquals(CjkDetector.decide(s("麵"), DEFAULT_THRESHOLD), DECISION_JAPANESE);

    // Not in Japanese Han. Simplified Chinese: depressed
    assertNotEquals(CjkDetector.decide(s("郁"), DEFAULT_THRESHOLD), DECISION_JAPANESE);

    // Not in Japanese Han. Traditional Chinese: dirty
    assertNotEquals(CjkDetector.decide(s("臟髒"), DEFAULT_THRESHOLD), DECISION_JAPANESE);

    // Not in Japanese Han. Simplified Chinese: dirty
    assertNotEquals(CjkDetector.decide(s("脏"), DEFAULT_THRESHOLD), DECISION_JAPANESE);

    // Not in Japanese Han. Traditional Chinese: mongolian
    assertNotEquals(CjkDetector.decide(s("懞濛矇"), DEFAULT_THRESHOLD), DECISION_JAPANESE);

    // Not in Japanese Han. Simplified Chinese: mongolian
    assertNotEquals(CjkDetector.decide(s("蒙"), DEFAULT_THRESHOLD), DECISION_JAPANESE);
  }

  @Test
  public void shouldDetectChineseInput() throws Exception {
    // Not in Japanese Han. Simplified Chinese random characters
    assertEquals(CjkDetector.decide(s("爱云发见"), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertEquals(CjkDetector.decide(s("爱云发见 ABCD"), DEFAULT_THRESHOLD), DECISION_CHINESE);

    // Only three(3) out of all Kanji are in Japanese Han
    assertEquals(CjkDetector.decide(s("愛雲髮見 爱云发见"), DEFAULT_THRESHOLD), DECISION_CHINESE);

    assertEquals(
        CjkDetector.decide(s("麵最终报告 1234567890.pdf"), DEFAULT_THRESHOLD), DECISION_CHINESE);

    // Not in Japanese Han. Traditional Chinese: noodles
    assertEquals(CjkDetector.decide(s("麵"), DEFAULT_THRESHOLD), DECISION_CHINESE);

    // Not in Japanese Han. Simplified Chinese: depressed
    assertEquals(CjkDetector.decide(s("郁"), DEFAULT_THRESHOLD), DECISION_CHINESE);

    // Not in Japanese Han. Traditional Chinese: dirty
    assertEquals(CjkDetector.decide(s("臟髒"), DEFAULT_THRESHOLD), DECISION_CHINESE);

    // Not in Japanese Han. Simplified Chinese: dirty
    assertEquals(CjkDetector.decide(s("脏"), DEFAULT_THRESHOLD), DECISION_CHINESE);

    // Not in Japanese Han. Traditional Chinese: mongolian
    assertEquals(CjkDetector.decide(s("懞濛矇"), DEFAULT_THRESHOLD), DECISION_CHINESE);

    // Not in Japanese Han. Simplified Chinese: mongolian
    assertEquals(CjkDetector.decide(s("蒙"), DEFAULT_THRESHOLD), DECISION_CHINESE);
  }

  @Test
  public void shouldNotDetectInputAsChinese() throws Exception {
    assertNotEquals(CjkDetector.decide(s(null), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(s(EMPTY_STRING), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(s(BLANK_SPACE), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(s("爱云发见"), 0), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(s("爱云发见"), -0.1), DECISION_CHINESE);

    assertNotEquals(CjkDetector.decide(s("カタカナ"), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(s("ひらがな"), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(s("関西国際空港"), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(s("愛雲髮見"), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(
        CjkDetector.decide(s("      東      A     京   "), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(s("東A京"), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(s("東京 AB"), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(s("東京に行きま ABCD"), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(
        CjkDetector.decide(s("東 A 京 B に C 行 D き E ま F し G た "), DEFAULT_THRESHOLD),
        DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(s("東京に行き ABCDEF"), 0.2), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(s("東 ABCDEF"), 0.1), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(s("東京に行き"), 1.0), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(s("東京に行き"), 1.1), DECISION_CHINESE);

    assertNotEquals(CjkDetector.decide(s("report.xls"), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(s("123.xls"), DEFAULT_THRESHOLD), DECISION_CHINESE);
    assertNotEquals(CjkDetector.decide(s("12345"), DEFAULT_THRESHOLD), DECISION_CHINESE);
  }

  @Test
  public void shoulddetectJapaneseInput_RealQueries_Threshold_0_5() throws Exception {
    final double threshold = 0.5;
    assertEquals(CjkDetector.decide(s("QRコード"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("in1729x01_J-LCM刷新プロジェクト"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("\"ヨキ\" AND \"杉山\""), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("RG-12　仕様書"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("発注-営推契-オ-第24-01905号"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("西条寺家Ⅲ１３号地ＳＲ８６０００３２"), threshold), DECISION_JAPANESE);
    assertEquals(
        CjkDetector.decide(s("【12.21s】【流山運動公園：流山市市野谷】【中村P：3台】"), threshold), DECISION_JAPANESE);
    assertEquals(
        CjkDetector.decide(s("ﾊｰﾄﾞｶﾌﾟｾﾙ(ｻｲｽﾞ3号 NATURAL B／C)"), threshold), DECISION_JAPANESE);
  }

  @Test
  public void shoulddetectJapaneseInput_RealQueries_Threshold_0_4() throws Exception {
    final double threshold = 0.4;
    assertEquals(CjkDetector.decide(s("CAD　キー"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("自動dys"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("SKS活動"), threshold), DECISION_JAPANESE);
  }

  @Test
  public void shoulddetectJapaneseInput_RealQueries_Threshold_0_3() throws Exception {
    final double threshold = 0.3;
    assertEquals(CjkDetector.decide(s("CHAINON上熊本"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("T-LINK判定書"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("RV  20UB　膜厚"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("【MANGO】_当月_9161_[CI][I]"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("#4_pj_23D002_HCMJ_デジ戦"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("Microsoft　インポート"), threshold), DECISION_JAPANESE);
  }

  @Test
  public void shoulddetectJapaneseInput_RealQueries_Threshold_0_2() throws Exception {
    final double threshold = 0.2;
    assertEquals(CjkDetector.decide(s("\"BH34\"AND\"PCT\"紹介"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("TOEIC 分布"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("BP3棟3階 FCU-302"), threshold), DECISION_JAPANESE);
    assertEquals(
        CjkDetector.decide(s("(Ts-Hf3 OR Hf-PAH) AND (気化器"), threshold), DECISION_JAPANESE);
    assertEquals(CjkDetector.decide(s("ミニWebinar 最新FE-SEM"), threshold), DECISION_JAPANESE);
  }

  private String s(final String input) {
    if (input == null || input.trim().isEmpty()) {
      return EMPTY_STRING;
    } else {
      return InputSanitizer.sanitize(input);
    }
  }
}
