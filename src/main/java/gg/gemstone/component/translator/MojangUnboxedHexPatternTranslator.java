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
public class MojangUnboxedHexPatternTranslator implements MiniMessageTranslator {

  /**
   * Matches Mojang-style unboxed hex codes (e.g. {@code &#FFFFFF}).
   */
  private static final Pattern UNBOXED_MOJANG_PATTERN = Pattern.compile("(?<!<)&#([A-Fa-f0-9]{6})(?![A-Fa-f0-9])");

  /**
   * MiniMessage-style boxed hex code replacement, where {@code $1} is substituted with a 6-digit hex string (e.g. {@code <#FFFFFF>}).
   */
  private static final String BOXED_HEX_REPLACEMENT = "<#$1>";

  /**
   * Use {@link MiniMessageTranslators#MOJANG_UNBOXED_HEX}.
   */
  MojangUnboxedHexPatternTranslator() {
  }

  @Override
  public @NotNull String translate(final @NotNull String input) {
    return UNBOXED_MOJANG_PATTERN.matcher(input).replaceAll(BOXED_HEX_REPLACEMENT);
  }
}
