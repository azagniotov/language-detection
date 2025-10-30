package io.github.azagniotov.language;

import static io.github.azagniotov.language.benchmark.ThirdPartyDetector.detectorFor;
import static java.lang.String.format;
import static java.nio.file.Paths.get;

import io.github.azagniotov.language.annotations.GeneratedCodeClassCoverageExclusion;
import io.github.azagniotov.language.benchmark.DetectorImpl;
import io.github.azagniotov.language.benchmark.ThirdPartyDetector;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

@GeneratedCodeClassCoverageExclusion
public class Runner {

  static final String SUBSET_OF_LANGUAGES = "en,ja,fr,de,it,es";

  private static final String ANSI_RESET = "\u001B[0m";
  private static final String ANSI_CYAN = "\u001B[36m";
  private static final String ANSI_BOLD = "\u001B[1m";
  private static final String ANSI_GREEN = "\u001B[32m";

  private static boolean verbose = false;
  private static final Path DATASET_DIRECTORY = get("build/benchmarkTestDataset/dataset");

  public static void main(final String[] args) {

    final String[] detectors =
        args[0].equals("all") ? DetectorImpl.valuesLowerCased : args[0].split(",");
    final String iso639_1Codes = args[1].equals("all") ? SUBSET_OF_LANGUAGES : args[1];
    verbose = args.length == 3 && Boolean.parseBoolean(args[2]);

    System.out.println(ANSI_BOLD + ANSI_CYAN + "\nUser-provided arguments:" + ANSI_RESET);
    System.out.println(
        "- Language detector: " + ANSI_BOLD + ANSI_CYAN + String.join(",", detectors) + ANSI_RESET);
    System.out.println(
        "- Directory names using ISO 639-1 codes for language-specific datasets: "
            + ANSI_BOLD
            + ANSI_CYAN
            + iso639_1Codes
            + ANSI_RESET);
    System.out.println(
        "- Datasets parent directory path: "
            + ANSI_BOLD
            + ANSI_CYAN
            + DATASET_DIRECTORY
            + ANSI_RESET);
    System.out.println("- Verbose mode: " + ANSI_BOLD + ANSI_CYAN + verbose + ANSI_RESET);

    System.out.println(
        "\n =============================== "
            + ANSI_GREEN
            + "STARTING"
            + ANSI_RESET
            + " =============================== ");

    final TreeSet<String> targetCodes = new TreeSet<>(Set.of(iso639_1Codes.split(",")));
    final TreeSet<String> targetDetectors = new TreeSet<>(Set.of(detectors));

    System.out.println(
        "\nWill process datasets for "
            + ANSI_BOLD
            + ANSI_CYAN
            + targetCodes.size()
            + ANSI_RESET
            + " ISO 639-1 code names: "
            + targetCodes
            + "\n");

    final Map<String, Map<String, Map<String, Integer>>> detectorDatasetCounts =
        initCountMap(targetDetectors, targetCodes);

    benchmark(detectorDatasetCounts, targetDetectors, targetCodes);

    System.out.println(
        ANSI_GREEN
            + "\nAll detection tasks completed successfully! Detection results:"
            + ANSI_RESET
            + "\n");
    new Reporter().printReportTable(detectorDatasetCounts);
    System.out.println("\nDone!\n");
  }

  private static Map<String, Map<String, Map<String, Integer>>> initCountMap(
      final Set<String> targetDetectors, final Set<String> targetCodes) {

    final Map<String, Map<String, Map<String, Integer>>> detectorDatasetCounts = new HashMap<>();

    for (final String detector : targetDetectors) {
      final Map<String, Map<String, Integer>> detectedDatasetCounts = new HashMap<>();
      for (final String targetCode : targetCodes) {
        detectedDatasetCounts.put(targetCode.toUpperCase(), new HashMap<>());
      }
      detectorDatasetCounts.put(detector, detectedDatasetCounts);
    }
    return detectorDatasetCounts;
  }

  private static void benchmark(
      final Map<String, Map<String, Map<String, Integer>>> detectorDetectionCounts,
      final TreeSet<String> targetDetectors,
      final TreeSet<String> targetCodes) {

    for (final String detectorName : targetDetectors) {

      final Map<String, Map<String, Integer>> detectionCounts =
          detectorDetectionCounts.get(detectorName);
      final ThirdPartyDetector thirdPartyDetector = detectorFor(detectorName, SUBSET_OF_LANGUAGES);

      final long startTime = System.nanoTime();
      for (final String targetCode : targetCodes) {
        detectDataset(targetCode, thirdPartyDetector, detectionCounts);
      }
      final long endTime = System.nanoTime();
      final long elapsedTime = endTime - startTime;
      System.out.println(
          ANSI_GREEN
              + "Detector "
              + ANSI_RESET
              + ANSI_BOLD
              + ANSI_CYAN
              + detectorName
              + ANSI_RESET
              + ANSI_GREEN
              + " completed in "
              + ANSI_RESET
              + ANSI_BOLD
              + ANSI_GREEN
              + formatElapsedTime(elapsedTime)
              + ANSI_RESET
              + "\n");
    }
  }

  private static void detectDataset(
      final String targetCode,
      final ThirdPartyDetector thirdPartyDetector,
      final Map<String, Map<String, Integer>> detectionCounts) {
    System.out.println(thirdPartyDetector.name() + " processes dataset [" + targetCode + "]");

    final Path languageDatasetPath = DATASET_DIRECTORY.resolve(targetCode);
    if (!Files.exists(languageDatasetPath)) {
      System.out.println("\nLanguage dataset path is missing. Skipping detection.\n");
      throw new UncheckedIOException(new IOException("Language dataset path is missing."));
    }

    try {
      try (final Stream<Path> paths = Files.list(languageDatasetPath)) {
        paths
            .filter(path -> path.toString().endsWith(".txt"))
            .forEach(
                path -> {
                  try {
                    final String key = thirdPartyDetector.detect(Files.readString(path));
                    if (!key.equals(targetCode)) {
                      if (verbose) {
                        System.out.printf(
                            "Misdetected for %s: %s => %s%n", targetCode.toUpperCase(), path, key);
                      }
                    }
                    final Map<String, Integer> countPerIsoCode =
                        detectionCounts.get(targetCode.toUpperCase());
                    countPerIsoCode.put(key, countPerIsoCode.getOrDefault(key, 0) + 1);
                  } catch (Exception e) {
                    throw new RuntimeException("Failed processing: " + path, e);
                  }
                });
      }

    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static String formatElapsedTime(final long elapsedTimeNano) {
    final double elapsedSeconds = (double) elapsedTimeNano / 1_000_000_000.0;
    final long milliseconds = (long) ((elapsedSeconds - (long) elapsedSeconds) * 1000);

    return format("%d seconds and %03d millis", (long) elapsedSeconds, milliseconds);
  }
}
