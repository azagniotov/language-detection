package io.github.azagniotov.language;

import static io.github.azagniotov.language.Runner.SUBSET_OF_LANGUAGES;
import static io.github.azagniotov.language.benchmark.ThirdPartyDetector.LANGUAGE_CODE_NONE;

import io.github.azagniotov.language.annotations.GeneratedCodeClassCoverageExclusion;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@GeneratedCodeClassCoverageExclusion
class Reporter {

  public static final String TABLE_COLUMN_SEPARATOR =
      "|---------------------|---------|---------|---------|---------|---------|---------|---------|";

  Reporter() {}

  void printReportTable(
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

  private String sanitizeIsoCode(
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
