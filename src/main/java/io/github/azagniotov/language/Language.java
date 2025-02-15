package io.github.azagniotov.language;

public class Language {

  private final String isoCode639_1;
  private final double probability;

  Language(final String isoCode639_1, final double probability) {
    this.isoCode639_1 = isoCode639_1;
    this.probability = probability;
  }

  public String getIsoCode639_1() {
    return isoCode639_1;
  }

  public double getProbability() {
    return probability;
  }

  @Override
  public String toString() {
    return String.format("%s=%s", isoCode639_1.toUpperCase(), probability);
  }
}
