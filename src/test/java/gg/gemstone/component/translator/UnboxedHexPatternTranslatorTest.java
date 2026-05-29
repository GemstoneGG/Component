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

class UnboxedHexPatternTranslatorTest {

  private final MiniMessageTranslator translator = new UnboxedHexPatternTranslator();

  @Test
  void shouldConvertLowercase() {
    assertEquals("<#aabbcc>", translator.translate("#aabbcc"));
  }

  @Test
  void shouldConvertUppercase() {
    assertEquals("<#AABBCC>", translator.translate("#AABBCC"));
  }

  @Test
  void shouldConvertMixedCase() {
    assertEquals("<#aAbBcC>", translator.translate("#aAbBcC"));
  }

  @Test
  void shouldConvertMultiple() {
    assertEquals("<#FFFFFF> <#000000>", translator.translate("#FFFFFF #000000"));
  }

  @Test
  void shouldNotMatchAlreadyConverted() {
    assertEquals("<#FFFFFF>", translator.translate("<#FFFFFF>"));
  }

  @Test
  void shouldNotMatchInvalidHex() {
    assertEquals("#GGGGGG", translator.translate("#GGGGGG"));
  }

  @Test
  void shouldNotMatchFiveDigitHex() {
    assertEquals("#FFFFF", translator.translate("#FFFFF"));
  }

  @Test
  void shouldNotMatchSevenDigitHex() {
    assertEquals("#FFFFFFF", translator.translate("#FFFFFFF"));
  }

  @Test
  void shouldPreserveSurroundingText() {
    assertEquals("Hello <#FFFFFF> world", translator.translate("Hello #FFFFFF world"));
  }

  @Test
  void shouldNotCorruptMojangBoxedAfterConversion() {
    // Simulates output from MojangBoxedHexPatternTranslator being passed in
    assertEquals("<#FFFFFF>", translator.translate("<#FFFFFF>"));
  }

  @Nested
  class InsideTags {

    @Test
    void shouldNotConvertHexArgumentInTag() {
      // #90630C is a tag argument, not a bare hex code - it must be left untouched.
      assertEquals("<c:#90630C>", translator.translate("<c:#90630C>"));
    }

    @Test
    void shouldNotConvertHexInGradientTag() {
      assertEquals("<gradient:#FCD620:#F0A615:#FCD620>",
          translator.translate("<gradient:#FCD620:#F0A615:#FCD620>"));
    }

    @Test
    void shouldConvertOutsideTagButNotInside() {
      assertEquals("<#FFFFFF> <c:#90630C>",
          translator.translate("#FFFFFF <c:#90630C>"));
    }

    @Test
    void shouldNotEscapeHexInsideTag() {
      assertEquals("<c:#90630C>", translator.escape("<c:#90630C>"));
    }

    @Test
    void shouldNotStripHexInsideTag() {
      assertEquals("<c:#90630C>", translator.strip("<c:#90630C>"));
    }

    @Test
    void shouldLeaveUnterminatedTagAsText() {
      // A '<' with no matching '>' is not a tag span, so the bare hex after it still converts.
      assertEquals("<c:<#90630C>", translator.translate("<c:#90630C"));
    }
  }

  @Nested
  class Escape {

    @Test
    void shouldEscapeSingle() {
      assertEquals("#\\FFFFFF", translator.escape("#FFFFFF"));
    }

    @Test
    void shouldEscapeMultiple() {
      assertEquals("#\\FFFFFF #\\000000", translator.escape("#FFFFFF #000000"));
    }

    @Test
    void shouldNotEscapeAlreadyConverted() {
      assertEquals("<#FFFFFF>", translator.escape("<#FFFFFF>"));
    }

    @Test
    void shouldNotEscapeMojangUnboxed() {
      assertEquals("&#FFFFFF", translator.escape("&#FFFFFF"));
    }

    @Test
    void shouldNotEscapeInvalidHex() {
      assertEquals("#GGGGGG", translator.escape("#GGGGGG"));
    }

    @Test
    void shouldNotEscapeSevenDigitHex() {
      assertEquals("#FFFFFFF", translator.escape("#FFFFFFF"));
    }

    @Test
    void shouldNotReEscapeBackslashEscapedHex() {
      // Simulates output of MojangBoxed/MojangUnboxed escape (e.g. "&\#FFFFFF").
      // The leading backslash must keep us from re-escaping the inner #FFFFFF.
      assertEquals("&\\#FFFFFF", translator.escape("&\\#FFFFFF"));
      assertEquals("<&\\#FFFFFF>", translator.escape("<&\\#FFFFFF>"));
    }

    @Test
    void escapedOutputIsNoLongerTranslated() {
      String escaped = translator.escape("#AABBCC");
      assertEquals("#\\AABBCC", escaped);
      assertEquals(escaped, translator.translate(escaped));
    }
  }

  @Nested
  class Strip {

    @Test
    void shouldStripSingle() {
      assertEquals("", translator.strip("#FFFFFF"));
    }

    @Test
    void shouldStripMultiple() {
      assertEquals(" ", translator.strip("#FFFFFF #000000"));
    }

    @Test
    void shouldNotStripAlreadyConverted() {
      assertEquals("<#FFFFFF>", translator.strip("<#FFFFFF>"));
    }

    @Test
    void shouldNotStripMojangUnboxed() {
      assertEquals("&#FFFFFF", translator.strip("&#FFFFFF"));
    }

    @Test
    void shouldNotStripInvalidHex() {
      assertEquals("#GGGGGG", translator.strip("#GGGGGG"));
    }

    @Test
    void shouldPreserveSurroundingText() {
      assertEquals("Hello  world", translator.strip("Hello #FFFFFF world"));
    }
  }
}
