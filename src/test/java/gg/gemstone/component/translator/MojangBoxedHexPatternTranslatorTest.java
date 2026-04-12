package gg.gemstone.component.translator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MojangBoxedHexPatternTranslatorTest {

  private final MiniMessageTranslator translator = new MojangBoxedHexPatternTranslator();

  @Test
  void shouldConvertLowercase() {
    assertEquals("<#aabbcc>", translator.translate("<&#aabbcc>"));
  }

  @Test
  void shouldConvertUppercase() {
    assertEquals("<#AABBCC>", translator.translate("<&#AABBCC>"));
  }

  @Test
  void shouldConvertMixedCase() {
    assertEquals("<#aAbBcC>", translator.translate("<&#aAbBcC>"));
  }

  @Test
  void shouldConvertMultiple() {
    assertEquals("<#FFFFFF> <#000000>", translator.translate("<&#FFFFFF> <&#000000>"));
  }

  @Test
  void shouldNotMatchUnboxed() {
    assertEquals("&#FFFFFF", translator.translate("&#FFFFFF"));
  }

  @Test
  void shouldNotMatchAlreadyConverted() {
    assertEquals("<#FFFFFF>", translator.translate("<#FFFFFF>"));
  }

  @Test
  void shouldNotMatchInvalidHex() {
    assertEquals("<&#GGGGGG>", translator.translate("<&#GGGGGG>"));
  }

  @Test
  void shouldNotMatchFiveDigitHex() {
    assertEquals("<&#FFFFF>", translator.translate("<&#FFFFF>"));
  }

  @Test
  void shouldNotMatchSevenDigitHex() {
    assertEquals("<&#FFFFFFF>", translator.translate("<&#FFFFFFF>"));
  }

  @Test
  void shouldPreservesSurroundingText() {
    assertEquals("Hello <#FFFFFF> world", translator.translate("Hello <&#FFFFFF> world"));
  }
}
