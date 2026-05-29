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

import java.util.function.UnaryOperator;

/**
 * Helpers for working with MiniMessage tag spans inside a raw string.
 *
 * <p>These let translators reason about which parts of a string are MiniMessage tags (everything
 * between a {@code <} and its matching {@code >}) and which parts are plain text. By transforming
 * only the plain-text runs, a translator never has to defend its regex against hex codes or other
 * tokens that legitimately appear <em>inside</em> a tag (e.g. {@code <gradient:#FCD620:#F0A615>} or
 * {@code <c:#90630C>}), which is otherwise impossible to express reliably with a lookbehind.
 */
final class MiniMessageTags {

  private MiniMessageTags() {
  }

  /**
   * Applies {@code transform} to every run of text that lies outside MiniMessage tag spans,
   * copying tag spans through unchanged.
   *
   * <p>A tag span is a {@code <...>} sequence recognised by {@link #findTagEnd(String, int)}. Any
   * {@code <} that does not open a well-formed tag (no matching {@code >}, or an unquoted nested
   * {@code <}) is treated as ordinary text and handed to {@code transform} along with its
   * surrounding characters.
   *
   * @param input the string to process
   * @param transform the transformation to apply to each plain-text run
   * @return the input with {@code transform} applied outside tag spans and tag spans left intact
   */
  static String transformOutsideTags(String input, UnaryOperator<String> transform) {
    if (input.isEmpty()) {
      return input;
    }

    StringBuilder output = new StringBuilder(input.length());

    int segmentStart = 0;
    int i = 0;
    while (i < input.length()) {
      if (input.charAt(i) == '<') {
        int tagEnd = findTagEnd(input, i);

        if (tagEnd != -1) {
          if (segmentStart < i) {
            output.append(transform.apply(input.substring(segmentStart, i)));
          }
          output.append(input, i, tagEnd + 1);
          i = tagEnd + 1;
          segmentStart = i;
          continue;
        }
      }

      i++;
    }

    if (segmentStart < input.length()) {
      output.append(transform.apply(input.substring(segmentStart)));
    }

    return output.toString();
  }

  /**
   * Finds the index of the {@code >} that closes the tag opened by the {@code <} at {@code start}.
   *
   * <p>Quoted sections (single or double quotes) inside the tag are honoured, so a {@code >} that
   * appears inside a quoted argument does not prematurely end the tag. An unquoted nested {@code <}
   * with no preceding {@code >} means this is not a well-formed tag, and {@code -1} is returned.
   *
   * @param input the string to scan
   * @param start the index of the candidate opening {@code <}
   * @return the index of the closing {@code >}, or {@code -1} if no well-formed tag is found
   */
  static int findTagEnd(String input, int start) {
    char quoteChar = 0;

    for (int i = start + 1; i < input.length(); i++) {
      char c = input.charAt(i);

      if (quoteChar != 0) {
        // Inside a quoted string - only look for the matching closing quote.
        if (c == quoteChar) {
          quoteChar = 0;
        }
      } else if (c == '\'' || c == '"') {
        quoteChar = c;
      } else if (c == '>') {
        return i;
      } else if (c == '<' && i != start) {
        // Unquoted nested '<' with no prior '>' - not a valid tag, bail out.
        return -1;
      }
    }

    return -1;
  }
}
