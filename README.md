# Crispy Couscous

This is a refined and re-implemented version of the archived plugin for ElasticSearch [elasticsearch-langdetect](https://github.com/jprante/elasticsearch-langdetect), which itself builds upon the original work by Nakatani Shuyo, found at https://github.com/shuyo/language-detection. The aforementioned implementation by Nakatani Shuyo serves as the default language detection component within Apache Solr.

<!-- TOC -->
* [Crispy Couscous](#crispy-couscous)
  * [About this library](#about-this-library)
    * [Supported ISO 639-1 codes](#supported-iso-639-1-codes)
    * [Quick detection of CJK languages](#quick-detection-of-cjk-languages)
    * [Model parameters](#model-parameters)
  * [How to use?](#how-to-use)
    * [Basic usage](#basic-usage)
    * [Methods to build the LanguageDetectionSettings](#methods-to-build-the-languagedetectionsettings)
      * [Configuring ISO 639-1 codes](#configuring-iso-639-1-codes)
      * [Maximum text chars](#maximum-text-chars)
      * [Skipping input sanitization for search](#skipping-input-sanitization-for-search)
      * [Classify any Chinese content as Japanese](#classify-any-chinese-content-as-japanese)
      * [Minimum detection certainty](#minimum-detection-certainty)
  * [Local development](#local-development)
    * [System requirements](#system-requirements)
    * [Build system](#build-system)
      * [List of Gradle tasks](#list-of-gradle-tasks)
      * [Building](#building)
      * [Formatting](#formatting)
    * [Testing](#testing)
      * [Unit tests](#unit-tests)
<!-- TOC -->

## About this library

The library uses 3-gram character and a Bayesian filter with various normalizations and feature sampling.
The precision is over **99%** for **53** languages.

See the following PR description to read about the benchmaks done by @yanirs : https://github.com/jprante/elasticsearch-langdetect/pull/69

### Supported ISO 639-1 codes

The following is a list of ISO 639-1 languages code recognized:

| Code   | Description                                      |
|--------|--------------------------------------------------|
| af     | Afrikaans                                        |
| ar     | Arabic                                           |
| bg     | Bulgarian                                        |
| bn     | Bengali                                          |
| cs     | Czech                                            |
| da     | Danish                                           |
| de     | German                                           |
| el     | Greek                                            |
| en     | English                                          |
| es     | Spanish                                          |
| et     | Estonian                                         |
| fa     | Farsi                                            |
| fi     | Finnish                                          |
| fr     | French                                           |
| gu     | Gujarati                                         |
| he     | Hebrew                                           |
| hi     | Hindi                                            |
| hr     | Croatian                                         |
| hu     | Hungarian                                        |
| id     | Indonesian                                       |
| it     | Italian                                          |
| ja     | Japanese                                         |
| kn     | Kannada                                          |
| ko     | Korean                                           |
| lt     | Lithuanian                                       |
| lv     | Latvian                                          |
| mk     | Macedonian                                       |
| ml     | Malayalam                                        |
| mr     | Marathi                                          |
| ne     | Nepali                                           |
| nl     | Dutch                                            |
| no     | Norwegian                                        |
| pa     | Eastern Punjabi                                  |
| pl     | Polish                                           |
| pt     | Portuguese                                       |
| ro     | Romanian                                         |
| ru     | Russian                                          |
| sk     | Slovak                                           |
| sl     | Slovene                                          |
| so     | Somali                                           |
| sq     | Albanian                                         |
| sv     | Swedish                                          |
| sw     | Swahili                                          |
| ta     | Tamil                                            |
| te     | Telugu                                           |
| th     | Thai                                             |
| tl     | Tagalog                                          |
| tr     | Turkish                                          |
| uk     | Ukrainian                                        |
| ur     | Urdu                                             |
| vi     | Vietnamese                                       |
| zh-cn  | Chinese                                          |
| zh-tw  | Traditional Chinese (Taiwan, Hongkong and Macau) |

### Quick detection of CJK languages

Furthermore, the library offers a highly accurate CJK language detection mode specifically designed for short strings
where there can be a mix of CJK/Latin/Numeric characters.

The library bypasses the performance bottlenecks of traditional machine learning or n-gram based solutions,
which are ill-suited for such limited / mixed text. By directly iterating over characters, the library efficiently
identifies CJK script usage, enabling rapid and precise language classification. This direct character analysis is
significantly faster and simpler for short texts, avoiding the complexities of statistical models.

### Model parameters

These settings can be set as ENV vars to modify language detection. [TBD / WiP]

Use with caution. You don't need to modify settings. This list is just for the sake of completeness.
For successful modification of the model parameters, you should study the source code and be familiar with
probabilistic matching using naive bayes with character n-gram. See also Ted Dunning, [Statistical Identification of Language](https://www.researchgate.net/publication/2263394_Statistical_Identification_of_Language), 1994.

| Name              | Description                                                                 |
|-------------------|-----------------------------------------------------------------------------|
| `number_of_trials`| Number of trials, affects CPU usage (default: 7)                             |
| `alpha`           | Additional smoothing parameter, default: 0.5                                |
| `alpha_width`     | The width of smoothing, default: 0.05                                        |
| `iteration_limit` | Safeguard to break loop, default: 10000                                      |
| `prob_threshold`  | Default: 0.1                                                                |
| `conv_threshold`  | Detection is terminated when normalized probability exceeds this threshold, default: 0.99999 |
| `base_freq`       | Default: 10000                                                              |

## How to use?

Search language detection can be used programmatically in your own code

### Basic usage

The API is fairly straightforward that allows to configure the language detector via a builder. The public API of the library never returns `null`.

The following is a reasonable configuration:
```java
final LanguageDetectionSettings languageDetectionSettings =
  LanguageDetectionSettings
    .fromIsoCodes639_1("en,ja,es,fr,de,it,zh-cn") // or: en, ja, es, fr, de, it, zh-cn
    .withClassifyChineseAsJapanese()
    .build();

final LanguageDetectionOrchestrator orchestrator = new LanguageDetectionOrchestrator(languageDetectionSettings);
final Language language = orchestrator.detect("languages are awesome");

final String languageCode = language.getIsoCode639_1();
final double probability = language.getProbability();
```

### Methods to build the LanguageDetectionSettings

#### Configuring ISO 639-1 codes

In some classification tasks, you may already know that your language data is not written in the Latin script, such as with languages that use different alphabets. In these situations, the accuracy of language detection can improve by either excluding unrelated languages from the process or by focusing specifically on the languages that are relevant:

`.fromAllIsoCodes639_1()`
- **Default**: N/A
- **Description**: Enables the library to perform language detection for all the 53 languages by the ISO 639-1 codes

`.fromIsoCodes639_1(String)`
- **Default**: N/A
- **Description**: Enables the library to perform language detection for specific languages by the ISO 639-1 codes

```java
LanguageDetectionSettings
    .fromAllIsoCodes639_1()
    .build();

LanguageDetectionSettings
    .fromIsoCodes639_1("en,ja,es,fr,de,it,zh-cn")
    .build();
```

#### Maximum text chars

`.withMaxTextChars(Integer)`
- **Default**: `7,000`. The default limit is set to `7,000` characters (this corresponds to around 3 to 4 page document). For comparison, in Solr, the default maximum text length is set to `20,000` characters.
- **Description**: Restricts the maximum number of characters from the input text that will be processed for language detection by the library. This functionality is valuable because the library does not need to analyze the entire document to accurately detect the language; a sufficient portion of the text is often enough to achieve reliable results.


```java
LanguageDetectionSettings
    .fromIsoCodes639_1("en,ja,es,fr,de,it,zh-cn")
    .withMaxTextChars(3000)
    .build();
```

#### Skipping input sanitization for search

`.withoutSanitizeForSearch()`
- **Default**: `true` (perform input sanitization for search). By default, the library sanitizes short input strings for search purposes by removing file extensions from any part of the text and filtering out Solr boolean operators (AND, NOT, and OR), as these elements are irrelevant to language detection.
- **Description**: Invoking the API bypasses this sanitization process for short input strings, allowing the text to be processed without such modifications.


```java
LanguageDetectionSettings
    .fromIsoCodes639_1("en,ja,es,fr,de,it,zh-cn")
    .withoutSanitizeForSearch()
    .build();
```

#### Classify any Chinese content as Japanese

`.withClassifyChineseAsJapanese()`
- **Default**: `false` (does not classify Chinese text as Japanese)
- **Description**: Invoking this API enables the classification of Kanji-only text (text containing only Chinese characters, without any Japanese Hiragana or Katakana characters) or mixed text containing both Latin and Kanji characters as Japanese. This functionality is particularly important when Japanese identification must be prioritized. As such, this config option aims to optimize for more accurate language detection to minimize the misclassification of Japanese text. Additionally, this approach proves useful when identifying the language of very short strings.


```java
LanguageDetectionSettings
    .fromIsoCodes639_1("en,ja,es,fr,de,it,zh-cn")
    .withClassifyChineseAsJapanese()
    .build();
```

#### Minimum detection certainty

`.withMininumCertainty(Double, String)`
- **Default**: `0.65, "en"`. Specifies a certainty threshold value between `0...1` and a fallback language ISO 639-1 code. These are the same values that are currently used in Production in Solr.
- **Description**: The language identification probability must reach the threshold value before the library accepts it. In case if the threshold has not been reached, the library falls back on the configured ISO 639-1 code as a detected language.


```java
LanguageDetectionSettings
    .fromIsoCodes639_1("en,ja,es,fr,de,it,zh-cn")
    .withMininumCertainty(0.65, "en")
    .build();
```

## Local development

### System requirements

- The plugin keeps Java 11 source compatibility at the moment
- At least JDK 11

### Build system

The plugin uses [Gradle](https://gradle.org/) for as a build system.

#### List of Gradle tasks

For list of all the available Gradle tasks, run the following command:

```bash
./gradlew tasks
```

#### Building

Building and packaging can be done with the following command:

```bash
./gradlew build
```

#### Formatting

Formatting the sources can be done with the following command:

```bash
./gradlew spotlessApply
```

[`Back to top`](#table-of-contents)

### Testing

#### Unit tests

To run unit tests, run the following command:

```bash
./gradlew test
```

[`Back to top`](#table-of-contents)

