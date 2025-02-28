# Language Detection

[![Build and Test](https://github.com/azagniotov/language-detection/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/azagniotov/language-detection/actions/workflows/ci.yml)
[![Maven Central][maven-badge]][maven-link]
[![GitHub Packages][github-badge]][github-link]

This is a refined and re-implemented version of the archived plugin for ElasticSearch [elasticsearch-langdetect](https://github.com/jprante/elasticsearch-langdetect), which itself builds upon the original work by Nakatani Shuyo, found at https://github.com/shuyo/language-detection. The aforementioned implementation by Nakatani Shuyo serves as the default language detection component within Apache Solr.

## Table of Contents
<!-- TOC -->
* [Language Detection](#language-detection)
  * [Table of Contents](#table-of-contents)
  * [About this library](#about-this-library)
    * [Enhancements over past implementations](#enhancements-over-past-implementations)
    * [Supported ISO 639-1 codes](#supported-iso-639-1-codes)
    * [Model parameters](#model-parameters)
    * [Quick detection of CJK languages](#quick-detection-of-cjk-languages)
  * [How to use?](#how-to-use)
    * [Basic usage](#basic-usage)
    * [Methods to build the LanguageDetectionSettings](#methods-to-build-the-languagedetectionsettings)
      * [Configuring ISO 639-1 codes](#configuring-iso-639-1-codes)
      * [Maximum text chars](#maximum-text-chars)
      * [Skipping input sanitization for search](#skipping-input-sanitization-for-search)
      * [Classify any Chinese content as Japanese](#classify-any-chinese-content-as-japanese)
      * [General minimum detection certainty](#general-minimum-detection-certainty)
      * [Minimum detection certainty for top language with a fallback](#minimum-detection-certainty-for-top-language-with-a-fallback)
  * [Local development](#local-development)
    * [System requirements](#system-requirements)
    * [Pre-commit Hook](#pre-commit-hook)
    * [Build system](#build-system)
      * [List of Gradle tasks](#list-of-gradle-tasks)
      * [Building](#building)
      * [Formatting](#formatting)
    * [Testing](#testing)
      * [Unit tests](#unit-tests)
    * [Classification accuracy analysis](#classification-accuracy-analysis)
<!-- TOC -->

## About this library

The library leverages an n-gram probabilistic model, utilizing n-grams of sizes ranging from 1 to 3, alongside a Bayesian filter that incorporates various normalization techniques and feature sampling methods.

The precision is over **99%** for **72** languages. See the following PR description to read about the benchmaks done by @yanirs : https://github.com/jprante/elasticsearch-langdetect/pull/69

### Enhancements over past implementations

The current version of the library introduces several enhancements compared to previous implementations, which may offer improvements in efficiency and performance under specific conditions.

For clarity, I'm linking these enhancements to the original implementation with examples:

1. **Eliminating unnecessary ArrayList resizing** during n-gram extraction from the input string. In the current implementation, the ArrayList is pre-allocated based on the estimated number of n-grams, thereby reducing the overhead caused by element copying during resizing.
[See the original code here](https://github.com/shuyo/language-detection/blob/c92ca72192b79ac421e809de46d5d0dafaef98ef/src/com/cybozu/labs/langdetect/Detector.java#L278).

2. **Removing per-character normalization at runtime**. In the current implementation, instead of normalizing characters during execution, all `65,535` Unicode BMP characters are pre-normalized into a char[] array, making runtime normalization a simple array lookup.
[See the original code here](https://github.com/shuyo/language-detection/blob/c92ca72192b79ac421e809de46d5d0dafaef98ef/src/com/cybozu/labs/langdetect/util/NGram.java#L75-L103).

### Supported ISO 639-1 codes

The following is a list of ISO 639-1 languages code supported by the library:


| Language         | Flag                                                                             | Country                   | ISO 639-1 |
|------------------|----------------------------------------------------------------------------------|---------------------------|-----------|
| Afrikaans        | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇿🇦</span> | South Africa              | af        |
| Albanian         | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇦🇱</span> | Albania                   | sq        |
| Amharic          | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇪🇹</span> | Ethiopia                  | am        |
| Arabic           | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇦🇪</span> | UAE                       | ar        |
| Armenian         | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇦🇲</span> | Armenia                   | hy        |
| Azerbaijani      | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇦🇿</span> | Azerbaijan                | az        |
| Bangla           | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇧🇩</span> | Bangladesh                | bn        |
| Basque           | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇪🇸</span> | Spain                     | eu        |
| Breton           | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇫🇷</span> | France                    | br        |
| Bulgarian        | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇧🇬</span> | Bulgaria                  | bg        |
| Catalan          | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇪🇸</span> | Spain                     | ca        |
| Chinese (China)  | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇨🇳</span> | China                     | zh-cn     |
| Chinese (Taiwan) | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇹🇼</span> | Taiwan                    | zh-tw     |
| Croatian         | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇭🇷</span> | Croatia                   | hr        |
| Czech            | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇨🇿</span> | Czech Republic            | cs        |
| Danish           | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇩🇰</span> | Denmark                   | da        |
| Dutch            | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇳🇱</span> | Netherlands               | nl        |
| English          | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇺🇸</span> | United States             | en        |
| Estonian         | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇪🇪</span> | Estonia                   | et        |
| Filipino         | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇵🇭</span> | Philippines               | tl        |
| Finnish          | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇫🇮</span> | Finland                   | fi        |
| French           | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇫🇷</span> | France                    | fr        |
| Georgian         | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇬🇪</span> | Georgia                   | ka        |
| German           | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇩🇪</span> | Germany                   | de        |
| Greek            | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇬🇷</span> | Greece                    | el        |
| Gujarati         | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇮🇳</span> | India                     | gu        |
| Hebrew           | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇮🇱</span> | Israel                    | he        |
| Hindi            | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇮🇳</span> | India                     | hi        |
| Hungarian        | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇭🇺</span> | Hungary                   | hu        |
| Indonesian       | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇮🇩</span> | Indonesia                 | id        |
| Irish            | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇮🇪</span> | Ireland                   | ga        |
| Italian          | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇮🇹</span> | Italy                     | it        |
| Japanese         | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇯🇵</span> | Japan                     | ja        |
| Kannada          | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇮🇳</span> | India                     | kn        |
| Kazakh           | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇰🇿</span> | Kazakhstan                | kk        |
| Korean           | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇰🇷</span> | South Korea               | ko        |
| Kyrgyz           | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇰🇬</span> | Kyrgyzstan                | ky        |
| Latvian          | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇱🇻</span> | Latvia                    | lv        |
| Lithuanian       | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇱🇹</span> | Lithuania                 | lt        |
| Luxembourgish    | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇱🇺</span> | Luxembourg                | lb        |
| Macedonian       | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇲🇰</span> | North Macedonia           | mk        |
| Malayalam        | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇮🇳</span> | India                     | ml        |
| Marathi          | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇮🇳</span> | India                     | mr        |
| Mongolian        | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇲🇳</span> | Mongolia                  | mn        |
| Nepali           | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇳🇵</span> | Nepal                     | ne        |
| Norwegian        | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇳🇴</span> | Norway                    | no        |
| Persian          | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇮🇷</span> | Iran                      | fa        |
| Polish           | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇵🇱</span> | Poland                    | pl        |
| Portuguese       | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇵🇹</span> | Portugal                  | pt        |
| Punjabi          | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇮🇳</span> | India                     | pa        |
| Romanian         | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇷🇴</span> | Romania                   | ro        |
| Russian          | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇷🇺</span> | Russia                    | ru        |
| Serbian          | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇷🇸</span> | Serbia                    | sr        |
| Sinhala          | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇱🇰</span> | Sri Lanka                 | si        |
| Slovak           | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇸🇰</span> | Slovakia                  | sk        |
| Slovenian        | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇸🇮</span> | Slovenia                  | sl        |
| Somali           | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇸🇴</span> | Somalia                   | so        |
| Spanish          | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇪🇸</span> | Spain                     | es        |
| Swahili          | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇹🇿</span> | Tanzania                  | sw        |
| Swedish          | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇸🇪</span> | Sweden                    | sv        |
| Tajik            | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇹🇯</span> | Tajikistan                | tg        |
| Tamil            | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇮🇳</span> | India                     | ta        |
| Telugu           | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇮🇳</span> | India                     | te        |
| Thai             | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇹🇭</span> | Thailand                  | th        |
| Tibetan          | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇨🇳</span> | China                     | bo        |
| Tigrinya         | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇪🇷</span> | Eritrea                   | ti        |
| Turkish          | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇹🇷</span> | Turkey                    | tr        |
| Ukrainian        | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇺🇦</span> | Ukraine                   | uk        |
| Urdu             | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇵🇰</span> | Pakistan                  | ur        |
| Vietnamese       | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇻🇳</span> | Vietnam                   | vi        |
| Welsh            | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇬🇧</span> | United Kingdom            | cy        |
| Yiddish          | <span style="font-size: 2em; display: inline-block; line-height: 1;">🇮🇱</span> | Israel                    | yi        |

### Model parameters

These settings can be set as ENV vars to modify language detection. [TBD / WiP]

Use with caution. You don't need to modify settings. This list is just for the sake of completeness.
For successful modification of the model parameters, you should study the source code and be familiar with
probabilistic matching using naive bayes with character n-gram. See also Ted Dunning, [Statistical Identification of Language](https://www.researchgate.net/publication/2263394_Statistical_Identification_of_Language), 1994.

| Name               | Description                                                                                  |
|--------------------|----------------------------------------------------------------------------------------------|
| `number_of_trials` | Number of trials, affects CPU usage (default: 7)                                             |
| `alpha`            | Additional smoothing parameter, default: 0.5                                                 |
| `alpha_width`      | The width of smoothing, default: 0.05                                                        |
| `iteration_limit`  | Safeguard to break loop, default: 10000                                                      |
| `conv_threshold`   | Detection is terminated when normalized probability exceeds this threshold, default: 0.99999 |
| `base_freq`        | Default: 10000                                                                               |

### Quick detection of CJK languages

Furthermore, the library offers a highly accurate CJK language detection mode specifically designed for short strings
where there can be a mix of CJK/Latin/Numeric characters.

The library bypasses the performance bottlenecks of traditional machine learning or n-gram based solutions,
which are ill-suited for such limited / mixed text. By directly iterating over characters, the library efficiently
identifies CJK script usage, enabling rapid and precise language classification. This direct character analysis is
significantly faster and simpler for short texts, avoiding the complexities of statistical models.

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

[`Back to top`](#table-of-contents)

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

[`Back to top`](#table-of-contents)

#### Maximum text chars

`.withMaxTextChars(Integer)`
- **Default**: `3,000`. The default limit is set to `3,000` characters (this corresponds to around 2 to 3 page document). For comparison, in Solr, the default maximum text length is set to `20,000` characters.
- **Description**: Restricts the maximum number of characters from the input text that will be processed for language detection by the library. This functionality is valuable because the library does not need to analyze the entire document to accurately detect the language; a sufficient portion of the text is often enough to achieve reliable results.


```java
LanguageDetectionSettings
    .fromIsoCodes639_1("en,ja,es,fr,de,it,zh-cn")
    .withMaxTextChars(3000)
    .build();
```

[`Back to top`](#table-of-contents)

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

[`Back to top`](#table-of-contents)

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

[`Back to top`](#table-of-contents)

#### General minimum detection certainty

`.withMininumCertainty(Float)`
- **Default**: `0.1f`. Specifies a certainty threshold value between `0...1`.
- **Description**: The library requires that the language identification probability surpass a predefined threshold for any detected language. If the probability falls short of this threshold, the library systematically filters out those languages, excluding them from the results.

Please be aware that the `.withMininumCertainty(Float)` method cannot be used in conjunction with the `.withTopLanguageMininumCertainty(Float, String)` method (explained in the next section). The setting that is applied last during the configuration process will take priority.

```java
LanguageDetectionSettings
    .fromIsoCodes639_1("en,ja,es,fr,de,it,zh-cn")
    .withMininumCertainty(0.65f)
    .build();
```

[`Back to top`](#table-of-contents)

#### Minimum detection certainty for top language with a fallback

`.withTopLanguageMininumCertainty(Float, String)`
- **Default**: Not set. Specifies a certainty threshold value between `0...1` and a fallback language ISO 639-1 code.
- **Description**: The language identification probability must exceed the threshold value for the top detected language. If this threshold is not met, the library defaults to the configured ISO 639-1 fallback code, treating it as the top and sole detected language.

Please be aware that the `.withTopLanguageMininumCertainty(Float, String)` method cannot be used in conjunction with the `.withMinimumCertainty(Float)` method (explained in the previous section). The setting that is applied last during the configuration process will take priority.

```java
LanguageDetectionSettings
    .fromIsoCodes639_1("en,ja,es,fr,de,it,zh-cn")
    .withTopLanguageMininumCertainty(0.65f, "en")
    .build();
```

[`Back to top`](#table-of-contents)

## Local development

### System requirements

- The plugin keeps Java 11 source compatibility at the moment
- At least JDK 11

### Pre-commit Hook

Before your first commit, run this command in the root project directory:

```
cp pre-commit .git/hooks
```

If you forget to do this, there is a Gradle task defined in [build.gradle](./build.gradle) that installs the hook for you.

[`Back to top`](#table-of-contents)

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

The sources will be auto-formatted using Google Java format upon each commit. But, should there ba  need to manually format, run the following command:

```bash
./gradlew googleJavaFormat
```

[`Back to top`](#table-of-contents)

### Testing

#### Unit tests

To run unit tests, run the following command:

```bash
./gradlew test
```

[`Back to top`](#table-of-contents)

### Classification accuracy analysis

The classification accuracy analysis help to improve our understanding of how the library performs on texts of various lengths and types, see [src/accuracyTest/java/io/github/azagniotov/language/LanguageDetectorAccuracyTest.java](src/accuracyTest/java/io/github/azagniotov/language/LanguageDetectorAccuracyTest.java)

To run the classification accuracy tests and generate an accuracy report CSV, run the following command:

```bash
./gradlew clean accuracyTest
```

The generated report will be found under `build/reports/accuracy/accuracy-report-<UNIX_TIMESTAMP>.csv`

[`Back to top`](#table-of-contents)


[maven-badge]: https://img.shields.io/maven-central/v/io.github.azagniotov/language-detection.svg?style=flat&label=maven-central
[maven-link]: https://central.sonatype.com/search?q=g:io.github.azagniotov%20%20a:language-detection

[github-badge]: https://img.shields.io/github/v/release/azagniotov/language-detection?label=github-packages&color=green
[github-link]: https://github.com/azagniotov/language-detection/packages/2402358
