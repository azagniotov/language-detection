package io.github.azagniotov.language;

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

  static Model fromJson(final InputStream inputStream) throws IOException {
    try (final InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
      return GSON.fromJson(inputStreamReader, Model.class);
    }
  }
}
