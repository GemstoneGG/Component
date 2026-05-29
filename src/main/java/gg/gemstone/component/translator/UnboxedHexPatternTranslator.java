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
 * Translates bare unboxed hex color codes into MiniMessage hex tags.
 *
 * <p>The pattern {@code #RRGGBB} is replaced with {@code <#RRGGBB>}. Matching happens only outside
 * MiniMessage tag spans (see {@link RegexMiniMessageTranslator}), so hex codes that appear inside a
 * tag - whether already-boxed ({@code <#RRGGBB>}) or as a tag argument
 * ({@code <gradient:#FCD620:#F0A615>}, {@code <c:#90630C>}) - are left untouched. Negative
 * lookbehinds for {@code &} and {@code \} additionally skip Mojang-style {@code &#RRGGBB} and
 * backslash-escaped {@code \#RRGGBB} sequences in plain text. Only exactly six hex digits are
 * matched; seven-or-more digit sequences are left untouched.
 */
class UnboxedHexPatternTranslator extends RegexMiniMessageTranslator {

  /**
   * Matches unboxed hex codes (e.g. {@code #FFFFFF}) in plain text, excluding those preceded by
   * {@code &} (Mojang-style, e.g. {@code &#FFFFFF}) or by a backslash (escape marker, e.g.
   * {@code \#FFFFFF}) via a negative lookbehind. Codes inside tag spans (e.g. {@code <#FFFFFF>})
   * are skipped by the tag-aware matching in {@link RegexMiniMessageTranslator}.
   */
  private static final Pattern UNBOXED_HEX_PATTERN = Pattern.compile("(?<![&\\\\])#([A-Fa-f0-9]{6})(?![A-Fa-f0-9])");

  /**
   * MiniMessage-style boxed hex code replacement, where {@code $1} is substituted with a 6-digit hex string (e.g. {@code <#FFFFFF>}).
   */
  private static final String BOXED_HEX_REPLACEMENT = "<#$1>";

  /**
   * Escape replacement: {@code #RRGGBB} becomes {@code #\RRGGBB}, neutralising the pattern
   * for this translator.
   */
  private static final String UNBOXED_HEX_ESCAPE_REPLACEMENT = "#\\\\$1";

  /**
   * Use {@link MiniMessageTranslators#UNBOXED_HEX}.
   */
  UnboxedHexPatternTranslator() {
    super(UNBOXED_HEX_PATTERN, BOXED_HEX_REPLACEMENT, UNBOXED_HEX_ESCAPE_REPLACEMENT);
  }
}
