package io.github.azagniotov.language;

import static io.github.azagniotov.language.StringConstants.BLANK_CHAR;
import static io.github.azagniotov.language.StringConstants.EMPTY_STRING;
import static io.github.azagniotov.language.TestDefaultConstants.MAX_NGRAM_LENGTH;
import static io.github.azagniotov.language.TestDefaultConstants.MIN_NGRAM_LENGTH;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Test;

public class NGramTest {

  @BeforeClass
  public static void setUp() throws IOException {
    // Warm-up for the static initializers
    NGram.normalize('\u0000');
  }

  @Test
  public final void testDefaultValue() {
    final NGram ngram = new NGram("input", 1, 3);
    assertEquals(ngram.getMinNGramLength(), 1);
    assertEquals(ngram.getMaxNGramLength(), 3);
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
    final List<String> actual =
        ngram.extractNGrams(Set.of("A", " A", "ي", "ể", "あ", "ア", "あア", "ㄅ", "가", "가 ", "a", " a"));
    Collections.sort(actual);

    final List<String> expected =
        Arrays.asList("A", " A", "ي", "ể", "あ", "ア", "あア", "ㄅ", "가", "가 ", "a", " a");
    Collections.sort(expected);

    assertEquals(expected, actual);
  }

  /** Test method for {@link NGram#normalize(char)} with Romanian characters. */
  @Test
  public final void testNormalizeForRomanian() {
    assertEquals(NGram.normalize('\u015f'), '\u015f');
    assertEquals(NGram.normalize('\u0163'), '\u0163');
    assertEquals(NGram.normalize('\u0219'), '\u015f');
    assertEquals(NGram.normalize('\u021b'), '\u0163');
  }
}
