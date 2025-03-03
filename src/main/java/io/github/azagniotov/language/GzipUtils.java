package io.github.azagniotov.language;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

final class GzipUtils {

  private GzipUtils() {}

  static InputStream gzipString(final String inputString) throws IOException {
    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try (final GZIPOutputStream gzipOutputStream =
        new GZIPOutputStream(byteArrayOutputStream) {
          {
            def.setLevel(Deflater.BEST_COMPRESSION);
          }
        }) {
      gzipOutputStream.write(inputString.getBytes());
    }
    return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
  }
}
