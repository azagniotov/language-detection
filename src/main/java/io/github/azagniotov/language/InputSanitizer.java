package io.github.azagniotov.language;

import static io.github.azagniotov.language.StringConstants.BLANK_SPACE;

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
    // Do not .trim() the input nor the result, otherwise accuracy unit tests will fail
    return PATTERN_NOT_A_WORD.matcher(input).replaceAll(BLANK_SPACE);
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
    // It is fine to call .trim() in the current function
    return COMBINED_PATTERN.matcher(input).replaceAll(BLANK_SPACE).trim();
  }
}
