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

package gg.gemstone.component;

import static java.util.Objects.requireNonNull;

import gg.gemstone.component.translator.MiniMessageTranslator;
import gg.gemstone.component.translator.MiniMessageTranslators;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    return miniMessage.deserialize(translate(input));
  }

  @Override
  public @NotNull Component parse(@NotNull String input, @NotNull Pointered target) {
    return miniMessage.deserialize(translate(input), target);
  }

  @Override
  public @NotNull Component parse(@NotNull String input, @NotNull TagResolver tagResolver) {
    return miniMessage.deserialize(translate(input), tagResolver);
  }

  @Override
  public @NotNull Component parse(@NotNull String input, @NotNull Pointered target, @NotNull TagResolver tagResolver) {
    return miniMessage.deserialize(translate(input), target, tagResolver);
  }

  @Override
  public @NotNull Component parse(@NotNull String input, TagResolver... tagResolvers) {
    return miniMessage.deserialize(translate(input), tagResolvers);
  }

  @Override
  public @NotNull Component parse(@NotNull String input, @NotNull Pointered target, TagResolver... tagResolvers) {
    return miniMessage.deserialize(translate(input), target, tagResolvers);
  }

  @Override
  public Node.@NotNull Root parseToTree(@NotNull String input) {
    return miniMessage.deserializeToTree(translate(input));
  }

  @Override
  public Node.@NotNull Root parseToTree(@NotNull String input, @NotNull Pointered target) {
    return miniMessage.deserializeToTree(translate(input), target);
  }

  @Override
  public Node.@NotNull Root parseToTree(@NotNull String input, @NotNull TagResolver tagResolver) {
    return miniMessage.deserializeToTree(translate(input), tagResolver);
  }

  @Override
  public Node.@NotNull Root parseToTree(@NotNull String input, @NotNull Pointered target, @NotNull TagResolver tagResolver) {
    return miniMessage.deserializeToTree(translate(input), target, tagResolver);
  }

  @Override
  public Node.@NotNull Root parseToTree(@NotNull String input, TagResolver... tagResolvers) {
    return miniMessage.deserializeToTree(translate(input), tagResolvers);
  }

  @Override
  public Node.@NotNull Root parseToTree(@NotNull String input, @NotNull Pointered target, TagResolver... tagResolvers) {
    return miniMessage.deserializeToTree(translate(input), target, tagResolvers);
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

  @VisibleForTesting
  @NotNull String translate(@NotNull String input) {
    requireNonNull(input, "input");

    String result = input;
    for (MiniMessageTranslator translator : translators) {
      result = translator.translate(result);
    }

    return result;
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
      this.translators.add(translator);
      return this;
    }

    @Override
    public ComponentParser.Builder withTranslators(MiniMessageTranslator... translators) {
      this.translators.clear();
      this.translators.addAll(List.of(translators));
      return this;
    }

    @Override
    public ComponentParser.Builder withMiniMessage(MiniMessage miniMessage) {
      this.miniMessage = miniMessage;
      return this;
    }

    @Override
    public ComponentParser build() {
      return new ComponentParserImpl(new ArrayList<>(translators), miniMessage);
    }
  }
}
