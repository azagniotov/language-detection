package io.github.azagniotov.language;

import static io.github.azagniotov.language.StringConstants.COMMA;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LanguageDetectionSettings {

  private static final String ALL_SUPPORTED_ISO_CODES_639_1 =
      "af,am,ar,az,bg,bn,bo,br,ca,cs,cy,da,de,el,en,es,et,eu,fa,fi,fr,ga,gu,he,hi,hr,hu,hy,id,it,ja,ka,kk,kn,ko,ky,lb,lt,lv,mk,ml,mn,mr,ne,nl,no,pa,pl,pt,"
          + "ro,ru,si,sk,sl,so,sr,sq,sv,sw,ta,te,tg,th,ti,tl,tr,uk,ur,vi,yi,zh-cn,zh-tw";

  static final LanguageDetectionSettings DEFAULT_SETTINGS_ALL_LANGUAGES =
      LanguageDetectionSettings.fromAllIsoCodes639_1().build();

  private static final int MAX_TEXT_CHARS_UPPER_BOUND = 20000;

  private static final int FLAG_MINIMUM_CERTAINTY = 1; // 0001
  private static final int FLAG_TOP_LANGUAGE_CERTAINTY = 2; // 0010
  private static final int FLAG_SANITIZE_FOR_SEARCH = 4; // 0100
  private static final int FLAG_CLASSIFY_AS_JAPANESE = 8; // 1000

  private final String profile;
  private final int minNGramLength;
  private final int maxNGramLength;
  private final int maxTextChars;
  private final List<String> isoCodes639_1;
  private final int sanitizeForSearchThreshold;
  private final float classifyChineseAsJapaneseThreshold;

  private final float topLanguageCertaintyThreshold;
  private final String topLanguageFallbackIsoCode639_1;
  private final float minimumCertaintyThreshold;

  private final int bitFlags;

  private LanguageDetectionSettings(final Builder builder) {
    this.profile = builder.profile;
    this.minNGramLength = builder.minNGramLength;
    this.maxNGramLength = builder.maxNGramLength;
    this.maxTextChars = builder.maxTextChars;
    this.isoCodes639_1 = builder.isoCodes639_1;
    this.sanitizeForSearchThreshold = builder.sanitizeForSearchThreshold;
    this.classifyChineseAsJapaneseThreshold = builder.classifyChineseAsJapaneseThreshold;

    this.topLanguageCertaintyThreshold = builder.topLanguageCertaintyThreshold;
    this.topLanguageFallbackIsoCode639_1 = builder.topLanguageFallbackIsoCode639_1;
    this.minimumCertaintyThreshold = builder.minimumCertaintyThreshold;

    this.bitFlags = builder.bitFlags;
  }

  String getProfile() {
    return profile;
  }

  int getMinNGramLength() {
    return minNGramLength;
  }

  int getMaxNGramLength() {
    return maxNGramLength;
  }

  public int getMaxTextChars() {
    return maxTextChars;
  }

  List<String> getIsoCodes639_1() {
    return isoCodes639_1;
  }

  int getSanitizeForSearchThreshold() {
    return sanitizeForSearchThreshold;
  }

  boolean isSanitizeForSearch() {
    return (bitFlags & FLAG_SANITIZE_FOR_SEARCH) != 0;
  }

  boolean isClassifyChineseAsJapanese() {
    return (bitFlags & FLAG_CLASSIFY_AS_JAPANESE) != 0;
  }

  float getClassifyChineseAsJapaneseThreshold() {
    return classifyChineseAsJapaneseThreshold;
  }

  float getTopLanguageCertaintyThreshold() {
    return topLanguageCertaintyThreshold;
  }

  String getTopLanguageFallbackIsoCode639_1() {
    return topLanguageFallbackIsoCode639_1;
  }

  boolean isTopLanguageCertaintyThresholdSet() {
    return (bitFlags & FLAG_TOP_LANGUAGE_CERTAINTY) != 0;
  }

  float getMinimumCertaintyThreshold() {
    return minimumCertaintyThreshold;
  }

  boolean isMinimumCertaintyThresholdSet() {
    return (bitFlags & FLAG_MINIMUM_CERTAINTY) != 0;
  }

  public static Builder fromAllIsoCodes639_1() {
    final List<String> allIsoCodes639_1 = Arrays.asList(ALL_SUPPORTED_ISO_CODES_639_1.split(COMMA));
    return new Builder(allIsoCodes639_1);
  }

  public static Builder fromIsoCodes639_1(final String isoCodes639_1csv) {
    final List<String> isoCodes639_1 =
        Arrays.stream(isoCodes639_1csv.split(COMMA))
            .filter(Objects::nonNull)
            .map(String::trim)
            .map(String::toLowerCase)
            .collect(toList());
    return new Builder(isoCodes639_1);
  }

  public static class Builder {

    private String profile;
    private int minNGramLength;
    private int maxNGramLength;
    private int maxTextChars;
    private List<String> isoCodes639_1;

    // Deeming an input string of 128 characters as 'short input'
    // At this point this is not exposed to configure via a Buildr setter
    private int sanitizeForSearchThreshold;

    // At this point this is not exposed to configure via a Buildr setter
    private float classifyChineseAsJapaneseThreshold;
    private float topLanguageCertaintyThreshold;
    private String topLanguageFallbackIsoCode639_1;
    private float minimumCertaintyThreshold;

    private int bitFlags;

    private Builder(final List<String> isoCodes639_1) {
      this.isoCodes639_1 = List.copyOf(isoCodes639_1);
      this.profile = "merged-average"; // A name of a subdirectory under the resources
      this.minNGramLength = 1;
      this.maxNGramLength = 3;
      this.maxTextChars = 3000;
      this.sanitizeForSearchThreshold = 128;
      this.classifyChineseAsJapaneseThreshold = 0.1f;
      this.topLanguageCertaintyThreshold = 0.65f;
      this.topLanguageFallbackIsoCode639_1 = "en";
      this.minimumCertaintyThreshold = 0.1f;
      this.bitFlags = FLAG_SANITIZE_FOR_SEARCH | FLAG_MINIMUM_CERTAINTY;
    }

    private Builder(final Builder that) {
      this.profile = that.profile;
      this.minNGramLength = that.minNGramLength;
      this.maxNGramLength = that.maxNGramLength;
      this.maxTextChars = that.maxTextChars;
      this.isoCodes639_1 = that.isoCodes639_1;
      this.sanitizeForSearchThreshold = that.sanitizeForSearchThreshold;
      this.classifyChineseAsJapaneseThreshold = that.classifyChineseAsJapaneseThreshold;
      this.topLanguageCertaintyThreshold = that.topLanguageCertaintyThreshold;
      this.topLanguageFallbackIsoCode639_1 = that.topLanguageFallbackIsoCode639_1;
      this.minimumCertaintyThreshold = that.minimumCertaintyThreshold;
      this.bitFlags = that.bitFlags;
    }

    public Builder withProfile(final String profile) {
      this.profile = profile;
      return new Builder(this);
    }

    public Builder withMaxTextChars(final int maxTextChars) {
      this.maxTextChars = Math.min(Math.max(1, maxTextChars), MAX_TEXT_CHARS_UPPER_BOUND);
      return new Builder(this);
    }

    public Builder withoutSanitizeForSearch() {
      this.bitFlags = this.bitFlags & ~FLAG_SANITIZE_FOR_SEARCH;
      return new Builder(this);
    }

    public Builder withClassifyChineseAsJapanese() {
      this.bitFlags = this.bitFlags | FLAG_CLASSIFY_AS_JAPANESE;
      return new Builder(this);
    }

    public Builder withTopLanguageMininumCertainty(
        final float topLanguageCertaintyThreshold, final String topLanguageFallbackIsoCode639_1) {
      this.topLanguageCertaintyThreshold = topLanguageCertaintyThreshold;
      this.topLanguageFallbackIsoCode639_1 = topLanguageFallbackIsoCode639_1;
      // Unset MINIMUM_CERTAINTY_THRESHOLD, set TOP_LANGUAGE_CERTAINTY_THRESHOLD
      this.bitFlags = (this.bitFlags & ~FLAG_MINIMUM_CERTAINTY) | FLAG_TOP_LANGUAGE_CERTAINTY;
      return new Builder(this);
    }

    public Builder withMininumCertainty(final float minimumCertaintyThreshold) {
      this.minimumCertaintyThreshold = minimumCertaintyThreshold;
      // Unset TOP_LANGUAGE_CERTAINTY_THRESHOLD, set MINIMUM_CERTAINTY_THRESHOLD
      this.bitFlags = (this.bitFlags & ~FLAG_TOP_LANGUAGE_CERTAINTY) | FLAG_MINIMUM_CERTAINTY;
      return new Builder(this);
    }

    public LanguageDetectionSettings build() {
      return new LanguageDetectionSettings(this);
    }
  }
}
