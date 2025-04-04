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
      * [CJK detection threshold](#cjk-detection-threshold)
      * [Classify any Chinese content as Japanese](#classify-any-chinese-content-as-japanese)
      * [General minimum detection certainty](#general-minimum-detection-certainty)
      * [Minimum detection certainty for top language with a fallback](#minimum-detection-certainty-for-top-language-with-a-fallback)
  * [Language detection benchmarks against other libraries](#language-detection-benchmarks-against-other-libraries)
    * [Running the benchmarks](#running-the-benchmarks)
    * [Accuracy report](#accuracy-report)
    * [Speed of execution](#speed-of-execution)
    * [Key takeaways](#key-takeaways)
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

The accuracy exceeds **99%** across **79** languages, encompassing a diverse range, including five (5) Celtic languages that are still actively spoken and ten (10) languages from the African continent.

See the following PR description to read about the benchmaks done by [@yanirs](https://github.com/yanirs) : https://github.com/jprante/elasticsearch-langdetect/pull/69

### Enhancements over past implementations

The current version of the library introduces several enhancements compared to previous implementations, which may offer improvements in efficiency and performance under specific conditions.

For clarity, I'm linking these enhancements to the original implementation with examples:

1. **Eliminating unnecessary ArrayList resizing** during n-gram extraction from the input string. In the current implementation, the ArrayList is pre-allocated based on the estimated number of n-grams, thereby reducing the overhead caused by element copying during resizing.
[See the original code here](https://github.com/shuyo/language-detection/blob/c92ca72192b79ac421e809de46d5d0dafaef98ef/src/com/cybozu/labs/langdetect/Detector.java#L278).

2. **Removing per-character normalization at runtime**. In the current implementation, instead of normalizing characters during execution, all `65,535` Unicode BMP characters are pre-normalized into a char[] array, making runtime normalization a simple array lookup.
[See the original code here](https://github.com/shuyo/language-detection/blob/c92ca72192b79ac421e809de46d5d0dafaef98ef/src/com/cybozu/labs/langdetect/util/NGram.java#L75-L103).

3. **Using a float-level precision**. Since Java's `double`-level precision is not neccessary for the current library, a switch to `float` type has been made when storing and computing probabilities. This will improve memory efficiency, and may also potentially provide a slight performance boost. Modern CPUs are very efficient at floating point calculations, so the performance increase may be small, but it will be there.

### Supported ISO 639-1 codes

The following is a list of languages and their ISO 639-1 language codes supported by the library:

| Language           | ISO 639-1 | Language family                     | Country         | Flag                         |
|--------------------|-----------|-------------------------------------|-----------------|------------------------------|
| Afrikaans          | af        | Indo-European / Germanic            | South Africa    | &nbsp;&nbsp;🇿🇦&nbsp;&nbsp; |
| Albanian           | sq        | Indo-European / Albanoid            | Albania         | &nbsp;&nbsp;🇦🇱&nbsp;&nbsp; |
| Amharic            | am        | Afro-Asiatic / Semitic              | Ethiopia        | &nbsp;&nbsp;🇪🇹&nbsp;&nbsp; |
| Arabic             | ar        | Afro-Asiatic / Semitic              | UAE             | &nbsp;&nbsp;🇦🇪&nbsp;&nbsp; |
| Armenian           | hy        | Indo-European / Armenian            | Armenia         | &nbsp;&nbsp;🇦🇲&nbsp;&nbsp; |
| Azerbaijani        | az        | Turkic  / Western Oghuz             | Azerbaijan      | &nbsp;&nbsp;🇦🇿&nbsp;&nbsp; |
| Bangla             | bn        | Indo-European / Indo-Iranian        | Bangladesh      | &nbsp;&nbsp;🇧🇩&nbsp;&nbsp; |
| Basque             | eu        | Isolate                             | Spain           | &nbsp;&nbsp;🇪🇸&nbsp;&nbsp; |
| Breton             | br        | Indo-European / Celtic              | France          | &nbsp;&nbsp;🇫🇷&nbsp;&nbsp; |
| Bulgarian          | bg        | Indo-European / Balto-Slavic        | Bulgaria        | &nbsp;&nbsp;🇧🇬&nbsp;&nbsp; |
| Catalan            | ca        | Indo-European / Italic              | Spain           | &nbsp;&nbsp;🇪🇸&nbsp;&nbsp; |
| Chinese (China)    | zh-cn     | Sino-Tibetan / Sinitic              | China           | &nbsp;&nbsp;🇨🇳&nbsp;&nbsp; |
| Chinese (Taiwan)   | zh-tw     | Sino-Tibetan / Sinitic              | Taiwan          | &nbsp;&nbsp;🇹🇼&nbsp;&nbsp; |
| Cornish (Kernewek) | kw        | Indo-European / Celtic              | United Kingdom  | &nbsp;&nbsp;🇬🇧&nbsp;&nbsp; |
| Croatian           | hr        | Indo-European / Balto-Slavic        | Croatia         | &nbsp;&nbsp;🇭🇷&nbsp;&nbsp; |
| Czech              | cs        | Indo-European / Balto-Slavic        | Czech Republic  | &nbsp;&nbsp;🇨🇿&nbsp;&nbsp; |
| Danish             | da        | Indo-European / Germanic            | Denmark         | &nbsp;&nbsp;🇩🇰&nbsp;&nbsp; |
| Dutch              | nl        | Indo-European / Germanic            | Netherlands     | &nbsp;&nbsp;🇳🇱&nbsp;&nbsp; |
| English            | en        | Indo-European / Germanic            | United States   | &nbsp;&nbsp;🇺🇸&nbsp;&nbsp; |
| Estonian           | et        | Uralic / Finnic                     | Estonia         | &nbsp;&nbsp;🇪🇪&nbsp;&nbsp; |
| Filipino           | tl        | Austronesian / Malayo-Polynesian    | Philippines     | &nbsp;&nbsp;🇵🇭&nbsp;&nbsp; |
| Finnish            | fi        | Uralic / Finnic                     | Finland         | &nbsp;&nbsp;🇫🇮&nbsp;&nbsp; |
| French             | fr        | Indo-European / Italic              | France          | &nbsp;&nbsp;🇫🇷&nbsp;&nbsp; |
| Georgian           | ka        | Kartvelian / Karto-Zan              | Georgia         | &nbsp;&nbsp;🇬🇪&nbsp;&nbsp; |
| German             | de        | Indo-European / Germanic            | Germany         | &nbsp;&nbsp;🇩🇪&nbsp;&nbsp; |
| Greek              | el        | Indo-European / Hellenic            | Greece          | &nbsp;&nbsp;🇬🇷&nbsp;&nbsp; |
| Gujarati           | gu        | Indo-European / Indo-Iranian        | India           | &nbsp;&nbsp;🇮🇳&nbsp;&nbsp; |
| Hausa              | ha        | Afro-Asiatic / Chadic               | Nigeria         | &nbsp;&nbsp;🇳🇬&nbsp;&nbsp; |
| Hebrew             | he        | Afro-Asiatic / Semitic              | Israel          | &nbsp;&nbsp;🇮🇱&nbsp;&nbsp; |
| Hindi              | hi        | Indo-European / Indo-Iranian        | India           | &nbsp;&nbsp;🇮🇳&nbsp;&nbsp; |
| Hungarian          | hu        | Uralic / Ugric                      | Hungary         | &nbsp;&nbsp;🇭🇺&nbsp;&nbsp; |
| Indonesian         | id        | Austronesian / Malayo-Polynesian    | Indonesia       | &nbsp;&nbsp;🇮🇩&nbsp;&nbsp; |
| Irish              | ga        | Indo-European / Celtic              | Ireland         | &nbsp;&nbsp;🇮🇪&nbsp;&nbsp; |
| Italian            | it        | Indo-European / Italic              | Italy           | &nbsp;&nbsp;🇮🇹&nbsp;&nbsp; |
| Japanese           | ja        | Japonic                             | Japan           | &nbsp;&nbsp;🇯🇵&nbsp;&nbsp; |
| Kannada            | kn        | Dravidian / Southern Dravidian      | India           | &nbsp;&nbsp;🇮🇳&nbsp;&nbsp; |
| Kazakh             | kk        | Turkic / Common Turkic              | Kazakhstan      | &nbsp;&nbsp;🇰🇿&nbsp;&nbsp; |
| Korean             | ko        | Koreanic                            | South Korea     | &nbsp;&nbsp;🇰🇷&nbsp;&nbsp; |
| Kyrgyz             | ky        | Turkic / Common Turkic              | Kyrgyzstan      | &nbsp;&nbsp;🇰🇬&nbsp;&nbsp; |
| Latvian            | lv        | Indo-European / Balto-Slavic        | Latvia          | &nbsp;&nbsp;🇱🇻&nbsp;&nbsp; |
| Lithuanian         | lt        | Indo-European / Balto-Slavic        | Lithuania       | &nbsp;&nbsp;🇱🇹&nbsp;&nbsp; |
| Luxembourgish      | lb        | Indo-European / Germanic            | Luxembourg      | &nbsp;&nbsp;🇱🇺&nbsp;&nbsp; |
| Macedonian         | mk        | Indo-European / Balto-Slavic        | North Macedonia | &nbsp;&nbsp;🇲🇰&nbsp;&nbsp; |
| Malayalam          | ml        | Dravidian / Southern Dravidian      | India           | &nbsp;&nbsp;🇮🇳&nbsp;&nbsp; |
| Manx               | gv        | Indo-European / Celtic              | Isle of Man     | &nbsp;&nbsp;🇮🇲&nbsp;&nbsp; |
| Marathi            | mr        | Indo-European / Indo-Iranian        | India           | &nbsp;&nbsp;🇮🇳&nbsp;&nbsp; |
| Mongolian          | mn        | Mongolic / Central Mongolic         | Mongolia        | &nbsp;&nbsp;🇲🇳&nbsp;&nbsp; |
| Nepali             | ne        | Indo-European / Indo-Iranian        | Nepal           | &nbsp;&nbsp;🇳🇵&nbsp;&nbsp; |
| Norwegian          | no        | Indo-European / Germanic            | Norway          | &nbsp;&nbsp;🇳🇴&nbsp;&nbsp; |
| Oromo              | om        | Afro-Asiatic / Cushitic             | Kenya           | &nbsp;&nbsp;🇰🇪&nbsp;&nbsp; |
| Persian            | fa        | Indo-European / Indo-Iranian        | Iran            | &nbsp;&nbsp;🇮🇷&nbsp;&nbsp; |
| Polish             | pl        | Indo-European / Balto-Slavic        | Poland          | &nbsp;&nbsp;🇵🇱&nbsp;&nbsp; |
| Portuguese         | pt        | Indo-European / Italic              | Portugal        | &nbsp;&nbsp;🇵🇹&nbsp;&nbsp; |
| Punjabi            | pa        | Indo-European / Indo-Iranian        | India           | &nbsp;&nbsp;🇮🇳&nbsp;&nbsp; |
| Romanian           | ro        | Indo-European / Italic              | Romania         | &nbsp;&nbsp;🇷🇴&nbsp;&nbsp; |
| Russian            | ru        | Indo-European / Balto-Slavic        | Russia          | &nbsp;&nbsp;🇷🇺&nbsp;&nbsp; |
| Serbian            | sr        | Indo-European / Balto-Slavic        | Serbia          | &nbsp;&nbsp;🇷🇸&nbsp;&nbsp; |
| Shona              | sn        | Niger–Congo / Atlantic–Congo        | Zimbabwe        | &nbsp;&nbsp;🇿🇼&nbsp;&nbsp; |
| Sinhala            | si        | Indo-European / Indo-Iranian        | Sri Lanka       | &nbsp;&nbsp;🇱🇰&nbsp;&nbsp; |
| Slovak             | sk        | Indo-European / Balto-Slavic        | Slovakia        | &nbsp;&nbsp;🇸🇰&nbsp;&nbsp; |
| Slovenian          | sl        | Indo-European / Balto-Slavic        | Slovenia        | &nbsp;&nbsp;🇸🇮&nbsp;&nbsp; |
| Somali             | so        | Afro-Asiatic / Cushitic             | Somalia         | &nbsp;&nbsp;🇸🇴&nbsp;&nbsp; |
| Spanish            | es        | Indo-European / Italic              | Spain           | &nbsp;&nbsp;🇪🇸&nbsp;&nbsp; |
| Swahili            | sw        | Niger-Congo / Atlantic-Congo        | Tanzania        | &nbsp;&nbsp;🇹🇿&nbsp;&nbsp; |
| Swedish            | sv        | Indo-European / Germanic            | Sweden          | &nbsp;&nbsp;🇸🇪&nbsp;&nbsp; |
| Tajik              | tg        | Indo-European / Indo-Iranian        | Tajikistan      | &nbsp;&nbsp;🇹🇯&nbsp;&nbsp; |
| Tamil              | ta        | Dravidian / Southern Dravidian      | India           | &nbsp;&nbsp;🇮🇳&nbsp;&nbsp; |
| Telugu             | te        | Dravidian / South-Central Dravidian | India           | &nbsp;&nbsp;🇮🇳&nbsp;&nbsp; |
| Thai               | th        | Kra-Dai / Tai                       | Thailand        | &nbsp;&nbsp;🇹🇭&nbsp;&nbsp; |
| Tibetan            | bo        | Sino-Tibetan / Tibeto-Burman        | China           | &nbsp;&nbsp;🇨🇳&nbsp;&nbsp; |
| Tigrinya           | ti        | Afro-Asiatic / Semitic              | Eritrea         | &nbsp;&nbsp;🇪🇷&nbsp;&nbsp; |
| Turkish            | tr        | Turkic / Common Turkic              | Turkey          | &nbsp;&nbsp;🇹🇷&nbsp;&nbsp; |
| Ukrainian          | uk        | Indo-European / Balto-Slavic        | Ukraine         | &nbsp;&nbsp;🇺🇦&nbsp;&nbsp; |
| Urdu               | ur        | Indo-European / Indo-Iranian        | Pakistan        | &nbsp;&nbsp;🇵🇰&nbsp;&nbsp; |
| Vietnamese         | vi        | Austroasiatic / Vietic              | Vietnam         | &nbsp;&nbsp;🇻🇳&nbsp;&nbsp; |
| Welsh              | cy        | Indo-European / Celtic              | United Kingdom  | &nbsp;&nbsp;🇬🇧&nbsp;&nbsp; |
| Yiddish            | yi        | Indo-European / Germanic            | Israel          | &nbsp;&nbsp;🇮🇱&nbsp;&nbsp; |
| Yoruba             | yo        | Niger–Congo / Atlantic–Congo        | Nigeria         | &nbsp;&nbsp;🇳🇬&nbsp;&nbsp; |
| Zulu               | zu        | Niger–Congo / Atlantic–Congo        | South Africa    | &nbsp;&nbsp;🇿🇦&nbsp;&nbsp; |


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

The language detection library can be integrated into your code using a Builder with the fluent API.

The API is simple to use, enabling easy configuration of the language detector. Additionally, the public API of the library is designed to never return `null`.

### Basic usage

The following is a reasonable configuration:
```java
final LanguageDetectionSettings languageDetectionSettings =
  LanguageDetectionSettings
    .fromIsoCodes639_1("en,ja,es,fr,de,it,zh-cn") // or: en, ja, es, fr, de, it, zh-cn
    .withClassifyChineseAsJapanese()
    .build();

final LanguageDetectionOrchestrator orchestrator = LanguageDetectionOrchestrator.fromSettings(languageDetectionSettings);
final Language language = orchestrator.detect("languages are awesome");

final String languageCode = language.getIsoCode639_1();
final float probability = language.getProbability();
```

[`Back to top`](#table-of-contents)

### Methods to build the LanguageDetectionSettings

#### Configuring ISO 639-1 codes

In some classification tasks, you may already know that your language data is not written in the Latin script, such as with languages that use different alphabets. In these situations, the accuracy of language detection can improve by either excluding unrelated languages from the process or by focusing specifically on the languages that are relevant:

`.fromAllIsoCodes639_1()`
- **Default**: N/A
- **Description**: Enables the library to perform language detection for all the supported languages by the ISO 639-1 codes

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
- **Default**: `false` (`false` means that input sanitization is enabled by default). By default, the library sanitizes input strings by removing URLs from any part of the input text, as these elements are irrelevant to language detection.
- **Description**: Invoking the API bypasses this input sanitization process for, allowing the text to be processed without such modifications.


```java
LanguageDetectionSettings
    .fromIsoCodes639_1("en,ja,es,fr,de,it,zh-cn")
    .withoutInputSanitize()
    .build();
```

[`Back to top`](#table-of-contents)

#### CJK detection threshold

`.withCjkDetectionThreshold(Double)`
- **Default**: `0.1`. When the proportion of CJK characters in the input string exceeds `10%`, the library bypasses statistical detection via Naive Bayes. This decision is based on the heuristic threshold, which indicates the input is likely CJK.
- **Description**: When the threshold is set to a value greater than zero, the library first applies a heuristic check to determine whether the input string contains CJK characters. If the heuristic confirms the presence of CJK text, statistical detection via Naive Bayes is not performed.


```java
LanguageDetectionSettings
    .fromIsoCodes639_1("en,ja,es,fr,de,it,zh-cn")
    .withCjkDetectionThreshold(0.25)
    .build();
```

[`Back to top`](#table-of-contents)

#### Classify any Chinese content as Japanese

`.withClassifyChineseAsJapanese()`
- **Default**: `false` (`false` means that Chinese text is not classified as Japanese)
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

## Language detection benchmarks against other libraries

This library provides a suite of benchmarks to assess its performance against other language detection libraries. The benchmark uses a fixed set of languages, including `Japanese (ja)`, `English (en)`, `French (fr)`, `Spanish (es)`, `Italian (it)`, and `German (de)`. These languages are part of the [multilingual mMARCO dataset](https://github.com/unicamp-dl/mMARCO). The dataset consists of `59,096` files per language, with each file containing one to four sentence paragraphs.

Currently, the following libraries are evaluated in terms of accuracy and speed of execution:

1. Default (the current library)
2. Optimaize [Optimaize GitHub](https://github.com/optimaize/language-detector)
3. Lingua with a low accuracy mode on [Lingua GitHub](https://github.com/pemistahl/lingua)
4. Lingua with the default high accuracy mode on [Lingua GitHub](https://github.com/pemistahl/lingua)
5. Apache OpenNLP language detector [Apache OpenNLP](https://opennlp.apache.org/)
6. Apache Tika with Optimaize language detector [Apache Tika](https://tika.apache.org)
7. Apache Tika with OpenNLP language detector. This is based on OpenNLP's language detector. However, they've built their own ProbingLanguageDetector and their own language models. [Apache Tika](https://tika.apache.org)
8. [jFastText](https://github.com/vinhkhuc/JFastText). A Java wrapper for Facebook's [fastText](https://github.com/facebookresearch/fastText)

[`Back to top`](#table-of-contents)

### Running the benchmarks

Please note that the test dataset is quite large when unzipped. When running the benchmarks for the first time, the dataset located in [src/benchmarkTest/dataset.tar.gz](src/benchmarkTest/dataset.tar.gz) (approximately 95 MB) will be extracted into the `build/resources/benchmarkTest` directory. This extraction requires about **1.05 GB** of disk space.

To run benchmarks across all the libraries and datasets, execute the following Gradle command:

```bash
./gradlew runBenchmarks
```

Please note, by default all the benchmarks run on a single worker thread. Currently, this is not configurable. PR pending.

Alternatively, you can run benchmarks for specific language detectors and datasets by specifying the desired options. The following `-Pdetector` arguments are currently supported:

1. `default` - The current library.
2. `optimaize` - Optimaize language detector [Optimaize GitHub](https://github.com/optimaize/language-detector)
3. `lingua_low` - Lingua with low accuracy mode enabled [Lingua GitHub](https://github.com/pemistahl/lingua)
4. `lingua_high` - Lingua with default high accuracy mode enabled [Lingua GitHub](https://github.com/pemistahl/lingua)
5. `opennlp` - Apache OpenNLP language detector [Apache OpenNLP](https://opennlp.apache.org)
6. `tika_optimaize` - Apache Tika with Optimaize language detector [Apache Tika](https://tika.apache.org)
7. `tika_opennlp` - Apache Tika with OpenNLP language detector. This is based on OpenNLP's language detector. However, they've built their own ProbingLanguageDetector and their own language models. [Apache Tika](https://tika.apache.org)
8. `jfasttext` - [jFastText](https://github.com/vinhkhuc/JFastText) is a Java wrapper for Facebook's [fastText](https://github.com/facebookresearch/fastText)

For example, to run benchmarks using the `Optimaize`, `Apache Tika with Optimaize` and `Default` language detectors on the `en` (English) and `ja` (Japanese) datasets, use the following command:

```bash
./gradlew runBenchmarks -Pdetector=optimaize,default,tika_optimaize -PisoCodesCsv=en,ja
```

[`Back to top`](#table-of-contents)

### Accuracy report

Once the benchmark process completes, a report will be generated showing the accuracy of each detector. Here's an example of how to interpret the results:

For instance, in a row like `DE-optimaize`, the output indicates that the **Optimaize** detector processed the German dataset of `59,096` files. Out of those, `58,880` files were correctly identified as German, while the remaining files were misidentified as other languages.

Each group of `<UPPERCASE_ISO-CODE-639-1>-<DETECTOR_NAME>` rows is organized in descending order based on the count in the corresponding **ISO 639-1 code column**, reflecting a sorting by the **detection accuracy** in descending order.

```bash
|---------------------|---------|---------|---------|---------|---------|---------|---------|
| Dataset-to-Detector | de      | en      | es      | fr      | it      | ja      | unknown |
|---------------------|---------|---------|---------|---------|---------|---------|---------|
| DE-jfasttext        | 59008   | 73      | 5       | 2       | 0       | 0       | 8       |
| DE-lingua_high      | 58916   | 163     | 4       | 11      | 2       | 0       | 0       |
| DE-default          | 58914   | 171     | 3       | 5       | 3       | 0       | 0       |
| DE-lingua_low       | 58889   | 184     | 4       | 13      | 5       | 0       | 1       |
| DE-optimaize        | 58880   | 209     | 3       | 1       | 3       | 0       | 0       |
| DE-tika_optimaize   | 58880   | 209     | 3       | 1       | 3       | 0       | 0       |
| DE-opennlp          | 58633   | 257     | 1       | 3       | 3       | 0       | 199     |
| DE-tika_opennlp     | 58434   | 307     | 0       | 3       | 4       | 0       | 348     |
|---------------------|---------|---------|---------|---------|---------|---------|---------|
| EN-optimaize        | 8       | 59070   | 5       | 5       | 8       | 0       | 0       |
| EN-tika_optimaize   | 8       | 59070   | 5       | 5       | 8       | 0       | 0       |
| EN-jfasttext        | 9       | 59068   | 5       | 2       | 2       | 0       | 10      |
| EN-default          | 17      | 59041   | 8       | 22      | 8       | 0       | 0       |
| EN-opennlp          | 0       | 58976   | 2       | 0       | 2       | 0       | 116     |
| EN-lingua_high      | 50      | 58972   | 30      | 35      | 9       | 0       | 0       |
| EN-lingua_low       | 62      | 58942   | 33      | 48      | 11      | 0       | 0       |
| EN-tika_opennlp     | 1       | 58794   | 4       | 4       | 6       | 0       | 287     |
|---------------------|---------|---------|---------|---------|---------|---------|---------|
| ES-jfasttext        | 4       | 27      | 59025   | 23      | 0       | 0       | 17      |
| ES-default          | 6       | 154     | 58906   | 11      | 19      | 0       | 0       |
| ES-optimaize        | 9       | 160     | 58906   | 9       | 12      | 0       | 0       |
| ES-tika_optimaize   | 9       | 160     | 58906   | 9       | 12      | 0       | 0       |
| ES-lingua_high      | 8       | 173     | 58889   | 10      | 16      | 0       | 0       |
| ES-lingua_low       | 10      | 180     | 58871   | 17      | 18      | 0       | 0       |
| ES-tika_opennlp     | 0       | 146     | 58644   | 4       | 17      | 0       | 285     |
| ES-opennlp          | 0       | 200     | 58351   | 5       | 16      | 0       | 524     |
|---------------------|---------|---------|---------|---------|---------|---------|---------|
| FR-jfasttext        | 1       | 30      | 0       | 59063   | 0       | 0       | 2       |
| FR-default          | 12      | 144     | 7       | 58930   | 3       | 0       | 0       |
| FR-opennlp          | 1       | 117     | 1       | 58909   | 2       | 0       | 66      |
| FR-optimaize        | 13      | 161     | 12      | 58907   | 3       | 0       | 0       |
| FR-tika_optimaize   | 13      | 161     | 12      | 58907   | 3       | 0       | 0       |
| FR-lingua_high      | 23      | 239     | 9       | 58822   | 3       | 0       | 0       |
| FR-lingua_low       | 37      | 257     | 12      | 58786   | 4       | 0       | 0       |
| FR-tika_opennlp     | 2       | 184     | 3       | 58706   | 11      | 0       | 190     |
|---------------------|---------|---------|---------|---------|---------|---------|---------|
| IT-jfasttext        | 9       | 52      | 4       | 1       | 59025   | 0       | 5       |
| IT-default          | 6       | 214     | 7       | 5       | 58864   | 0       | 0       |
| IT-optimaize        | 6       | 257     | 4       | 2       | 58827   | 0       | 0       |
| IT-tika_optimaize   | 6       | 257     | 4       | 2       | 58827   | 0       | 0       |
| IT-opennlp          | 0       | 248     | 3       | 0       | 58742   | 0       | 103     |
| IT-tika_opennlp     | 1       | 235     | 4       | 1       | 58667   | 0       | 188     |
| IT-lingua_high      | 19      | 467     | 58      | 17      | 58535   | 0       | 0       |
| IT-lingua_low       | 26      | 492     | 65      | 24      | 58489   | 0       | 0       |
|---------------------|---------|---------|---------|---------|---------|---------|---------|
| JA-default          | 0       | 2       | 0       | 0       | 1       | 59093   | 0       |
| JA-jfasttext        | 0       | 8       | 0       | 0       | 1       | 59076   | 11      |
| JA-lingua_low       | 10      | 31      | 4       | 1       | 1       | 59049   | 0       |
| JA-lingua_high      | 7       | 36      | 2       | 3       | 1       | 59047   | 0       |
| JA-opennlp          | 5       | 51      | 14      | 9       | 9       | 58742   | 266     |
| JA-tika_opennlp     | 8       | 110     | 14      | 3       | 13      | 58386   | 562     |
| JA-optimaize        | 1055    | 5421    | 351     | 534     | 440     | 51289   | 6       |
| JA-tika_optimaize   | 1054    | 5421    | 352     | 535     | 441     | 51287   | 6       |
|---------------------|---------|---------|---------|---------|---------|---------|---------|
```

[`Back to top`](#table-of-contents)

### Speed of execution

As the benchmarks are running, the console will display the execution times for each language detector on the selected datasets.

Here's an example output that displays the execution times:

```bash
Will process datasets for 6 ISO 639-1 code names: [de, en, es, fr, it, ja]

default processes dataset [de]
default processes dataset [en]
default processes dataset [es]
default processes dataset [fr]
default processes dataset [it]
default processes dataset [ja]
Detector default completed in 32 seconds and 834 millis

jfasttext processes dataset [de]
jfasttext processes dataset [en]
jfasttext processes dataset [es]
jfasttext processes dataset [fr]
jfasttext processes dataset [it]
jfasttext processes dataset [ja]
Detector jfasttext completed in 35 seconds and 625 millis

lingua_high processes dataset [de]
lingua_high processes dataset [en]
lingua_high processes dataset [es]
lingua_high processes dataset [fr]
lingua_high processes dataset [it]
lingua_high processes dataset [ja]
Detector lingua_high completed in 91 seconds and 540 millis

lingua_low processes dataset [de]
lingua_low processes dataset [en]
lingua_low processes dataset [es]
lingua_low processes dataset [fr]
lingua_low processes dataset [it]
lingua_low processes dataset [ja]
Detector lingua_low completed in 92 seconds and 164 millis

opennlp processes dataset [de]
opennlp processes dataset [en]
opennlp processes dataset [es]
opennlp processes dataset [fr]
opennlp processes dataset [it]
opennlp processes dataset [ja]
Detector opennlp completed in 86 seconds and 247 millis

optimaize processes dataset [de]
optimaize processes dataset [en]
optimaize processes dataset [es]
optimaize processes dataset [fr]
optimaize processes dataset [it]
optimaize processes dataset [ja]
Detector optimaize completed in 34 seconds and 823 millis

tika_opennlp processes dataset [de]
tika_opennlp processes dataset [en]
tika_opennlp processes dataset [es]
tika_opennlp processes dataset [fr]
tika_opennlp processes dataset [it]
tika_opennlp processes dataset [ja]
Detector tika_opennlp completed in 169 seconds and 209 millis

tika_optimaize processes dataset [de]
tika_optimaize processes dataset [en]
tika_optimaize processes dataset [es]
tika_optimaize processes dataset [fr]
tika_optimaize processes dataset [it]
tika_optimaize processes dataset [ja]
Detector tika_optimaize completed in 38 seconds and 451 millis
```

[`Back to top`](#table-of-contents)

### Key takeaways

From the [Accuracy report](#accuracy-report) and [Speed of execution](#speed-of-execution) sections, we can conclude:

- `jFastText` consistently ranks among the top three for detection accuracy, with `Default` (the current library) securing the second position, just behind `jFastText` in terms of accuracy.
- `Default`, `jFastText`, and `Optimaize` are the fastest libraries, offering a robust balance of high accuracy and speed, outperforming other libraries in both categories.
- `OpenNLP`, `Apache Tika OpenNLP`, and `Lingua` (across all accuracy modes) are noticeably slower in comparison, which may affect efficiency.

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
