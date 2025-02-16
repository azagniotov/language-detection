package io.github.azagniotov.language;

import static io.github.azagniotov.language.TestDefaultConstants.MAX_NGRAM_LENGTH;
import static io.github.azagniotov.language.TestHelper.getTopLanguageCode;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * This class tests classification accuracy on various datasets and parameters, as specified in the
 * accuracies.csv resource file.
 *
 * <p>Original author and ideas: By @yanirs, <a
 * href="https://github.com/jprante/elasticsearch-langdetect/pull/69">https://github.com/jprante/elasticsearch-langdetect/pull/69</a>
 */
@RunWith(Parameterized.class)
public class LanguageDetectorAccuracyTest {

  private static final double ACCURACY_DELTA = 1e-6;
  private static final String ALL_LANGUAGES =
      "af,ar,bg,bn,ca,cs,da,de,el,en,es,et,fa,fi,fr,gu,he,hi,hr,hu,id,it,ja,kn,ko,lt,lv,mk,ml,mr,ne,nl,no,pa,pl,pt,"
          + "ro,ru,si,sk,sl,so,sq,sv,sw,ta,te,th,tl,tr,uk,ur,vi,zh-cn,zh-tw";
  private static final String OLD_DEFAULT_LANGUAGES =
      "ar,bg,bn,cs,da,de,el,en,es,et,fa,fi,fr,gu,he,hi,hr,hu,id,it,ja,ko,lt,lv,mk,ml,nl,no,pa,pl,pt,ro,ru,sq,sv,ta,"
          + "te,th,tl,tr,uk,ur,vi,zh-cn,zh-tw";
  private static final String ALL_DEFAULT_PROFILE_LANGUAGES =
      "af,ar,bg,bn,cs,da,de,el,en,es,et,fa,fi,fr,gu,he,hi,hr,hu,id,it,ja,kn,ko,lt,lv,mk,ml,mr,ne,nl,no,pa,pl,pt,ro,"
          + "ru,sk,sl,so,sq,sv,sw,ta,te,th,tl,tr,uk,ur,vi,zh-cn,zh-tw";
  private static final String ALL_SHORT_PROFILE_LANGUAGES =
      "ar,bg,bn,ca,cs,da,de,el,en,es,et,fa,fi,fr,gu,he,hi,hr,hu,id,it,ja,ko,lt,lv,mk,ml,nl,no,pa,pl,pt,ro,ru,si,sq,"
          + "sv,ta,te,th,tl,tr,uk,ur,vi,zh-cn,zh-tw";

  private static final String TAB_CHARACTER = "\t";
  private static final String COMMA_CHARACTER = ",";
  private static final String ACCURACY_REPORT_HOME = "./build/reports/accuracy";
  private static final String ACCURACY_REPORT_PATH_TEMPLATE =
      ACCURACY_REPORT_HOME + "/accuracy-report-%s.csv";
  private static String ACCURACY_REPORT_NAME;

  private static Map<String, Map<String, List<String>>> allDatasets;

  private final String dataset;
  private final int substringLength;
  private final int sampleSize;
  private final String profile;
  private final boolean useAllLanguages;
  private final Map<String, Double> languageToExpectedAccuracy;

  /**
   * Construct a test for classification accuracies on substrings of texts from a single dataset.
   *
   * <p>For each text and substring length, this test generates a sample of substrings (drawn
   * uniformly with replacement from the set of possible substrings of the given length), runs the
   * language identification code, measures the per-language accuracy (percentage of substrings
   * classified correctly), and fails if the accuracy varies by more than {@link #ACCURACY_DELTA}
   * from the expected accuracy for the language.
   *
   * @param dataset multi-language dataset name, as read in the setup step (see {@link #setUp()})
   * @param profile profile name parameter to pass to the detection service
   * @param substringLength substring length to test (see {@link #sampleText(String, String, int,
   *     int)})
   * @param sampleSize number of substrings to test (see {@link #sampleText(String, String, int,
   *     int)})
   * @param useAllLanguages if true, all supported languages will be used instead of just the old
   *     default ones
   * @param languageToExpectedAccuracy mapping from language code to expected accuracy
   */
  public LanguageDetectorAccuracyTest(
      final String dataset,
      final String profile,
      final int substringLength,
      final int sampleSize,
      final boolean useAllLanguages,
      final Map<String, Double> languageToExpectedAccuracy) {
    this.dataset = dataset;
    this.profile = profile;
    this.substringLength = substringLength;
    this.sampleSize = sampleSize;
    this.useAllLanguages = useAllLanguages;
    this.languageToExpectedAccuracy = Map.copyOf(languageToExpectedAccuracy);
  }

  /**
   * Perform the common set up tasks for tests of this class: read the datasets, and write the
   * header row of the output CSV if the path.accuracies.out system property is set.
   */
  @BeforeClass
  public static void setUp() throws IOException {
    allDatasets = new HashMap<>();
    allDatasets.put("udhr", readDataset("/udhr.tsv"));
    allDatasets.put("wordpress-translations", readDataset("/wordpress-translations.tsv"));

    final File directory = new File(ACCURACY_REPORT_HOME);
    if (!directory.exists()) {
      boolean mkdirs = directory.mkdirs(); // Create the directory if it doesn't exist
      if (mkdirs) {
        System.out.println("Created " + ACCURACY_REPORT_HOME + " directory");
      }
    }

    final String header = "dataset,profile,substringLength,sampleSize,useAllLanguages,";
    final long reportTimestamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    ACCURACY_REPORT_NAME = String.format(ACCURACY_REPORT_PATH_TEMPLATE, reportTimestamp);
    Files.write(
        Path.of(ACCURACY_REPORT_NAME),
        Collections.singletonList(header + ALL_LANGUAGES),
        StandardCharsets.UTF_8);
  }

  /** Run the test according to the parameters passed to the constructor. */
  @Test
  public void simulation() throws Exception {

    String languageCodes = OLD_DEFAULT_LANGUAGES;
    if (useAllLanguages) {
      if (profile.equals("default")) {
        languageCodes = ALL_DEFAULT_PROFILE_LANGUAGES;
      } else if (profile.equals("short-text")) {
        languageCodes = ALL_SHORT_PROFILE_LANGUAGES;
      } else {
        assertEquals(profile, "merged-average");
        languageCodes = ALL_LANGUAGES;
      }
    }

    final LanguageDetectionSettings testSettings =
        LanguageDetectionSettings.fromIsoCodes639_1(languageCodes)
            .withProfile(profile.equals("default") ? "" : profile)
            .build();
    final LanguageDetectorFactory factory = new LanguageDetectorFactory(testSettings);
    final LanguageDetector languageDetector =
        new LanguageDetector(
            factory.getSupportedIsoCodes639_1(),
            factory.getLanguageCorporaProbabilities(),
            MAX_NGRAM_LENGTH);

    final Map<String, List<String>> languageToFullTexts = allDatasets.get(dataset);
    final Set<String> testedLanguages = new TreeSet<>(languageToFullTexts.keySet());
    testedLanguages.retainAll(testSettings.getIsoCodes639_1());

    // Classify the texts and calculate the accuracy for each language
    final Map<String, Double> languageToDetectedAccuracy = new HashMap<>(testedLanguages.size());
    for (final String language : testedLanguages) {
      double correctDetections = 0;
      final List<String> languageFullTexts = languageToFullTexts.get(language);
      for (final String text : languageFullTexts) {
        for (String substring : sampleText(language, text, substringLength, sampleSize)) {
          if (Objects.equals(getTopLanguageCode(languageDetector, substring), language)) {
            correctDetections++;
          }
        }
      }
      final double accuracy = correctDetections / (languageFullTexts.size() * sampleSize);
      languageToDetectedAccuracy.put(language, accuracy);
      System.out.printf("Detected accuracy: %s = %s%n", language, accuracy);
    }

    assertEquals(languageToExpectedAccuracy.size(), languageToDetectedAccuracy.size());

    // Generate accuracy report regardless of the upcoming assertions
    writeAccuracyReport(languageToDetectedAccuracy);

    for (Map.Entry<String, Double> detected : languageToDetectedAccuracy.entrySet()) {
      final String targetLanguage = detected.getKey();
      final double expectedAccuracy = languageToExpectedAccuracy.get(targetLanguage);
      final String failureMessage = String.format("FAILED [%s]: ", targetLanguage);

      assertEquals(failureMessage, expectedAccuracy, detected.getValue(), ACCURACY_DELTA);
    }
  }

  private void writeAccuracyReport(Map<String, Double> languageToDetectedAccuracy)
      throws IOException {
    final List<String> row = new ArrayList<>();
    Collections.addAll(
        row,
        dataset,
        profile,
        String.valueOf(substringLength),
        String.valueOf(sampleSize),
        String.valueOf(useAllLanguages));

    for (final String language : ALL_LANGUAGES.split(COMMA_CHARACTER)) {
      row.add(languageToDetectedAccuracy.getOrDefault(language, Double.NaN).toString());
    }
    Files.write(
        Path.of(ACCURACY_REPORT_NAME),
        Collections.singletonList(String.join(COMMA_CHARACTER, row)),
        StandardCharsets.UTF_8,
        StandardOpenOption.APPEND);
  }

  /**
   * Read and parse the test parameters from the accuracies.csv resource.
   *
   * @return the parsed parameters
   */
  @Parameterized.Parameters(
      name = "{0}: profile={1} substringLength={2} sampleSize={3} useAllLanguages={4}")
  public static Collection<Object[]> data() throws IOException {
    final List<Object[]> data = new ArrayList<>();
    try (final BufferedReader bufferedReader = getResourceReader("/accuracies.csv")) {
      // Skip header line
      bufferedReader.readLine();
      while (bufferedReader.ready()) {
        final String line = bufferedReader.readLine();
        final Scanner scanner =
            new Scanner(line).useLocale(Locale.US).useDelimiter(COMMA_CHARACTER);
        data.add(
            new Object[] {
              // dataset
              scanner.next(),
              // profile
              scanner.next(),
              // substringLength
              scanner.nextInt(),
              // sampleSize
              scanner.nextInt(),
              // useAllLanguages
              scanner.nextBoolean(),
              // expectedAccuraciesPerLanguage
              null
            });

        final Map<String, Double> expectedAccuraciesPerLanguage = new HashMap<>();
        for (String language : ALL_LANGUAGES.split(COMMA_CHARACTER)) {
          double expectedAccuracy = scanner.nextDouble();
          if (!Double.isNaN(expectedAccuracy)) {
            expectedAccuraciesPerLanguage.put(language, expectedAccuracy);
          }
        }
        final Object[] parameters = data.get(data.size() - 1);
        parameters[parameters.length - 1] = expectedAccuraciesPerLanguage;
      }
    }
    return data;
  }

  /**
   * Read and parse a multi-language dataset from the given path.
   *
   * @param path resource path, where the file is in tab-separated format with two columns: language
   *     code and text
   * @return a mapping from each language code found in the file to the texts of this language
   */
  private static Map<String, List<String>> readDataset(final String path) throws IOException {
    final Map<String, List<String>> languageToFullTexts = new HashMap<>();

    try (final BufferedReader bufferedReader = getResourceReader(path)) {
      while (bufferedReader.ready()) {
        final String[] csvLineChunks = bufferedReader.readLine().split(TAB_CHARACTER);
        final String language = csvLineChunks[0];
        final String textSubstring = csvLineChunks[1];

        languageToFullTexts.computeIfAbsent(language, k -> new ArrayList<>()).add(textSubstring);
      }
    }
    return languageToFullTexts;
  }

  /** Helper method to open a resource path and return it as a BufferedReader instance. */
  private static BufferedReader getResourceReader(final String path) {
    return new BufferedReader(
        new InputStreamReader(
            Objects.requireNonNull(LanguageDetectorAccuracyTest.class.getResourceAsStream(path)),
            StandardCharsets.UTF_8));
  }

  /**
   * Generate a random sample of substrings from the given text.
   *
   * <p>Sampling is performed uniformly with replacement from the set of substrings of the provided
   * text, ignoring whitespace-only substrings. The random seed is set to a deterministic function
   * of the method's parameters, so repeated calls to this method with the same parameters will
   * return the same sample.
   *
   * @param text the text from which the substring sample is drawn
   * @param substringLength length of each generated substring (set to zero to return a singleton
   *     list with the text -- sampleSize must be 1 in this case)
   * @param sampleSize number of substrings to include in the sample
   * @return the sample (a list of strings)
   */
  private List<String> sampleText(
      final String language, final String text, final int substringLength, final int sampleSize) {
    if (substringLength == 0 && sampleSize == 1) {
      return Collections.singletonList(text);
    }

    final int textLength = text.trim().length();
    if (substringLength > textLength) {
      final String template =
          "Provided [%s] text [%s] length %s is too short for the requested substring length %s";
      throw new IllegalArgumentException(
          String.format(template, language, text, textLength, substringLength));
    }

    final int seed = Objects.hash(text, substringLength, sampleSize);
    final Random rnd = new Random(seed);
    final List<String> sampledTexts = new ArrayList<>(sampleSize);

    while (sampledTexts.size() < sampleSize) {
      int startIndex = rnd.nextInt(textLength - substringLength + 1);
      final String substring = text.substring(startIndex, startIndex + substringLength);
      if (!substring.trim().isEmpty()) {
        sampledTexts.add(substring);
      }
    }
    return sampledTexts;
  }
}
