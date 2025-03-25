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
      * [Skipping input sanitization](#skipping-input-sanitization)
      * [Classify any Chinese content as Japanese](#classify-any-chinese-content-as-japanese)
      * [General minimum detection certainty](#general-minimum-detection-certainty)
      * [Minimum detection certainty for top language with a fallback](#minimum-detection-certainty-for-top-language-with-a-fallback)
  * [Running language detection benchmarks](#running-language-detection-benchmarks)
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

The library leverages an n-gram probabilistic model, utilizing n-grams of sizes ranging from `1` to `3` (incl.), alongside a Bayesian classifier (Naive Bayes classification algorithm, see [LanguageDetector#detectBlock(String)](src/main/java/io/github/azagniotov/language/LanguageDetector.java)) that incorporates various normalization techniques and feature sampling methods.

The precision is over **99%** for **74** languages. See the following PR description to read about the benchmaks done by [@yanirs](https://github.com/yanirs) : https://github.com/jprante/elasticsearch-langdetect/pull/69

### Enhancements over past implementations

The current version of the library introduces several enhancements compared to previous implementations, which may offer improvements in efficiency and performance under specific conditions.

For clarity, I'm linking these enhancements to the original implementation with examples:

1. **Eliminating unnecessary ArrayList resizing** during n-gram extraction from the input string. In the current implementation, the ArrayList is pre-allocated based on the estimated number of n-grams, thereby reducing the overhead caused by element copying during resizing.
[See the original code here](https://github.com/shuyo/language-detection/blob/c92ca72192b79ac421e809de46d5d0dafaef98ef/src/com/cybozu/labs/langdetect/Detector.java#L278).

2. **Removing per-character normalization at runtime**. In the current implementation, instead of normalizing characters during execution, all `65,535` Unicode BMP characters are pre-normalized into a char[] array, making runtime normalization a simple array lookup.
[See the original code here](https://github.com/shuyo/language-detection/blob/c92ca72192b79ac421e809de46d5d0dafaef98ef/src/com/cybozu/labs/langdetect/util/NGram.java#L75-L103).

3. **Using a float-level precision**. Since Java's `double`-level precision is not neccessary for the current library, a switch to `float` type has been made when storing and computing probabilities. This will improve memory efficiency, and may also potentially provide a slight performance boost. Modern CPUs are very efficient at floating point calculations, so the performance increase may be small, but it will be there.

### Supported ISO 639-1 codes

The following is a list of ISO 639-1 languages code supported by the library:

| Language           | ISO 639-1 | Language family                                 | Country         | Flag                         |
|--------------------|-----------|-------------------------------------------------|-----------------|------------------------------|
| Afrikaans          | af        | Indo-European / Germanic / West Germanic        | South Africa    | &nbsp;&nbsp;🇿🇦&nbsp;&nbsp; |
| Albanian           | sq        | Indo-European / Albanoid / Albanian             | Albania         | &nbsp;&nbsp;🇦🇱&nbsp;&nbsp; |
| Amharic            | am        | Afro-Asiatic / Semitic / South Semitic          | Ethiopia        | &nbsp;&nbsp;🇪🇹&nbsp;&nbsp; |
| Arabic             | ar        | Afro-Asiatic / Semitic / West Semitic           | UAE             | &nbsp;&nbsp;🇦🇪&nbsp;&nbsp; |
| Armenian           | hy        | Indo-European / Armenian                        | Armenia         | &nbsp;&nbsp;🇦🇲&nbsp;&nbsp; |
| Azerbaijani        | az        | Turkic / Oghuz / Western Oghuz                  | Azerbaijan      | &nbsp;&nbsp;🇦🇿&nbsp;&nbsp; |
| Bangla             | bn        | Indo-European / Indo-Iranian / Indo-Aryan       | Bangladesh      | &nbsp;&nbsp;🇧🇩&nbsp;&nbsp; |
| Basque             | eu        | Isolate                                         | Spain           | &nbsp;&nbsp;🇪🇸&nbsp;&nbsp; |
| Breton             | br        | Indo-European / Celtic / Brythonic              | France          | &nbsp;&nbsp;🇫🇷&nbsp;&nbsp; |
| Bulgarian          | bg        | Indo-European / Balto-Slavic / Slavic           | Bulgaria        | &nbsp;&nbsp;🇧🇬&nbsp;&nbsp; |
| Catalan            | ca        | Indo-European / Italic / Romance                | Spain           | &nbsp;&nbsp;🇪🇸&nbsp;&nbsp; |
| Chinese (China)    | zh-cn     | Sino-Tibetan / Sinitic                          | China           | &nbsp;&nbsp;🇨🇳&nbsp;&nbsp; |
| Chinese (Taiwan)   | zh-tw     | Sino-Tibetan / Sinitic                          | Taiwan          | &nbsp;&nbsp;🇹🇼&nbsp;&nbsp; |
| Cornish (Kernewek) | kw        | Indo-European / Celtic / Brythonic              | United Kingdom  | &nbsp;&nbsp;🇬🇧&nbsp;&nbsp; |
| Croatian           | hr        | Indo-European / Balto-Slavic / Slavic           | Croatia         | &nbsp;&nbsp;🇭🇷&nbsp;&nbsp; |
| Czech              | cs        | Indo-European / Balto-Slavic / Slavic           | Czech Republic  | &nbsp;&nbsp;🇨🇿&nbsp;&nbsp; |
| Danish             | da        | Indo-European / Germanic / North Germanic       | Denmark         | &nbsp;&nbsp;🇩🇰&nbsp;&nbsp; |
| Dutch              | nl        | Indo-European / Germanic / West Germanic        | Netherlands     | &nbsp;&nbsp;🇳🇱&nbsp;&nbsp; |
| English            | en        | Indo-European / Germanic / West Germanic        | United States   | &nbsp;&nbsp;🇺🇸&nbsp;&nbsp; |
| Estonian           | et        | Uralic / Finnic                                 | Estonia         | &nbsp;&nbsp;🇪🇪&nbsp;&nbsp; |
| Filipino           | tl        | Austronesian / Malayo-Polynesian / Philippine   | Philippines     | &nbsp;&nbsp;🇵🇭&nbsp;&nbsp; |
| Finnish            | fi        | Uralic / Finnic                                 | Finland         | &nbsp;&nbsp;🇫🇮&nbsp;&nbsp; |
| French             | fr        | Indo-European / Italic / Romance                | France          | &nbsp;&nbsp;🇫🇷&nbsp;&nbsp; |
| Georgian           | ka        | Kartvelian / Karto-Zan                          | Georgia         | &nbsp;&nbsp;🇬🇪&nbsp;&nbsp; |
| German             | de        | Indo-European / Germanic / West Germanic        | Germany         | &nbsp;&nbsp;🇩🇪&nbsp;&nbsp; |
| Greek              | el        | Indo-European / Hellenic                        | Greece          | &nbsp;&nbsp;🇬🇷&nbsp;&nbsp; |
| Gujarati           | gu        | Indo-European / Indo-Iranian / Indo-Aryan       | India           | &nbsp;&nbsp;🇮🇳&nbsp;&nbsp; |
| Hebrew             | he        | Afro-Asiatic / Semitic / West Semitic           | Israel          | &nbsp;&nbsp;🇮🇱&nbsp;&nbsp; |
| Hindi              | hi        | Indo-European / Indo-Iranian / Indo-Aryan       | India           | &nbsp;&nbsp;🇮🇳&nbsp;&nbsp; |
| Hungarian          | hu        | Uralic / Ugric                                  | Hungary         | &nbsp;&nbsp;🇭🇺&nbsp;&nbsp; |
| Indonesian         | id        | Austronesian / Malayo-Polynesian / Sundic       | Indonesia       | &nbsp;&nbsp;🇮🇩&nbsp;&nbsp; |
| Irish              | ga        | Indo-European / Celtic / Goidelic               | Ireland         | &nbsp;&nbsp;🇮🇪&nbsp;&nbsp; |
| Italian            | it        | Indo-European / Italic / Latino-Faliscan        | Italy           | &nbsp;&nbsp;🇮🇹&nbsp;&nbsp; |
| Japanese           | ja        | Japonic                                         | Japan           | &nbsp;&nbsp;🇯🇵&nbsp;&nbsp; |
| Kannada            | kn        | Dravidian / Southern Dravidian                  | India           | &nbsp;&nbsp;🇮🇳&nbsp;&nbsp; |
| Kazakh             | kk        | Turkic / Common Turkic/ Kipchak                 | Kazakhstan      | &nbsp;&nbsp;🇰🇿&nbsp;&nbsp; |
| Korean             | ko        | Koreanic                                        | South Korea     | &nbsp;&nbsp;🇰🇷&nbsp;&nbsp; |
| Kyrgyz             | ky        | Turkic / Common Turkic/ Kipchak                 | Kyrgyzstan      | &nbsp;&nbsp;🇰🇬&nbsp;&nbsp; |
| Latvian            | lv        | Indo-European / Balto-Slavic / Baltic           | Latvia          | &nbsp;&nbsp;🇱🇻&nbsp;&nbsp; |
| Lithuanian         | lt        | Indo-European / Balto-Slavic / Baltic           | Lithuania       | &nbsp;&nbsp;🇱🇹&nbsp;&nbsp; |
| Luxembourgish      | lb        | Indo-European / Germanic / West Germanic        | Luxembourg      | &nbsp;&nbsp;🇱🇺&nbsp;&nbsp; |
| Macedonian         | mk        | Indo-European / Balto-Slavic / Slavic           | North Macedonia | &nbsp;&nbsp;🇲🇰&nbsp;&nbsp; |
| Malayalam          | ml        | Dravidian / Southern Dravidian                  | India           | &nbsp;&nbsp;🇮🇳&nbsp;&nbsp; |
| Manx               | gv        | Indo-European / Celtic / Goidelic               | Isle of Man     | &nbsp;&nbsp;🇮🇲&nbsp;&nbsp; |
| Marathi            | mr        | Indo-European / Indo-Iranian / Indo-Aryan       | India           | &nbsp;&nbsp;🇮🇳&nbsp;&nbsp; |
| Mongolian          | mn        | Mongolic / Central Mongolic / Buryat–Mongolian  | Mongolia        | &nbsp;&nbsp;🇲🇳&nbsp;&nbsp; |
| Nepali             | ne        | Indo-European / Indo-Iranian / Indo-Aryan       | Nepal           | &nbsp;&nbsp;🇳🇵&nbsp;&nbsp; |
| Norwegian          | no        | Indo-European / Germanic / North Germanic       | Norway          | &nbsp;&nbsp;🇳🇴&nbsp;&nbsp; |
| Persian            | fa        | Indo-European / Indo-Iranian / Iranian          | Iran            | &nbsp;&nbsp;🇮🇷&nbsp;&nbsp; |
| Polish             | pl        | Indo-European / Balto-Slavic / Slavic           | Poland          | &nbsp;&nbsp;🇵🇱&nbsp;&nbsp; |
| Portuguese         | pt        | Indo-European / Italic / Latino-Faliscan        | Portugal        | &nbsp;&nbsp;🇵🇹&nbsp;&nbsp; |
| Punjabi            | pa        | Indo-European / Indo-Iranian / Indo-Aryan       | India           | &nbsp;&nbsp;🇮🇳&nbsp;&nbsp; |
| Romanian           | ro        | Indo-European / Italic / Romance                | Romania         | &nbsp;&nbsp;🇷🇴&nbsp;&nbsp; |
| Russian            | ru        | Indo-European / Balto-Slavic / Slavic           | Russia          | &nbsp;&nbsp;🇷🇺&nbsp;&nbsp; |
| Serbian            | sr        | Indo-European / Balto-Slavic / Slavic           | Serbia          | &nbsp;&nbsp;🇷🇸&nbsp;&nbsp; |
| Sinhala            | si        | Indo-European / Indo-Iranian / Indo-Aryan       | Sri Lanka       | &nbsp;&nbsp;🇱🇰&nbsp;&nbsp; |
| Slovak             | sk        | Indo-European / Balto-Slavic / Slavic           | Slovakia        | &nbsp;&nbsp;🇸🇰&nbsp;&nbsp; |
| Slovenian          | sl        | Indo-European / Balto-Slavic / Slavic           | Slovenia        | &nbsp;&nbsp;🇸🇮&nbsp;&nbsp; |
| Somali             | so        | Afro-Asiatic / Cushitic / Lowland East Cushitic | Somalia         | &nbsp;&nbsp;🇸🇴&nbsp;&nbsp; |
| Spanish            | es        | Indo-European / Italic / Latino-Faliscan        | Spain           | &nbsp;&nbsp;🇪🇸&nbsp;&nbsp; |
| Swahili            | sw        | Niger-Congo / Atlantic-Congo / Bantu            | Tanzania        | &nbsp;&nbsp;🇹🇿&nbsp;&nbsp; |
| Swedish            | sv        | Indo-European / Germanic / North Germanic       | Sweden          | &nbsp;&nbsp;🇸🇪&nbsp;&nbsp; |
| Tajik              | tg        | Indo-European / Indo-Iranian / Iranian          | Tajikistan      | &nbsp;&nbsp;🇹🇯&nbsp;&nbsp; |
| Tamil              | ta        | Dravidian / Southern Dravidian                  | India           | &nbsp;&nbsp;🇮🇳&nbsp;&nbsp; |
| Telugu             | te        | Dravidian / South-Central Dravidian             | India           | &nbsp;&nbsp;🇮🇳&nbsp;&nbsp; |
| Thai               | th        | Kra-Dai / Tai / Southwestern Tai                | Thailand        | &nbsp;&nbsp;🇹🇭&nbsp;&nbsp; |
| Tibetan            | bo        | Sino-Tibetan / Tibeto-Burman                    | China           | &nbsp;&nbsp;🇨🇳&nbsp;&nbsp; |
| Tigrinya           | ti        | Afro-Asiatic / Semitic / South Semitic          | Eritrea         | &nbsp;&nbsp;🇪🇷&nbsp;&nbsp; |
| Turkish            | tr        | Turkic / Common Turkic / Oghuz                  | Turkey          | &nbsp;&nbsp;🇹🇷&nbsp;&nbsp; |
| Ukrainian          | uk        | Indo-European / Balto-Slavic / Slavic           | Ukraine         | &nbsp;&nbsp;🇺🇦&nbsp;&nbsp; |
| Urdu               | ur        | Indo-European / Indo-Iranian / Indo-Aryan       | Pakistan        | &nbsp;&nbsp;🇵🇰&nbsp;&nbsp; |
| Vietnamese         | vi        | Austroasiatic / Vietic                          | Vietnam         | &nbsp;&nbsp;🇻🇳&nbsp;&nbsp; |
| Welsh              | cy        | Indo-European / Celtic / Brythonic              | United Kingdom  | &nbsp;&nbsp;🇬🇧&nbsp;&nbsp; |
| Yiddish            | yi        | Indo-European / Germanic / West Germanic        | Israel          | &nbsp;&nbsp;🇮🇱&nbsp;&nbsp; |


### Model parameters

The following model [src/main/resources/model/parameters.json](src/main/resources/model/parameters.json) can be configured as ENV vars to modify language detection at runtime.

Use with caution. You don't need to modify the default settings. This list is just for the sake of completeness.
For successful modification of the model parameters, you should study the source code (see [LanguageDetector#detectBlock(String)](src/main/java/io/github/azagniotov/language/LanguageDetector.java)) to familiarize yourself with probabilistic matching using Naive Bayes classification algorithm with character n-gram. See also Ted Dunning, [Statistical Identification of Language](https://www.researchgate.net/publication/2263394_Statistical_Identification_of_Language), 1994.

| Name                   | Configured by the ENV variable          | Description                                                                                                                          |
|------------------------|-----------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| `baseFrequency`        | `LANGUAGE_DETECT_BASE_FREQUENCY`        | Default: `10000`                                                                                                                     |
| `iterationLimit`       | `LANGUAGE_DETECT_ITERATION_LIMIT`       | Safeguard to break loop. Default: `10000`                                                                                            |
| `numberOfTrials`       | `LANGUAGE_DETECT_NUMBER_OF_TRIALS`      | Number of trials (affects CPU usage). Default: `7`                                                                                   |
| `alpha`                | `LANGUAGE_DETECT_ALPHA`                 | Naive Bayes classifier smoothing parameterto prevent zero probabilities and improve the robustness of the classifier. Default: `0.5` |
| `alphaWidth`           | `LANGUAGE_DETECT_ALPHA_WIDTH`           | The width of smoothing. Default: `0.05`                                                                                              |
| `convergenceThreshold` | `LANGUAGE_DETECT_CONVERGENCE_THRESHOLD` | Detection is terminated when normalized probability exceeds this threshold. Default: `0.99999`                                       |

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

```java
LanguageDetectionSettings
    .fromAllIsoCodes639_1()
    .build();
```

`.fromIsoCodes639_1(String)`
- **Default**: N/A
- **Description**: Enables the library to perform language detection for specific languages by the ISO 639-1 codes

```java
LanguageDetectionSettings
    .fromIsoCodes639_1("en,ja,es,fr,de,it,zh-cn")
    .build();
```

[`Back to top`](#table-of-contents)

#### Maximum text chars

`.withMaxTextChars(Integer)`
- **Default**: `2,000`. The default limit is set to `2,000` characters (this corresponds to around 2 to 3-page document). For comparison, in Solr, the default maximum text length is set to `20,000` characters.
- **Description**: Restricts the maximum number of characters from the input text that will be processed for language detection by the library. This functionality is valuable because the library does not need to analyze the entire document to accurately detect the language; a sufficient portion of the text is often enough to achieve reliable results.


```java
LanguageDetectionSettings
    .fromIsoCodes639_1("en,ja,es,fr,de,it,zh-cn")
    .withMaxTextChars(2000)
    .build();
```

[`Back to top`](#table-of-contents)

#### Skipping input sanitization

`.withoutInputSanitize()`
- **Default**: `false` (input sanitization is enabled by default). By default, the library sanitizes input strings by removing file extensions from any part of the text, URLs and filtering out Solr boolean operators (AND, NOT, and OR), as these elements are irrelevant to language detection.
- **Description**: Invoking the API bypasses this input sanitization process for, allowing the text to be processed without such modifications.


```java
LanguageDetectionSettings
    .fromIsoCodes639_1("en,ja,es,fr,de,it,zh-cn")
    .withoutInputSanitize()
    .build();
```

[`Back to top`](#table-of-contents)

#### Classify any Chinese content as Japanese

`.withClassifyChineseAsJapanese()`
- **Default**: `false` (does not classify Chinese text as Japanese)
- **Description**: Invoking this API enables the classification of Kanji-only text (text containing only Chinese characters, without any Japanese Hiragana or Katakana characters) or mixed text containing both Latin and Kanji characters as Japanese. This functionality is particularly important when we aim to optimize for more accurate language detection to minimize the misclassification of Japanese text. Additionally, this approach proves useful when indexing short strings such as `#7_pj_12345_ABCD_戦` or `SOMETHING_2010下_詳細_20130304.xls`.


```java
LanguageDetectionSettings
    .fromIsoCodes639_1("en,ja,es,fr,de,it,zh-cn")
    .withClassifyChineseAsJapanese()
    .build();
```

[`Back to top`](#table-of-contents)

#### General minimum detection certainty

`.withMininumCertainty(Double)`
- **Default**: `0.1`. Specifies a certainty threshold value between `0...1`.
- **Description**: The library requires that the language identification probability surpass a predefined threshold for any detected language. If the probability falls short of this threshold, the library systematically filters out those languages, excluding them from the results.

Please be aware that the `.withMininumCertainty(Double)` method cannot be used in conjunction with the `.withTopLanguageMininumCertainty(Double, String)` method (explained in the next section). The setting that is applied last during the configuration process will take priority.

```java
LanguageDetectionSettings
    .fromIsoCodes639_1("en,ja,es,fr,de,it,zh-cn")
    .withMininumCertainty(0.65)
    .build();
```

[`Back to top`](#table-of-contents)

#### Minimum detection certainty for top language with a fallback

`.withTopLanguageMininumCertainty(Double, String)`
- **Default**: Not set. Specifies a certainty threshold value between `0...1` and a fallback language ISO 639-1 code.
- **Description**: The language identification probability must exceed the threshold value for the top detected language. If this threshold is not met, the library defaults to the configured ISO 639-1 fallback code, treating it as the top and sole detected language.

Please be aware that the `.withTopLanguageMininumCertainty(Double, String)` method cannot be used in conjunction with the `.withMinimumCertainty(Double)` method (explained in the previous section). The setting that is applied last during the configuration process will take priority.

```java
LanguageDetectionSettings
    .fromIsoCodes639_1("en,ja,es,fr,de,it,zh-cn")
    .withTopLanguageMininumCertainty(0.65, "en")
    .build();
```

[`Back to top`](#table-of-contents)

## Running language detection benchmarks

This library provides an executable Uber JAR, which can be invoked from the command line to perform language detection. The JAR file contains a main method that enables it to be run directly, making it easy to detect languages from files in the local filesystem.

To use the library, direct the JAR to a parent directory on your computer that holds subdirectories for each language. Each subdirectory should be named using the appropriate ISO 639-1 code for that language. Inside each subdirectory, you'll place .txt files that will be used for language detection.

The datasets you provide will be checked against a fixed set of languages: `Japanese (ja)`, `English (en)`, `French (fr)`, `Spanish (es)`, `Italian (it)`, and `German (de)`. For now this is not configurable. PR is pending.

The JAR file accepts the following command-line arguments:

| Status     | Argument name                  |
|------------|--------------------------------|
| `REQUIRED` | `<NUM_OF_WORKERS>`             |
| `REQUIRED` | `<ISO_639-1_CODE_CSV>`         |
| `REQUIRED` | `<ABSOLUTE_PATH_TO_DIRECTORY>` |
| `OPTIONAL` | `<VERBOSE_MODE>`               |

```bash
java -jar build/libs/language-detection-x.x.x.jar <NUM_OF_WORKERS> <ISO_639-1_CODE_CSV> <ABSOLUTE_PATH_TO_DIRECTORY> <VERBOSE_MODE>
```

Example usage:
```bash
java -jar build/libs/language-detection-3.1.0.jar 2 ja,en /Users/aschwarzenegger/datasets true
```
In this example, the argument `<ISO_639-1_CODE_CSV>` specifies a comma-separated list of ISO 639-1 language codes (such as `ja`, `en`, `fr`, etc.). These codes must correspond to the names of the subdirectories located within the specified directory (`<ABSOLUTE_PATH_TO_DIRECTORY>`).

Once the process is complete, a report will be generated and displayed, similar to the example below:

``` bash
Total runtime: 14 seconds and 419 millis. Detection results:

{
  Dataset-DE : { de=58910 , en=173   , es=2     , fr=7     , it=4     , ja=2     , und=1 }
  Dataset-EN : { de=18    , en=59041 , es=9     , fr=22    , it=6     , ja=2     , und=1 }
  Dataset-ES : { de=7     , en=151   , es=58905 , fr=11    , it=22    , ja=2     , und=1 }
  Dataset-FR : { de=17    , en=139   , es=11    , fr=58925 , it=4     , ja=2     , und=1 }
  Dataset-IT : { de=6     , en=209   , es=7     , fr=4     , it=58870 , ja=2     , und=1 }
  Dataset-JA : { en=2     , it=1     , ja=59095 , und=1 }
}
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
