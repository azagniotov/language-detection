package io.github.azagniotov.language;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import util.ZstdUtils;

/** */
public class LanguageProfilesAsProtobufsWithZstd {

  public static void main(String[] args) {
    String resourceDirectory = "src/main/resources/profiles";
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
                final String fileName = filePath.getFileName().toString();
                if (isValidZstdArchive(fileName)) {
                  serializeAndCompress(filePath);
                }
                //                if (isValidZstdProtoArchive(fileName)) {
                //                  deserializeAndUncompress(filePath);
                //                }
                //                if (isValidJson(fileName)) {
                //                  compressJson(filePath);
                //                }
              });
      System.out.println("Gzip operation completed.");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static boolean isValidZstdArchive(String fileName) {
    return !fileName.endsWith(".pb.zst");
  }

  private static boolean isValidZstdProtoArchive(final String fileName) {
    return fileName.endsWith(".pb.zst");
  }

  private static boolean isValidJson(final String fileName) {
    return fileName.endsWith(".json");
  }

  private static void serializeAndCompress(final Path filePath) {
    final String inputFilePath = filePath.toString();
    final String outputFilePath =
        inputFilePath.substring(0, inputFilePath.lastIndexOf(".")) + ".pb.zst";

    try (final FileInputStream fis = new FileInputStream(inputFilePath);
        final FileOutputStream fos = new FileOutputStream(outputFilePath)) {

      final LanguageProfile languageProfile = LanguageProfile.fromZstdCompressedJson(fis);

      final Profile profile =
          Profile.newBuilder()
              .setName(languageProfile.getIsoCode639_1())
              .addAllNWords(languageProfile.getNGramCounts())
              .putAllFreq(languageProfile.getWordFrequencies())
              .build();

      final byte[] profileByteArray = profile.toByteArray();
      final byte[] compressedWithPrefix = ZstdUtils.compressWithPrefix(profileByteArray);

      fos.write(compressedWithPrefix);

      System.out.println("Data serialized to " + outputFilePath);

      // Optionally delete the original file after successful compression.
      Files.delete(filePath);

    } catch (IOException e) {
      System.err.println("Error gzipping " + filePath.getFileName() + ": " + e.getMessage());
    }
  }

  private static void deserializeAndUncompress(final Path filePath) {
    final String drop = ".pb.zst";
    final String inputFilePath = filePath.toString();
    final String outputFilePath =
        inputFilePath.substring(0, inputFilePath.length() - drop.length()) + ".json";

    try (final FileInputStream fis = new FileInputStream(inputFilePath);
        final FileOutputStream fos = new FileOutputStream(outputFilePath)) {

      final InputStream inputStream = ZstdUtils.decompressWithPrefix(fis);
      final Profile deserialized = Profile.parseFrom(inputStream);

      final LanguageProfile languageProfile =
          new LanguageProfile(
              deserialized.getName(), deserialized.getFreqMap(), deserialized.getNWordsList());
      final String languageProfileJson = languageProfile.toJson();

      fos.write(languageProfileJson.getBytes(StandardCharsets.UTF_8));

      System.out.println("Data deserialized to " + outputFilePath);

      // Optionally delete the original file after successful compression.
      Files.delete(filePath);

    } catch (IOException e) {
      System.err.println("Error gzipping " + filePath.getFileName() + ": " + e.getMessage());
    }
  }

  private static void compressJson(final Path filePath) {
    final String inputFilePath = filePath.toString();
    final String outputFilePath =
        inputFilePath.substring(0, inputFilePath.lastIndexOf(".")) + ".zst";

    try (final FileInputStream fis = new FileInputStream(inputFilePath);
        final FileOutputStream fos = new FileOutputStream(outputFilePath)) {

      final byte[] compressedWithPrefix = ZstdUtils.compressWithPrefix(fis.readAllBytes());
      fos.write(compressedWithPrefix);

      System.out.println("JSON data serialized to " + outputFilePath);

      // Optionally delete the original file after successful compression.
      Files.delete(filePath);

    } catch (IOException e) {
      System.err.println("Error gzipping " + filePath.getFileName() + ": " + e.getMessage());
    }
  }
}
