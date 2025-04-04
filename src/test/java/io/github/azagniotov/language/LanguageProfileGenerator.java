package io.github.azagniotov.language;

import static io.github.azagniotov.language.StringConstants.BLANK_SPACE;
import static io.github.azagniotov.language.StringConstants.EMPTY_STRING;
import static io.github.azagniotov.language.StringConstants.GZIP_EXTENSION;
import static java.util.Collections.nCopies;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.Ignore;
import org.junit.Test;

public class LanguageProfileGenerator {

  // 64 KB for the char[] elements
  // 8 bytes for the reference
  // ~12–16 bytes for the array object overhead
  // Memory usage for each chunk is: ~ 64 KB + 20–24 bytes
  private static final int CHUNK_SIZE = 32768;
  private static final int MIN_GRAM_SIZE = 1;
  private static final int MAX_GRAM_SIZE = 3;

  private static final Set<String> SPECIFIC_EAST_ASIAN_ISO_CODES = Set.of("ja", "zh", "ko");
  // Matches anything that is NOT Hiragana, Katakana, Kanji, Full-width characters or numbers
  // \\x{FF10}-\\x{FF19}: Matches full-width Arabic digits (０１２３４５６７８９).
  // \\x{FF21}-\\x{FF3A}: Matches full-width Latin uppercase letters (ＡＢＣＤＥ...).
  // \\x{FF41}-\\x{FF5A}: Matches full-width Latin lowercase letters (ａｂｃｄｅ...).
  // Cleans-up this "これはテスト123abcABCDEアイウエオこんにちは１２３ＡＢＣＤＥ ä, ö, ü, and ß á, à, è, é, û, ù,"
  private static final Pattern PATTERN_MATCH_ANYTHING_NON_EAST_ASIAN =
      Pattern.compile(
          "[^\\p{IsHiragana}\\p{IsKatakana}\\p{IsHan}\\d\\x{FF10}-\\x{FF19}\\x{FF21}-\\x{FF3A}\\x{FF41}-\\x{FF5A}]");
  private static final Pattern PATTERN_MATCH_SPECIFIC_EAST_ASIAN =
      Pattern.compile(
          "[\\p{IsHiragana}\\p{IsKatakana}\\p{IsHan}\\p{IsHangul}\\d\\x{FF10}-\\x{FF19}\\x{FF21}-\\x{FF3A}\\x{FF41}-\\x{FF5A}]");

  private static final Pattern PATTERN_MATCH_NUMERICS = Pattern.compile("[\\p{IsDigit}]");
  private static final Pattern PATTERN_MULTIPLE_SPACES = Pattern.compile("\\s+");

  // Regex to match both opening and closing WikiExtractor <doc> tags, including attributes
  private static final Pattern PATTERN_MATCH_WIKI_EXTRACTOR_TAGS =
      Pattern.compile("<doc[^>]*>|</doc>");

  @Test
  @Ignore
  public void generateProfiles() throws Exception {
    TreeSet<String> targetCodes =
        new TreeSet<>(
            Set.of(
                "am,az,bo,br,cy,eu,ga,gv,ha,hy,ka,kk,kw,ky,mn,om,sn,sr,tg,ti,yi,yo,zu".split(",")));
    targetCodes = new TreeSet<>(Set.of("ha".split(",")));
    System.out.println(
        "\nWill generate: ["
            + targetCodes.size()
            + "] profiles for ISO 639-1 codes: "
            + targetCodes);
    for (final String targetCode : targetCodes) {
      generate(targetCode, "profiles");
    }
  }

  private void generate(final String targetCode, final String profilesHome) throws Exception {
    System.out.println("Starting processing for: [" + targetCode + "] => " + profilesHome);
    final String sourcePath =
        String.format("/Users/azagniotov/Documents/data/%swiki/extracted/AA", targetCode);

    if (sourcePath.trim().isEmpty()) {
      System.out.println("\nSeed dataset is missing. Skipping generation.\n");
      return;
    }

    final List<Float> nWords = Arrays.asList(nCopies(MAX_GRAM_SIZE, 0.0f).toArray(new Float[0]));
    final Map<String, Long> freq = new HashMap<>();
    final LanguageProfile languageProfile = new LanguageProfile(targetCode, freq, nWords);

    processFilesInDirectory(sourcePath, languageProfile);

    System.out.println("\n.getNGramCounts(): " + languageProfile.getNGramCounts());
    languageProfile.omitLessFreq();
    System.out.println(".getNGramCounts(): " + languageProfile.getNGramCounts() + "\n");

    final String languageProfileJson = languageProfile.toJson();
    writeProfileGzipped(profilesHome, targetCode, languageProfileJson);

    assertTrue(true);
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
        final String noWikiTags = sanitize(PATTERN_MATCH_WIKI_EXTRACTOR_TAGS, input);

        final String sanitized;
        if (SPECIFIC_EAST_ASIAN_ISO_CODES.contains(languageProfile.getIsoCode639_1())) {
          sanitized = sanitize(PATTERN_MATCH_ANYTHING_NON_EAST_ASIAN, noWikiTags);
        } else {
          sanitized = sanitize(PATTERN_MATCH_SPECIFIC_EAST_ASIAN, noWikiTags);
        }

        final String sanitizedWithoutDigits = sanitize(PATTERN_MATCH_NUMERICS, sanitized);
        final Matcher matcher = PATTERN_MULTIPLE_SPACES.matcher(sanitizedWithoutDigits);
        languageProfile.update(matcher.replaceAll(BLANK_SPACE), MIN_GRAM_SIZE, MAX_GRAM_SIZE);
      }
    }
  }

  @Deprecated
  private void writeProfile(final String path, final String targetCode, final String json)
      throws IOException {
    final String resourcesRoot = "src/main/resources/profiles";
    final File childResourcesDir = new File(resourcesRoot + "/" + path);
    final File childResourcesDirFile = new File(childResourcesDir, targetCode);

    try (final FileWriter writer = new FileWriter(childResourcesDirFile)) {
      writer.write(json);
    }
  }

  private void writeProfileGzipped(
      final String profilesHome, final String targetCode, final String json) throws IOException {
    final String resourcesRoot = "src/main/resources";
    final File childResourcesDir = new File(resourcesRoot + "/" + profilesHome);
    final File childResourcesDirFile = new File(childResourcesDir, targetCode + GZIP_EXTENSION);

    try (final InputStream gzippedJsonStream = GzipUtils.gzipString(json);
        final FileOutputStream fileOutputStream = new FileOutputStream(childResourcesDirFile);
        final BufferedOutputStream bufferedOutputStream =
            new BufferedOutputStream(fileOutputStream)) {
      final byte[] buffer = new byte[2048];
      while (true) {
        final int bytesRead = gzippedJsonStream.read(buffer);
        if (bytesRead == -1) {
          break;
        }
        bufferedOutputStream.write(buffer, 0, bytesRead);
      }
    }
  }

  private String sanitize(final Pattern pattern, final String input) {
    final Matcher matcher = pattern.matcher(input);
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

  @Test
  public final void sanityCheckFilteringRegex() {
    final String input =
        "<doc id=\"599\" url=\"https://mn.wikipedia.org/wiki?curid=599\" title=\"Монгол Улс\">Some content"
            + "      "
            + ""
            + "これはテスト123abcABCDEアイウエオこんにちは１２３ＡＢＣＤＥ         ä, ö, ü,  and ß á, à, è, é, û, ù"
            + "</doc>";

    final String noWikiTags = sanitize(PATTERN_MATCH_WIKI_EXTRACTOR_TAGS, input);
    assertEquals(
        noWikiTags,
        "Some content      これはテスト123abcABCDEアイウエオこんにちは１２３ＡＢＣＤＥ         ä, ö, ü,  and ß á, à, è, é, û, ù");

    final String onlyEastAsian = sanitize(PATTERN_MATCH_ANYTHING_NON_EAST_ASIAN, noWikiTags);
    assertEquals(onlyEastAsian, "これはテスト123アイウエオこんにちは１２３ＡＢＣＤＥ");

    final String onlyEastAsianWithoutDigits = sanitize(PATTERN_MATCH_NUMERICS, onlyEastAsian);
    assertEquals(onlyEastAsianWithoutDigits, "これはテストアイウエオこんにちはＡＢＣＤＥ");

    final String withoutEastAsian = sanitize(PATTERN_MATCH_SPECIFIC_EAST_ASIAN, noWikiTags);
    assertEquals(
        withoutEastAsian, "Some content      abcABCDE         ä, ö, ü,  and ß á, à, è, é, û, ù");

    final String withoutEastAsianAndDigits = sanitize(PATTERN_MATCH_NUMERICS, withoutEastAsian);
    assertEquals(
        withoutEastAsianAndDigits,
        "Some content      abcABCDE         ä, ö, ü,  and ß á, à, è, é, û, ù");

    final Matcher matcher = PATTERN_MULTIPLE_SPACES.matcher(withoutEastAsianAndDigits);
    assertEquals(
        matcher.replaceAll(BLANK_SPACE), "Some content abcABCDE ä, ö, ü, and ß á, à, è, é, û, ù");
  }
}
