package io.github.azagniotov.language;

import io.github.azagniotov.language.annotations.GeneratedCodeClassCoverageExclusion;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@GeneratedCodeClassCoverageExclusion
public class Main {

  private static final Map<String, Map<String, Integer>> detectionCounts =
      new ConcurrentSkipListMap<>();

  private static final String SUBSET_OF_LANGUAGES = "en,ja,fr,de,it,es";
  private static final int DEFAULT_MAX_CHARS = 2000;

  private static LanguageDetectionOrchestrator languageDetectionOrchestrator;

  private static final String ANSI_RESET = "\u001B[0m";
  private static final String ANSI_CYAN = "\u001B[36m";
  private static final String ANSI_BOLD = "\u001B[1m";
  private static final String ANSI_GREEN = "\u001B[32m";
  private static final String ANSI_YELLOW = "\u001B[33m";

  private static boolean verbose = false;

  public static void main(final String[] args) {
    if (args.length < 3) {
      System.out.println(
          ANSI_BOLD
              + ANSI_YELLOW
              + "\nPlease provide exactly three (3) arguments separated by a single space caracter:\n"
              + ANSI_RESET);
      System.out.println("- number of workers as an integer");
      System.out.println(
          "- an ISO 639-1 code CSV string which are directory names for language-specific datasets.");
      System.out.println(
          "- An absolute path to a parent directory in the local filesystem that holds subdirectories for each language.");
      System.out.println(
          "\nExample: java -jar <PATH_TO_JAR> "
              + ANSI_BOLD
              + ANSI_YELLOW
              + "2 en,ja /Users/aschwarzenegger/datasets\n"
              + ANSI_RESET);
      System.exit(1);
    }

    final int numberOfWorkers = Integer.parseInt(args[0]);
    final String iso639_1Codes = args[1];
    final String directoryPath = args[2];
    verbose = args.length == 4 && Boolean.parseBoolean(args[3]);

    System.out.println(ANSI_BOLD + ANSI_CYAN + "\nUser-provided arguments:" + ANSI_RESET);
    System.out.println(
        "- Number of workers: " + ANSI_BOLD + ANSI_CYAN + numberOfWorkers + ANSI_RESET);
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
            + directoryPath
            + ANSI_RESET);
    System.out.println("- Verbose mode: " + ANSI_BOLD + ANSI_CYAN + verbose + ANSI_RESET);

    System.out.println(
        "\n =============================== "
            + ANSI_GREEN
            + "STARTING"
            + ANSI_RESET
            + " =============================== ");

    final TreeSet<String> targetCodes = new TreeSet<>(Set.of(iso639_1Codes.split(",")));
    System.out.println(
        "\nWill process datasets for "
            + ANSI_BOLD
            + ANSI_CYAN
            + targetCodes.size()
            + ANSI_RESET
            + " ISO 639-1 code names: "
            + targetCodes);
    System.out.println(
        "Non-configurable max text chars limit set to "
            + ANSI_BOLD
            + ANSI_CYAN
            + DEFAULT_MAX_CHARS
            + ANSI_RESET
            + "\n");

    final LanguageDetectionSettings settings =
        LanguageDetectionSettings.fromIsoCodes639_1(SUBSET_OF_LANGUAGES)
            .withMininumCertainty(0.1)
            .withClassifyChineseAsJapanese()
            .withMaxTextChars(DEFAULT_MAX_CHARS)
            .build();
    languageDetectionOrchestrator = LanguageDetectionOrchestrator.fromSettings(settings);

    for (final String targetCode : targetCodes) {
      if (!detectionCounts.containsKey(targetCode.toUpperCase())) {
        detectionCounts.put(targetCode.toUpperCase(), new TreeMap<>());
      }
    }

    final long startTime = System.nanoTime();
    final ExecutorService executor = Executors.newFixedThreadPool(numberOfWorkers);
    CompletableFuture.allOf(
            targetCodes.stream()
                .map(
                    targetCode ->
                        CompletableFuture.runAsync(
                            () -> detect(targetCode, directoryPath), executor))
                .toArray(CompletableFuture[]::new))
        .thenRun(
            () -> {
              try {
                final long endTime = System.nanoTime();
                final long elapsedTime = endTime - startTime;
                System.out.println(
                    ANSI_GREEN + "\nAll detection tasks completed successfully!" + ANSI_RESET);
                System.out.println(
                    ANSI_GREEN
                        + "Total runtime: "
                        + ANSI_RESET
                        + ANSI_BOLD
                        + ANSI_GREEN
                        + formatElapsedTime(elapsedTime)
                        + ANSI_RESET
                        + ANSI_GREEN
                        + ". Detection results:\n"
                        + ANSI_RESET);
                printLanguageCounts();
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

  private static void detect(final String targetCode, final String directoryPath) {
    System.out.println("Starting processing for: [" + targetCode + "]");
    final String languageDatasetPath = String.format("%s/%s", directoryPath, targetCode);

    if (languageDatasetPath.trim().isEmpty()) {
      System.out.println("\nLanguage dataset path is missing. Skipping detection.\n");
    }

    try {
      try (final Stream<Path> paths = Files.list(Paths.get(languageDatasetPath))) {
        paths
            .filter(path -> path.toString().endsWith(".txt"))
            .forEach(
                path -> {
                  try {
                    final Language detected =
                        languageDetectionOrchestrator.detect(Files.readString(path));
                    final String key = detected.getIsoCode639_1();
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
                  } catch (IOException e) {
                    throw new UncheckedIOException(e);
                  }
                });
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static String formatElapsedTime(long elapsedTimeNano) {
    double elapsedTimeSeconds = (double) elapsedTimeNano / 1_000_000_000.0;
    long milliseconds = (long) ((elapsedTimeSeconds - (long) elapsedTimeSeconds) * 1000);

    return String.format("%d seconds and %03d millis", (long) elapsedTimeSeconds, milliseconds);
  }

  private static void printLanguageCounts() {

    final Map<String, Integer> columnWidths = calculateColumnWidths();

    System.out.println("{");
    detectionCounts.forEach(
        (key, value) -> {
          System.out.print("  Dataset-" + key + " : { ");
          final StringBuilder innerMapString = new StringBuilder();
          value.forEach(
              (innerKey, innerValue) -> {
                final String formattedValue =
                    String.format(
                        "%-" + columnWidths.get(innerKey) + "s", innerKey + "=" + innerValue);
                innerMapString.append(formattedValue).append(", ");
              });
          if (innerMapString.length() > 2) {
            innerMapString.delete(innerMapString.length() - 2, innerMapString.length());
          }
          System.out.println(innerMapString + "}");
        });
    System.out.println("}");
  }

  private static Map<String, Integer> calculateColumnWidths() {
    final Map<String, Integer> columnWidths = new HashMap<>();
    detectionCounts
        .values()
        .forEach(
            innerMap ->
                innerMap.forEach(
                    (key, value) -> {
                      final int width = (key + "=" + value).length() + 1;
                      columnWidths.merge(key, width, Math::max);
                    }));
    return columnWidths;
  }
}
