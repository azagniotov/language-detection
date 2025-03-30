package io.github.azagniotov.language;

import static io.github.azagniotov.language.InputSanitizer.filterOutNonWords;
import static io.github.azagniotov.language.InputSanitizer.sanitize;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class InputSanitizerTest {

  @Test
  public void shouldRemoveNonWordCharacters() {
    assertEquals(filterOutNonWords(".#[],_\"-"), "     _  ");
    assertEquals(filterOutNonWords("I love coding, it's fun!"), "I love coding  it s fun ");
    assertEquals(filterOutNonWords("Hello! How are you today?"), "Hello  How are you today ");
    assertEquals(filterOutNonWords("She said: 'I'm busy!'"), "She said   I m busy  ");
    assertEquals(filterOutNonWords("This is a test sentence."), "This is a test sentence ");
    assertEquals(filterOutNonWords("Wait for me... please!"), "Wait for me    please ");
    assertEquals(filterOutNonWords("Let's go, guys!"), "Let s go  guys ");
    assertEquals(filterOutNonWords("What time is it?"), "What time is it ");
    assertEquals(filterOutNonWords("The sky is blue, isn't it?"), "The sky is blue  isn t it ");
    assertEquals(filterOutNonWords("I can't believe it!"), "I can t believe it ");
    assertEquals(
        filterOutNonWords("I'm reading a book right now."), "I m reading a book right now ");

    assertEquals(filterOutNonWords("Ich liebe Programmieren!"), "Ich liebe Programmieren ");
    assertEquals(filterOutNonWords("Hallo! Wie geht's dir?"), "Hallo  Wie geht s dir ");
    assertEquals(
        filterOutNonWords("Sie sagte: 'Ich bin beschäftigt!'"),
        "Sie sagte   Ich bin beschäftigt  ");
    assertEquals(filterOutNonWords("Das ist ein Testsatz."), "Das ist ein Testsatz ");
    assertEquals(filterOutNonWords("Warte auf mich... bitte!"), "Warte auf mich    bitte ");
    assertEquals(filterOutNonWords("Los geht's, Leute!"), "Los geht s  Leute ");
    assertEquals(filterOutNonWords("Wie spät ist es?"), "Wie spät ist es ");
    assertEquals(filterOutNonWords("Der Himmel ist blau, oder?"), "Der Himmel ist blau  oder ");
    assertEquals(filterOutNonWords("Ich kann es nicht glauben!"), "Ich kann es nicht glauben ");
    assertEquals(filterOutNonWords("Ich lese gerade ein Buch."), "Ich lese gerade ein Buch ");

    assertEquals(filterOutNonWords("私はプログラミングが好きです!"), "私はプログラミングが好きです ");
    assertEquals(filterOutNonWords("こんにちは！お元気ですか？"), "こんにちは お元気ですか ");
    assertEquals(filterOutNonWords("彼女は言った：'忙しい!'"), "彼女は言った  忙しい  ");
    assertEquals(filterOutNonWords("これはテスト文です。"), "これはテスト文です ");
    assertEquals(filterOutNonWords("待っててください…お願い！"), "待っててください お願い ");
    assertEquals(filterOutNonWords("行こう！みんな！"), "行こう みんな ");
    assertEquals(filterOutNonWords("今、何時ですか？"), "今 何時ですか ");
    assertEquals(filterOutNonWords("空は青いですね?"), "空は青いですね ");
    assertEquals(filterOutNonWords("信じられません！"), "信じられません ");
    assertEquals(filterOutNonWords("今、本を読んでいます。"), "今 本を読んでいます ");
  }

  @Test
  public void shouldSanitize() {
    assertEquals(sanitize("http://www.google.com"), "");
    assertEquals(sanitize("https://www.google.com"), "");
    assertEquals(sanitize("hello this is a https://www.google.com URL"), "hello this is a URL");
    assertEquals(
        sanitize("hello this is www.google.com domain"), "hello this is www.google.com domain");
  }
}
