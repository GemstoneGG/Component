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

import static gg.gemstone.component.translator.MiniMessageTags.transformOutsideTags;

import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for {@link MiniMessageTranslator}s that recognise a single regex pattern and rewrite,
 * neutralise, or remove it.
 *
 * <p>By default matching is performed only on the plain-text runs that lie <em>outside</em>
 * MiniMessage tag spans (see {@link MiniMessageTags#transformOutsideTags}). This means a pattern
 * never has to guard against tokens that legitimately appear inside a tag - for example a hex
 * colour argument in {@code <gradient:#FCD620:#F0A615>} or {@code <c:#90630C>} - so the pattern
 * itself can stay simple.
 *
 * <p>Translators whose pattern deliberately targets a {@code <...>} construct (and would therefore
 * be skipped as a tag span) override {@link #replace(String, String)} to match the whole string.
 */
abstract class RegexMiniMessageTranslator implements MiniMessageTranslator {

  private final Pattern pattern;
  private final String translateReplacement;
  private final String escapeReplacement;

  /**
   * @param pattern the pattern recognised by this translator
   * @param translateReplacement the {@link java.util.regex.Matcher#replaceAll replaceAll} template
   *     used to convert a match into MiniMessage syntax
   * @param escapeReplacement the {@code replaceAll} template used to neutralise a match so a
   *     subsequent {@code translate} leaves it untouched
   */
  RegexMiniMessageTranslator(Pattern pattern, String translateReplacement, String escapeReplacement) {
    this.pattern = pattern;
    this.translateReplacement = translateReplacement;
    this.escapeReplacement = escapeReplacement;
  }

  @Override
  public @NotNull String translate(@NotNull String input) {
    return replace(input, translateReplacement);
  }

  @Override
  public @NotNull String escape(@NotNull String input) {
    return replace(input, escapeReplacement);
  }

  @Override
  public @NotNull String strip(@NotNull String input) {
    return replace(input, "");
  }

  /**
   * Replaces every match of {@link #pattern} that lies outside a MiniMessage tag span with
   * {@code replacement}. Override to change which parts of the input are considered.
   */
  String replace(String input, String replacement) {
    return transformOutsideTags(input, segment -> pattern.matcher(segment).replaceAll(replacement));
  }

  /**
   * Replaces every match of {@link #pattern} across the whole string, ignoring tag spans. Intended
   * for use by subclasses whose pattern itself targets a {@code <...>} construct.
   */
  final String replaceWholeString(String input, String replacement) {
    return pattern.matcher(input).replaceAll(replacement);
  }
}
