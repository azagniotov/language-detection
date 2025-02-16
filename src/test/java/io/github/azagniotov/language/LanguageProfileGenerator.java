package io.github.azagniotov.language;

import static io.github.azagniotov.language.StringConstants.EMPTY_STRING;
import static java.util.Collections.nCopies;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.Ignore;
import org.junit.Test;

public class LanguageProfileGenerator {

  // 32 KB for the char[] elements
  // 8 bytes for the reference
  // ~12–16 bytes for the array object overhead
  // Memory usage for each chunk is: ~ 32 KB + 20–24 bytes
  private static final int CHUNK_SIZE = 16384;
  private static final int MAX_GRAM_SIZE = 3;

  // Matches anything that is NOT Hiragana, Katakana, Kanji, Full-width characters or numbers
  // \\x{FF10}-\\x{FF19}: Matches full-width Arabic digits (０１２３４５６７８９).
  // \\x{FF21}-\\x{FF3A}: Matches full-width Latin uppercase letters (ＡＢＣＤＥ...).
  // \\x{FF41}-\\x{FF5A}: Matches full-width Latin lowercase letters (ａｂｃｄｅ...).
  // Cleans-up this "これはテスト123abcABCDEアイウエオこんにちは１２３ＡＢＣＤＥ ä, ö, ü, and ß á, à, è, é, û, ù,"
  private static final String regex =
      "[^\\p{IsHiragana}\\p{IsKatakana}\\p{IsHan}\\d\\x{FF10}-\\x{FF19}\\x{FF21}-\\x{FF3A}\\x{FF41}-\\x{FF5A}]";
  private static final Pattern PATTERN_ANYTHING_NON_JAPANESE = Pattern.compile(regex);

  @Test
  @Ignore
  public void generateProfile() throws Exception {
    final String targetCode = "ja";
    final String sourcePath =
        String.format("/Users/azagniotov/Documents/data/%swiki/extracted/AA", targetCode);

    if (sourcePath.equals("")) {
      System.out.println("\nSeed dataset is missing. Skipping generation.\n");
      return;
    }

    final File directory = new File("src/main/resources/langdetect/high-accuracy");
    if (!directory.exists()) {
      boolean mkdirs = directory.mkdirs(); // Create the directory if it doesn't exist
      if (mkdirs) {
        System.out.println("Created high-accuracy directory");
      }
    }

    final List<Double> nWords = Arrays.asList(nCopies(MAX_GRAM_SIZE, 0.0).toArray(new Double[0]));
    final LanguageProfile languageProfile =
        new LanguageProfile(targetCode, new HashMap<>(), nWords);

    processFilesInDirectory(sourcePath, languageProfile);

    System.out.println("\n.getNGramCounts(): " + languageProfile.getNGramCounts());
    languageProfile.omitLessFreq();
    System.out.println(".getNGramCounts(): " + languageProfile.getNGramCounts() + "\n");

    final String languageProfileJson = languageProfile.toJson();
    writeProfile("high-accuracy", targetCode, languageProfileJson);

    assertEquals("apples", "apples");
  }

  public void processFilesInDirectory(
      final String directoryPath, final LanguageProfile languageProfile) throws IOException {

    try (final Stream<Path> paths = Files.list(Paths.get(directoryPath))) {
      paths
          .filter(path -> path.toString().endsWith(".txt"))
          .forEach(
              path -> {
                System.out.println("\nReading: " + path + "..");
                try {
                  updateProfileInChunks(path.toFile(), languageProfile);
                  System.out.println("Finished processing!!");
                } catch (IOException e) {
                  throw new UncheckedIOException(e);
                }
              });
    }
  }

  public void updateProfileInChunks(final File file, final LanguageProfile languageProfile)
      throws IOException {
    try (final FileInputStream fileInputStream = new FileInputStream(file);
        final InputStreamReader inputStreamReader =
            new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        final BufferedReader reader = new BufferedReader(inputStreamReader)) {

      final char[] buffer = new char[CHUNK_SIZE];
      while (true) {
        final int bytesRead = reader.read(buffer);
        if (bytesRead == -1) {
          break;
        }
        final String input = new String(buffer, 0, bytesRead);
        if (languageProfile.getIsoCode639_1().equals("ja")) {
          languageProfile.update(sanitizeForJapanese(input), MAX_GRAM_SIZE);
        } else {
          languageProfile.update(input, MAX_GRAM_SIZE);
        }
      }
    }
  }

  private void writeProfile(final String path, final String targetCode, final String json)
      throws IOException {
    final String resourcesRoot = "src/main/resources/langdetect";
    final File childResourcesDir = new File(resourcesRoot + "/" + path);
    final File childResourcesDirFile = new File(childResourcesDir, targetCode);

    try (final FileWriter writer = new FileWriter(childResourcesDirFile)) {
      writer.write(json);
    }
  }

  private String sanitizeForJapanese(final String input) {
    final Matcher matcher = PATTERN_ANYTHING_NON_JAPANESE.matcher(input);
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
      matcher.appendReplacement(result, EMPTY_STRING);
    }
    matcher.appendTail(result);

    return result.toString().trim();
  }
}
