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
 * Converts a string containing non-MiniMessage syntax into a MiniMessage-compatible string.
 *
 * <p>Implementations may replace, remove, or insert MiniMessage tags as needed. If the input
 * contains no pattern recognized by this translator the method acts as a no-op and returns the
 * input unchanged. Existing, correctly-formed MiniMessage tags always pass through untouched.
 */
public interface MiniMessageTranslator {

  /**
   * Translates {@code input} into a MiniMessage-compatible string.
   *
   * @param input the string to translate
   * @return the translated string, or {@code input} unchanged if no recognized patterns were found
   */
  @NotNull String translate(@NotNull String input);

  /**
   * Escapes every pattern recognized by this translator so it will not be picked up by a
   * subsequent {@link #translate(String)} call. The convention is to insert a backslash
   * inside the recognized token.
   *
   * <p>Implementations must not re-escape tokens that have already been escaped by another
   * translator earlier in the chain.
   *
   * @param input the string to escape
   * @return the escaped string, or {@code input} unchanged if no recognized patterns were found
   */
  @NotNull String escape(@NotNull String input);

  /**
   * Removes every pattern recognized by this translator from {@code input}.
   *
   * @param input the string to strip
   * @return the stripped string, or {@code input} unchanged if no recognized patterns were found
   */
  @NotNull String strip(@NotNull String input);
}
