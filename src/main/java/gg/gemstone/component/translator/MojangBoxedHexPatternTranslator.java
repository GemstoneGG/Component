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

/**
 * Translates Mojang-style <em>boxed</em> hex color codes into MiniMessage hex tags.
 *
 * <p>The pattern {@code <&#RRGGBB>} is replaced with {@code <#RRGGBB>}. Only exactly
 * six hex digits are matched; seven-or-more digit sequences are left untouched.
 *
 * <p>Unlike the other hex translators this one matches across the whole string rather than only
 * outside tag spans: its pattern deliberately targets a {@code <...>} construct, which would
 * otherwise be skipped as a tag span. This is safe because {@code <&#RRGGBB>} is never valid
 * MiniMessage syntax.
 */
class MojangBoxedHexPatternTranslator extends RegexMiniMessageTranslator {

  /**
   * Matches Mojang-style boxed hex codes (e.g. {@code <&#FFFFFF>}).
   */
  private static final Pattern BOXED_MOJANG_PATTERN = Pattern.compile("<&#([A-Fa-f0-9]{6})>");

  /**
   * MiniMessage-style boxed hex code replacement, where {@code $1} is substituted with a 6-digit hex string (e.g. {@code <#FFFFFF>}).
   */
  private static final String BOXED_HEX_REPLACEMENT = "<#$1>";

  /**
   * Escape replacement: {@code <&#RRGGBB>} becomes {@code <&\#RRGGBB>}, neutralising the pattern
   * for both this translator and {@link MojangUnboxedHexPatternTranslator}.
   */
  private static final String BOXED_HEX_ESCAPE_REPLACEMENT = "<&\\\\#$1>";

  /**
   * Use {@link MiniMessageTranslators#MOJANG_BOXED_HEX}.
   */
  MojangBoxedHexPatternTranslator() {
    super(BOXED_MOJANG_PATTERN, BOXED_HEX_REPLACEMENT, BOXED_HEX_ESCAPE_REPLACEMENT);
  }

  @Override
  String replace(String input, String replacement) {
    // The pattern targets a `<...>` construct, so it must run over the whole string; the
    // default tag-aware matching would skip `<&#RRGGBB>` as a tag span.
    return replaceWholeString(input, replacement);
  }
}
