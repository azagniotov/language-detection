package io.github.azagniotov.language;

import static io.github.azagniotov.language.EnvironmentUtils.getEnvFloat;
import static io.github.azagniotov.language.EnvironmentUtils.getEnvInt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class Model {

  private static final Gson GSON = new GsonBuilder().create();

  private final int baseFrequency;
  private final int iterationLimit;
  private final int numberOfTrials;
  private final float alpha;
  private final float alphaWidth;
  private final float convergenceThreshold;

  Model(
      final int baseFrequency,
      final int iterationLimit,
      final int numberOfTrials,
      final float alpha,
      final float alphaWidth,
      final float convergenceThreshold) {
    this.baseFrequency = baseFrequency;
    this.iterationLimit = iterationLimit;
    this.numberOfTrials = numberOfTrials;
    this.alpha = alpha;
    this.alphaWidth = alphaWidth;
    this.convergenceThreshold = convergenceThreshold;
  }

  public int getBaseFrequency() {
    return baseFrequency;
  }

  public int getIterationLimit() {
    return iterationLimit;
  }

  public int getNumberOfTrials() {
    return numberOfTrials;
  }

  public float getAlpha() {
    return alpha;
  }

  public float getAlphaWidth() {
    return alphaWidth;
  }

  public float getConvergenceThreshold() {
    return convergenceThreshold;
  }

  static Model fromJsonOrEnv(final InputStream inputStream) throws IOException {
    try (final InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {

      final Model defaults = GSON.fromJson(inputStreamReader, Model.class);
      return new Model(
          getEnvInt("BASE_FREQUENCY", defaults.baseFrequency),
          getEnvInt("ITERATION_LIMIT", defaults.iterationLimit),
          getEnvInt("NUMBER_OF_TRIALS", defaults.numberOfTrials),
          getEnvFloat("ALPHA", defaults.alpha),
          getEnvFloat("ALPHA_WIDTH", defaults.alphaWidth),
          getEnvFloat("CONVERGENCE_THRESHOLD", defaults.convergenceThreshold));
    }
  }
}
