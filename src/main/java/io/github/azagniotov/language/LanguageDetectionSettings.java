package io.github.azagniotov.language;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LanguageDetectionSettings {

  private static final String ALL_SUPPORTED_ISO_CODES_639_1 =
      "af,am,ar,az,bg,bn,br,ca,cs,cy,da,de,el,en,es,et,eu,fa,fi,fr,ga,gu,he,hi,hr,hu,hy,id,it,ja,ka,kn,ko,lb,lt,lv,mk,ml,mr,ne,nl,no,pa,pl,pt,"
          + "ro,ru,si,sk,sl,so,sq,sv,sw,ta,te,th,ti,tl,tr,uk,ur,vi,yi,zh-cn,zh-tw";

  static final LanguageDetectionSettings DEFAULT_SETTINGS_ALL_LANGUAGES =
      LanguageDetectionSettings.fromAllIsoCodes639_1().build();

  private static final String COMMA = ",";

  private static final int MAX_TEXT_CHARS_UPPER_BOUND = 20000;

  private final String profile;
  private final int maxTextChars;
  private final List<String> isoCodes639_1;
  private final int sanitizeForSearchThreshold;
  private final boolean sanitizeForSearch;
  private final boolean classifyChineseAsJapanese;
  private final double classifyChineseAsJapaneseThreshold;
  private final double certaintyThreshold;
  private final String fallbackIsoCode639_1;

  private LanguageDetectionSettings(final Builder builder) {
    this.profile = builder.profile;
    this.maxTextChars = builder.maxTextChars;
    this.isoCodes639_1 = builder.isoCodes639_1;
    this.sanitizeForSearchThreshold = builder.sanitizeForSearchThreshold;
    this.sanitizeForSearch = builder.sanitizeForSearch;
    this.classifyChineseAsJapanese = builder.classifyChineseAsJapanese;
    this.classifyChineseAsJapaneseThreshold = builder.classifyChineseAsJapaneseThreshold;
    this.certaintyThreshold = builder.certaintyThreshold;
    this.fallbackIsoCode639_1 = builder.fallbackIsoCode639_1;
  }

  String getProfile() {
    return profile;
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
    return sanitizeForSearch;
  }

  boolean isClassifyChineseAsJapanese() {
    return classifyChineseAsJapanese;
  }

  double getClassifyChineseAsJapaneseThreshold() {
    return classifyChineseAsJapaneseThreshold;
  }

  double getCertaintyThreshold() {
    return certaintyThreshold;
  }

  String getFallbackIsoCode639_1() {
    return fallbackIsoCode639_1;
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
    private int maxTextChars;
    private List<String> isoCodes639_1;

    private boolean sanitizeForSearch;
    // Deeming an input string of 128 characters as 'short input'
    // At this point this is not exposed to configure via a Buildr setter
    private int sanitizeForSearchThreshold;

    private boolean classifyChineseAsJapanese;
    // At this point this is not exposed to configure via a Buildr setter
    private double classifyChineseAsJapaneseThreshold;
    private double certaintyThreshold;
    private String fallbackIsoCode639_1;

    private Builder(final List<String> isoCodes639_1) {
      this.isoCodes639_1 = List.copyOf(isoCodes639_1);
      this.profile = "merged-average"; // A name of a subdirectory under the resources
      this.maxTextChars = 3000;
      this.sanitizeForSearch = true;
      this.sanitizeForSearchThreshold = 128;
      this.classifyChineseAsJapanese = false;
      this.classifyChineseAsJapaneseThreshold = 0.1;
      this.certaintyThreshold = 0.65;
      this.fallbackIsoCode639_1 = "en";
    }

    private Builder(final Builder that) {
      this.profile = that.profile;
      this.maxTextChars = that.maxTextChars;
      this.isoCodes639_1 = that.isoCodes639_1;
      this.sanitizeForSearch = that.sanitizeForSearch;
      this.sanitizeForSearchThreshold = that.sanitizeForSearchThreshold;
      this.classifyChineseAsJapanese = that.classifyChineseAsJapanese;
      this.classifyChineseAsJapaneseThreshold = that.classifyChineseAsJapaneseThreshold;
      this.certaintyThreshold = that.certaintyThreshold;
      this.fallbackIsoCode639_1 = that.fallbackIsoCode639_1;
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
      this.sanitizeForSearch = false;
      return new Builder(this);
    }

    public Builder withClassifyChineseAsJapanese() {
      this.classifyChineseAsJapanese = true;
      return new Builder(this);
    }

    public Builder withMininumCertainty(
        final double certaintyThreshold, final String fallbackIsoCode639_1) {
      this.certaintyThreshold = certaintyThreshold;
      this.fallbackIsoCode639_1 = fallbackIsoCode639_1;
      return new Builder(this);
    }

    public LanguageDetectionSettings build() {
      return new LanguageDetectionSettings(this);
    }
  }
}
