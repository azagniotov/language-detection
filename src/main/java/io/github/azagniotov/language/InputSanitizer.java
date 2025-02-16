package io.github.azagniotov.language;

import static io.github.azagniotov.language.StringConstants.BLANK_SPACE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class InputSanitizer {

  private static final Pattern PATTERN_NOT_A_WORD =
      Pattern.compile("\\P{IsWord}", Pattern.UNICODE_CHARACTER_CLASS);

  private static final String FILENAME_EXTENSION_ANYWHERE = "\\.[a-zA-Z0-9]{0,10}(?=\\s|$)";
  private static final String SOLR_BOOLEAN_QUERIES = "(AND|OR|NOT)";
  private static final String SUBSET_LATIN_PUNCTUATION_MARKS = "[.#\\[\\],_\"\\-]";

  private static final Pattern COMBINED_PATTERN =
      Pattern.compile(
          String.format(
              "%s|%s|%s",
              FILENAME_EXTENSION_ANYWHERE, SOLR_BOOLEAN_QUERIES, SUBSET_LATIN_PUNCTUATION_MARKS));

  private InputSanitizer() {}

  static String filterOutNonWords(final String input) {
    final Matcher matcher = PATTERN_NOT_A_WORD.matcher(input);

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

  /**
   * Removes:
   *
   * <p>1. Solr boolean queries
   *
   * <p>2. Any file extensions
   *
   * <p>3. A subset of Latin punctuation marks.
   */
  static String sanitizeForSearch(final String input) {
    // Removing file extensions and Solr boolean operators, which may cause
    // language detection for filenames and/or short search queries to misdetect
    final Matcher matcher = COMBINED_PATTERN.matcher(input);
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

    // It is fine to call .trim() in the current function
    return result.toString().trim();
  }
}
