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
  public void shouldsanitize() {
    assertEquals(sanitize("NOT banana AND apple OR AND"), "banana   apple");
    assertEquals(sanitize("not banana and apple or and"), "not banana and apple or and");
    assertEquals(sanitize(".pptx"), "");
    assertEquals(
        sanitize("Hello How .t .tt .ttt are .xls you .pdf today.gdoc"),
        "Hello How       are   you   today");
    assertEquals(sanitize("123.xls"), "123");
    assertEquals(
        sanitize(
            "Oops .txt .csv .pdf .ppt .xls .xlsx .xlsm .doc .docx .docm .pptx .pptm .ppsx .rtf .tiff .png .jpg .jpeg"),
        "Oops");
    assertEquals(sanitize("報告 123.xls"), "報告 123");
    assertEquals(sanitize("七月の報告.xls"), "七月の報告");
    assertEquals(sanitize("1234567890.xls 報告"), "1234567890  報告");
    assertEquals(sanitize("１２３４５６７８９０.xls 報告"), "１２３４５６７８９０  報告");
    assertEquals(sanitize("２０２４七月　報告"), "２０２４七月　報告");
    assertEquals(sanitize("２０２４七月　報告.pptx"), "２０２４七月　報告");
  }

  @Test
  public void shouldPerformCombinedSanitization() {
    assertEquals(filterOutNonWords(sanitize("...")), "");
    assertEquals(
        filterOutNonWords(sanitize("in1729x01_J-LCM刷新プロジェクト.docx")), "in1729x01 J LCM刷新プロジェクト");
    assertEquals(
        filterOutNonWords(sanitize("Fußball-Weltmeisterschaft-Haus-Tür-Schlüssel.pdf")),
        "Fußball Weltmeisterschaft Haus Tür Schlüssel");
    assertEquals(filterOutNonWords(sanitize("㈱_(株)_①②③_㈱㈲㈹.pdf")), "   株         ");
    assertEquals(filterOutNonWords(sanitize("ｼｰｻｲﾄﾞ_ﾗｲﾅｰ.pdf")), "ｼｰｻｲﾄﾞ ﾗｲﾅｰ");
    assertEquals(filterOutNonWords(sanitize("Ｃｕｌｔｕｒｅ　ｏｆ　Ｊａｐａｎ.pdf")), "Ｃｕｌｔｕｒｅ ｏｆ Ｊａｐａｎ");
    assertEquals(filterOutNonWords(sanitize("#4_pj_23D002_HCMJ_デジ戦")), "4 pj 23D002 HCMJ デジ戦");
  }
}
