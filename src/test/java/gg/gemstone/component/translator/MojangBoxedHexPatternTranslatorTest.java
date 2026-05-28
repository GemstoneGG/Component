/*
 * Copyright (C) 2026 GemstoneGG/Component Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package gg.gemstone.component.translator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Nested;
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

  @Nested
  class Escape {

    @Test
    void shouldEscapeSingle() {
      assertEquals("<&\\#FFFFFF>", translator.escape("<&#FFFFFF>"));
    }

    @Test
    void shouldEscapeMultiple() {
      assertEquals("<&\\#FFFFFF> <&\\#000000>", translator.escape("<&#FFFFFF> <&#000000>"));
    }

    @Test
    void shouldNotEscapeUnboxedMojang() {
      assertEquals("&#FFFFFF", translator.escape("&#FFFFFF"));
    }

    @Test
    void shouldNotEscapeAlreadyConverted() {
      assertEquals("<#FFFFFF>", translator.escape("<#FFFFFF>"));
    }

    @Test
    void shouldNotEscapeInvalidHex() {
      assertEquals("<&#GGGGGG>", translator.escape("<&#GGGGGG>"));
    }

    @Test
    void shouldNotEscapeFiveDigitHex() {
      assertEquals("<&#FFFFF>", translator.escape("<&#FFFFF>"));
    }

    @Test
    void shouldNotEscapeSevenDigitHex() {
      assertEquals("<&#FFFFFFF>", translator.escape("<&#FFFFFFF>"));
    }

    @Test
    void escapedOutputIsNoLongerTranslated() {
      String escaped = translator.escape("<&#AABBCC>");
      assertEquals("<&\\#AABBCC>", escaped);
      assertEquals(escaped, translator.translate(escaped));
    }
  }

  @Nested
  class Strip {

    @Test
    void shouldStripSingle() {
      assertEquals("", translator.strip("<&#FFFFFF>"));
    }

    @Test
    void shouldStripMultiple() {
      assertEquals(" ", translator.strip("<&#FFFFFF> <&#000000>"));
    }

    @Test
    void shouldNotStripUnboxedMojang() {
      assertEquals("&#FFFFFF", translator.strip("&#FFFFFF"));
    }

    @Test
    void shouldNotStripAlreadyConverted() {
      assertEquals("<#FFFFFF>", translator.strip("<#FFFFFF>"));
    }

    @Test
    void shouldNotStripInvalidHex() {
      assertEquals("<&#GGGGGG>", translator.strip("<&#GGGGGG>"));
    }

    @Test
    void shouldPreserveSurroundingText() {
      assertEquals("Hello  world", translator.strip("Hello <&#FFFFFF> world"));
    }
  }
}
