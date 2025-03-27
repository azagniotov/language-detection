package io.github.azagniotov.language;

import io.github.azagniotov.language.annotations.GeneratedCodeClassCoverageExclusion;
import java.lang.reflect.Field;

@GeneratedCodeClassCoverageExclusion
class TestReflectionUtils {

  private TestReflectionUtils() {}

  static void resetLanguageDetectorFactoryInstance()
      throws NoSuchFieldException, IllegalAccessException {
    try {
      final Field field = LanguageDetectorFactory.class.getDeclaredField("instance");
      field.setAccessible(true);
      field.set(null, null); // Set to null
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw e; // Re-throw the exceptions
    }
  }
}
