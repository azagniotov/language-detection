package io.github.azagniotov.language;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Warning. Heads up. Attention.
 *
 * <p>Tests in the current class invoke the public API of {@link LanguageDetectionOrchestrator},
 * which internally calls the {@link LanguageDetectorFactory#detector(LanguageDetectionSettings)},
 * which caches the singleton instance of the created {@link LanguageDetectorFactory} which caches
 * the loaded ISO codes and profiles.
 *
 * <p>With that in mind, please make sure that all tests make use of the same
 * LanguageDetectionSettings (which has the same configured ISO codes) to avoid flaky tests due to
 * the cached singleton instance of the {@link LanguageDetectorFactory}.
 */
@RunWith(Parameterized.class)
public class LanguageDetectionOrchestratorPerformanceTest {

  private static final String FILE_CONTENT;

  static {
    try (final InputStream datasetInputStream =
        TestHelper.class.getResourceAsStream("/large.japanese.txt")) {
      assert datasetInputStream != null;
      FILE_CONTENT = new String(datasetInputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static final String ISO_CODES = "en, ja, es, fr, de, it, zh-cn, af, nl, ko";
  private final String testName;
  private final int iterations;
  private final int maxTextChars;

  public LanguageDetectionOrchestratorPerformanceTest(
      final String testName, final int iterations, final int maxTextChars) {
    this.testName = testName;
    this.iterations = iterations;
    this.maxTextChars = maxTextChars;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {"dry-run-warmup", 2_500, 5_000},
          {"solr-max-chars", 5_000, 20_000}, /* 20k is Solr default    */
          {"library-max-chars-7k", 5_000, 7_000},
          {"library-max-chars-3k", 5_000, 3_000},
          {"library-max-chars-1k", 5_000, 1_000}
        });
  }

  @Test
  public void testLargeJapaneseText() throws Exception {
    final LanguageDetectionSettings settings =
        LanguageDetectionSettings.fromIsoCodes639_1(ISO_CODES)
            .withMaxTextChars(this.maxTextChars)
            .build();
    final LanguageDetectionOrchestrator orchestrator = new LanguageDetectionOrchestrator(settings);

    // Causes LanguageDetectorFactory instance to be created
    // and language profiles are loaded and cached.
    orchestrator.detect(FILE_CONTENT);

    final Instant start = Instant.now();
    for (int idx = 0; idx < iterations; idx++) {
      orchestrator.detect(FILE_CONTENT);
    }
    final Instant stop = Instant.now();

    final Duration duration = Duration.between(start, stop);
    final int seconds = duration.toSecondsPart();
    final int millis = duration.toMillisPart();

    System.out.printf(
        "\nDEBUG: [test=%s, iterations=%s] took %s seconds, %s millis\n",
        this.testName, this.iterations, seconds, millis);

    final Language language = orchestrator.detect(FILE_CONTENT);
    assertEquals("ja", language.getIsoCode639_1());
  }
}
