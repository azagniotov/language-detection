package io.github.azagniotov.language;

import java.lang.Character.UnicodeBlock;
import java.lang.Character.UnicodeScript;

class UnicodeCache {

  // Unicode BMP (Basic Multilingual Plane) lookup.
  // Character.MAX_VALUE has the Unicode code point U+FFFF, which is
  // the largest value of type char in Java (which uses UTF-16 encoding).
  private static final UnicodeScript[] UNICODE_SCRIPT_LOOKUP =
      new UnicodeScript[Character.MAX_VALUE + 1];
  private static final UnicodeBlock[] UNICODE_BLOCK_LOOKUP =
      new UnicodeBlock[Character.MAX_VALUE + 1];
  private static final boolean[] UPPER_CASE_LOOKUP = new boolean[Character.MAX_VALUE + 1];

  /**
   * Estimates the JVM heap memory consumption for storing all 65,536 Unicode Basic Multilingual
   * Plane (BMP) characters (U+0000 to U+FFFF) as individual {@code String} objects within a {@code
   * String[]} array on a 64-bit JVM.
   *
   * <p><b>TL;DR</b>: <b>~3.25 MB</b>, or <b>~4.0 MB</b> if CompressedOops are enabled.
   *
   * <p><b>DETAILED BREAKDOWN:</b>
   *
   * <p><b>Assumptions:</b>
   *
   * <ul>
   *   <li><b>64-bit JVM:</b> Standard object header/reference sizes apply.
   *   <li><b>CompressedOops Enabled:</b> {@code -XX:+UseCompressedOops} (default for heaps &lt;
   *       ~32GB). Object references are 4 bytes, object headers are typically 12 bytes.
   *   <li><b>Java 9+ with Compact Strings:</b> {@code -XX:+CompactStrings} (default). Strings
   *       containing only Latin-1 characters (U+0000 to U+00FF) are backed by {@code byte[]},
   *       others use {@code char[]}.
   *   <li><b>Object Alignment:</b> Objects are typically padded to 8-byte boundaries.
   *   <li><b>No String Interning:</b> Assumes each character generates a distinct {@code String}
   *       and backing array object on the heap (e.g., via {@code new String(new char[]{ch})}).
   * </ul>
   *
   * <p><b>Memory Breakdown (with CompressedOops Enabled):</b>
   *
   * <ol>
   *   <li><b>The {@code String[]} Array Object:</b>
   *       <ul>
   *         <li>Object Header: 12 bytes
   *         <li>Array Length (int): 4 bytes
   *         <li>Data (References to Strings): 65,536 refs * 4 bytes/ref = 262,144 bytes
   *         <li>Padding: (Header + Length = 16 bytes, aligns to 8, so 0 bytes padding needed)
   *         <li><i>Subtotal for Array Object: 16 + 262,144 = 262,160 bytes</i>
   *       </ul>
   *   <li><b>Each {@code String} Object (65,536 instances):</b>
   *       <ul>
   *         <li>Object Header: 12 bytes
   *         <li>Reference to backing array ('value' field): 4 bytes
   *         <li>Hash field (int): 4 bytes
   *         <li>Coder field (byte, for Compact Strings): 1 byte
   *         <li>Subtotal: 12 + 4 + 4 + 1 = 21 bytes
   *         <li>Padding: (Aligns to next multiple of 8) -> 24 bytes
   *         <li><i>Total per String Object: 24 bytes</i>
   *       </ul>
   *   <li><b>Each Backing Array ({@code char[1]} or {@code byte[1]}) (65,536 instances):</b>
   *       <ul>
   *         <li>Object Header: 12 bytes
   *         <li>Array Length (int): 4 bytes
   *         <li>Data: 1 byte (for {@code byte[1]}) or 2 bytes (for {@code char[1]})
   *         <li>Subtotal: 16 + 1 = 17 bytes (byte[]) or 16 + 2 = 18 bytes (char[])
   *         <li>Padding: (Aligns to next multiple of 8) -> 24 bytes in both cases
   *         <li><i>Total per Backing Array: 24 bytes</i>
   *       </ul>
   * </ol>
   *
   * <p><b>Total Estimated Heap Usage:</b>
   *
   * <ul>
   *   <li>Array Object: 262,160 bytes
   *   <li>String Objects Total: 65,536 strings * 24 bytes/string = 1,572,864 bytes
   *   <li>Backing Arrays Total: 65,536 arrays * 24 bytes/array = 1,572,864 bytes
   *   <li><b>Grand Total: 262,160 + 1,572,864 + 1,572,864 = 3,407,888 bytes</b>
   *   <li><b>Approximately: 3.25 MB</b> (using 1 MB = 1024 * 1024 bytes)
   * </ul>
   *
   * <p><b>Note on CompressedOops Disabled:</b> If CompressedOops is disabled (e.g., via {@code
   * -XX:-UseCompressedOops} or very large heaps), references will become 8 bytes and headers 16
   * bytes. This would increase the total estimate significantly, likely to around <b>4.0 MB</b>.
   *
   * <p><b>Disclaimer:</b> This is an estimate of direct object heap usage based on typical JVM
   * memory layouts. It does not include other JVM overhead such as class metadata (Metaspace), JIT
   * compiled code, garbage collection information, etc. Actual memory usage can vary slightly based
   * on the specific JVM version, configuration, and platform.
   */
  private static final String[] CHAR_TO_STRING_LOOKUP = new String[Character.MAX_VALUE + 1];

  static {
    //  Character.MAX_VALUE has the Unicode code point U+FFFF, which is the highest
    //  valid character code point for a char in Java (which uses UTF-16 encoding).
    for (int codePoint = 0; codePoint <= Character.MAX_VALUE; codePoint++) {
      UNICODE_SCRIPT_LOOKUP[codePoint] = UnicodeScript.of(codePoint);
      UNICODE_BLOCK_LOOKUP[codePoint] = UnicodeBlock.of(codePoint);
      UPPER_CASE_LOOKUP[codePoint] = Character.isUpperCase(codePoint);
      CHAR_TO_STRING_LOOKUP[codePoint] = Character.toString(codePoint);
    }
  }

  static UnicodeScript scriptOf(final int codePoint) {
    if (codePoint > Character.MAX_VALUE) {
      return null;
    }
    return UNICODE_SCRIPT_LOOKUP[codePoint];
  }

  static UnicodeBlock blockOf(final int codePoint) {
    if (codePoint > Character.MAX_VALUE) {
      return null;
    }
    return UNICODE_BLOCK_LOOKUP[codePoint];
  }

  static boolean isUpper(final int codePoint) {
    if (codePoint > Character.MAX_VALUE) {
      return false;
    }
    return UPPER_CASE_LOOKUP[codePoint];
  }

  static String stringOf(final int codePoint) {
    if (codePoint > Character.MAX_VALUE) {
      return null;
    }
    return CHAR_TO_STRING_LOOKUP[codePoint];
  }
}
