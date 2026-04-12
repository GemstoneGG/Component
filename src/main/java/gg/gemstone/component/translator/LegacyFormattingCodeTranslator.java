/*
 * Copyright (C) 2018-2026 Velocity Contributors
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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * Translates legacy Minecraft color and formatting codes (e.g. {@code §c}, {@code &l}) into
 * their MiniMessage equivalents (e.g. {@code <red>}, {@code <bold>}).
 *
 * <p>The prefix character - typically {@code §} or {@code &} - is supplied at construction time.
 * The translator is tag-aware: it tracks open MiniMessage tags and open legacy tags separately so
 * that closing tags are never inserted across a MiniMessage tag boundary, preserving correct
 * nesting when legacy codes and MiniMessage tags are mixed in the same string.
 *
 * <p>color codes follow vanilla Minecraft semantics: applying a new color closes all currently
 * active legacy tags before opening the new one. Formatting codes (bold, italic, etc.) stack and
 * are idempotent - applying one that is already active has no effect.
 */
class LegacyFormattingCodeTranslator implements MiniMessageTranslator {

  private static final Map<Character, String> COLOR_CODES = Map.ofEntries(
      Map.entry('0', "black"),
      Map.entry('1', "dark_blue"),
      Map.entry('2', "dark_green"),
      Map.entry('3', "dark_aqua"),
      Map.entry('4', "dark_red"),
      Map.entry('5', "dark_purple"),
      Map.entry('6', "gold"),
      Map.entry('7', "gray"),
      Map.entry('8', "dark_gray"),
      Map.entry('9', "blue"),
      Map.entry('a', "green"),
      Map.entry('b', "aqua"),
      Map.entry('c', "red"),
      Map.entry('d', "light_purple"),
      Map.entry('e', "yellow"),
      Map.entry('f', "white")
  );

  private static final Map<Character, String> FORMAT_CODES = Map.of(
      'k', "obfuscated",
      'l', "bold",
      'm', "strikethrough",
      'n', "underlined",
      'o', "italic"
  );

  private static final char RESET_CODE = 'r';

  private static final Set<String> MM_VOID_TAGS = Set.of(
      "reset",
      "newline", "br",
      "key",
      "lang", "tr", "translate",
      "lang_or", "tr_or", "translate_or",
      "selector", "sel",
      "score",
      "nbt", "data",
      "sprite",
      "head"
  );

  private static final String MM_RESET_TAG = "reset";

  private final char sectionChar;

  /**
   * Creates a translator that recognises the given {@code sectionChar} as the legacy code prefix.
   * Use {@link MiniMessageTranslators#LEGACY_CODE_SECTION} or {@link MiniMessageTranslators#LEGACY_CODE_AMPERSAND}.
   *
   * @param sectionChar the prefix character, typically {@code §} or {@code &}
   */
  LegacyFormattingCodeTranslator(char sectionChar) {
    this.sectionChar = sectionChar;
  }

  @Override
  public @NotNull String translate(@NotNull String input) {
    if (input.isEmpty()) {
      return input;
    }

    StringBuilder output = new StringBuilder(input.length());

    Deque<String> legacyStack = new ArrayDeque<>();
    Deque<String> mmStack = new ArrayDeque<>();

    int innerLegacyCount = 0;

    int i = 0;
    while (i < input.length()) {
      char c = input.charAt(i);

      if (c == sectionChar && i + 1 < input.length()) {
        char code = Character.toLowerCase(input.charAt(i + 1));

        if (code == RESET_CODE || COLOR_CODES.containsKey(code) || FORMAT_CODES.containsKey(code)) {
          if (code == RESET_CODE) {
            // Reset: close all active legacy tags.
            // Inside a MM scope, only close inner tags - outer tags span the MM
            // boundary and cannot be safely closed here.
            if (mmStack.isEmpty()) {
              flushLegacyStack(legacyStack, output);
            } else {
              innerLegacyCount = flushInnerLegacyTags(legacyStack, output, innerLegacyCount);
            }
          } else if (COLOR_CODES.containsKey(code)) {
            // Color code: vanilla behavior closes all active legacy tags, then
            // opens the new color. Inside a MM scope, only close inner tags.
            if (mmStack.isEmpty()) {
              flushLegacyStack(legacyStack, output);
            } else {
              innerLegacyCount = flushInnerLegacyTags(legacyStack, output, innerLegacyCount);
            }

            String colorName = COLOR_CODES.get(code);
            output.append('<').append(colorName).append('>');
            legacyStack.push(colorName);

            if (!mmStack.isEmpty()) {
              innerLegacyCount++;
            }
          } else {
            // Formatting code: stacks on top; idempotent if already active
            // anywhere in the legacy stack (inner or outer).
            String formatName = FORMAT_CODES.get(code);

            if (!legacyStack.contains(formatName)) {
              output.append('<').append(formatName).append('>');
              legacyStack.push(formatName);
              if (!mmStack.isEmpty()) {
                innerLegacyCount++;
              }
            }
          }

          i += 2;
          continue;
        }
      }

      if (c == '<') {
        int tagEnd = findTagEnd(input, i);

        if (tagEnd != -1) {
          String tag = input.substring(i, tagEnd + 1);

          // Before emitting any MM tag: auto-close inner legacy tags if we are
          // currently inside a MM scope. This keeps inner legacy tags strictly
          // self-contained between MM tag boundaries.
          if (!mmStack.isEmpty() && innerLegacyCount > 0) {
            innerLegacyCount = flushInnerLegacyTags(legacyStack, output, innerLegacyCount);
          }

          if (tag.endsWith("/>")) {
            // Self-closing tag - never touches either stack.
            output.append(tag);
            i = tagEnd + 1;
            continue;
          }

          if (tag.startsWith("</")) {
            // Closing tag - pop from mmStack if it matches the top.
            String tagName = extractTagName(tag.substring(2, tag.length() - 1));
            if (!mmStack.isEmpty() && mmStack.peek().equals(tagName)) {
              mmStack.pop();
            }

            output.append(tag);
            i = tagEnd + 1;
            continue;
          }

          String tagName = extractTagName(tag.substring(1, tag.length() - 1));

          if (tagName.equals(MM_RESET_TAG)) {
            // <reset> semantically resets all styles. Flush outer legacy tags
            // too, but only when the MM stack is clean - otherwise we cannot
            // safely close outer tags that span the MM boundary.
            // Inner tags were already flushed by the pre-flush above.
            if (mmStack.isEmpty()) {
              flushLegacyStack(legacyStack, output);
            }
          }

          output.append(tag);

          if (!MM_VOID_TAGS.contains(tagName)) {
            mmStack.push(tagName);
          }

          i = tagEnd + 1;
          continue;
        }
      }

      output.append(c);
      i++;
    }

    // Always close inner legacy tags - they were opened inside a MM scope and are
    // self-contained; MiniMessage in lenient mode will auto-close any remaining MM
    // tags around them.
    if (innerLegacyCount > 0) {
      flushInnerLegacyTags(legacyStack, output, innerLegacyCount);
    }

    // Only close outer legacy tags if the MM stack is clean. If there are unclosed
    // MM tags, inserting closing tags across their boundary would corrupt nesting.
    if (mmStack.isEmpty()) {
      flushLegacyStack(legacyStack, output);
    }

    return output.toString();
  }

  private String extractTagName(String inner) {
    String lower = inner.toLowerCase();
    int colon = lower.indexOf(':');
    return colon == -1 ? lower : lower.substring(0, colon);
  }

  private void flushLegacyStack(Deque<String> legacyStack, StringBuilder output) {
    while (!legacyStack.isEmpty()) {
      output.append("</").append(legacyStack.pop()).append('>');
    }
  }

  private int flushInnerLegacyTags(Deque<String> legacyStack, StringBuilder output, int innerCount) {
    for (int j = 0; j < innerCount && !legacyStack.isEmpty(); j++) {
      output.append("</").append(legacyStack.pop()).append('>');
    }
    return 0;
  }

  private int findTagEnd(String input, int start) {
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
