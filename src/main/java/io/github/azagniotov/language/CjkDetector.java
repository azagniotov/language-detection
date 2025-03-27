package io.github.azagniotov.language;

import static java.lang.Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION;
import static java.lang.Character.UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS;
import static java.lang.Character.UnicodeBlock.IDEOGRAPHIC_SYMBOLS_AND_PUNCTUATION;
import static java.lang.Character.UnicodeBlock.SUPPLEMENTAL_PUNCTUATION;

import io.github.azagniotov.language.CharacterCounts.CharType;
import java.lang.Character.UnicodeBlock;
import java.lang.Character.UnicodeScript;
import java.util.Set;

/**
 * Iterates over characters in a given input and determines whether it is a Chinese or a Japanese
 * input string.
 */
class CjkDetector {

  private static final Set<UnicodeBlock> PUNCTUATION_AND_MISC_BLOCKS =
      Set.of(
          CJK_SYMBOLS_AND_PUNCTUATION,
          ENCLOSED_CJK_LETTERS_AND_MONTHS,
          IDEOGRAPHIC_SYMBOLS_AND_PUNCTUATION,
          SUPPLEMENTAL_PUNCTUATION);

  static CjkDecision decide(final String input, final double threshold) {
    final CharacterCounts characterCounts = CharacterCounts.create();
    input
        .codePoints()
        .forEach(
            codePoint -> {
              if (isIrrelevantChar(codePoint)) {
                characterCounts.mark(CharType.IRRELEVANT);
              } else {
                characterCounts.mark(determineCharType(codePoint));
              }
            });

    if (characterCounts.get(CharType.KATAKANA) == 0
        && characterCounts.get(CharType.HIRAGANA) == 0) {
      if (characterCounts.get(CharType.JAPANESE_HAN) < characterCounts.get(CharType.CHINESE_HAN)) {
        // There are less Japanese Han kanji than Chinese Han.
        // Not a Japanese language input, therefore check for a Chinese language input
        final boolean decision =
            checkThreshold(
                input,
                characterCounts.get(CharType.CHINESE_HAN),
                characterCounts.irrelevant(),
                threshold);
        return decision ? CjkDecision.DECISION_CHINESE : CjkDecision.DECISION_NONE;
      } else {
        // We have a Japanese input
        final boolean decision =
            checkThreshold(
                input, characterCounts.allJapanese(), characterCounts.irrelevant(), threshold);
        return decision ? CjkDecision.DECISION_JAPANESE : CjkDecision.DECISION_NONE;
      }
    } else {
      // We have some Katakana and/or some Hiragana and/or some Japanese Han Kanji
      final boolean decision =
          checkThreshold(
              input, characterCounts.allJapanese(), characterCounts.irrelevant(), threshold);
      return decision ? CjkDecision.DECISION_JAPANESE : CjkDecision.DECISION_NONE;
    }
  }

  private static CharType determineCharType(final int codePoint) {
    if (!Character.isValidCodePoint(codePoint)) {
      return CharType.INVALID_CODEPOINT_OR_NULL_UNCODE_BLOCK;
    }
    final char debugging = (char) codePoint;

    final UnicodeScript charUnicodeScript = UnicodeScript.of(codePoint);
    final UnicodeBlock charUnicodeBlock = UnicodeBlock.of(codePoint);

    // Check the UnicodeScripts
    if (JapaneseHan.of(codePoint)) {
      return CharType.JAPANESE_HAN;
    } else if (UnicodeScript.HAN == charUnicodeScript) {
      return CharType.CHINESE_HAN;
    } else if (UnicodeScript.KATAKANA == charUnicodeScript) {
      return CharType.KATAKANA;
    } else if (UnicodeScript.HIRAGANA == charUnicodeScript) {
      return CharType.HIRAGANA;
    }

    // Only the actual syllables belong in the UnicodeScript, we need to utilize
    // UnicodeBlocks to check for:
    // - Various marks, like full-width/half-width Japanese prolonged sound mark or
    // Japanese forward slash '／'
    // - Half-width Katakana forms
    else if (charUnicodeBlock == null) {
      return CharType.INVALID_CODEPOINT_OR_NULL_UNCODE_BLOCK;
    } else if (UnicodeBlock.KATAKANA == charUnicodeBlock) {
      return CharType.KATAKANA;
    } else if (UnicodeBlock.HIRAGANA == charUnicodeBlock) {
      return CharType.HIRAGANA;
    } else if (UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS == charUnicodeBlock) {
      return CharType.KATAKANA;
    } else if (PUNCTUATION_AND_MISC_BLOCKS.contains(charUnicodeBlock)) {
      // Do the current else-if CJK punctuation check last after the irrelevant chars,
      // so that we won't count the SPACE chars as CJK punctuation
      // UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION Unicode ranges
      // https://stackoverflow.com/a/53807563
      // For example, what we want to count is Japanese brackets: 【 】
      return CharType.CJK_PUNCTUATION_AND_MISC;
    }

    return CharType.INVALID_CODEPOINT_OR_NULL_UNCODE_BLOCK;
  }

  /**
   * Determines if a given character Unicode code point should be considered irrelevant for the
   * purpose of language detection.
   *
   * @param codePoint character Unicode code point
   * @return true if character should be considered irrelevant
   */
  static boolean isIrrelevantChar(final int codePoint) {
    return Character.isSpaceChar(codePoint) /* Unicode's specification for what is whitespace */
        || Character.isWhitespace(codePoint) /* Java's specification for what is whitespace    */
        || Character.isDigit(codePoint);
  }

  private static boolean checkThreshold(
      final String input,
      final double totalMatchedChars,
      final double irrelevantChars,
      final double threshold) {

    if (threshold <= 0.0) {
      return false;
    }

    final double finalLength = ((double) input.length()) - irrelevantChars;
    // if (finalLength == 0) return false; // avoid division by zero
    final double matchedRatio = totalMatchedChars / finalLength;

    return matchedRatio >= Math.min(threshold, 1.0);
  }
}
