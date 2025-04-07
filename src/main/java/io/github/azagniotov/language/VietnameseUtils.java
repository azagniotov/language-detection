package io.github.azagniotov.language;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracted from the original implementation: <a
 * href="https://github.com/shuyo/language-detection/blob/c92ca72192b79ac421e809de46d5d0dafaef98ef/src/com/cybozu/labs/langdetect/util/NGram.java#L105-L135">See
 * here</a>
 */
class VietnameseUtils {

  private static final String[] VI_NORMALIZED_CHARS = {
    "\u00C0\u00C8\u00CC\u00D2\u00D9\u1EF2\u00E0\u00E8\u00EC\u00F2\u00F9\u1EF3\u1EA6\u1EC0\u1ED2\u1EA7\u1EC1\u1ED3\u1EB0\u1EB1\u1EDC\u1EDD\u1EEA\u1EEB",
    "\u00C1\u00C9\u00CD\u00D3\u00DA\u00DD\u00E1\u00E9\u00ED\u00F3\u00FA\u00FD\u1EA4\u1EBE\u1ED0\u1EA5\u1EBF\u1ED1\u1EAE\u1EAF\u1EDA\u1EDB\u1EE8\u1EE9",
    "\u00C3\u1EBC\u0128\u00D5\u0168\u1EF8\u00E3\u1EBD\u0129\u00F5\u0169\u1EF9\u1EAA\u1EC4\u1ED6\u1EAB\u1EC5\u1ED7\u1EB4\u1EB5\u1EE0\u1EE1\u1EEE\u1EEF",
    "\u1EA2\u1EBA\u1EC8\u1ECE\u1EE6\u1EF6\u1EA3\u1EBB\u1EC9\u1ECF\u1EE7\u1EF7\u1EA8\u1EC2\u1ED4\u1EA9\u1EC3\u1ED5\u1EB2\u1EB3\u1EDE\u1EDF\u1EEC\u1EED",
    "\u1EA0\u1EB8\u1ECA\u1ECC\u1EE4\u1EF4\u1EA1\u1EB9\u1ECB\u1ECD\u1EE5\u1EF5\u1EAC\u1EC6\u1ED8\u1EAD\u1EC7\u1ED9\u1EB6\u1EB7\u1EE2\u1EE3\u1EF0\u1EF1"
  };
  private static final String VI_CHARS =
      "AEIOUYaeiouy\u00c2\u00ca\u00d4\u00e2\u00ea\u00f4\u0102\u0103\u01a0\u01a1\u01af\u01b0";
  private static final String VI_DIACRITICS = "\u0300\u0301\u0303\u0309\u0323";
  private static final Pattern VI_CHARS_WITH_DIACRITIC_PATTERN =
      Pattern.compile("([" + VI_CHARS + "])([" + VI_DIACRITICS + "])");

  private VietnameseUtils() {}

  /** Normalize Vietnamese letter + diacritical mark (U+03xx) to a single character (U+1Exx). */
  static String normalizeVietnamese(String text) {
    Matcher matcher = VI_CHARS_WITH_DIACRITIC_PATTERN.matcher(text);
    final StringBuilder stringBuilder = new StringBuilder();
    while (matcher.find()) {
      int charIndex = VI_CHARS.indexOf(matcher.group(1));
      matcher.appendReplacement(
          stringBuilder,
          VI_NORMALIZED_CHARS[VI_DIACRITICS.indexOf(matcher.group(2))].substring(
              charIndex, charIndex + 1));
    }
    if (stringBuilder.length() == 0) {
      return text;
    }
    matcher.appendTail(stringBuilder);
    return stringBuilder.toString();
  }
}
