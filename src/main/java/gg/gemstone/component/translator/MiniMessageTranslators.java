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
