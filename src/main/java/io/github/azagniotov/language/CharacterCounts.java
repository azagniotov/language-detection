package io.github.azagniotov.language;

class CharacterCounts {

  enum CharType {
    JAPANESE_HAN,
    HIRAGANA,
    KATAKANA,
    CJK_PUNCTUATION_AND_MISC,
    IRRELEVANT,
    CHINESE_HAN,
    NON_A_CJK_UNICODE_CODEPOINT
  }

  private final int[] charTypeCounts;

  private CharacterCounts() {
    charTypeCounts = new int[CharType.values().length];
  }

  double allCjkCounts() {
    return get(CharType.KATAKANA)
        + get(CharType.HIRAGANA)
        + get(CharType.JAPANESE_HAN)
        + get(CharType.CHINESE_HAN)
        + get(CharType.CJK_PUNCTUATION_AND_MISC);
  }

  double irrelevant() {
    return get(CharType.IRRELEVANT);
  }

  double get(final CharType key) {
    return charTypeCounts[key.ordinal()];
  }

  void mark(final CharType key) {
    charTypeCounts[key.ordinal()]++;
  }

  static CharacterCounts create() {
    return new CharacterCounts();
  }
}
