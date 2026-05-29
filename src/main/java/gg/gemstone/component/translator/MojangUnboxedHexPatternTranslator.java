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
 * Translates Mojang-style <em>unboxed</em> hex color codes into MiniMessage hex tags.
 *
 * <p>The pattern {@code &#RRGGBB} is replaced with {@code <#RRGGBB>}. Matching happens only outside
 * MiniMessage tag spans (see {@link RegexMiniMessageTranslator}), so an already-boxed
 * {@code <&#RRGGBB>} sequence is skipped as a tag span (those are handled by
 * {@link MojangBoxedHexPatternTranslator}). Only exactly six hex digits are matched;
 * seven-or-more digit sequences are left untouched.
 */
class MojangUnboxedHexPatternTranslator extends RegexMiniMessageTranslator {

  /**
   * Matches Mojang-style unboxed hex codes (e.g. {@code &#FFFFFF}). Boxed {@code <&#FFFFFF>}
   * sequences are skipped as tag spans by the tag-aware matching in
   * {@link RegexMiniMessageTranslator}.
   */
  private static final Pattern UNBOXED_MOJANG_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})(?![A-Fa-f0-9])");

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
    super(UNBOXED_MOJANG_PATTERN, BOXED_HEX_REPLACEMENT, UNBOXED_HEX_ESCAPE_REPLACEMENT);
  }
}
