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

import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

/**
 * Translates Mojang-style <em>unboxed</em> hex color codes into MiniMessage hex tags.
 *
 * <p>The pattern {@code &#RRGGBB} is replaced with {@code <#RRGGBB>}. A negative lookbehind
 * for {@code <} ensures that already-boxed {@code <&#RRGGBB>} sequences are not matched here
 * (those are handled by {@link MojangBoxedHexPatternTranslator}). Only exactly six hex digits
 * are matched; seven-or-more digit sequences are left untouched.
 */
class MojangUnboxedHexPatternTranslator implements MiniMessageTranslator {

  /**
   * Matches Mojang-style unboxed hex codes (e.g. {@code &#FFFFFF}).
   */
  private static final Pattern UNBOXED_MOJANG_PATTERN = Pattern.compile("(?<!<)&#([A-Fa-f0-9]{6})(?![A-Fa-f0-9])");

  /**
   * MiniMessage-style boxed hex code replacement, where {@code $1} is substituted with a 6-digit hex string (e.g. {@code <#FFFFFF>}).
   */
  private static final String BOXED_HEX_REPLACEMENT = "<#$1>";

  /**
   * Escape replacement: {@code &#RRGGBB} becomes {@code &\#RRGGBB}, neutralising the pattern
   * for this translator.
   */
  private static final String UNBOXED_HEX_ESCAPE_REPLACEMENT = "&\\\\#$1";

  /**
   * Use {@link MiniMessageTranslators#MOJANG_UNBOXED_HEX}.
   */
  MojangUnboxedHexPatternTranslator() {
  }

  @Override
  public @NotNull String translate(final @NotNull String input) {
    return UNBOXED_MOJANG_PATTERN.matcher(input).replaceAll(BOXED_HEX_REPLACEMENT);
  }

  @Override
  public @NotNull String escape(final @NotNull String input) {
    return UNBOXED_MOJANG_PATTERN.matcher(input).replaceAll(UNBOXED_HEX_ESCAPE_REPLACEMENT);
  }

  @Override
  public @NotNull String strip(final @NotNull String input) {
    return UNBOXED_MOJANG_PATTERN.matcher(input).replaceAll("");
  }
}
