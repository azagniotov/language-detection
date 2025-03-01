package io.github.azagniotov.language;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

/** */
public class LanguageProfilesGZipped {

  public static void main(String[] args) {
    String resourceDirectory = "src/main/resources/merged-average"; // Adjust if needed.
    Path profilesPath = Paths.get(resourceDirectory);

    if (!Files.exists(profilesPath) || !Files.isDirectory(profilesPath)) {
      System.err.println("Profiles directory not found: " + profilesPath);
      return;
    }

    try {
      Files.list(profilesPath)
          .filter(Files::isRegularFile)
          .forEach(
              filePath -> {
                String fileName = filePath.getFileName().toString();
                if (isValidIso6391(fileName)) {
                  gzipFile(filePath);
                }
              });
      System.out.println("Gzip operation completed.");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static boolean isValidIso6391(String fileName) {
    return true;
  }

  private static void gzipFile(final Path filePath) {
    final String inputFilePath = filePath.toString();
    final String outputFilePath = inputFilePath + ".gz";

    try (final FileInputStream fis = new FileInputStream(inputFilePath);
        final FileOutputStream fos = new FileOutputStream(outputFilePath);
        final GZIPOutputStream gzipOS =
            new GZIPOutputStream(fos) {
              {
                def.setLevel(Deflater.BEST_COMPRESSION);
              }
            }) {

      final byte[] buffer = new byte[4096];
      while (true) {
        final int len = fis.read(buffer);
        if (len == -1) {
          break;
        }
        gzipOS.write(buffer, 0, len);
      }
      System.out.println("Gzipped: " + filePath.getFileName());

      // Optionally delete the original file after successful compression.
      Files.delete(filePath);

    } catch (IOException e) {
      System.err.println("Error gzipping " + filePath.getFileName() + ": " + e.getMessage());
    }
  }
}
