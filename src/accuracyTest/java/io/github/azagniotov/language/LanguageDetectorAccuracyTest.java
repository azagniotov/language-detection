package io.github.azagniotov.language;

import static io.github.azagniotov.language.LanguageDetectionSettings.ALL_SUPPORTED_ISO_CODES_639_1;
import static io.github.azagniotov.language.StringConstants.COMMA;
import static io.github.azagniotov.language.TestHelper.ACCURACY_DELTA;
import static io.github.azagniotov.language.TestHelper.getResourceReader;
import static io.github.azagniotov.language.TestHelper.getTopLanguageCode;
import static io.github.azagniotov.language.TestHelper.readDataset;
import static io.github.azagniotov.language.TestHelper.resetLanguageDetectorFactoryInstance;
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
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
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

  private static final String SMALL_LANG_SUBSET = "en,ja,de,es,fr,it";

  private static final String ACCURACY_REPORT_HOME = "./build/reports/accuracy";
  private static final String ACCURACY_REPORT_PATH_TEMPLATE =
      ACCURACY_REPORT_HOME + "/accuracy-report-%s.csv";
  private static String ACCURACY_REPORT_NAME;

  private static Map<String, Map<String, List<String>>> allDatasets;

  private final String dataset;
  private final int substringLength;
  private final int sampleSize;
  private final String profilesHome;
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
   * @param profilesHome profiles home directory parameter to pass to the detection service
   * @param substringLength substring length to test (see {@link #sampleText(String, String, int,
   *     int)})
   * @param sampleSize number of substrings to test (see {@link #sampleText(String, String, int,
   *     int)})
   * @param languageToExpectedAccuracy mapping from language code to expected accuracy
   */
  public LanguageDetectorAccuracyTest(
      final String dataset,
      final String profilesHome,
      final int substringLength,
      final int sampleSize,
      final Map<String, Float> languageToExpectedAccuracy) {
    this.dataset = dataset;
    this.profilesHome = profilesHome;
    this.substringLength = substringLength;
    this.sampleSize = sampleSize;
    this.languageToExpectedAccuracy = Map.copyOf(languageToExpectedAccuracy);
  }

  /**
   * Perform the common set up tasks for tests of this class: read the datasets, and write the
   * header row of the output CSV.
   */
  @BeforeClass
  public static void beforeClass() throws IOException {
    allDatasets = new HashMap<>();
    allDatasets.put("udhr", readDataset("/datasets/udhr.tsv"));
    allDatasets.put("tatoeba", readDataset("/datasets/tatoeba-short-sentences.tsv"));
    allDatasets.put("tatoeba-mixed", readDataset("/datasets/tatoeba-mixed-sentences.tsv"));
    allDatasets.put("wordpress-translations", readDataset("/datasets/wordpress-translations.tsv"));

    final File directory = new File(ACCURACY_REPORT_HOME);
    if (!directory.exists()) {
      boolean mkdirs = directory.mkdirs(); // Create the directory if it doesn't exist
      if (mkdirs) {
        System.out.println("Created " + ACCURACY_REPORT_HOME + " directory");
      }
    }

    final String header = "dataset,profilesHome,substringLength,sampleSize,";
    final long reportTimestamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    ACCURACY_REPORT_NAME = String.format(ACCURACY_REPORT_PATH_TEMPLATE, reportTimestamp);
    Files.write(
        Path.of(ACCURACY_REPORT_NAME),
        Collections.singletonList(header + ALL_SUPPORTED_ISO_CODES_639_1),
        StandardCharsets.UTF_8);
  }

  @Before
  public void setUp() throws Exception {
    resetLanguageDetectorFactoryInstance();
  }

  /** Run the test according to the parameters passed to the constructor. */
  @Test
  public void simulation() throws Exception {
    final String languageCodes = configureProfileDependentLanguageCodes();

    // small-lang-subset is not a real directory under resources
    final String canonicalProfile =
        profilesHome.equals("small-lang-subset") ? "profiles" : profilesHome;

    final LanguageDetectionSettings configuredSettings =
        LanguageDetectionSettings.fromIsoCodes639_1(languageCodes)
            .withProfilesHome(canonicalProfile)
            .build();

    final LanguageDetectorFactory factory = new LanguageDetectorFactory(configuredSettings);
    final LanguageDetector languageDetector =
        new LanguageDetector(
            factory.getModel(),
            factory.getSupportedIsoCodes639_1(),
            factory.getLanguageCorporaProbabilities(),
            factory.getMinNGramLength(),
            factory.getMaxNGramLength());

    final Map<String, List<String>> languageToFullTexts = allDatasets.get(dataset);
    final Set<String> datasetTargetLanguages = new TreeSet<>(languageToFullTexts.keySet());

    // Based on what language codes we passed in the setting,
    // we want to make sure that our target dataset has the same languages
    datasetTargetLanguages.retainAll(configuredSettings.getIsoCodes639_1());

    // Classify the texts and calculate the accuracy for each language
    final Map<String, Float> languageToDetectedAccuracy =
        new HashMap<>(datasetTargetLanguages.size());

    for (final String targetLanguage : datasetTargetLanguages) {
      float correctDetections = 0.0f;
      final List<String> allLanguageTexts = languageToFullTexts.get(targetLanguage);
      for (final String fullText : allLanguageTexts) {
        for (String substring : sampleText(targetLanguage, fullText, substringLength, sampleSize)) {
          if (Objects.equals(getTopLanguageCode(languageDetector, substring), targetLanguage)) {
            correctDetections++;
          }
        }
      }
      final float accuracy = correctDetections / (allLanguageTexts.size() * sampleSize);
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
    // This decision tree has been created by the original author,
    // @yanirs, who wanted to distinguish which set of languages to use.
    // This can be splified, but I am keeping this for posterity.
    //
    // Any new language configuration that will be added to the
    // accuracies.csv will not be a part of the following decision tree.
    // The following profiles were not a part of @yanirs's work.
    if (profilesHome.equals("small-lang-subset")) {
      return SMALL_LANG_SUBSET;
    } else {
      return ALL_SUPPORTED_ISO_CODES_639_1;
    }
  }

  private void writeAccuracyReport(final Map<String, Float> languageToDetectedAccuracy)
      throws IOException {
    final List<String> row = new ArrayList<>();
    Collections.addAll(
        row, dataset, profilesHome, String.valueOf(substringLength), String.valueOf(sampleSize));

    for (final String language : ALL_SUPPORTED_ISO_CODES_639_1.split(COMMA)) {
      row.add(languageToDetectedAccuracy.getOrDefault(language, Float.NaN).toString());
    }
    Files.write(
        Path.of(ACCURACY_REPORT_NAME),
        Collections.singletonList(String.join(COMMA, row)),
        StandardCharsets.UTF_8,
        StandardOpenOption.APPEND);
  }

  /**
   * Read and parse the test parameters from the accuracies.csv resource.
   *
   * @return the parsed parameters
   */
  @Parameterized.Parameters(name = "{0}: profilesHome={1} substringLength={2} sampleSize={3}")
  public static Collection<Object[]> data() throws IOException {
    final List<Object[]> data = new ArrayList<>();
    try (final BufferedReader bufferedReader = getResourceReader("/accuracies.csv")) {
      // Skip header line
      bufferedReader.readLine();
      while (bufferedReader.ready()) {
        final String line = bufferedReader.readLine();
        final Scanner scanner = new Scanner(line).useLocale(Locale.US).useDelimiter(COMMA);
        data.add(
            new Object[] {
              // dataset
              scanner.next(),
              // profilesHome
              scanner.next(),
              // substringLength
              scanner.nextInt(),
              // sampleSize
              scanner.nextInt(),
              // expectedAccuraciesPerLanguage
              null
            });

        final Map<String, Float> expectedAccuraciesPerLanguage = new HashMap<>();
        for (String language : ALL_SUPPORTED_ISO_CODES_639_1.split(COMMA)) {
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

  /**
   * Generate a random sample of substrings from the given text.
   *
   * <p>Sampling is performed uniformly with replacement from the set of substrings of the provided
   * text, ignoring whitespace-only substrings. The random seed is set to a deterministic function
   * of the method's parameters, so repeated calls to this method with the same parameters will
   * return the same sample.
   *
   * @param text the text from which the substring sample is drawn
   * @param configuredSubstringLength length of each generated substring (set to zero to return a
   *     singleton list with the text -- sampleSize must be 1 in this case)
   * @param sampleSize number of substrings to include in the sample
   * @return the sample (a list of strings)
   */
  private List<String> sampleText(
      final String language,
      final String text,
      final int configuredSubstringLength,
      final int sampleSize) {
    if (configuredSubstringLength == 0 && sampleSize == 1) {
      return Collections.singletonList(text);
    }

    final int textLength = text.trim().length();
    final int minSubstringLength = Math.min(textLength, configuredSubstringLength);
    //    if (configuredSubstringLength > textLength) {
    //      final String template =
    //          "Provided [%s] text [%s] length %s is too short for the requested substring length
    // %s";
    //      throw new IllegalArgumentException(
    //          String.format(template, language, text, textLength, configuredSubstringLength));
    //    }

    final int seed = Objects.hash(text, minSubstringLength, sampleSize);
    final Random rnd = new Random(seed);
    final List<String> sampledTexts = new ArrayList<>(sampleSize);

    while (sampledTexts.size() < sampleSize) {
      int startIndex = rnd.nextInt(textLength - minSubstringLength + 1);
      final String substring = text.substring(startIndex, startIndex + minSubstringLength);
      if (!substring.trim().isEmpty()) {
        sampledTexts.add(substring);
      }
    }
    return sampledTexts;
  }
}
