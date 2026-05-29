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

package gg.gemstone.component;

import static org.junit.jupiter.api.Assertions.assertEquals;

import gg.gemstone.component.translator.MiniMessageTranslators;
import java.util.List;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ComponentParserImplTest {

  private final ComponentParserImpl parser = new ComponentParserImpl(
      List.of(MiniMessageTranslators.MOJANG_BOXED_HEX, MiniMessageTranslators.MOJANG_UNBOXED_HEX,
          MiniMessageTranslators.UNBOXED_HEX, MiniMessageTranslators.LEGACY_CODE_SECTION),
      MiniMessage.miniMessage()
  );

  @Nested
  class ConvertSectionSign {

    @Test
    void convertsLegacyColorCode() {
      assertEquals("<red>Hello</red>", parser.translate("§cHello"));
    }

    @Test
    void convertsMojangBoxedHex() {
      assertEquals("<#FFFFFF>", parser.translate("<&#FFFFFF>"));
    }

    @Test
    void convertsMojangUnboxedHex() {
      assertEquals("<#FFFFFF>", parser.translate("&#FFFFFF"));
    }

    @Test
    void convertsUnboxedHex() {
      assertEquals("<#FFFFFF>", parser.translate("#FFFFFF"));
    }

    @Test
    void convertsMixedLegacyAndMojangBoxedHex() {
      // Legacy translator treats <&#AABBCC> as an MM tag, keeping red open across it;
      // MojangBoxedHex then converts <&#AABBCC> to <#AABBCC>
      assertEquals("<red>Hello <#AABBCC>World",
          parser.translate("§cHello <&#AABBCC>World"));
    }

    @Test
    void convertsMixedLegacyAndMojangUnboxedHex() {
      // Legacy wraps the whole string in red; MojangUnboxedHex converts &#AABBCC in-place
      assertEquals("<red>Hello <#AABBCC> World",
          parser.translate("§cHello &#AABBCC World"));
    }

    @Test
    void convertsMixedLegacyAndUnboxedHex() {
      // Legacy wraps the whole string in red; UnboxedHex converts #AABBCC in-place
      assertEquals("<red>Hello <#AABBCC> World",
          parser.translate("§cHello #AABBCC World"));
    }

    @Test
    void sectionSignFollowedByHashIsConvertedByUnboxedHex() {
      // §# is not a valid legacy code; § passes through as-is, and #FFFFFF is matched
      // by UnboxedHexPatternTranslator since § is not in its negative lookbehind
      assertEquals("§<#FFFFFF>", parser.translate("§#FFFFFF"));
    }

    @Test
    void allThreeHexFormatsConvertedInSameString() {
      assertEquals("<#FF0000> <#00FF00> <#0000FF>",
          parser.translate("<&#FF0000> &#00FF00 #0000FF"));
    }
  }

  @Nested
  class ConvertAmpersand {

    private final ComponentParserImpl parserAmpersand = new ComponentParserImpl(
        List.of(MiniMessageTranslators.MOJANG_BOXED_HEX, MiniMessageTranslators.MOJANG_UNBOXED_HEX,
            MiniMessageTranslators.UNBOXED_HEX, MiniMessageTranslators.LEGACY_CODE_AMPERSAND),
        MiniMessage.miniMessage()
    );

    @Test
    void convertsLegacyColorCode() {
      assertEquals("<red>Hello</red>", parserAmpersand.translate("&cHello"));
    }

    @Test
    void convertsMojangBoxedHex() {
      assertEquals("<#FFFFFF>", parserAmpersand.translate("<&#FFFFFF>"));
    }

    @Test
    void convertsMojangUnboxedHex() {
      assertEquals("<#FFFFFF>", parserAmpersand.translate("&#FFFFFF"));
    }

    @Test
    void convertsUnboxedHex() {
      assertEquals("<#FFFFFF>", parserAmpersand.translate("#FFFFFF"));
    }

    @Test
    void ampersandLegacyCodeDoesNotInterfereWithMojangUnboxedHex() {
      // &c is converted to <red> by the legacy translator; &#AABBCC is then converted
      // by MojangUnboxedHex; legacy leaves the color open across the hex token
      assertEquals("<red>Hello <#AABBCC> World",
          parserAmpersand.translate("&cHello &#AABBCC World"));
    }

    @Test
    void ampersandFollowedByHashAndInvalidHexIsPassedThrough() {
      assertEquals("&#GGGGGG", parserAmpersand.translate("&#GGGGGG"));
    }

    @Test
    void sectionSignPassesThroughWhenAmpersandIsLegacyChar() {
      assertEquals("§cHello", parserAmpersand.translate("§cHello"));
    }

    @Test
    void allThreeHexFormatsConvertedInSameString() {
      assertEquals("<#FF0000> <#00FF00> <#0000FF>",
          parserAmpersand.translate("<&#FF0000> &#00FF00 #0000FF"));
    }
  }

  @Nested
  class NonInterference {

    @Test
    void mojangBoxedHexOutputIsNotCorruptedByUnboxedHexTranslator() {
      // <&#FFFFFF> -> <#FFFFFF> by MojangBoxedHex; UnboxedHex must not then match
      // the inner #FFFFFF and produce <<#FFFFFF>>
      assertEquals("<#FFFFFF>", parser.translate("<&#FFFFFF>"));
    }

    @Test
    void mojangUnboxedHexOutputIsNotCorruptedByUnboxedHexTranslator() {
      // &#FFFFFF -> <#FFFFFF> by MojangUnboxedHex; UnboxedHex must not then match
      // the inner #FFFFFF and produce <<#FFFFFF>>
      assertEquals("<#FFFFFF>", parser.translate("&#FFFFFF"));
    }

    @Test
    void alreadyBoxedMiniMessageHexIsNotDoubleConverted() {
      assertEquals("<#FFFFFF>", parser.translate("<#FFFFFF>"));
    }

    @Test
    void multipleHexCodesInChainDoNotCorruptEachOther() {
      assertEquals("<#FF0000> <#00FF00> <#0000FF>",
          parser.translate("<&#FF0000> &#00FF00 #0000FF"));
    }

    @Test
    void legacyTranslatorDoesNotCorruptHexOutput() {
      assertEquals("<red><#AABBCC>",
          parser.translate("§c<&#AABBCC>"));
    }

    @Test
    void sevenDigitHexIsNotPartiallyMatched() {
      assertEquals("#FFFFFFF", parser.translate("#FFFFFFF"));
    }

    @Test
    void sevenDigitMojangUnboxedHexIsNotPartiallyMatched() {
      assertEquals("&#FFFFFFF", parser.translate("&#FFFFFFF"));
    }

    @Test
    void hexArgumentsInsideMiniMessageTagsAreNotConverted() {
      // #RRGGBB is valid MiniMessage syntax inside a tag (e.g. gradient stops, <c:#...>).
      // These hex codes must pass through untouched - only bare hex in plain text is converted.
      String input = "<gradient:#FCD620:#F0A615:#FCD620><b>Heaven</b></gradient> "
          + "<c:#90630C>x</c> <c:#F7C23D><b>y</b></c>";
      assertEquals(input, parser.translate(input));
    }

    @Test
    void reportedMiniMessageStringIsLeftIntact() {
      // Regression: every #RRGGBB here sits inside a MiniMessage tag, so the full chain must
      // be a no-op rather than rewriting the hex args and corrupting the tags.
      String input = "<gradient:#FCD620:#F0A615:#FCD620><b>HᴇᴀᴠᴇɴCᴜʙᴇ</b></gradient>"
          + " <c:#90630C>❭</c> <c:#F7C23D><b>Sᴜʀᴠɪᴇ</b> ᐠ1</c>"
          + "                    <c:#D69F06><b>ѕᴇᴍɪ-ʀᴘ</b></c><newline>"
          + "<c:#E64545><b>➥</b></c> <c:#EF8888>Nouvelle version en préparation !</c>"
          + "                <c:#D69F06>v2...</c>";
      assertEquals(input, parser.translate(input));
    }
  }

  @Nested
  class BuilderTests {

    @Test
    void addTranslatorAppendsToChain() {
      var parser = (ComponentParserImpl) ComponentParser.builder()
          .addTranslator(MiniMessageTranslators.MOJANG_BOXED_HEX)
          .addTranslator(MiniMessageTranslators.MOJANG_UNBOXED_HEX)
          .build();

      assertEquals("<#FF0000> <#00FF00>", parser.translate("<&#FF0000> &#00FF00"));
    }

    @Test
    void withTranslatorsReplacesExistingChain() {
      var parser = (ComponentParserImpl) ComponentParser.builder()
          .addTranslator(MiniMessageTranslators.LEGACY_CODE_SECTION)
          .withTranslators(MiniMessageTranslators.MOJANG_BOXED_HEX)
          .build();

      // Legacy translator was replaced - §c should pass through unchanged
      assertEquals("§cHello", parser.translate("§cHello"));
      // Boxed hex translator is now the only one active
      assertEquals("<#FFFFFF>", parser.translate("<&#FFFFFF>"));
    }

    @Test
    void toBuilderProducesEqualParser() {
      var original = (ComponentParserImpl) ComponentParser.builder()
          .withTranslators(MiniMessageTranslators.MOJANG_BOXED_HEX)
          .build();

      var copy = (ComponentParserImpl) original.toBuilder().build();

      assertEquals(original, copy);
    }

    @Test
    void toBuilderAllowsExtension() {
      var base = (ComponentParserImpl) ComponentParser.builder()
          .withTranslators(MiniMessageTranslators.MOJANG_BOXED_HEX)
          .build();

      var extended = (ComponentParserImpl) base.toBuilder()
          .addTranslator(MiniMessageTranslators.MOJANG_UNBOXED_HEX)
          .build();

      // base only handles boxed hex
      assertEquals("<#FFFFFF> &#FFFFFF", base.translate("<&#FFFFFF> &#FFFFFF"));
      // extended handles both
      assertEquals("<#FFFFFF> <#FFFFFF>", extended.translate("<&#FFFFFF> &#FFFFFF"));
    }

    @Test
    void toBuilderDoesNotMutateOriginal() {
      var original = (ComponentParserImpl) ComponentParser.builder()
          .withTranslators(MiniMessageTranslators.MOJANG_BOXED_HEX)
          .build();

      original.toBuilder()
          .addTranslator(MiniMessageTranslators.MOJANG_UNBOXED_HEX)
          .build();

      // Original should still only translate boxed hex
      assertEquals("<#FFFFFF> &#FFFFFF", original.translate("<&#FFFFFF> &#FFFFFF"));
    }
  }

  @Nested
  class CustomTranslatorSelection {

    @Test
    void onlyMojangBoxedHexTranslatorApplied() {
      ComponentParserImpl parser = new ComponentParserImpl(
          List.of(MiniMessageTranslators.MOJANG_BOXED_HEX),
          MiniMessage.miniMessage()
      );

      // &#FFFFFF (unboxed) should remain unchanged when only boxed translator is used
      String input = "<&#FFFFFF> &#FFFFFF";
      assertEquals("<#FFFFFF> &#FFFFFF", parser.translate(input));
    }

    @Test
    void onlyMojangUnboxedHexTranslatorApplied() {
      ComponentParserImpl parser = new ComponentParserImpl(
          List.of(MiniMessageTranslators.MOJANG_UNBOXED_HEX),
          MiniMessage.miniMessage()
      );

      // <&#FFFFFF> (boxed) should remain unchanged when only unboxed translator is used
      String input = "<&#FFFFFF> &#FFFFFF";
      assertEquals("<&#FFFFFF> <#FFFFFF>", parser.translate(input));
    }

    @Test
    void onlyUnboxedHexTranslatorApplied() {
      ComponentParserImpl parser = new ComponentParserImpl(
          List.of(MiniMessageTranslators.UNBOXED_HEX),
          MiniMessage.miniMessage()
      );

      // &#FFFFFF is excluded by the negative lookbehind for &, and <&#FFFFFF> is skipped as a
      // tag span; only the bare #FFFFFF is matched
      String input = "<&#FFFFFF> &#FFFFFF #FFFFFF";
      assertEquals("<&#FFFFFF> &#FFFFFF <#FFFFFF>", parser.translate(input));
    }

    @Test
    void onlyAmpersandLegacyTranslatorApplied() {
      ComponentParserImpl parser = new ComponentParserImpl(
          List.of(MiniMessageTranslators.LEGACY_CODE_AMPERSAND),
          MiniMessage.miniMessage()
      );

      // Hex patterns must remain unconverted; legacy closes the color tag at end of string
      assertEquals("<red>&#AABBCC</red>", parser.translate("&c&#AABBCC"));
    }

    @Test
    void boxedAndUnboxedMojangHexTranslatorsChainCorrectly() {
      ComponentParserImpl parser = new ComponentParserImpl(
          List.of(MiniMessageTranslators.MOJANG_BOXED_HEX, MiniMessageTranslators.MOJANG_UNBOXED_HEX),
          MiniMessage.miniMessage()
      );

      String input = "<&#FF0000> &#00FF00";
      assertEquals("<#FF0000> <#00FF00>", parser.translate(input));
    }

    @Test
    void explicitEmptyTranslatorArrayReturnsInputUnchanged() {
      ComponentParserImpl parser = new ComponentParserImpl(
          List.of(),
          MiniMessage.miniMessage()
      );

      String input = "§cHello <&#FFFFFF> &#AABBCC #123456";
      assertEquals(input, parser.translate(input));
    }
  }

  @Nested
  class Escape {

    @Test
    void escapeNeutralisesAllTranslatorPatterns() {
      // Each translator inserts its own backslash; no translator re-escapes another's output.
      // MM.escapeTags leaves the boxed token alone because <&\#AABBCC> is not a known MM tag.
      String input = "§cRed <&#AABBCC> &#112233 #445566 done";
      assertEquals("§\\cRed <&\\#AABBCC> &\\#112233 #\\445566 done", parser.escape(input));
    }

    @Test
    void escapedOutputIsNotRetranslated() {
      // Re-running translate over escaped text must be a no-op for each translator.
      String escaped = parser.escape("§cHello <&#AABBCC> &#112233 #445566 done");
      assertEquals(escaped, parser.translate(escaped));
    }

    @Test
    void miniMessageTagsAreEscapedByMiniMessage() {
      // <red> is not a translator pattern; MM.escapeTags must escape it (both open and close).
      assertEquals("\\<red>Hi\\</red>", parser.escape("<red>Hi</red>"));
    }

    @Test
    void emptyStringReturnsEmpty() {
      assertEquals("", parser.escape(""));
    }

    @Test
    void plainTextReturnsUnchanged() {
      assertEquals("Hello, world!", parser.escape("Hello, world!"));
    }
  }

  @Nested
  class Strip {

    @Test
    void stripRemovesAllTranslatorPatternsAndMmTags() {
      // §c, <&#AABBCC>, &#112233, and #445566 are each stripped, leaving their surrounding
      // spaces intact - four single-space gaps between "Red" and "done".
      assertEquals("Red    done",
          parser.strip("§cRed <&#AABBCC> &#112233 #445566 done"));
    }

    @Test
    void stripRemovesMiniMessageTags() {
      assertEquals("Hi", parser.strip("<red>Hi</red>"));
    }

    @Test
    void stripRemovesMixedLegacyAndMmTags() {
      assertEquals("Hello world",
          parser.strip("§c<bold>Hello</bold> §rworld"));
    }

    @Test
    void emptyStringReturnsEmpty() {
      assertEquals("", parser.strip(""));
    }

    @Test
    void plainTextReturnsUnchanged() {
      assertEquals("Hello, world!", parser.strip("Hello, world!"));
    }

    @Test
    void stripWithAmpersandLegacyTranslator() {
      ComponentParserImpl parserAmpersand = new ComponentParserImpl(
          List.of(MiniMessageTranslators.MOJANG_BOXED_HEX, MiniMessageTranslators.MOJANG_UNBOXED_HEX,
              MiniMessageTranslators.UNBOXED_HEX, MiniMessageTranslators.LEGACY_CODE_AMPERSAND),
          MiniMessage.miniMessage()
      );

      assertEquals("Hello world", parserAmpersand.strip("&cHello &rworld"));
    }
  }
}
