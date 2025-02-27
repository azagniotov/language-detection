package io.github.azagniotov.language;

public class Language implements Comparable<Language> {

  private final String isoCode639_1;
  private final float probability;

  Language(final String isoCode639_1, final float probability) {
    this.isoCode639_1 = isoCode639_1;
    this.probability = probability;
  }

  public String getIsoCode639_1() {
    return isoCode639_1;
  }

  public float getProbability() {
    return probability;
  }

  @Override
  public String toString() {
    return String.format("%s=%s", isoCode639_1.toUpperCase(), probability);
  }

  @Override
  public int compareTo(final Language other) {
    return Float.compare(other.probability, this.probability);
  }
}
