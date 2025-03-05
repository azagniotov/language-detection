package io.github.azagniotov.language;

import static io.github.azagniotov.language.StringConstants.BLANK_CHAR;
import static io.github.azagniotov.language.StringConstants.EMPTY_STRING;
import static io.github.azagniotov.language.TestDefaultConstants.MAX_NGRAM_LENGTH;
import static io.github.azagniotov.language.TestDefaultConstants.MIN_NGRAM_LENGTH;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Test;

/** */
public class NGramTest {

  @BeforeClass
  public static void setUp() throws IOException {
    // Warm-up for the static initializers
    NGram.normalize('\u0000');
  }

  @Test
  public final void testDefaultValue() {
    final NGram ngram = new NGram("input", 5, 7);
    assertEquals(ngram.getMinNGramLength(), 5);
    assertEquals(ngram.getMaxNGramLength(), 7);
  }

  @Test
  public final void testNormalizeWithLatin() {
    assertEquals(NGram.normalize('\u0000'), BLANK_CHAR);
    assertEquals(NGram.normalize('\u0009'), BLANK_CHAR);
    assertEquals(NGram.normalize(BLANK_CHAR), BLANK_CHAR);
    assertEquals(NGram.normalize('\u0030'), BLANK_CHAR);
    assertEquals(NGram.normalize('\u0040'), BLANK_CHAR);
    assertEquals(NGram.normalize('\u0041'), '\u0041');
    assertEquals(NGram.normalize('\u005a'), '\u005a');
    assertEquals(NGram.normalize('\u005b'), BLANK_CHAR);
    assertEquals(NGram.normalize('\u0060'), BLANK_CHAR);
    assertEquals(NGram.normalize('\u0061'), '\u0061');
    assertEquals(NGram.normalize('\u007a'), '\u007a');
    assertEquals(NGram.normalize('\u007b'), BLANK_CHAR);
    assertEquals(NGram.normalize('\u007f'), BLANK_CHAR);
    assertEquals(NGram.normalize('\u0080'), '\u0080');
    assertEquals(NGram.normalize('\u00a0'), BLANK_CHAR);
    assertEquals(NGram.normalize('\u00a1'), '\u00a1');
  }

  @Test
  public final void testNormalizePunctuation() {
    assertEquals(NGram.normalize('.'), BLANK_CHAR); // Full stop
    assertEquals(NGram.normalize(','), BLANK_CHAR); // Comma
    assertEquals(NGram.normalize('!'), BLANK_CHAR); // Exclamation mark
    assertEquals(NGram.normalize('?'), BLANK_CHAR); // Question mark
    assertEquals(NGram.normalize(';'), BLANK_CHAR); // Semicolon
    assertEquals(NGram.normalize(':'), BLANK_CHAR); // Colon
    assertEquals(NGram.normalize('-'), BLANK_CHAR); // Hyphen
    assertEquals(NGram.normalize('—'), BLANK_CHAR); // Em Dash
    assertEquals(NGram.normalize('–'), BLANK_CHAR); // En Dash
    assertEquals(NGram.normalize('('), BLANK_CHAR); // Left parenthesis
    assertEquals(NGram.normalize(')'), BLANK_CHAR); // Right parenthesis
    assertEquals(NGram.normalize('['), BLANK_CHAR); // Left square bracket
    assertEquals(NGram.normalize(']'), BLANK_CHAR); // Right square bracket
    assertEquals(NGram.normalize('{'), BLANK_CHAR); // Left curly bracket
    assertEquals(NGram.normalize('}'), BLANK_CHAR); // Right curly bracket
    assertEquals(NGram.normalize('"'), BLANK_CHAR); // Double quotation mark
    assertEquals(NGram.normalize('\''), BLANK_CHAR); // Single quotation mark
    assertEquals(NGram.normalize('%'), BLANK_CHAR); // Percent sign
    assertEquals(NGram.normalize('$'), BLANK_CHAR); // Dollar sign
    assertEquals(NGram.normalize('&'), BLANK_CHAR); // Ampersand
    assertEquals(NGram.normalize('@'), BLANK_CHAR); // At symbol
    assertEquals(NGram.normalize('#'), BLANK_CHAR); // Hash/Pound sign

    assertEquals(NGram.normalize('、'), BLANK_CHAR); // Ideographic comma (U+3001)
    assertEquals(NGram.normalize('。'), BLANK_CHAR); // Ideographic full stop (U+3002)
    assertEquals(NGram.normalize('「'), BLANK_CHAR); // Left corner bracket (U+300C)
    assertEquals(NGram.normalize('」'), BLANK_CHAR); // Right corner bracket (U+300D)
    assertEquals(NGram.normalize('『'), BLANK_CHAR); // Left double corner bracket (U+3010)
    assertEquals(NGram.normalize('』'), BLANK_CHAR); // Right double corner bracket (U+3011)
    assertEquals(NGram.normalize('（'), BLANK_CHAR); // Left parenthesis (U+FF08)
    assertEquals(NGram.normalize('）'), BLANK_CHAR); // Right parenthesis (U+FF09)
    assertEquals(NGram.normalize('【'), BLANK_CHAR); // Left black lenticular bracket (U+3010)
    assertEquals(NGram.normalize('】'), BLANK_CHAR); // Right black lenticular bracket (U+3011)
    assertEquals(NGram.normalize('、'), BLANK_CHAR); // Ideographic comma (U+3001)
    assertEquals(NGram.normalize('〜'), BLANK_CHAR); // Wave dash (U+301C)
    assertEquals(NGram.normalize('〓'), BLANK_CHAR); // Geta mark (U+3013)
    assertEquals(NGram.normalize('〆'), BLANK_CHAR); // Japanese iteration mark (U+3016)
    assertEquals(NGram.normalize('〤'), BLANK_CHAR); // Kanji iteration mark (U+301F)
    assertEquals(NGram.normalize('＂'), BLANK_CHAR); // Full-width double quotation mark (U+FF02)
    assertEquals(NGram.normalize('＇'), BLANK_CHAR); // Full-width single quotation mark (U+FF07)
    assertEquals(NGram.normalize('￥'), BLANK_CHAR); // Yen sign (U+00A5)
    assertEquals(NGram.normalize('※'), BLANK_CHAR); // Reference mark (U+203B)
    assertEquals(NGram.normalize('％'), BLANK_CHAR); // Full-width percent sign (U+FF05)
    assertEquals(NGram.normalize('＠'), BLANK_CHAR); // Full-width at symbol (U+FF20)

    // SUPPLEMENTAL_PUNCTUATION UnicodeBlock
    assertEquals(NGram.normalize('⸀'), BLANK_CHAR); // (U+2E00)
    assertEquals(NGram.normalize('⸁'), BLANK_CHAR); // (U+2E01)
  }

  @Test
  public final void testNormalizeHalfWidthKatakana() {
    assertEquals(NGram.normalize('ﾅ'), 'ア');
    assertEquals(NGram.normalize('ｼ'), 'ア');
  }

  @Test
  public final void testNormalizeFullWidthLatinJapanese() {
    assertEquals(NGram.normalize('Ｊ'), 'ア');
    assertEquals(NGram.normalize('Ｃ'), 'ア');
  }

  /** Test method for {@link NGram#normalize(char)} with CJK Kanji characters. */
  @Test
  public final void testNormalizeWithCJKKanji() {
    assertEquals(NGram.normalize('\u4E00'), '\u4E00');
    assertEquals(NGram.normalize('\u4E01'), '\u4E01');
    assertEquals(NGram.normalize('\u4E02'), '\u4E02');
    assertEquals(NGram.normalize('\u4E03'), '\u4E01');
    assertEquals(NGram.normalize('\u4E04'), '\u4E04');
    assertEquals(NGram.normalize('\u4E05'), '\u4E05');
    assertEquals(NGram.normalize('\u4E06'), '\u4E06');
    assertEquals(NGram.normalize('\u4E07'), '\u4E07');
    assertEquals(NGram.normalize('\u4E08'), '\u4E08');
    assertEquals(NGram.normalize('\u4E09'), '\u4E09');
    assertEquals(NGram.normalize('\u4E10'), '\u4E10');
    assertEquals(NGram.normalize('\u4E11'), '\u4E11');
    assertEquals(NGram.normalize('\u4E12'), '\u4E12');
    assertEquals(NGram.normalize('\u4E13'), '\u4E13');
    assertEquals(NGram.normalize('\u4E14'), '\u4E14');
    assertEquals(NGram.normalize('\u4E15'), '\u4E15');
    assertEquals(NGram.normalize('\u4E1e'), '\u4E1e');
    assertEquals(NGram.normalize('\u4E1f'), '\u4E1f');
    assertEquals(NGram.normalize('\u4E20'), '\u4E20');
    assertEquals(NGram.normalize('\u4E21'), '\u4E21');
    assertEquals(NGram.normalize('\u4E22'), '\u4E22');
    assertEquals(NGram.normalize('\u4E23'), '\u4E23');
    assertEquals(NGram.normalize('\u4E24'), '\u4E13');
    assertEquals(NGram.normalize('\u4E25'), '\u4E13');
    assertEquals(NGram.normalize('\u4E30'), '\u4E30');
  }

  /** Test method for {@link NGram#get(int)} and {@link NGram#addChar(char)}. */
  @Test
  public final void testNGram() {
    final NGram ngram = new NGram("input", MIN_NGRAM_LENGTH, MAX_NGRAM_LENGTH);
    assertEquals(ngram.get(0), EMPTY_STRING);
    assertEquals(ngram.get(1), EMPTY_STRING);
    assertEquals(ngram.get(2), EMPTY_STRING);
    assertEquals(ngram.get(3), EMPTY_STRING);
    assertEquals(ngram.get(4), EMPTY_STRING);

    ngram.addChar(BLANK_CHAR);
    assertEquals(ngram.get(1), EMPTY_STRING);
    assertEquals(ngram.get(2), EMPTY_STRING);
    assertEquals(ngram.get(3), EMPTY_STRING);

    ngram.addChar('A');
    assertEquals(ngram.get(1), "A");
    assertEquals(ngram.get(2), " A");
    assertEquals(ngram.get(3), EMPTY_STRING);

    ngram.addChar('\u06cc');
    assertEquals(ngram.get(1), "\u064a");
    assertEquals(ngram.get(2), "A\u064a");
    assertEquals(ngram.get(3), " A\u064a");

    ngram.addChar('\u1ea0');
    assertEquals(ngram.get(1), "\u1ec3");
    assertEquals(ngram.get(2), "\u064a\u1ec3");
    assertEquals(ngram.get(3), "A\u064a\u1ec3");

    ngram.addChar('\u3044');
    assertEquals(ngram.get(1), "\u3042");
    assertEquals(ngram.get(2), "\u1ec3\u3042");
    assertEquals(ngram.get(3), "\u064a\u1ec3\u3042");

    ngram.addChar('\u30a4');
    assertEquals(ngram.get(1), "\u30a2");
    assertEquals(ngram.get(2), "\u3042\u30a2");
    assertEquals(ngram.get(3), "\u1ec3\u3042\u30a2");

    ngram.addChar('\u3106');
    assertEquals(ngram.get(1), "\u3105");
    assertEquals(ngram.get(2), "\u30a2\u3105");
    assertEquals(ngram.get(3), "\u3042\u30a2\u3105");

    ngram.addChar('\uac01');
    assertEquals(ngram.get(1), "\uac00");
    assertEquals(ngram.get(2), "\u3105\uac00");
    assertEquals(ngram.get(3), "\u30a2\u3105\uac00");

    ngram.addChar('\u2010');
    assertEquals(ngram.get(1), EMPTY_STRING);
    assertEquals(ngram.get(2), "\uac00 ");
    assertEquals(ngram.get(3), "\u3105\uac00 ");

    ngram.addChar('a');
    assertEquals(ngram.get(1), "a");
    assertEquals(ngram.get(2), " a");
    assertEquals(ngram.get(3), EMPTY_STRING);
  }

  @Test
  public final void testExtractNGrams() {
    final NGram ngram =
        new NGram(
            "A\u06cc\u1ea0\u3044\u30a4\u3106\uac01\u2010a", MIN_NGRAM_LENGTH, MAX_NGRAM_LENGTH);
    final Set<String> allowlist =
        Set.of("A", " A", "ي", "ể", "あ", "ア", "あア", "ㄅ", "가", "가 ", "a", " a");
    final List<String> extractedNGrams = ngram.extractNGrams(allowlist);

    assertEquals(
        extractedNGrams,
        Arrays.asList("A", " A", "ي", "ể", "あ", "ア", "あア", "ㄅ", "가", "가 ", "a", " a"));
  }

  @Test
  public final void testExtractLongNGrams() {
    final NGram ngram = new NGram("apples", 1, 5);
    final Set<String> allowlist =
        Set.of(
            "a", " a", "p", "ap", " ap", "pp", "app", " app", "l", "pl", "ppl", "appl", " appl",
            "e", "le", "ple", "pple", "apple", "s", "es", "les", "ples", "pples");
    final List<String> extractedNGrams = ngram.extractNGrams(allowlist);

    assertEquals(
        extractedNGrams,
        Arrays.asList(
            "a", " a", "p", "ap", " ap", "p", "pp", "app", " app", "l", "pl", "ppl", "appl",
            " appl", "e", "le", "ple", "pple", "apple", "s", "es", "les", "ples", "pples"));
  }

  @Test
  public final void testExtractCustomSizeNGrams() {
    final NGram ngram = new NGram("apples", 4, 5);
    final Set<String> allowlist = Set.of(" app", "appl", " appl", "pple", "apple", "ples", "pples");
    final List<String> extractedNGrams = ngram.extractNGrams(allowlist);

    assertEquals(
        extractedNGrams, Arrays.asList(" app", "appl", " appl", "pple", "apple", "ples", "pples"));
  }

  /** Test method for {@link NGram#normalize(char)} with Romanian characters. */
  @Test
  public final void testNormalizeForRomanian() {
    assertEquals(NGram.normalize('\u015f'), '\u015f');
    assertEquals(NGram.normalize('\u0163'), '\u0163');
    assertEquals(NGram.normalize('\u0219'), '\u015f');
    assertEquals(NGram.normalize('\u021b'), '\u0163');
  }

  @Test
  public final void testNormalizeVietnamese() {
    assertEquals(NGram.normalizeVietnamese(EMPTY_STRING), EMPTY_STRING);
    assertEquals(NGram.normalizeVietnamese("ABC"), "ABC");
    assertEquals(NGram.normalizeVietnamese("012"), "012");
    assertEquals(NGram.normalizeVietnamese("\u00c0"), "\u00c0");

    assertEquals(NGram.normalizeVietnamese("\u0041\u0300"), "\u00C0");
    assertEquals(NGram.normalizeVietnamese("\u0045\u0300"), "\u00C8");
    assertEquals(NGram.normalizeVietnamese("\u0049\u0300"), "\u00CC");
    assertEquals(NGram.normalizeVietnamese("\u004F\u0300"), "\u00D2");
    assertEquals(NGram.normalizeVietnamese("\u0055\u0300"), "\u00D9");
    assertEquals(NGram.normalizeVietnamese("\u0059\u0300"), "\u1EF2");
    assertEquals(NGram.normalizeVietnamese("\u0061\u0300"), "\u00E0");
    assertEquals(NGram.normalizeVietnamese("\u0065\u0300"), "\u00E8");
    assertEquals(NGram.normalizeVietnamese("\u0069\u0300"), "\u00EC");
    assertEquals(NGram.normalizeVietnamese("\u006F\u0300"), "\u00F2");
    assertEquals(NGram.normalizeVietnamese("\u0075\u0300"), "\u00F9");
    assertEquals(NGram.normalizeVietnamese("\u0079\u0300"), "\u1EF3");
    assertEquals(NGram.normalizeVietnamese("\u00C2\u0300"), "\u1EA6");
    assertEquals(NGram.normalizeVietnamese("\u00CA\u0300"), "\u1EC0");
    assertEquals(NGram.normalizeVietnamese("\u00D4\u0300"), "\u1ED2");
    assertEquals(NGram.normalizeVietnamese("\u00E2\u0300"), "\u1EA7");
    assertEquals(NGram.normalizeVietnamese("\u00EA\u0300"), "\u1EC1");
    assertEquals(NGram.normalizeVietnamese("\u00F4\u0300"), "\u1ED3");
    assertEquals(NGram.normalizeVietnamese("\u0102\u0300"), "\u1EB0");
    assertEquals(NGram.normalizeVietnamese("\u0103\u0300"), "\u1EB1");
    assertEquals(NGram.normalizeVietnamese("\u01A0\u0300"), "\u1EDC");
    assertEquals(NGram.normalizeVietnamese("\u01A1\u0300"), "\u1EDD");
    assertEquals(NGram.normalizeVietnamese("\u01AF\u0300"), "\u1EEA");
    assertEquals(NGram.normalizeVietnamese("\u01B0\u0300"), "\u1EEB");

    assertEquals(NGram.normalizeVietnamese("\u0041\u0301"), "\u00C1");
    assertEquals(NGram.normalizeVietnamese("\u0045\u0301"), "\u00C9");
    assertEquals(NGram.normalizeVietnamese("\u0049\u0301"), "\u00CD");
    assertEquals(NGram.normalizeVietnamese("\u004F\u0301"), "\u00D3");
    assertEquals(NGram.normalizeVietnamese("\u0055\u0301"), "\u00DA");
    assertEquals(NGram.normalizeVietnamese("\u0059\u0301"), "\u00DD");
    assertEquals(NGram.normalizeVietnamese("\u0061\u0301"), "\u00E1");
    assertEquals(NGram.normalizeVietnamese("\u0065\u0301"), "\u00E9");
    assertEquals(NGram.normalizeVietnamese("\u0069\u0301"), "\u00ED");
    assertEquals(NGram.normalizeVietnamese("\u006F\u0301"), "\u00F3");
    assertEquals(NGram.normalizeVietnamese("\u0075\u0301"), "\u00FA");
    assertEquals(NGram.normalizeVietnamese("\u0079\u0301"), "\u00FD");
    assertEquals(NGram.normalizeVietnamese("\u00C2\u0301"), "\u1EA4");
    assertEquals(NGram.normalizeVietnamese("\u00CA\u0301"), "\u1EBE");
    assertEquals(NGram.normalizeVietnamese("\u00D4\u0301"), "\u1ED0");
    assertEquals(NGram.normalizeVietnamese("\u00E2\u0301"), "\u1EA5");
    assertEquals(NGram.normalizeVietnamese("\u00EA\u0301"), "\u1EBF");
    assertEquals(NGram.normalizeVietnamese("\u00F4\u0301"), "\u1ED1");
    assertEquals(NGram.normalizeVietnamese("\u0102\u0301"), "\u1EAE");
    assertEquals(NGram.normalizeVietnamese("\u0103\u0301"), "\u1EAF");
    assertEquals(NGram.normalizeVietnamese("\u01A0\u0301"), "\u1EDA");
    assertEquals(NGram.normalizeVietnamese("\u01A1\u0301"), "\u1EDB");
    assertEquals(NGram.normalizeVietnamese("\u01AF\u0301"), "\u1EE8");
    assertEquals(NGram.normalizeVietnamese("\u01B0\u0301"), "\u1EE9");

    assertEquals(NGram.normalizeVietnamese("\u0041\u0303"), "\u00C3");
    assertEquals(NGram.normalizeVietnamese("\u0045\u0303"), "\u1EBC");
    assertEquals(NGram.normalizeVietnamese("\u0049\u0303"), "\u0128");
    assertEquals(NGram.normalizeVietnamese("\u004F\u0303"), "\u00D5");
    assertEquals(NGram.normalizeVietnamese("\u0055\u0303"), "\u0168");
    assertEquals(NGram.normalizeVietnamese("\u0059\u0303"), "\u1EF8");
    assertEquals(NGram.normalizeVietnamese("\u0061\u0303"), "\u00E3");
    assertEquals(NGram.normalizeVietnamese("\u0065\u0303"), "\u1EBD");
    assertEquals(NGram.normalizeVietnamese("\u0069\u0303"), "\u0129");
    assertEquals(NGram.normalizeVietnamese("\u006F\u0303"), "\u00F5");
    assertEquals(NGram.normalizeVietnamese("\u0075\u0303"), "\u0169");
    assertEquals(NGram.normalizeVietnamese("\u0079\u0303"), "\u1EF9");
    assertEquals(NGram.normalizeVietnamese("\u00C2\u0303"), "\u1EAA");
    assertEquals(NGram.normalizeVietnamese("\u00CA\u0303"), "\u1EC4");
    assertEquals(NGram.normalizeVietnamese("\u00D4\u0303"), "\u1ED6");
    assertEquals(NGram.normalizeVietnamese("\u00E2\u0303"), "\u1EAB");
    assertEquals(NGram.normalizeVietnamese("\u00EA\u0303"), "\u1EC5");
    assertEquals(NGram.normalizeVietnamese("\u00F4\u0303"), "\u1ED7");
    assertEquals(NGram.normalizeVietnamese("\u0102\u0303"), "\u1EB4");
    assertEquals(NGram.normalizeVietnamese("\u0103\u0303"), "\u1EB5");
    assertEquals(NGram.normalizeVietnamese("\u01A0\u0303"), "\u1EE0");
    assertEquals(NGram.normalizeVietnamese("\u01A1\u0303"), "\u1EE1");
    assertEquals(NGram.normalizeVietnamese("\u01AF\u0303"), "\u1EEE");
    assertEquals(NGram.normalizeVietnamese("\u01B0\u0303"), "\u1EEF");

    assertEquals(NGram.normalizeVietnamese("\u0041\u0309"), "\u1EA2");
    assertEquals(NGram.normalizeVietnamese("\u0045\u0309"), "\u1EBA");
    assertEquals(NGram.normalizeVietnamese("\u0049\u0309"), "\u1EC8");
    assertEquals(NGram.normalizeVietnamese("\u004F\u0309"), "\u1ECE");
    assertEquals(NGram.normalizeVietnamese("\u0055\u0309"), "\u1EE6");
    assertEquals(NGram.normalizeVietnamese("\u0059\u0309"), "\u1EF6");
    assertEquals(NGram.normalizeVietnamese("\u0061\u0309"), "\u1EA3");
    assertEquals(NGram.normalizeVietnamese("\u0065\u0309"), "\u1EBB");
    assertEquals(NGram.normalizeVietnamese("\u0069\u0309"), "\u1EC9");
    assertEquals(NGram.normalizeVietnamese("\u006F\u0309"), "\u1ECF");
    assertEquals(NGram.normalizeVietnamese("\u0075\u0309"), "\u1EE7");
    assertEquals(NGram.normalizeVietnamese("\u0079\u0309"), "\u1EF7");
    assertEquals(NGram.normalizeVietnamese("\u00C2\u0309"), "\u1EA8");
    assertEquals(NGram.normalizeVietnamese("\u00CA\u0309"), "\u1EC2");
    assertEquals(NGram.normalizeVietnamese("\u00D4\u0309"), "\u1ED4");
    assertEquals(NGram.normalizeVietnamese("\u00E2\u0309"), "\u1EA9");
    assertEquals(NGram.normalizeVietnamese("\u00EA\u0309"), "\u1EC3");
    assertEquals(NGram.normalizeVietnamese("\u00F4\u0309"), "\u1ED5");
    assertEquals(NGram.normalizeVietnamese("\u0102\u0309"), "\u1EB2");
    assertEquals(NGram.normalizeVietnamese("\u0103\u0309"), "\u1EB3");
    assertEquals(NGram.normalizeVietnamese("\u01A0\u0309"), "\u1EDE");
    assertEquals(NGram.normalizeVietnamese("\u01A1\u0309"), "\u1EDF");
    assertEquals(NGram.normalizeVietnamese("\u01AF\u0309"), "\u1EEC");
    assertEquals(NGram.normalizeVietnamese("\u01B0\u0309"), "\u1EED");

    assertEquals(NGram.normalizeVietnamese("\u0041\u0323"), "\u1EA0");
    assertEquals(NGram.normalizeVietnamese("\u0045\u0323"), "\u1EB8");
    assertEquals(NGram.normalizeVietnamese("\u0049\u0323"), "\u1ECA");
    assertEquals(NGram.normalizeVietnamese("\u004F\u0323"), "\u1ECC");
    assertEquals(NGram.normalizeVietnamese("\u0055\u0323"), "\u1EE4");
    assertEquals(NGram.normalizeVietnamese("\u0059\u0323"), "\u1EF4");
    assertEquals(NGram.normalizeVietnamese("\u0061\u0323"), "\u1EA1");
    assertEquals(NGram.normalizeVietnamese("\u0065\u0323"), "\u1EB9");
    assertEquals(NGram.normalizeVietnamese("\u0069\u0323"), "\u1ECB");
    assertEquals(NGram.normalizeVietnamese("\u006F\u0323"), "\u1ECD");
    assertEquals(NGram.normalizeVietnamese("\u0075\u0323"), "\u1EE5");
    assertEquals(NGram.normalizeVietnamese("\u0079\u0323"), "\u1EF5");
    assertEquals(NGram.normalizeVietnamese("\u00C2\u0323"), "\u1EAC");
    assertEquals(NGram.normalizeVietnamese("\u00CA\u0323"), "\u1EC6");
    assertEquals(NGram.normalizeVietnamese("\u00D4\u0323"), "\u1ED8");
    assertEquals(NGram.normalizeVietnamese("\u00E2\u0323"), "\u1EAD");
    assertEquals(NGram.normalizeVietnamese("\u00EA\u0323"), "\u1EC7");
    assertEquals(NGram.normalizeVietnamese("\u00F4\u0323"), "\u1ED9");
    assertEquals(NGram.normalizeVietnamese("\u0102\u0323"), "\u1EB6");
    assertEquals(NGram.normalizeVietnamese("\u0103\u0323"), "\u1EB7");
    assertEquals(NGram.normalizeVietnamese("\u01A0\u0323"), "\u1EE2");
    assertEquals(NGram.normalizeVietnamese("\u01A1\u0323"), "\u1EE3");
    assertEquals(NGram.normalizeVietnamese("\u01AF\u0323"), "\u1EF0");
    assertEquals(NGram.normalizeVietnamese("\u01B0\u0323"), "\u1EF1");
  }
}
