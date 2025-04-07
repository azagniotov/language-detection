package io.github.azagniotov.language;

import static io.github.azagniotov.language.StringConstants.EMPTY_STRING;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class VietnameseUtilsTest {

  @Test
  public final void testNormalizeVietnamese() {
    assertEquals(VietnameseUtils.normalizeVietnamese(EMPTY_STRING), EMPTY_STRING);
    assertEquals(VietnameseUtils.normalizeVietnamese("ABC"), "ABC");
    assertEquals(VietnameseUtils.normalizeVietnamese("012"), "012");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00c0"), "\u00c0");

    assertEquals(VietnameseUtils.normalizeVietnamese("\u0041\u0300"), "\u00C0");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0045\u0300"), "\u00C8");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0049\u0300"), "\u00CC");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u004F\u0300"), "\u00D2");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0055\u0300"), "\u00D9");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0059\u0300"), "\u1EF2");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0061\u0300"), "\u00E0");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0065\u0300"), "\u00E8");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0069\u0300"), "\u00EC");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u006F\u0300"), "\u00F2");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0075\u0300"), "\u00F9");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0079\u0300"), "\u1EF3");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00C2\u0300"), "\u1EA6");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00CA\u0300"), "\u1EC0");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00D4\u0300"), "\u1ED2");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00E2\u0300"), "\u1EA7");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00EA\u0300"), "\u1EC1");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00F4\u0300"), "\u1ED3");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0102\u0300"), "\u1EB0");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0103\u0300"), "\u1EB1");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u01A0\u0300"), "\u1EDC");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u01A1\u0300"), "\u1EDD");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u01AF\u0300"), "\u1EEA");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u01B0\u0300"), "\u1EEB");

    assertEquals(VietnameseUtils.normalizeVietnamese("\u0041\u0301"), "\u00C1");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0045\u0301"), "\u00C9");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0049\u0301"), "\u00CD");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u004F\u0301"), "\u00D3");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0055\u0301"), "\u00DA");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0059\u0301"), "\u00DD");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0061\u0301"), "\u00E1");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0065\u0301"), "\u00E9");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0069\u0301"), "\u00ED");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u006F\u0301"), "\u00F3");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0075\u0301"), "\u00FA");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0079\u0301"), "\u00FD");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00C2\u0301"), "\u1EA4");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00CA\u0301"), "\u1EBE");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00D4\u0301"), "\u1ED0");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00E2\u0301"), "\u1EA5");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00EA\u0301"), "\u1EBF");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00F4\u0301"), "\u1ED1");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0102\u0301"), "\u1EAE");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0103\u0301"), "\u1EAF");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u01A0\u0301"), "\u1EDA");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u01A1\u0301"), "\u1EDB");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u01AF\u0301"), "\u1EE8");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u01B0\u0301"), "\u1EE9");

    assertEquals(VietnameseUtils.normalizeVietnamese("\u0041\u0303"), "\u00C3");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0045\u0303"), "\u1EBC");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0049\u0303"), "\u0128");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u004F\u0303"), "\u00D5");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0055\u0303"), "\u0168");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0059\u0303"), "\u1EF8");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0061\u0303"), "\u00E3");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0065\u0303"), "\u1EBD");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0069\u0303"), "\u0129");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u006F\u0303"), "\u00F5");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0075\u0303"), "\u0169");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0079\u0303"), "\u1EF9");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00C2\u0303"), "\u1EAA");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00CA\u0303"), "\u1EC4");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00D4\u0303"), "\u1ED6");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00E2\u0303"), "\u1EAB");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00EA\u0303"), "\u1EC5");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00F4\u0303"), "\u1ED7");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0102\u0303"), "\u1EB4");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0103\u0303"), "\u1EB5");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u01A0\u0303"), "\u1EE0");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u01A1\u0303"), "\u1EE1");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u01AF\u0303"), "\u1EEE");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u01B0\u0303"), "\u1EEF");

    assertEquals(VietnameseUtils.normalizeVietnamese("\u0041\u0309"), "\u1EA2");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0045\u0309"), "\u1EBA");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0049\u0309"), "\u1EC8");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u004F\u0309"), "\u1ECE");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0055\u0309"), "\u1EE6");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0059\u0309"), "\u1EF6");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0061\u0309"), "\u1EA3");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0065\u0309"), "\u1EBB");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0069\u0309"), "\u1EC9");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u006F\u0309"), "\u1ECF");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0075\u0309"), "\u1EE7");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0079\u0309"), "\u1EF7");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00C2\u0309"), "\u1EA8");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00CA\u0309"), "\u1EC2");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00D4\u0309"), "\u1ED4");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00E2\u0309"), "\u1EA9");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00EA\u0309"), "\u1EC3");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00F4\u0309"), "\u1ED5");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0102\u0309"), "\u1EB2");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0103\u0309"), "\u1EB3");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u01A0\u0309"), "\u1EDE");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u01A1\u0309"), "\u1EDF");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u01AF\u0309"), "\u1EEC");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u01B0\u0309"), "\u1EED");

    assertEquals(VietnameseUtils.normalizeVietnamese("\u0041\u0323"), "\u1EA0");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0045\u0323"), "\u1EB8");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0049\u0323"), "\u1ECA");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u004F\u0323"), "\u1ECC");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0055\u0323"), "\u1EE4");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0059\u0323"), "\u1EF4");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0061\u0323"), "\u1EA1");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0065\u0323"), "\u1EB9");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0069\u0323"), "\u1ECB");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u006F\u0323"), "\u1ECD");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0075\u0323"), "\u1EE5");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0079\u0323"), "\u1EF5");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00C2\u0323"), "\u1EAC");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00CA\u0323"), "\u1EC6");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00D4\u0323"), "\u1ED8");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00E2\u0323"), "\u1EAD");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00EA\u0323"), "\u1EC7");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u00F4\u0323"), "\u1ED9");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0102\u0323"), "\u1EB6");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u0103\u0323"), "\u1EB7");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u01A0\u0323"), "\u1EE2");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u01A1\u0323"), "\u1EE3");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u01AF\u0323"), "\u1EF0");
    assertEquals(VietnameseUtils.normalizeVietnamese("\u01B0\u0323"), "\u1EF1");
  }
}
