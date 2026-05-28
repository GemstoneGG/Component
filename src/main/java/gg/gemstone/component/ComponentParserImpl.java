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

package gg.gemstone.component;

import static java.util.Objects.requireNonNull;

import gg.gemstone.component.translator.MiniMessageTranslator;
import gg.gemstone.component.translator.MiniMessageTranslators;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tree.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

class ComponentParserImpl implements ComponentParser {

  private final List<MiniMessageTranslator> translators;
  private final MiniMessage miniMessage;

  @VisibleForTesting
  ComponentParserImpl(List<MiniMessageTranslator> translators, MiniMessage miniMessage) {
    this.translators = translators;
    this.miniMessage = miniMessage;
  }

  @Override
  public @NotNull Component parse(@NotNull String input) {
    return parse(input, MiniMessage::deserialize);
  }

  @Override
  public @NotNull String escape(@NotNull String input) {
    return escape(input, MiniMessage::escapeTags);
  }

  @Override
  public @NotNull String strip(@NotNull String input) {
    return strip(input, MiniMessage::stripTags);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ComponentParserImpl other)) return false;
    return translators.equals(other.translators) && miniMessage.equals(other.miniMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(translators, miniMessage);
  }

  @Override
  public ComponentParser.Builder toBuilder() {
    return new Builder(translators, miniMessage);
  }

  @Override
  public MiniMessage asMiniMessage() {
    return new MiniMessageAdapter();
  }

  @VisibleForTesting
  @NotNull String translate(@NotNull String input) {
    String result = input;
    for (MiniMessageTranslator translator : translators) {
      result = translator.translate(result);
    }

    return result;
  }

  private <R> @NotNull R parse(@NotNull String input, BiFunction<MiniMessage, String, R> miniMessageFunction) {
    requireNonNull(input, "input");

    String translated = translate(input);
    return miniMessageFunction.apply(miniMessage, translated);
  }

  private <R> @NotNull R escape(@NotNull String input, BiFunction<MiniMessage, String, R> miniMessageFunction) {
    requireNonNull(input, "input");

    String result = input;
    for (MiniMessageTranslator translator : translators) {
      result = translator.escape(result);
    }

    return miniMessageFunction.apply(miniMessage, result);
  }

  private <R> @NotNull R strip(@NotNull String input, BiFunction<MiniMessage, String, R> miniMessageFunction) {
    requireNonNull(input, "input");

    String result = input;
    for (MiniMessageTranslator translator : translators) {
      result = translator.strip(result);
    }

    return miniMessageFunction.apply(miniMessage, result);
  }

  static class Instances {

    static final ComponentParser COMPONENT_PARSER = new ComponentParserImpl(
        List.of(MiniMessageTranslators.MOJANG_BOXED_HEX, MiniMessageTranslators.MOJANG_UNBOXED_HEX,
            MiniMessageTranslators.UNBOXED_HEX, MiniMessageTranslators.LEGACY_CODE_SECTION),
        MiniMessage.miniMessage()
    );

    static final ComponentParser COMPONENT_PARSER_AMPERSAND = new ComponentParserImpl(
        List.of(MiniMessageTranslators.MOJANG_BOXED_HEX, MiniMessageTranslators.MOJANG_UNBOXED_HEX,
            MiniMessageTranslators.UNBOXED_HEX, MiniMessageTranslators.LEGACY_CODE_AMPERSAND),
        MiniMessage.miniMessage()
    );
  }

  static class Builder implements ComponentParser.Builder {

    private final List<MiniMessageTranslator> translators = new ArrayList<>();

    private MiniMessage miniMessage = MiniMessage.miniMessage();

    Builder() {
    }

    Builder(List<MiniMessageTranslator> translators, MiniMessage miniMessage) {
      this.translators.addAll(translators);
      this.miniMessage = miniMessage;
    }

    @Override
    public ComponentParser.Builder addTranslator(MiniMessageTranslator translator) {
      this.translators.add(requireNonNull(translator, "translator"));
      return this;
    }

    @Override
    public ComponentParser.Builder withTranslators(MiniMessageTranslator... translators) {
      requireNonNull(translators, "translators");
      this.translators.clear();
      this.translators.addAll(List.of(translators));
      return this;
    }

    @Override
    public ComponentParser.Builder withMiniMessage(MiniMessage miniMessage) {
      this.miniMessage = requireNonNull(miniMessage, "miniMessage");
      return this;
    }

    @Override
    public ComponentParser build() {
      return new ComponentParserImpl(new ArrayList<>(translators), miniMessage);
    }
  }

  /**
   * Adapts this {@link ComponentParser} to the {@link MiniMessage} interface.
   */
  class MiniMessageAdapter implements MiniMessage {

    @Override
    public @NotNull String escapeTags(@NotNull String input) {
      return escape(input, MiniMessage::escapeTags);
    }

    @Override
    public @NotNull String escapeTags(@NotNull String input, @NotNull TagResolver tagResolver) {
      return escape(input, (mm, s) -> mm.escapeTags(s, tagResolver));
    }

    @Override
    public @NotNull String stripTags(@NotNull String input) {
      return strip(input, MiniMessage::stripTags);
    }

    @Override
    public @NotNull String stripTags(@NotNull String input, @NotNull TagResolver tagResolver) {
      return strip(input, (mm, s) -> mm.stripTags(s, tagResolver));
    }

    @Override
    public @NotNull Component deserialize(@NotNull String input) {
      return parse(input, MiniMessage::deserialize);
    }

    @Override
    public @NotNull Component deserialize(@NotNull String input, @NotNull Pointered target) {
      return parse(input, (mm, s) -> mm.deserialize(s, target));
    }

    @Override
    public @NotNull Component deserialize(@NotNull String input, @NotNull TagResolver tagResolver) {
      return parse(input, (mm, s) -> mm.deserialize(s, tagResolver));
    }

    @Override
    public @NotNull Component deserialize(@NotNull String input, @NotNull Pointered target, @NotNull TagResolver tagResolver) {
      return parse(input, (mm, s) -> mm.deserialize(s, target, tagResolver));
    }

    @Override
    public Node.@NotNull Root deserializeToTree(@NotNull String input) {
      return parse(input, MiniMessage::deserializeToTree);
    }

    @Override
    public Node.@NotNull Root deserializeToTree(@NotNull String input, @NotNull Pointered target) {
      return parse(input, (mm, s) -> mm.deserializeToTree(s, target));
    }

    @Override
    public Node.@NotNull Root deserializeToTree(@NotNull String input, @NotNull TagResolver tagResolver) {
      return parse(input, (mm, s) -> mm.deserializeToTree(s, tagResolver));
    }

    @Override
    public Node.@NotNull Root deserializeToTree(@NotNull String input, @NotNull Pointered target, @NotNull TagResolver tagResolver) {
      return parse(input, (mm, s) -> mm.deserializeToTree(s, target, tagResolver));
    }

    @Override
    public boolean strict() {
      return miniMessage.strict();
    }

    @Override
    public @NotNull TagResolver tags() {
      return miniMessage.tags();
    }

    @Override
    public @NotNull String serialize(@NotNull Component component) {
      // Serializing can be done straight with the `miniMessage` reference. It does not make
      // sense to inject other ways of formatting here; the ComponentParser natively supports
      // the MiniMessage format, so anything serialized here will be able to be deserialized by
      // any ComponentParser.
      return miniMessage.serialize(component);
    }
  }
}
