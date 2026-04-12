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
}
