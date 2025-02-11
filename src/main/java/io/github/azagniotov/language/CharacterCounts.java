package io.github.azagniotov.language;

import java.util.EnumMap;
import java.util.Map;

class CharacterCounts {

    enum CharType {
        JAPANESE_HAN,
        HIRAGANA,
        KATAKANA,
        CJK_PUNCTUATION,
        IRRELEVANT,
        CHINESE_HAN,
        CJK
    }

    private final Map<CharType, Integer> charTypeCounts;

    private CharacterCounts() {
        charTypeCounts = new EnumMap<>(CharType.class);
        // Initialize all types to 0
        for (CharType type : CharType.values()) {
            charTypeCounts.put(type, 0);
        }
    }

    double allJapanese() {
        return get(CharType.KATAKANA)
                + get(CharType.HIRAGANA)
                + get(CharType.JAPANESE_HAN)
                + get(CharType.CJK_PUNCTUATION);
    }

    double irrelevant() {
        return get(CharType.IRRELEVANT);
    }

    double get(final CharType key) {
        return charTypeCounts.getOrDefault(key, 0);
    }

    void mark(final CharType key) {
        charTypeCounts.put(key, charTypeCounts.getOrDefault(key, 0) + 1);
    }

    static CharacterCounts create() {
        return new CharacterCounts();
    }
}
