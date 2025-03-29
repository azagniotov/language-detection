package io.github.azagniotov.language;

import java.lang.Character.UnicodeBlock;
import java.lang.Character.UnicodeScript;

class UnicodeCache {

  private static final UnicodeScript[] UNICODE_SCRIPT_LOOKUP =
      new UnicodeScript[Character.MAX_VALUE + 1];
  private static final UnicodeBlock[] UNICODE_BLOCK_LOOKUP =
      new UnicodeBlock[Character.MAX_VALUE + 1];

  static {
    //  Character.MAX_VALUE has the Unicode code point U+FFFF, which is the highest
    //  valid character code point for a char in Java (which uses UTF-16 encoding).
    for (int codePoint = 0; codePoint <= Character.MAX_VALUE; codePoint++) {
      UNICODE_SCRIPT_LOOKUP[codePoint] = UnicodeScript.of(codePoint);
      UNICODE_BLOCK_LOOKUP[codePoint] = UnicodeBlock.of(codePoint);
    }
  }

  static UnicodeScript scriptOf(final int codePoint) {
    if (codePoint > Character.MAX_VALUE) {
      return null;
    }
    return UNICODE_SCRIPT_LOOKUP[codePoint];
  }

  static UnicodeBlock blockOf(final int codePoint) {
    if (codePoint > Character.MAX_VALUE) {
      return null;
    }
    return UNICODE_BLOCK_LOOKUP[codePoint];
  }
}
