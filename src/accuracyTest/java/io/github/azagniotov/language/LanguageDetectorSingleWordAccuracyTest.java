package io.github.azagniotov.language;

import static io.github.azagniotov.language.StringConstants.COMMA_CHAR;
import static io.github.azagniotov.language.TestHelper.ACCURACY_DELTA;
import static io.github.azagniotov.language.TestHelper.ALL_LANGUAGES;
import static io.github.azagniotov.language.TestHelper.getResourceReader;
import static io.github.azagniotov.language.TestHelper.getTopLanguageCode;
import static io.github.azagniotov.language.TestHelper.readDataset;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
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
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * This class tests classification accuracy on datasets with single-words, as specified in the
 * accuracies-single-words.csv resource file.
 *
 * <p>Original author and ideas: By @yanirs, <a
 * href="https://github.com/jprante/elasticsearch-langdetect/pull/69">https://github.com/jprante/elasticsearch-langdetect/pull/69</a>
 */
@RunWith(Parameterized.class)
public class LanguageDetectorSingleWordAccuracyTest {

  private static final String HIGH_ACCURACY_SUPPORTED_ISO_CODES = "de,en,es,fr,it";

  private static final String ACCURACY_REPORT_HOME = "./build/reports/accuracy";
  private static final String ACCURACY_REPORT_PATH_TEMPLATE =
      ACCURACY_REPORT_HOME + "/accuracy-single-words-report-%s.csv";
  private static String ACCURACY_REPORT_NAME;

  private static Map<String, Map<String, List<String>>> allDatasets;

  private final String dataset;
  private final String profile;
  private final Map<String, Float> languageToExpectedAccuracy;

  /**
   * Construct a test for classification accuracies on substrings of texts from a single dataset.
   *
   * <p>For each text and substring length, this test generates a sample of substrings (drawn
   * uniformly with replacement from the set of possible substrings of the given length), runs the
   * language identification code, measures the per-language accuracy (percentage of substrings
   * classified correctly), and fails if the accuracy varies by more than {@link
   * TestHelper#ACCURACY_DELTA} from the expected accuracy for the language.
   *
   * @param dataset multi-language dataset name, as read in the setup step (see {@link #setUp()})
   * @param profile profile name parameter to pass to the detection service
   * @param languageToExpectedAccuracy mapping from language code to expected accuracy
   */
  public LanguageDetectorSingleWordAccuracyTest(
      final String dataset,
      final String profile,
      final Map<String, Float> languageToExpectedAccuracy) {
    this.dataset = dataset;
    this.profile = profile;
    this.languageToExpectedAccuracy = Map.copyOf(languageToExpectedAccuracy);
  }

  /**
   * Perform the common set up tasks for tests of this class: read the datasets, and write the
   * header row of the output CSV.
   */
  @BeforeClass
  public static void setUp() throws IOException {
    allDatasets = new HashMap<>();
    allDatasets.put(
        "single-words-capitalized", readDataset("/datasets/single-words-capitalized.tsv"));
    allDatasets.put(
        "single-words-lowercased", readDataset("/datasets/single-words-lowercased.tsv"));

    final File directory = new File(ACCURACY_REPORT_HOME);
    if (!directory.exists()) {
      boolean mkdirs = directory.mkdirs(); // Create the directory if it doesn't exist
      if (mkdirs) {
        System.out.println("Created " + ACCURACY_REPORT_HOME + " directory");
      }
    }

    final String header = "dataset,profile,";
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
    final String languageCodes = configureProfileDependentLanguageCodes();
    final LanguageDetectionSettings configuredSettings =
        LanguageDetectionSettings.fromIsoCodes639_1(languageCodes)
            .withProfile(this.profile)
            .build();

    final LanguageDetectorFactory factory = new LanguageDetectorFactory(configuredSettings);
    final LanguageDetector languageDetector =
        new LanguageDetector(
            factory.getSupportedIsoCodes639_1(),
            factory.getLanguageCorporaProbabilities(),
            factory.getMinNGramLength(),
            factory.getMaxNGramLength());

    final Map<String, List<String>> languageToWords = allDatasets.get(dataset);
    final Set<String> datasetTargetLanguages = new TreeSet<>(languageToWords.keySet());

    // Based on what language codes we passed in the setting,
    // we want to make sure that our target dataset has the same languages
    datasetTargetLanguages.retainAll(configuredSettings.getIsoCodes639_1());

    // Classify the texts and calculate the accuracy for each language
    final Map<String, Float> languageToDetectedAccuracy =
        new HashMap<>(datasetTargetLanguages.size());

    for (final String targetLanguage : datasetTargetLanguages) {
      float correctDetections = 0;
      final List<String> languageAllWords = languageToWords.get(targetLanguage);
      for (final String word : languageAllWords) {
        if (Objects.equals(getTopLanguageCode(languageDetector, word), targetLanguage)) {
          correctDetections++;
        }
      }
      final float accuracy = correctDetections / languageAllWords.size();
      languageToDetectedAccuracy.put(targetLanguage, accuracy);
      // System.out.printf("Detected accuracy: %s = %s%n", targetLanguage, accuracy);
    }

    // To disable a language from being evaluated, we need to set its
    // probability in the CSV as NaN. Then, it will be filtered out.
    assertEquals(languageToExpectedAccuracy.size(), languageToDetectedAccuracy.size());

    // Generate accuracy report regardless of the upcoming assertions
    writeAccuracyReport(languageToDetectedAccuracy);

    for (Map.Entry<String, Float> detected : languageToDetectedAccuracy.entrySet()) {
      final String targetLanguage = detected.getKey();
      final float expectedAccuracy = languageToExpectedAccuracy.get(targetLanguage);
      final String failureMessage = String.format("FAILED [%s]: ", targetLanguage);

      assertEquals(failureMessage, expectedAccuracy, detected.getValue(), ACCURACY_DELTA);
    }
  }

  private String configureProfileDependentLanguageCodes() {
    if (profile.equals("high-accuracy")) {
      return HIGH_ACCURACY_SUPPORTED_ISO_CODES;
    } else if (profile.equals("merged-average")) {
      return ALL_LANGUAGES;
    } else {
      return "NULL";
    }
  }

  private void writeAccuracyReport(final Map<String, Float> languageToDetectedAccuracy)
      throws IOException {
    final List<String> row = new ArrayList<>();
    Collections.addAll(row, dataset, profile);

    for (final String language : ALL_LANGUAGES.split(COMMA_CHAR)) {
      row.add(languageToDetectedAccuracy.getOrDefault(language, Float.NaN).toString());
    }
    Files.write(
        Path.of(ACCURACY_REPORT_NAME),
        Collections.singletonList(String.join(COMMA_CHAR, row)),
        StandardCharsets.UTF_8,
        StandardOpenOption.APPEND);
  }

  /**
   * Read and parse the test parameters from the accuracies.csv resource.
   *
   * @return the parsed parameters
   */
  @Parameterized.Parameters(name = "{0}: profile={1}")
  public static Collection<Object[]> data() throws IOException {
    final List<Object[]> data = new ArrayList<>();
    try (final BufferedReader bufferedReader = getResourceReader("/accuracies-single-words.csv")) {
      // Skip header line
      bufferedReader.readLine();
      while (bufferedReader.ready()) {
        final String line = bufferedReader.readLine();
        final Scanner scanner = new Scanner(line).useLocale(Locale.US).useDelimiter(COMMA_CHAR);
        data.add(
            new Object[] {
              // dataset
              scanner.next(),
              // profile
              scanner.next(),
              // expectedAccuraciesPerLanguage
              null
            });

        final Map<String, Float> expectedAccuraciesPerLanguage = new HashMap<>();
        for (String language : ALL_LANGUAGES.split(COMMA_CHAR)) {
          float expectedAccuracy = scanner.nextFloat();

          // To disable a language from being evaluated, we need to set its
          // probability in the CSV as NaN. Then, it will be filtered out.
          if (!Float.isNaN(expectedAccuracy)) {
            expectedAccuraciesPerLanguage.put(language, expectedAccuracy);
          }
        }
        final Object[] parameters = data.get(data.size() - 1);
        parameters[parameters.length - 1] = expectedAccuraciesPerLanguage;
      }
    }
    return data;
  }
}
