package io.github.azagniotov.language;

import static io.github.azagniotov.language.StringConstants.BLANK_SPACE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class InputSanitizer {

  private static final Pattern PATTERN_FILTER_ALL_BUT_ALPHANUMERIC =
      Pattern.compile("\\P{IsWord}", Pattern.UNICODE_CHARACTER_CLASS);

  private static final String URLS = "(https?://[\\w.-]+(?:/[\\w\\d&%_./=-]*)?)([\\p{P}\\s]+)?";
  private static final Pattern URLS_PATTERN = Pattern.compile(URLS);

  private InputSanitizer() {}

  static String filterOutNonWords(final String input) {
    final Matcher matcher = PATTERN_FILTER_ALL_BUT_ALPHANUMERIC.matcher(input);

    final StringBuilder result = new StringBuilder();
    while (matcher.find()) {
      // It is recommended to avoid using .replaceAll when replacing
      // matches with a blank space, as this method generates a new
      // intermediate string for each replacement, which can be costly
      // in terms of both time and memory. In contrast, for scenarios
      // involving frequent replacements or processing large strings,
      // the Matcher approach used here is typically far more efficient
      // as it minimizes the overhead associated with string creation
      // and reduces memory consumption.
      matcher.appendReplacement(result, BLANK_SPACE);
    }
    matcher.appendTail(result);

    // Do not .trim() the input nor the result, otherwise accuracy unit tests will fail
    return result.toString();
  }

  static String sanitize(final String input) {
    if (input.contains("https://") || input.contains("http://")) {
      return URLS_PATTERN.matcher(input).replaceAll(StringConstants.EMPTY_STRING);
    } else {
      return input;
    }
  }
}
