package io.github.azagniotov.language;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;

public class LanguageProfileGenerator {

    @Test
    public void generateProfile() throws Exception {
        final String targetCode = "ga";
        final String resourceName = targetCode + "wiki.txt";
        final File seedDataset = new File("src/test/resources/" + resourceName);

        if (!seedDataset.exists()) {
            System.out.println("\nSeed dataset is missing. Skipping generation.\n");
            return;
        }

        final List<Double> nWords = Arrays.asList(0.0, 0.0, 0.0);
        final LanguageProfile languageProfile = new LanguageProfile(targetCode, new HashMap<>(), nWords);

        try (final InputStream datasetInputStream = TestHelper.class.getResourceAsStream("/" + resourceName)) {
            assert datasetInputStream != null;
            final String dataset = new String(datasetInputStream.readAllBytes(), StandardCharsets.UTF_8);
            languageProfile.update(dataset);
        }

        languageProfile.omitLessFreq();
        final String languageProfileJson = languageProfile.toJson();

        writeProfile("langdetect", targetCode, languageProfileJson);
        writeProfile("langdetect/short-text", targetCode, languageProfileJson);
        writeProfile("langdetect/merged-average", targetCode, languageProfileJson);

        if (seedDataset.exists()) {
            if (seedDataset.delete()) {
                System.out.println("\nSeed dataset deleted successfully: " + seedDataset.getAbsolutePath() + "\n");
            } else {
                System.out.println("\nFailed to delete the seed dataset: " + seedDataset.getAbsolutePath() + "\n");
            }
        }

        assertEquals("apples", "apples");
    }

    private void writeProfile(final String path, final String targetCode, final String json) throws IOException {
        final String resourcesRoot = "src/main/resources";
        final File childResourcesDir = new File(resourcesRoot + "/" + path);
        final File childResourcesDirFile = new File(childResourcesDir, targetCode);

        try (final FileWriter writer = new FileWriter(childResourcesDirFile)) {
            writer.write(json);
        }
    }
}
