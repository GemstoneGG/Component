package gg.gemstone.component.translator;

import org.jetbrains.annotations.NotNull;

/**
 * Pre-built {@link MiniMessageTranslator} implementations available for use in a
 * {@link gg.gemstone.component.ComponentParser} translator chain.
 *
 * <p>Each constant is itself a {@link MiniMessageTranslator}. Pass one or more constants - or any
 * custom {@link MiniMessageTranslator} - to
 * {@link gg.gemstone.component.ComponentParser.Builder#addTranslator} or
 * {@link gg.gemstone.component.ComponentParser.Builder#withTranslators} to configure which
 * conversions are applied, and in what order.
 */
public enum MiniMessageTranslators implements MiniMessageTranslator {

  /**
   * Translates {@code &}-prefixed legacy color and formatting codes into MiniMessage tags.
   */
  LEGACY_CODE_AMPERSAND(new LegacyFormattingCodeTranslator('&')),

  /**
   * Translates {@code §}-prefixed legacy color and formatting codes into MiniMessage tags.
   */
  LEGACY_CODE_SECTION(new LegacyFormattingCodeTranslator('§')),

  /**
   * Translates Mojang-style <em>boxed</em> hex colors ({@code <&#RRGGBB>}) into MiniMessage hex tags ({@code <#RRGGBB>}).
   */
  MOJANG_BOXED_HEX(new MojangBoxedHexPatternTranslator()),

  /**
   * Translates Mojang-style <em>unboxed</em> hex colors ({@code &#RRGGBB}) into MiniMessage hex tags ({@code <#RRGGBB>}).
   */
  MOJANG_UNBOXED_HEX(new MojangUnboxedHexPatternTranslator()),

  /**
   * Translates bare hex colors ({@code #RRGGBB}) into MiniMessage hex tags ({@code <#RRGGBB>}).
   */
  UNBOXED_HEX(new UnboxedHexPatternTranslator());

  private final MiniMessageTranslator delegate;

  MiniMessageTranslators(MiniMessageTranslator delegate) {
    this.delegate = delegate;
  }

  @Override
  public @NotNull String translate(@NotNull String input) {
    return delegate.translate(input);
  }

  /**
   * Returns a {@link MiniMessageTranslator} that translates legacy color and formatting codes
   * prefixed by {@code sectionChar} into MiniMessage tags.
   *
   * <p>Use this when neither {@link #LEGACY_CODE_SECTION} ({@code §}) nor
   * {@link #LEGACY_CODE_AMPERSAND} ({@code &}) matches your input format.
   *
   * @param sectionChar the prefix character that precedes legacy formatting codes
   * @return a translator for the given prefix character
   */
  public static MiniMessageTranslator legacyCodeTranslator(char sectionChar) {
    return new LegacyFormattingCodeTranslator(sectionChar);
  }
}
