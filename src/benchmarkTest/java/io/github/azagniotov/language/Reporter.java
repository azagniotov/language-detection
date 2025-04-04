package io.github.azagniotov.language;

import static io.github.azagniotov.language.Runner.SUBSET_OF_LANGUAGES;
import static io.github.azagniotov.language.benchmark.ThirdPartyDetector.LANGUAGE_CODE_NONE;

import io.github.azagniotov.language.annotations.GeneratedCodeClassCoverageExclusion;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

@GeneratedCodeClassCoverageExclusion
class Reporter {

  public static final String TABLE_COLUMN_SEPARATOR =
      "|---------------------|---------|---------|---------|---------|---------|---------|---------|";

  Reporter() {}

  /**
   * Prints a formatted summary table of language detection results to standard output.
   *
   * <p>The table summarizes counts for various ISO 639-1 language codes, grouped by dataset (e.g.,
   * "EN", "JA") and detector combinations. Rows within each dataset group are sorted by the count
   * of that dataset's primary language code (descending). Columns represent counts for a predefined
   * set of ISO codes plus "unknown".
   *
   * @param detectorDatasetCounts A nested map containing the raw count data. Structure: {@code
   *     Map<DetectorName, Map<DatasetName, Map<IsoCode, Count>>>} Example: {@code {"default":
   *     {"EN": {"en": 123, "ja": 456}, "JA": {...}}, "optimaize": {...}}} It's assumed that
   *     DatasetNames (e.g., "EN") are upper-case ISO 639-1 codes, and IsoCodes within the innermost
   *     map (e.g., "en") are lower-case.
   * @implNote This method relies on several external components:
   *     <ul>
   *       <li>Constants: {@code SUBSET_OF_LANGUAGES} (comma-separated ISO codes), {@code
   *           TABLE_COLUMN_SEPARATOR}.
   *       <li>Class: {@code ReportRow} to encapsulate row data, assumed to have fields like {@code
   *           rowCanonicalName}, {@code datasetIsoCodeCounts} and a static method {@code
   *           descComparator(String)}.
   *       <li>Helper Methods: {@code groupByDataset(Map)} to transform the input map, and {@code
   *           sanitizeIsoCode(String, Map)} to potentially adjust ISO codes before lookup.
   *     </ul>
   *     The table layout involves:
   *     <ul>
   *       <li>A header row with "Dataset-to-Detector" and sorted ISO codes from {@code
   *           SUBSET_OF_LANGUAGES} + "unknown".
   *       <li>Rows grouped by dataset name (sorted alphabetically).
   *       <li>A separator line printed between different dataset groups (except potentially before
   *           the very first group).
   *       <li>Rows within a dataset group sorted in descending order based on the count for that
   *           dataset's corresponding lower-case ISO code (e.g., rows in the "EN" group are sorted
   *           by "en" count).
   *       <li>Formatted columns using {@code System.out.printf} for alignment.
   *     </ul>
   *
   * @see ReportRow
   * @see #groupByDataset(Map)
   * @see #sanitizeIsoCode(String, Map)
   */
  void printReportTable(
      final Map<String, Map<String, Map<String, Integer>>> detectorDatasetCounts) {
    final String[] isoCodes = (SUBSET_OF_LANGUAGES + ",unknown").split(",");
    Arrays.sort(isoCodes);

    System.out.println(TABLE_COLUMN_SEPARATOR);
    System.out.printf("| %-20s", "Dataset-to-Detector");
    for (final String isoCode : isoCodes) {
      System.out.printf("| %-8s", isoCode);
    }
    System.out.println("|");
    System.out.println(TABLE_COLUMN_SEPARATOR);

    final List<String> sortedDatasetIsoCodes =
        detectorDatasetCounts.entrySet().stream()
            .flatMap(detectorEntry -> detectorEntry.getValue().keySet().stream())
            .distinct()
            .sorted()
            .collect(Collectors.toList());

    final Map<String, List<ReportRow>> groupedByDataset = groupByDataset(detectorDatasetCounts);

    final Set<String> datasetSeparators = new HashSet<>();
    datasetSeparators.add("DE"); // the first in the sorted list

    for (final String datasetIsoCode : sortedDatasetIsoCodes) {

      if (!datasetSeparators.contains(datasetIsoCode)) {
        System.out.println(TABLE_COLUMN_SEPARATOR);
        datasetSeparators.add(datasetIsoCode);
      }

      final List<ReportRow> datasetRows = groupedByDataset.get(datasetIsoCode);
      datasetRows.sort(ReportRow.descCountThenNameAscComparator(datasetIsoCode.toLowerCase()));

      datasetRows.forEach(
          reportRow -> {
            // Print the dataset-detector combo name
            System.out.printf("| %-20s", reportRow.rowCanonicalName);

            for (final String iso639_1Code : isoCodes) {
              final String sanitizedIsCode =
                  sanitizeIsoCode(iso639_1Code, reportRow.datasetIsoCodeCounts);
              final Integer count = reportRow.datasetIsoCodeCounts.getOrDefault(sanitizedIsCode, 0);
              System.out.printf("| %-8d", count);
            }
            System.out.println("|");
          });
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

  /**
   * Transforms a nested map structure into a map where keys are datasets (upper-case ISO codes) and
   * values are lists of ReportRow objects containing corresponding data.
   *
   * @param detectorDatasetCounts The input map with structure: Detector -> Dataset -> IsoCodeCounts
   * @return A map with structure: Dataset -> List<ReportRow>
   */
  static Map<String, List<ReportRow>> groupByDataset(
      final Map<String, Map<String, Map<String, Integer>>> detectorDatasetCounts) {

    return detectorDatasetCounts.entrySet().stream()
        .flatMap(
            detectorEntry -> {
              final String detectorName = detectorEntry.getKey();
              final Map<String, Map<String, Integer>> datasetsIsoCodeCounts =
                  detectorEntry.getValue();

              return datasetsIsoCodeCounts.entrySet().stream()
                  .map(
                      datasetEntry -> {
                        final String datasetName = datasetEntry.getKey(); // e.g., "EN", "JA"
                        final Map<String, Integer> isoCodeCounts = datasetEntry.getValue();

                        // Create a ReportRow for this specific detector/dataset combination
                        return new ReportRow(datasetName, detectorName, isoCodeCounts);
                      });
            })
        .collect(Collectors.groupingBy(ReportRow::getDataset));
  }

  static class ReportRow {

    // "EN" or "JA", etc.
    private final String dataset;

    // "EN-default" or "EN-optimaize"
    private final String rowCanonicalName;

    // Mapping between ISO 639-1 codes to counts
    // {
    //   "en": 12345,
    //   "ja": 8765,
    //   ...
    // }
    private final Map<String, Integer> datasetIsoCodeCounts;

    ReportRow(
        final String dataset,
        final String detector,
        final Map<String, Integer> datasetIsoCodeCounts) {
      this.dataset = Objects.requireNonNull(dataset, "dataset cannot be null");
      this.rowCanonicalName = String.format("%s-%s", dataset, detector);
      this.datasetIsoCodeCounts = new TreeMap<>(datasetIsoCodeCounts);
    }

    String getDataset() {
      return dataset;
    }

    public String getRowCanonicalName() {
      return rowCanonicalName;
    }

    public Map<String, Integer> getDatasetIsoCodeCounts() {
      return datasetIsoCodeCounts;
    }

    @Override
    public String toString() {
      return "ReportRow{"
          + "dataset='"
          + dataset
          + '\''
          + ", rowCanonicalName='"
          + rowCanonicalName
          + '\''
          + ", counts="
          + datasetIsoCodeCounts
          + '}';
    }

    static Comparator<ReportRow> descCountThenNameAscComparator(final String isoCodeToSortBy) {
      Objects.requireNonNull(isoCodeToSortBy, "ISO code to sort by cannot be null");

      // Define the primary key extractor (count for the given ISO code)
      final ToIntFunction<ReportRow> countExtractor =
          row -> row.getDatasetIsoCodeCounts().getOrDefault(isoCodeToSortBy, 0);

      // Define the secondary key extractor (the canonical name)
      final Function<ReportRow, String> nameExtractor = ReportRow::getRowCanonicalName;

      // Build the chained comparator:
      return Comparator
          // Primary sort: Compare by extracted count, ascending...
          .comparingInt(countExtractor)
          // ...then reverse it for descending order.
          .reversed()
          // Secondary sort: If counts are equal, compare by extracted name,
          // ascending (natural String order).
          .thenComparing(nameExtractor);
    }
  }
}
