package io.github.azagniotov.language;

import static io.github.azagniotov.language.InputSanitizer.filterOutNonWords;
import static io.github.azagniotov.language.LanguageDetector.PERFECT_PROBABILITY;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import io.github.azagniotov.language.annotations.GeneratedCodeClassCoverageExclusion;
import io.github.azagniotov.language.annotations.GeneratedCodeMethodCoverageExclusion;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

class LanguageProfile {
  private static final Gson GSON =
      new GsonBuilder()
          // Have to use a custom model deserializer in order to avoid
          // Gson's duplicate key exception, as it appears the language
          // profiles may contain duplicate keys.
          .registerTypeAdapter(LanguageProfile.class, new LanguageProfileDeserializer())
          .registerTypeAdapter(LanguageProfile.class, new LanguageProfileSerializer())
          .create();

  private static final float MINIMUM_FREQ = 2.0f;
  private static final float LESS_FREQ_RATIO = 100_000.0f;
  private static final Pattern PATTERN_ONE_ALPHABETIC_CHAR_ONLY = Pattern.compile("^[A-Za-z]$");

  private final String isoCode639_1;
  private final Map<String, Long> freq;
  private final List<Float> nWords;

  private int minNGramLength;
  private int maxNGramLength;

  LanguageProfile(
      final String isoCode639_1, final Map<String, Long> freq, final List<Float> nWords) {
    this.isoCode639_1 = isoCode639_1;
    this.freq = freq;
    this.nWords = nWords;
  }

  /** Create a language profile from a Gzipped JSON input stream. */
  static LanguageProfile fromGzippedJson(final InputStream compressedInputStream)
      throws IOException {

    try (final GZIPInputStream gzipInputStream = new GZIPInputStream(compressedInputStream);
        final InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        final JsonReader jsonReader = new JsonReader(bufferedReader)) {

      // JsonReader provides a streaming API which is recommended for large JSONs
      return GSON.fromJson(jsonReader, LanguageProfile.class);
    }
  }

  String toJson() {
    return GSON.toJson(this);
  }

  void add(final String gram, final int minNGramLength, final int maxNGramLength) {
    if (isoCode639_1 == null || gram == null || gram.trim().isEmpty()) {
      return;
    }
    int len = gram.length();
    if (len < minNGramLength || len > maxNGramLength) {
      return;
    }
    int nGramTypeIndex = len - 1;
    nWords.set(nGramTypeIndex, nWords.get(nGramTypeIndex) + PERFECT_PROBABILITY);

    if (freq.containsKey(gram)) {
      freq.put(gram, freq.get(gram) + 1);
    } else {
      freq.put(gram, 1L);
    }
  }

  String getIsoCode639_1() {
    return isoCode639_1;
  }

  List<Float> getNGramCounts() {
    return nWords;
  }

  Map<String, Long> getWordFrequencies() {
    return freq;
  }

  /**
   * This function is used during generation of a new language profile.
   *
   * <p>It extracts n-grams from the given input and adds their frequency into the language profile.
   *
   * @param input input text to extract n-grams
   */
  public void update(final String input, final int minNGramLength, final int maxNGramLength) {
    if (input == null) {
      return;
    }
    this.minNGramLength = minNGramLength;
    this.maxNGramLength = maxNGramLength;
    final String sanitizedInput = filterOutNonWords(input);
    final String normalizedInput = VietnameseUtils.normalizeVietnamese(sanitizedInput);
    final NGram gram = new NGram(normalizedInput, minNGramLength, maxNGramLength);

    for (int i = 0; i < normalizedInput.length(); ++i) {
      final char currentChar = normalizedInput.charAt(i);
      gram.addChar(currentChar);
      for (int n = gram.getMinNGramLength(); n <= gram.getMaxNGramLength(); ++n) {
        final String nGram = String.valueOf(gram.get(n));

        if (nGram.length() >= this.minNGramLength) {
          add(nGram, gram.getMinNGramLength(), gram.getMaxNGramLength());
        }
      }
    }
  }

  /** Eliminate below less frequency n-grams and noisy Latin alphabets */
  @GeneratedCodeMethodCoverageExclusion
  public void omitLessFreq() {
    if (this.isoCode639_1 == null || this.isoCode639_1.trim().isEmpty()) {
      return; // Illegal
    }
    float threshold = nWords.get(this.minNGramLength - 1) / LESS_FREQ_RATIO;
    if (threshold < MINIMUM_FREQ) {
      threshold = MINIMUM_FREQ;
    }

    final Set<String> keys = freq.keySet();
    float roman = 0;
    for (Iterator<String> iterator = keys.iterator(); iterator.hasNext(); ) {
      final String key = iterator.next();
      final float count = freq.get(key);
      if (count <= threshold) {
        final float current = nWords.get(key.length() - 1);
        nWords.set(key.length() - 1, current - count);
        iterator.remove();
      } else {
        // Checks for presence single-character Latin keys
        if (PATTERN_ONE_ALPHABETIC_CHAR_ONLY.matcher(key).matches()) {
          roman += count;
        }
      }
    }

    // Remove the noisy single-character Latin keys, only if we have found some earlier
    if (roman > 0 && roman < nWords.get(this.minNGramLength - 1) / this.maxNGramLength) {
      final Set<String> keys2 = freq.keySet();
      for (Iterator<String> iterator = keys2.iterator(); iterator.hasNext(); ) {
        final String key = iterator.next();
        if (PATTERN_ONE_ALPHABETIC_CHAR_ONLY.matcher(key).matches()) {
          final float current = nWords.get(key.length() - 1);
          nWords.set(key.length() - 1, current - freq.get(key));

          iterator.remove();
        }
      }
    }
  }

  private static class LanguageProfileDeserializer implements JsonDeserializer<LanguageProfile> {
    @Override
    public LanguageProfile deserialize(
        final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
        throws JsonParseException {
      final JsonObject jsonObject = json.getAsJsonObject();
      final String name = jsonObject.get("name").getAsString();

      // Another approach:
      // final Type listOfFloat = new TypeToken<ArrayList<Float>>(){}.getType();
      final Type arrayOfFloat = Float[].class;
      final Float[] nWords = context.deserialize(jsonObject.get("n_words"), arrayOfFloat);

      // Manually handle duplicate keys in the "freq" map
      final Map<String, Long> freq = new HashMap<>();
      JsonObject freqJson = jsonObject.getAsJsonObject("freq");

      for (Map.Entry<String, JsonElement> entry : freqJson.entrySet()) {
        freq.put(
            entry.getKey(), entry.getValue().getAsLong()); // Keeps only the last value for each key
      }

      return new LanguageProfile(name, freq, Arrays.asList(nWords));
    }
  }

  @GeneratedCodeClassCoverageExclusion
  private static class LanguageProfileSerializer implements JsonSerializer<LanguageProfile> {
    @Override
    public JsonElement serialize(
        final LanguageProfile languageProfile,
        final Type typeOfT,
        final JsonSerializationContext context)
        throws JsonParseException {
      final JsonObject object = new JsonObject();
      object.add("freq", context.serialize(languageProfile.getWordFrequencies()));
      object.add("n_words", context.serialize(languageProfile.getNGramCounts()));
      object.add("name", context.serialize(languageProfile.getIsoCode639_1()));

      return object;
    }
  }
}
