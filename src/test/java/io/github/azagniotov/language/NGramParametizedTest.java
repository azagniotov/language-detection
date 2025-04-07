package io.github.azagniotov.language;

import static io.github.azagniotov.language.TestDefaultConstants.MAX_NGRAM_LENGTH;
import static io.github.azagniotov.language.TestDefaultConstants.MIN_NGRAM_LENGTH;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class NGramParametizedTest {

  private final String input;
  private final List<String> expected;

  @BeforeClass
  public static void setUp() throws IOException {
    // Warm-up for the static initializers
    char normalized = NGram.normalize('\u0000');
  }

  public NGramParametizedTest(final String input, final List<String> expected) {
    this.input = input;
    this.expected = expected;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    final List<Object[]> data = new ArrayList<>();

    data.add(new Object[] {"a", asList(" a", "a")});
    data.add(new Object[] {"ab", asList(" a", " ab", "a", "ab", "b")});
    data.add(new Object[] {" abc", asList(" a", " ab", "a", "ab", "abc", "b", "bc", "c")});
    data.add(new Object[] {"Ab", asList(" A", " Ab", "A", "Ab", "b")});
    data.add(new Object[] {"aB", asList(" a", " aB", "B", "a", "aB")});
    data.add(new Object[] {"aBc", asList(" a", " aB", "B", "Bc", "a", "aB", "aBc", "c")});
    data.add(new Object[] {"AB", asList(" A", "A")});
    data.add(
        new Object[] {
          "AbcD", asList(" A", " Ab", "A", "Ab", "Abc", "D", "b", "bc", "bcD", "c", "cD")
        });
    data.add(
        new Object[] {
          "Abc D", asList(" A", " Ab", " D", "A", "Ab", "Abc", "D", "b", "bc", "bc ", "c", "c ")
        });
    data.add(new Object[] {"aBCd", asList(" a", " aB", "B", "BCd", "Cd", "a", "aB", "d")});
    data.add(new Object[] {" z aBC", asList(" a", " aB", " z", " z ", "B", "a", "aB", "z", "z ")});
    data.add(
        new Object[] {
          "apples",
          asList(
              " a", " ap", "a", "ap", "app", "e", "es", "l", "le", "les", "p", "p", "pl", "ple",
              "pp", "ppl", "s")
        });

    return data;
  }

  @Test
  public final void extractNGrams() {
    final NGram ngram = new NGram(this.input, MIN_NGRAM_LENGTH, MAX_NGRAM_LENGTH);
    final List<String> actual = ngram.extractNGrams(new PrimitiveTrie());
    Collections.sort(actual);

    Collections.sort(expected);
    assertEquals(expected, actual);
  }
}
