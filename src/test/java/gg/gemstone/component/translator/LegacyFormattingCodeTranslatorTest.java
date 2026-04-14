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

/**
 * Unit tests for {@link LegacyFormattingCodeTranslator}.
 *
 * <p>Tests are organised into nested classes by scenario category. The section sign
 * {@code §} (U+00A7) is used as the section character throughout, unless a test is
 * explicitly verifying custom section character support.
 *
 * <p>Legacy code to MiniMessage tag name reference:
 * <pre>
 *   §0 black       §1 dark_blue   §2 dark_green  §3 dark_aqua
 *   §4 dark_red    §5 dark_purple §6 gold         §7 gray
 *   §8 dark_gray   §9 blue        §a green        §b aqua
 *   §c red         §d light_purple §e yellow      §f white
 *   §k obfuscated  §l bold        §m strikethrough §n underlined  §o italic
 *   §r reset
 * </pre>
 */
class LegacyFormattingCodeTranslatorTest {

  private final LegacyFormattingCodeTranslator translator = new LegacyFormattingCodeTranslator('§');

  @Nested
  class NoOp {

    @Test
    void emptyStringIsReturnedUnchanged() {
      assertEquals("", translator.translate(""));
    }

    @Test
    void plainTextWithNoCodesIsReturnedUnchanged() {
      assertEquals("Hello, world!", translator.translate("Hello, world!"));
    }

    @Test
    void pureMinimessageStringIsReturnedUnchanged() {
      String input = "<red>Hello</red> <bold>world</bold>!";
      assertEquals(input, translator.translate(input));
    }

    @Test
    void unknownCodeCharacterIsNotInterpreted() {
      // §z is not a valid code - § and 'z' pass through as literal text
      assertEquals("§zHello", translator.translate("§zHello"));
    }

    @Test
    void sectionCharAtEndOfStringIsPassedThrough() {
      // § with nothing following it cannot form a code
      assertEquals("Hello§", translator.translate("Hello§"));
    }

    @Test
    void uppercaseLegacyCodeCharIsNormalized() {
      // §C is equivalent to §c
      assertEquals("<red>Hello</red>", translator.translate("§CHello"));
    }
  }

  @Nested
  class ColorCodes {

    @Test
    void singleColorCode() {
      assertEquals("<red>Hello</red>", translator.translate("§cHello"));
    }

    @Test
    void allSixteenColorCodesMapToCorrectTagNames() {
      assertEquals("<black>t</black>", translator.translate("§0t"));
      assertEquals("<dark_blue>t</dark_blue>", translator.translate("§1t"));
      assertEquals("<dark_green>t</dark_green>", translator.translate("§2t"));
      assertEquals("<dark_aqua>t</dark_aqua>", translator.translate("§3t"));
      assertEquals("<dark_red>t</dark_red>", translator.translate("§4t"));
      assertEquals("<dark_purple>t</dark_purple>", translator.translate("§5t"));
      assertEquals("<gold>t</gold>", translator.translate("§6t"));
      assertEquals("<gray>t</gray>", translator.translate("§7t"));
      assertEquals("<dark_gray>t</dark_gray>", translator.translate("§8t"));
      assertEquals("<blue>t</blue>", translator.translate("§9t"));
      assertEquals("<green>t</green>", translator.translate("§at"));
      assertEquals("<aqua>t</aqua>", translator.translate("§bt"));
      assertEquals("<red>t</red>", translator.translate("§ct"));
      assertEquals("<light_purple>t</light_purple>", translator.translate("§dt"));
      assertEquals("<yellow>t</yellow>", translator.translate("§et"));
      assertEquals("<white>t</white>", translator.translate("§ft"));
    }

    @Test
    void colorCodeWithNoTrailingTextProducesEmptyTagPair() {
      // §c at end of string: produces <red></red> - semantically correct empty pair
      assertEquals("<red></red>", translator.translate("§c"));
    }

    @Test
    void consecutiveColorCodesProduceEmptyTagPairForFirst() {
      // §c§a - red is opened then immediately replaced by green; <red></red> is expected
      assertEquals("<red></red><green>Hello</green>",
          translator.translate("§c§aHello"));
    }

    @Test
    void colorCodeClosesAndReopensCorrectly() {
      assertEquals("<red>A</red><green>B</green>",
          translator.translate("§cA§aB"));
    }

    @Test
    void threeConsecutiveColorChanges() {
      assertEquals("<red>A</red><green>B</green><aqua>C</aqua>",
          translator.translate("§cA§aB§bC"));
    }
  }

  @Nested
  class FormattingCodes {

    @Test
    void allFormattingCodesMapToCorrectTagNames() {
      assertEquals("<obfuscated>t</obfuscated>", translator.translate("§kt"));
      assertEquals("<bold>t</bold>", translator.translate("§lt"));
      assertEquals("<strikethrough>t</strikethrough>", translator.translate("§mt"));
      assertEquals("<underlined>t</underlined>", translator.translate("§nt"));
      assertEquals("<italic>t</italic>", translator.translate("§ot"));
    }

    @Test
    void formattingCodesStack() {
      // §l§n§o - bold + underlined + italic simultaneously
      assertEquals("<bold><underlined><italic>Hi</italic></underlined></bold>",
          translator.translate("§l§n§oHi"));
    }

    @Test
    void formattingCodeDoesNotCloseActiveColor() {
      // §c§n - color stays open when format is applied on top
      assertEquals("<red><underlined>Hi</underlined></red>",
          translator.translate("§c§nHi"));
    }

    @Test
    void formattingCodeIsIdempotent_doubleApplication() {
      // §l§l - second §l is a no-op; only one pair of tags produced
      assertEquals("<bold>Hi</bold>", translator.translate("§l§lHi"));
    }

    @Test
    void formattingCodeIsIdempotent_afterColorClearedAndReapplied() {
      // §l active, §c wipes it, §l re-opens bold - must work, not skip due to stale state
      assertEquals("<bold>A</bold><red><bold>B</bold></red>",
          translator.translate("§lA§c§lB"));
    }

    @Test
    void colorMustPrecedeFormatForCombinedEffect() {
      // §c§l - color first, then format: X is red AND bold
      assertEquals("<red><bold>X</bold></red>",
          translator.translate("§c§lX"));
    }
  }

  @Nested
  class VanillaBehavior {

    @Test
    void colorCodeAfterFormatWipesActiveFormat() {
      // §nX§cY - vanilla: X underlined, §c wipes underline, Y is plain red
      assertEquals("<underlined>X</underlined><red>Y</red>",
          translator.translate("§nX§cY"));
    }

    @Test
    void formatCodeAfterColorPreservesActiveColor() {
      // §cX§nY - vanilla: X plain red, §n stacks on top, Y is red AND underlined
      assertEquals("<red>X<underlined>Y</underlined></red>",
          translator.translate("§cX§nY"));
    }

    @Test
    void colorChangeMidFormatRequiresFormatRestatement() {
      // To have bold in both segments, §l must be restated after each color code
      assertEquals("<red><bold>Hello </bold></red><green><bold>World</bold></green>",
          translator.translate("§c§lHello §a§lWorld"));
    }

    @Test
    void colorAfterFormatProducesEmptyFormatTagPair() {
      // §n§c - §c immediately wipes §n with no text between them
      assertEquals("<underlined></underlined><red>text</red>",
          translator.translate("§n§ctext"));
    }
  }

  @Nested
  class ResetCode {

    @Test
    void resetClosesAllOpenFormattingTags() {
      assertEquals("<bold><underlined><italic>Hi</italic></underlined></bold> plain",
          translator.translate("§l§n§oHi§r plain"));
    }

    @Test
    void resetClosesColorAndAllFormatting() {
      assertEquals("<red><bold>Hi</bold></red> plain",
          translator.translate("§c§lHi§r plain"));
    }

    @Test
    void resetOnEmptyStackIsNoOp() {
      assertEquals("Hello", translator.translate("§rHello"));
    }

    @Test
    void resetFollowedByNewStyles() {
      assertEquals("<red>A</red><green>B</green>",
          translator.translate("§cA§r§aB"));
    }

    @Test
    void multipleConsecutiveResetsAreNoOpsAfterFirst() {
      assertEquals("<red>A</red>plain",
          translator.translate("§cA§r§r§rplain"));
    }
  }

  @Nested
  class MinimessagePassthrough {

    @Test
    void nestedMinimessageTagsArePassedThroughVerbatim() {
      String input = "<red><bold>Hello</bold></red>";
      assertEquals(input, translator.translate(input));
    }

    @Test
    void minimessageTagWithColonArgumentsIsPassedThroughVerbatim() {
      String input = "<click:run_command:/say hello>click me</click>";
      assertEquals(input, translator.translate(input));
    }

    @Test
    void minimessageTagWithSingleQuotedNestedAnglesIsPassedThroughVerbatim() {
      String input = "<hover:show_text:'<red>tooltip'>text</hover>";
      assertEquals(input, translator.translate(input));
    }

    @Test
    void minimessageTagWithDoubleQuotedNestedAnglesIsPassedThroughVerbatim() {
      String input = "<hover:show_text:\"<red>tooltip\">text</hover>";
      assertEquals(input, translator.translate(input));
    }

    @Test
    void selfClosingMinimessageTagIsPassedThroughVerbatim() {
      String input = "before<br/>after";
      assertEquals(input, translator.translate(input));
    }

    @Test
    void hexColorMinimessageTagIsPassedThroughVerbatim() {
      String input = "<#00ff00>green text</#00ff00>";
      assertEquals(input, translator.translate(input));
    }

    @Test
    void unclosedMinimessageTagsArePassedThroughWithoutModification() {
      // Lenient MM mode - unclosed MM tags are MM's problem, not ours
      String input = "<green>unclosed text";
      assertEquals(input, translator.translate(input));
    }

    @Test
    void malformedTagWithNoClosingAngleIsPassedThroughCharByChar() {
      String input = "<unclosed";
      assertEquals(input, translator.translate(input));
    }

    @Test
    void loneMiniMessageOpenAngleIsPassedThroughAsLiteral() {
      // A '<' with no valid tag following it is emitted literally
      assertEquals("a < b", translator.translate("a < b"));
    }
  }

  @Nested
  class MixedLegacyOutsideScope {

    @Test
    void legacyColorPersistsThroughInlineMinimessageTag() {
      // §c opens red as base; <green> overrides inline; after </green>, red resumes
      assertEquals("<red>Red. <green>Green!</green> Red again!</red>",
          translator.translate("§cRed. <green>Green!</green> Red again!"));
    }

    @Test
    void legacyFormattingPersistsThroughInlineMinimessageTag() {
      assertEquals("<bold>Bold <italic>italic bold</italic> bold</bold>",
          translator.translate("§lBold <italic>italic bold</italic> bold"));
    }

    @Test
    void legacyColorWithClickTag() {
      assertEquals("<red>Red <click:run_command:/say hi>click</click> still red</red>",
          translator.translate("§cRed <click:run_command:/say hi>click</click> still red"));
    }

    @Test
    void legacyColorWithHoverTag() {
      assertEquals("<red>Hover <hover:show_text:'tip'>here</hover> still red</red>",
          translator.translate("§cHover <hover:show_text:'tip'>here</hover> still red"));
    }

    @Test
    void minimessageResetTagFlushesOuterLegacyStackWhenMmStackIsClean() {
      assertEquals("<red><bold>A</bold></red><reset>C",
          translator.translate("§c§lA<reset>C"));
    }

    @Test
    void minimessageResetTagDoesNotFlushOuterLegacyStackWhenMmStackIsDirty() {
      // §c is outer; <green> scope contains <reset>; outer §c must survive
      assertEquals("<red><green><reset>after</green></red>",
          translator.translate("§c<green><reset>after</green>"));
    }

    @Test
    void outerLegacyTagsNotClosedAtEndOfStringWhenMmStackIsDirty() {
      // <green> is never closed - we must not append </red> across that boundary
      assertEquals("<red>A<green>B",
          translator.translate("§cA<green>B"));
    }
  }

  @Nested
  class InnerLegacyTags {

    @Test
    void legacyColorInsideMmScopeIsAutoClosedBeforeClosingMmTag() {
      assertEquals("<green>Text <red>and red</red></green>",
          translator.translate("<green>Text §cand red</green>"));
    }

    @Test
    void legacyColorInsideMmScopeIsAutoClosedBeforeNextOpeningMmTag() {
      assertEquals("<green>Text <red>red </red><click:run_command:/foo>click</click></green>",
          translator.translate("<green>Text §cred <click:run_command:/foo>click</click></green>"));
    }

    @Test
    void legacyFormattingInsideMmScopeIsAutoClosedBeforeClosingMmTag() {
      assertEquals("<green>Text <bold>bold</bold></green>",
          translator.translate("<green>Text §lbold</green>"));
    }

    @Test
    void multipleInnerLegacyTagsAreAllAutoClosedInLIFOOrder() {
      // §c§l inside <green> - both are inner, closed in reverse order before </green>
      assertEquals("<green>Text <red><bold>styled</bold></red></green>",
          translator.translate("<green>Text §c§lstyled</green>"));
    }

    @Test
    void innerColorChangeFlushesOnlyInnerColorAndOpensNew() {
      // §c then §b (aqua) inside <green> scope - §b flushes §c, opens aqua
      assertEquals("<green><red></red><aqua>aqua</aqua></green>",
          translator.translate("<green>§c§baqua</green>"));
    }

    @Test
    void innerResetFlushesOnlyInnerTags_notOuterOnes() {
      // §c outer, <green> scope, §l inner, §r inside scope - only §l is reset by §r
      assertEquals("<red><green><bold></bold></green></red>",
          translator.translate("§c<green>§l§r</green>"));
    }

    @Test
    void outerLegacyTagRemainsOpenAfterInnerLegacyFlush() {
      // §c outer; §b (aqua) inner inside <green>; </green> closes aqua but not red
      assertEquals("<red><green><aqua>aqua</aqua></green> still red</red>",
          translator.translate("§c<green>§baqua</green> still red"));
    }

    @Test
    void innerLegacyTagIsClosedAtEndOfStringWhenMmScopeStillOpen() {
      // <green> never closed; §c is inner - flushed at end-of-string; outer MM not touched
      assertEquals("<green><red>text</red>",
          translator.translate("<green>§ctext"));
    }

    @Test
    void outerLegacyTagNotFlushedAtEndOfStringWhenMmStackIsDirty() {
      // §c outer (not flushed), §b inner (flushed at end), <green> unclosed (not our problem)
      assertEquals("<red><green><aqua>text</aqua>",
          translator.translate("§c<green>§btext"));
    }

    @Test
    void innerLegacyTagAutoClosedBeforeSelfClosingMmTag() {
      assertEquals("<green><red>red </red><br/>after</green>",
          translator.translate("<green>§cred <br/>after</green>"));
    }

    @Test
    void innerLegacyTagAutoClosedBeforeVoidMmTag() {
      assertEquals("<green><bold>bold </bold><newline>after</green>",
          translator.translate("<green>§lbold <newline>after</green>"));
    }

    @Test
    void innerFormattingIsIdempotentWhenAlreadyActiveInOuterScope() {
      // §l active from outer scope; §l inside MM scope must not push a duplicate
      assertEquals("<bold><green>text</green></bold>",
          translator.translate("§l<green>§ltext</green>"));
    }

    @Test
    void nestedMmScopesEachAutoCloseTheirOwnInnerLegacyTags() {
      // §b (aqua) opened inside outer <green> scope, auto-closed before inner <blue> opens;
      // §e (yellow) opened inside <blue> scope, auto-closed before </blue>
      assertEquals("<green><aqua></aqua><blue><yellow>deep</yellow></blue></green>",
          translator.translate("<green>§b<blue>§edeep</blue></green>"));
    }

    @Test
    void innerLegacyColorDoesNotAffectOuterColorAfterMmScopeCloses() {
      // §c outer (red); §b inner (aqua) inside <green>; after </green> text is red again
      assertEquals("<red>red <green><aqua>aqua</aqua></green> red again</red>",
          translator.translate("§cred <green>§baqua</green> red again"));
    }

    @Test
    void legacyCodeAppliedAfterAllMmScopesClosed_behavesAsOuter() {
      // After </green> closes, mmStack is clean - §c here is an outer tag
      assertEquals("<green>text</green><red>red</red>",
          translator.translate("<green>text</green>§cred"));
    }
  }

  @Nested
  class VoidMinimessageTags {

    @Test
    void newlineTagDoesNotPolluteMmStack() {
      assertEquals("<newline><red>red</red>",
          translator.translate("<newline>§cred"));
    }

    @Test
    void brAliasDoesNotPolluteMmStack() {
      assertEquals("<br/><red>red</red>",
          translator.translate("<br/>§cred"));
    }

    @Test
    void keyTagDoesNotPolluteMmStack() {
      assertEquals("Press <key:key.jump> to jump <red>now</red>",
          translator.translate("Press <key:key.jump> to jump §cnow"));
    }

    @Test
    void langTagDoesNotPolluteMmStack() {
      assertEquals("<lang:block.minecraft.stone><red>after</red>",
          translator.translate("<lang:block.minecraft.stone>§cafter"));
    }

    @Test
    void selectorTagDoesNotPolluteMmStack() {
      assertEquals("<selector:@s><red>after</red>",
          translator.translate("<selector:@s>§cafter"));
    }

    @Test
    void scoreTagWithoutSlashDoesNotPolluteMmStack() {
      // Defensive: score without self-closing slash - still treated as void
      assertEquals("<score:rymiel:wins><red>after</red>",
          translator.translate("<score:rymiel:wins>§cafter"));
    }

    @Test
    void nbtTagWithoutSlashDoesNotPolluteMmStack() {
      assertEquals("<nbt:entity:'@s':Health><red>after</red>",
          translator.translate("<nbt:entity:'@s':Health>§cafter"));
    }

    @Test
    void headTagDoesNotPolluteMmStack() {
      assertEquals("<head:Notch><red>after</red>",
          translator.translate("<head:Notch>§cafter"));
    }

    @Test
    void spriteTagDoesNotPolluteMmStack() {
      assertEquals("<sprite:blocks:block/stone><red>after</red>",
          translator.translate("<sprite:blocks:block/stone>§cafter"));
    }
  }

  @Nested
  class TagNameExtraction {

    @Test
    void tagWithMultipleColonsExtractsFirstSegmentOnly() {
      // <click:run_command:/say hi> - tag name must be "click", not "click:run_command"
      assertEquals("<click:run_command:/say hi>text</click><red>red</red>",
          translator.translate("<click:run_command:/say hi>text</click>§cred"));
    }

    @Test
    void hoverTagWithSingleQuotedNestedAnglesDoesNotSplitPrematurely() {
      // The > inside '<red>test' must not be treated as the tag's closing delimiter
      String input = "<hover:show_text:'<red>test'>TEXT</hover>";
      assertEquals(input, translator.translate(input));
    }

    @Test
    void hoverTagWithDoubleQuotedNestedAnglesDoesNotSplitPrematurely() {
      String input = "<hover:show_text:\"<red>test\">TEXT</hover>";
      assertEquals(input, translator.translate(input));
    }

    @Test
    void hexColorTagOpenAndCloseBalanceCorrectly() {
      // <#ff5500> must push "#ff5500" and </#ff5500> must pop it
      assertEquals("<#ff5500>text</#ff5500><red>after</red>",
          translator.translate("<#ff5500>text</#ff5500>§cafter"));
    }

    @Test
    void hexColorInsideMmScopeIsAutoClosedBeforeClosingTag() {
      assertEquals("<green><#ff0000>red</#ff0000></green>",
          translator.translate("<green><#ff0000>red</#ff0000></green>"));
    }

    @Test
    void tagNamesAreMatchedCaseInsensitively() {
      // <GREEN> and </GREEN> must balance; §c inside is inner and auto-closed
      assertEquals("<GREEN><red>inner</red></GREEN><aqua>after</aqua>",
          translator.translate("<GREEN>§cinner</GREEN>§bafter"));
    }

    @Test
    void mismatchedClosingTagDoesNotDestroyMmStack() {
      // </blue> does not match open <green>; mmStack should remain [green]
      // so §c after </blue> is still treated as inner
      assertEquals("<green><red>red</red></blue><bold>bold</bold></green>",
          translator.translate("<green>§cred</blue>§lbold</green>"));
    }
  }

  @Nested
  class CustomSectionChar {

    private final LegacyFormattingCodeTranslator ampersandTranslator = new LegacyFormattingCodeTranslator('&');

    @Test
    void ampersandUsedAsSectionChar() {
      assertEquals("<red>Hello</red>", ampersandTranslator.translate("&cHello"));
    }

    @Test
    void defaultSectionSignIsLiteralTextWhenCustomCharIsUsed() {
      // § must be passed through as plain text when the section char is '&'
      assertEquals("§cHello", ampersandTranslator.translate("§cHello"));
    }

    @Test
    void ampersandWithMixedMinimessage() {
      assertEquals("<green>Green <red>red</red></green>",
          ampersandTranslator.translate("<green>Green &cred</green>"));
    }
  }

  @Nested
  class EndToEnd {

    @Test
    void vanillaExample_colorAfterFormatWipesFormat() {
      assertEquals("<underlined>X</underlined><red>Y</red>",
          translator.translate("§nX§cY"));
    }

    @Test
    void vanillaExample_formatAfterColorStacksOnColor() {
      assertEquals("<red>X<underlined>Y</underlined></red>",
          translator.translate("§cX§nY"));
    }

    @Test
    void vanillaExample_colorChangeMidBold() {
      assertEquals("<red><bold>Hello </bold></red><green>World</green>",
          translator.translate("§c§lHello §aWorld"));
    }

    @Test
    void vanillaExample_stackedFormatsWithReset() {
      assertEquals("<bold><underlined><italic>Hello</italic></underlined></bold> World",
          translator.translate("§l§n§oHello§r World"));
    }

    @Test
    void mixedExample_legacyBaseColorWithInlineGreenOverride() {
      assertEquals("<red>Red. <green>Green!</green> Red again!</red>",
          translator.translate("§cRed. <green>Green!</green> Red again!"));
    }

    @Test
    void mixedExample_innerAutoCloseBeforeClosingMmTag() {
      assertEquals("<green>The green text: <red>and red text</red></green>",
          translator.translate("<green>The green text: §cand red text</green>"));
    }

    @Test
    void mixedExample_innerAutoCloseBeforeClickOpeningTag() {
      assertEquals(
          "<green>The green text: <red>and red text </red><click:run_command:...>runs a command</click></green>",
          translator.translate(
              "<green>The green text: §cand red text <click:run_command:...>runs a command</click></green>"));
    }

    @Test
    void mixedExample_legacyCodeInsideUnclosedMmScopes_treatedAsInner() {
      // §c outer (red); <green> and <blue> opened but never closed;
      // §f (white) is inner - opened inside [green, blue] scope.
      // Inner §f is closed at end-of-string; outer §c is not (MM stack dirty).
      assertEquals("<red>I'm red. <green>I'm green. <blue>I'm blue! <white>Will I be white?</white>",
          translator.translate(
              "§cI'm red. <green>I'm green. <blue>I'm blue! §fWill I be white?"));
    }
  }
}
