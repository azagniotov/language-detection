package util;

import com.github.luben.zstd.Zstd;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class ZstdUtils {
  public static final int ZSTD_PREFIX_SIZE_BYTES = 4;

  private ZstdUtils() {}

  public static InputStream zstdString(final String input) throws IOException {
    final byte[] originalBytes = input.getBytes(StandardCharsets.UTF_8);
    final byte[] compressedWithPrefix = compressWithPrefix(originalBytes);

    return new ByteArrayInputStream(compressedWithPrefix);
  }

  public static byte[] compressWithPrefix(final byte[] originalBytes) throws IOException {
    final byte[] compressedBytes = Zstd.compress(originalBytes, Zstd.maxCompressionLevel());
    final int originalSize = originalBytes.length;

    try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      outputStream.write(
          ByteBuffer.allocate(ZSTD_PREFIX_SIZE_BYTES).putInt(originalSize).array(),
          0,
          ZSTD_PREFIX_SIZE_BYTES);
      outputStream.write(compressedBytes, 0, compressedBytes.length);

      return outputStream.toByteArray();
    }
  }

  public static InputStream decompressWithPrefix(final InputStream zstdCompressed)
      throws IOException {
    final byte[] sizeBytes = zstdCompressed.readNBytes(ZstdUtils.ZSTD_PREFIX_SIZE_BYTES);
    final int originalSize = ByteBuffer.wrap(sizeBytes).getInt();
    final byte[] compressedBytes = zstdCompressed.readAllBytes();

    return new ByteArrayInputStream(Zstd.decompress(compressedBytes, originalSize));
  }
}
