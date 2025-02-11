package io.github.azagniotov.language;

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
import java.util.List;
import java.util.Map;

class LanguageProfile {
    private static final Gson GSON = new GsonBuilder()
            // Have to use a custom model deserializer in order to avoid
            // Gson's duplicate key exception, as it appears the language
            // profiles may contain duplicate keys.
            .registerTypeAdapter(LanguageProfile.class, new LanguageProfileDeserializer())
            .create();

    private final String isoCode639_1;
    private final Map<String, Long> freq;
    private final List<Double> nWords;

    /** Create a language profile from a JSON input stream. */
    private LanguageProfile(final String isoCode639_1, final Map<String, Long> freq, final List<Double> nWords) {
        this.isoCode639_1 = isoCode639_1;
        this.freq = freq;
        this.nWords = nWords;
    }

    /** Create a language profile from a JSON input stream. */
    static LanguageProfile fromJson(final InputStream languageProfile) {
        return GSON.fromJson(new InputStreamReader(languageProfile), LanguageProfile.class);
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
