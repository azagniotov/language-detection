package io.github.azagniotov.language;

import static io.github.azagniotov.language.benchmark.ThirdPartyDetector.LANGUAGE_CODE_NONE;
import static java.util.Objects.requireNonNull;

import io.github.azagniotov.language.annotations.GeneratedCodeClassCoverageExclusion;
import io.github.azagniotov.language.benchmark.DetectorImpl;
import io.github.azagniotov.language.benchmark.ThirdPartyDetector;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@GeneratedCodeClassCoverageExclusion
public class Runner {

  private static final String SUBSET_OF_LANGUAGES = "en,ja,fr,de,it,es";

  private static final String ANSI_RESET = "\u001B[0m";
  private static final String ANSI_CYAN = "\u001B[36m";
  private static final String ANSI_BOLD = "\u001B[1m";
  private static final String ANSI_GREEN = "\u001B[32m";
  public static final String TABLE_COLUMN_SEPARATOR =
      "|---------------------|---------|---------|---------|---------|---------|---------|---------|";

  private static boolean verbose = false;
  private static final String DIRECTORY_PATH =
      requireNonNull(Runner.class.getResource("/dataset")).getPath();

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
        "- Datasets parent directory absolute path: "
            + ANSI_BOLD
            + ANSI_CYAN
            + DIRECTORY_PATH
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
        new ConcurrentSkipListMap<>();
    for (final String detector : targetDetectors) {
      final Map<String, Map<String, Integer>> detectionCounts = new ConcurrentSkipListMap<>();
      if (!detectorDatasetCounts.containsKey(detector)) {
        detectorDatasetCounts.put(detector, detectionCounts);
      }
      for (final String targetCode : targetCodes) {
        if (!detectionCounts.containsKey(targetCode.toUpperCase())) {
          detectionCounts.put(targetCode.toUpperCase(), new TreeMap<>());
        }
      }
    }

    final ExecutorService executor = Executors.newFixedThreadPool(1);
    CompletableFuture.allOf(
            CompletableFuture.runAsync(
                () -> detect(detectorDatasetCounts, targetDetectors, targetCodes), executor))
        .thenRun(
            () -> {
              try {
                System.out.println(
                    ANSI_GREEN
                        + "\nAll detection tasks completed successfully!"
                        + ANSI_RESET
                        + "\n");
                printReportTable(detectorDatasetCounts);
                executor.shutdown();
                if (!executor.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                  executor.shutdownNow();
                }
              } catch (final InterruptedException ex) {
                executor.shutdownNow();
                // Re-interrupt the thread to avoid losing its interruption status
                Thread.currentThread().interrupt();
              }
              System.out.println("\nDone!\n");
            });
  }

  private static void detect(
      final Map<String, Map<String, Map<String, Integer>>> detectorDetectionCounts,
      final TreeSet<String> targetDetectors,
      final TreeSet<String> targetCodes) {

    for (final String detector : targetDetectors) {

      final Map<String, Map<String, Integer>> detectionCounts =
          detectorDetectionCounts.get(detector);

      final ThirdPartyDetector thirdPartyDetector =
          ThirdPartyDetector.get(detector, SUBSET_OF_LANGUAGES);

      final long startTime = System.nanoTime();
      for (final String targetCode : targetCodes) {
        System.out.println(detector + " processes dataset [" + targetCode + "]");

        final Path languageDatasetPath =
            Paths.get(String.format("%s/%s", DIRECTORY_PATH, targetCode));
        if (!Files.exists(languageDatasetPath)) {
          System.out.println("\nLanguage dataset path is missing. Skipping detection.\n");
          return;
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
                            System.out.println(
                                String.format(
                                    "Misdetected for %s: %s => %s",
                                    targetCode.toUpperCase(), path, key));
                          }
                        }
                        final Map<String, Integer> languageCount =
                            detectionCounts.get(targetCode.toUpperCase());
                        languageCount.put(key, languageCount.getOrDefault(key, 0) + 1);
                      } catch (Exception e) {
                        throw new RuntimeException(e);
                      }
                    });
          }

        } catch (IOException e) {
          throw new UncheckedIOException(e);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      final long endTime = System.nanoTime();
      final long elapsedTime = endTime - startTime;
      System.out.println(
          ANSI_GREEN
              + "Detector "
              + ANSI_RESET
              + ANSI_BOLD
              + ANSI_CYAN
              + detector
              + ANSI_RESET
              + ANSI_GREEN
              + " total runtime: "
              + ANSI_RESET
              + ANSI_BOLD
              + ANSI_GREEN
              + formatElapsedTime(elapsedTime)
              + ANSI_RESET
              + "\n");
    }
  }

  public static String formatElapsedTime(long elapsedTimeNano) {
    double elapsedTimeSeconds = (double) elapsedTimeNano / 1_000_000_000.0;
    long milliseconds = (long) ((elapsedTimeSeconds - (long) elapsedTimeSeconds) * 1000);

    return String.format("%d seconds and %03d millis", (long) elapsedTimeSeconds, milliseconds);
  }

  private static void printReportTable(
      final Map<String, Map<String, Map<String, Integer>>> detectorDatasetCounts) {
    final String[] isoCodes = (SUBSET_OF_LANGUAGES + ",unknown").split(",");

    System.out.println(TABLE_COLUMN_SEPARATOR);
    System.out.printf("| %-20s", "Dataset-to-Detector");
    for (final String isoCode : isoCodes) {
      System.out.printf("| %-8s", isoCode);
    }
    System.out.println("|");
    System.out.println(TABLE_COLUMN_SEPARATOR);

    final List<String> detectorKeys =
        detectorDatasetCounts.entrySet().stream()
            .flatMap(
                detectorEntry ->
                    detectorEntry.getValue().keySet().stream()
                        .map(dataset -> dataset + "-" + detectorEntry.getKey()))
            .sorted()
            .collect(Collectors.toList());

    final Set<String> datasetSeparators = new HashSet<>();
    datasetSeparators.add("DE"); // the first in the sorted list

    for (final String datasetToDetectorName : detectorKeys) {
      final String[] split = datasetToDetectorName.split("-", 2);
      final String dataset = split[0];
      final String detectorName = split[1];

      if (!datasetSeparators.contains(dataset)) {
        System.out.println(TABLE_COLUMN_SEPARATOR);
        datasetSeparators.add(dataset);
      }

      final Map<String, Map<String, Integer>> allDatasetCounts =
          detectorDatasetCounts.get(detectorName);
      final Map<String, Integer> datasetCounts = allDatasetCounts.get(dataset);

      // Print the detector-dataset combo
      System.out.printf("| %-20s", dataset + "-" + detectorName);

      // Print the counts for each language
      for (final String iso639_1Code : isoCodes) {
        final String sanitizedIsCode = sanitizeIsoCode(iso639_1Code, datasetCounts);
        final Integer count = datasetCounts.getOrDefault(sanitizedIsCode, 0);
        System.out.printf("| %-8d", count);
      }
      System.out.println("|");
    }

    System.out.println(TABLE_COLUMN_SEPARATOR);
  }

  private static String sanitizeIsoCode(
      final String iso639_1Code, final Map<String, Integer> datasetCounts) {
    final String sanitizedIsCode;
    if (iso639_1Code.equals("unknown")) {
      if (datasetCounts.containsKey(LANGUAGE_CODE_NONE)) {
        sanitizedIsCode = LANGUAGE_CODE_NONE;
      } else {
        sanitizedIsCode = iso639_1Code;
      }
    } else {
      sanitizedIsCode = iso639_1Code;
    }
    return sanitizedIsCode;
  }
}
