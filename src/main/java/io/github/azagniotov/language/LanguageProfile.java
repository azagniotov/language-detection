package io.github.azagniotov.language;

import static io.github.azagniotov.language.InputSanitizer.filterOutNonWords;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

class LanguageProfile {
    private static final Gson GSON = new GsonBuilder()
            // Have to use a custom model deserializer in order to avoid
            // Gson's duplicate key exception, as it appears the language
            // profiles may contain duplicate keys.
            .registerTypeAdapter(LanguageProfile.class, new LanguageProfileDeserializer())
            .create();

    private static final double MINIMUM_FREQ = 2.0;
    private static final double LESS_FREQ_RATIO = 100000.0;

    private final String isoCode639_1;
    private final Map<String, Long> freq;
    private final List<Double> nWords;

    /** Create a language profile from a JSON input stream. */
    LanguageProfile(final String isoCode639_1, final Map<String, Long> freq, final List<Double> nWords) {
        this.isoCode639_1 = isoCode639_1;
        this.freq = freq;
        this.nWords = nWords;
    }

    /** Create a language profile from a JSON input stream. */
    static LanguageProfile fromJson(final InputStream languageProfile) {
        return GSON.fromJson(new InputStreamReader(languageProfile), LanguageProfile.class);
    }

    static String toJson(final LanguageProfile languageProfile) {
        return GSON.toJson(languageProfile);
    }

    void add(final String gram) {
        if (isoCode639_1 == null || gram == null || gram.trim().isEmpty()) {
            return;
        }
        int len = gram.length();
        if (len < 1 || len > NGram.TRI_GRAM_LENGTH) {
            return;
        }
        nWords.set(len - 1, nWords.get(len - 1) + 1.0);
        if (freq.containsKey(gram)) {
            freq.put(gram, freq.get(gram) + 1);
        } else {
            freq.put(gram, 1L);
        }
    }

    String getIsoCode639_1() {
        return isoCode639_1;
    }

    List<Double> getNGramCounts() {
        return nWords;
    }

    Map<String, Long> getWordFrequencies() {
        return freq;
    }

    /**
     * This function is used during generation of a new language profile.
     * <p>
     * It extracts n-grams from the given input and adds their frequency
     * into the language profile.
     *
     * @param input input text to extract n-grams
     */
    public void update(final String input) {
        if (input == null) {
            return;
        }
        final String sanitizedInput = filterOutNonWords(input);
        final String normalizedInput = NGram.normalizeVietnamese(sanitizedInput);

        final NGram gram = new NGram();
        for (int i = 0; i < normalizedInput.length(); ++i) {
            gram.addChar(input.charAt(i));
            for (int n = 1; n <= NGram.TRI_GRAM_LENGTH; ++n) {
                add(gram.get(n));
            }
        }
    }

    /**
     * Eliminate below less frequency n-grams and noisy Latin alphabets
     */
    public void omitLessFreq() {
        if (this.isoCode639_1 == null || this.isoCode639_1.trim().isEmpty()) {
            return; // Illegal
        }
        double threshold = nWords.get(0) / LESS_FREQ_RATIO;
        if (threshold < MINIMUM_FREQ) {
            threshold = MINIMUM_FREQ;
        }

        Set<String> keys = freq.keySet();
        double roman = 0;
        for (Iterator<String> iterator = keys.iterator(); iterator.hasNext(); ) {
            String key = iterator.next();
            double count = freq.get(key);
            if (count <= threshold) {
                final double current = nWords.get(key.length() - 1);
                nWords.set(key.length() - 1, current - count);
                iterator.remove();
            } else {
                if (key.matches("^[A-Za-z]$")) {
                    roman += count;
                }
            }
        }

        // roman check
        if (roman < nWords.get(0) / 3) {
            Set<String> keys2 = freq.keySet();
            for (Iterator<String> iterator = keys2.iterator(); iterator.hasNext(); ) {
                String key = iterator.next();
                if (key.matches(".*[A-Za-z].*")) {
                    final double current = nWords.get(key.length() - 1);
                    nWords.set(key.length() - 1, current - freq.get(key));

                    iterator.remove();
                }
            }
        }
    }

    private static class LanguageProfileDeserializer implements JsonDeserializer<LanguageProfile> {
        @Override
        public LanguageProfile deserialize(
                JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            final JsonObject jsonObject = json.getAsJsonObject();
            final String name = jsonObject.get("name").getAsString();
            final List<Double> nWords = context.deserialize(jsonObject.get("n_words"), List.class);

            // Manually handle duplicate keys in the "freq" map
            final Map<String, Long> freq = new HashMap<>();
            JsonObject freqJson = jsonObject.getAsJsonObject("freq");

            for (Map.Entry<String, JsonElement> entry : freqJson.entrySet()) {
                freq.put(entry.getKey(), entry.getValue().getAsLong()); // Keeps only the last value for each key
            }

            return new LanguageProfile(name, freq, nWords);
        }
    }
}
