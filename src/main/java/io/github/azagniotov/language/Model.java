package io.github.azagniotov.language;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

class Model {

  private static final Gson GSON =
      new GsonBuilder().registerTypeAdapter(Model.class, new Model.ModelDeserializer()).create();

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

  private static class ModelDeserializer implements JsonDeserializer<Model> {
    @Override
    public Model deserialize(
        final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
        throws JsonParseException {
      final JsonObject jsonObject = json.getAsJsonObject();

      final int baseFrequency =
          EnvironmentUtils.getEnvInt(
              "LANGUAGE_DETECT_BASE_FREQUENCY", jsonObject.get("baseFrequency").getAsInt());
      final int iterationLimit =
          EnvironmentUtils.getEnvInt(
              "LANGUAGE_DETECT_ITERATION_LIMIT", jsonObject.get("iterationLimit").getAsInt());
      final int numberOfTrials =
          EnvironmentUtils.getEnvInt(
              "LANGUAGE_DETECT_NUMBER_OF_TRIALS", jsonObject.get("numberOfTrials").getAsInt());
      final float alpha =
          EnvironmentUtils.getEnvFloat(
              "LANGUAGE_DETECT_ALPHA", jsonObject.get("alpha").getAsFloat());
      final float alphaWidth =
          EnvironmentUtils.getEnvFloat(
              "LANGUAGE_DETECT_ALPHA_WIDTH", jsonObject.get("alphaWidth").getAsFloat());
      final float convergenceThreshold =
          EnvironmentUtils.getEnvFloat(
              "LANGUAGE_DETECT_CONVERGENCE_THRESHOLD",
              jsonObject.get("convergenceThreshold").getAsFloat());

      return new Model(
          baseFrequency, iterationLimit, numberOfTrials, alpha, alphaWidth, convergenceThreshold);
    }
  }
}
