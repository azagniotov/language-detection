package io.github.azagniotov.language;

class EnvironmentUtils {

  private static final String ENV_BASE = "LANGUAGE_DETECT_";

  /**
   * Retrieves the value of an environment variable, with a default fallback.
   *
   * @param variableName The name of the environment variable.
   * @param defaultValue The default value to return if the environment variable is not set, empty,
   *     or not an float.
   * @return The float value of the environment variable, or the default value if it's not set,
   *     empty, or not an float.
   */
  public static float getEnvFloat(final String variableName, final float defaultValue) {
    final String value = getEnv(variableName);
    if (value == null) {
      return defaultValue;
    }
    try {
      return Float.parseFloat(value);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Retrieves the value of an environment variable, and converts it to an integer.
   *
   * @param variableName The name of the environment variable.
   * @param defaultValue The default value to return if the environment variable is not set, empty,
   *     or not an integer.
   * @return The integer value of the environment variable, or the default value if it's not set,
   *     empty, or not an integer.
   */
  public static int getEnvInt(final String variableName, final int defaultValue) {
    final String value = getEnv(variableName);
    if (value == null) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Retrieves the value of an environment variable.
   *
   * @param variableName The name of the environment variable.
   * @return The value of the environment variable, or null if it's not set or empty.
   */
  private static String getEnv(final String variableName) {
    final String value = System.getenv(String.format("%s%s", ENV_BASE, variableName));
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    return value;
  }
}
