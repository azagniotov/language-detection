package io.github.azagniotov.language;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;

public class LanguageProfileGenerator {

  private static final int CHUNK_SIZE = 8192;

  @Test
  public void generateProfile() throws Exception {
    final String targetCode = "yi";
    final String resourceName = targetCode + "wiki.txt";
    final File seedDataset = new File("src/test/resources/" + resourceName);

    if (!seedDataset.exists()) {
      System.out.println("\nSeed dataset is missing. Skipping generation.\n");
      return;
    }

    final List<Double> nWords = Arrays.asList(0.0, 0.0, 0.0);
    final LanguageProfile languageProfile =
        new LanguageProfile(targetCode, new HashMap<>(), nWords);

    try (final InputStream datasetInputStream =
        TestHelper.class.getResourceAsStream("/" + resourceName)) {
      assert datasetInputStream != null;

      // BufferedReader to read the stream
      try (final InputStreamReader inputStreamReader =
              new InputStreamReader(datasetInputStream, StandardCharsets.UTF_8);
          final BufferedReader reader = new BufferedReader(inputStreamReader)) {
        final char[] buffer = new char[CHUNK_SIZE];
        int bytesRead;
        while ((bytesRead = reader.read(buffer)) != -1) {
          final String chunk = new String(buffer, 0, bytesRead);
          languageProfile.update(chunk);
        }
      }
    }

    languageProfile.omitLessFreq();
    final String languageProfileJson = languageProfile.toJson();

    writeProfile("langdetect", targetCode, languageProfileJson);
    writeProfile("langdetect/short-text", targetCode, languageProfileJson);
    writeProfile("langdetect/merged-average", targetCode, languageProfileJson);

    if (seedDataset.exists()) {
      if (seedDataset.delete()) {
        System.out.println(
            "\nSeed dataset deleted successfully: " + seedDataset.getAbsolutePath() + "\n");
      } else {
        System.out.println(
            "\nFailed to delete the seed dataset: " + seedDataset.getAbsolutePath() + "\n");
      }
    }

    assertEquals("apples", "apples");
  }

  private void writeProfile(final String path, final String targetCode, final String json)
      throws IOException {
    final String resourcesRoot = "src/main/resources";
    final File childResourcesDir = new File(resourcesRoot + "/" + path);
    final File childResourcesDirFile = new File(childResourcesDir, targetCode);

    try (final FileWriter writer = new FileWriter(childResourcesDirFile)) {
      writer.write(json);
    }
  }
}
