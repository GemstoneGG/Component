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

import gg.gemstone.component.translator.MiniMessageTranslator;
import gg.gemstone.component.translator.MiniMessageTranslators;
import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tree.Node;
import org.jetbrains.annotations.NotNull;

/**
 * Parses strings containing legacy formatting codes and non-standard hex color
 * syntaxes into Adventure {@link Component} objects via MiniMessage.
 *
 * <p>Input strings are first run through a configurable chain of
 * {@link MiniMessageTranslators}, which normalize legacy {@code §}/{@code &}
 * color codes and Mojang-style hex colors into MiniMessage tags. The
 * translated string is then handed to {@link MiniMessage} for final
 * deserialization.
 *
 * <p>Two standard parsers are available via {@link #componentParser()} ({@code §}-prefixed) and
 * {@link #componentParserAmpersand()} ({@code &}-prefixed). Custom parsers can be constructed
 * with {@link #builder()}.
 *
 * @see ComponentParser.Builder
 */
public interface ComponentParser {

  /**
   * Returns a new {@link Builder} for constructing a custom {@code ComponentParser}.
   *
   * @return a new builder instance
   */
  static Builder builder() {
    return new ComponentParserImpl.Builder();
  }

  /**
   * Returns a parser that translates Mojang boxed/unboxed hex colors and
   * {@code §}-prefixed legacy formatting codes.
   *
   * @return a section-sign parser
   */
  static ComponentParser componentParser() {
    return ComponentParserImpl.Instances.COMPONENT_PARSER;
  }

  /**
   * Returns a parser that translates Mojang boxed/unboxed hex colors and
   * {@code &}-prefixed legacy formatting codes.
   *
   * @return an ampersand parser
   */
  static ComponentParser componentParserAmpersand() {
    return ComponentParserImpl.Instances.COMPONENT_PARSER_AMPERSAND;
  }

  /**
   * Translates {@code input} and deserializes it into a {@link Component}.
   *
   * @param input the raw input string
   * @return the parsed component
   */
  @NotNull Component parse(@NotNull String input);

  /**
   * Translates {@code input} and deserializes it into a {@link Component},
   * resolving tags against the given {@code target}.
   *
   * @param input  the raw input string
   * @param target the pointered context used for tag resolution
   * @return the parsed component
   */
  @NotNull Component parse(@NotNull String input, @NotNull Pointered target);

  /**
   * Translates {@code input} and deserializes it into a {@link Component},
   * applying the given {@code tagResolver}.
   *
   * @param input       the raw input string
   * @param tagResolver additional tag resolver
   * @return the parsed component
   */
  @NotNull Component parse(@NotNull String input, @NotNull TagResolver tagResolver);

  /**
   * Translates {@code input} and deserializes it into a {@link Component},
   * resolving tags against {@code target} and applying {@code tagResolver}.
   *
   * @param input       the raw input string
   * @param target      the pointered context used for tag resolution
   * @param tagResolver additional tag resolver
   * @return the parsed component
   */
  @NotNull Component parse(@NotNull String input, @NotNull Pointered target, @NotNull TagResolver tagResolver);

  /**
   * Translates {@code input} and deserializes it into a {@link Component},
   * applying zero or more {@code tagResolvers}.
   *
   * @param input        the raw input string
   * @param tagResolvers additional tag resolvers
   * @return the parsed component
   */
  @NotNull Component parse(@NotNull String input, TagResolver... tagResolvers);

  /**
   * Translates {@code input} and deserializes it into a {@link Component},
   * resolving tags against {@code target} and applying zero or more
   * {@code tagResolvers}.
   *
   * @param input        the raw input string
   * @param target       the pointered context used for tag resolution
   * @param tagResolvers additional tag resolvers
   * @return the parsed component
   */
  @NotNull Component parse(@NotNull String input, @NotNull Pointered target, TagResolver... tagResolvers);

  /**
   * Translates {@code input} and deserializes it into a MiniMessage parse tree.
   *
   * @param input the raw input string
   * @return the root node of the parse tree
   */
  Node.@NotNull Root parseToTree(@NotNull String input);

  /**
   * Translates {@code input} and deserializes it into a MiniMessage parse tree,
   * resolving tags against the given {@code target}.
   *
   * @param input  the raw input string
   * @param target the pointered context used for tag resolution
   * @return the root node of the parse tree
   */
  Node.@NotNull Root parseToTree(@NotNull String input, @NotNull Pointered target);

  /**
   * Translates {@code input} and deserializes it into a MiniMessage parse tree,
   * applying the given {@code tagResolver}.
   *
   * @param input       the raw input string
   * @param tagResolver additional tag resolver
   * @return the root node of the parse tree
   */
  Node.@NotNull Root parseToTree(@NotNull String input, @NotNull TagResolver tagResolver);

  /**
   * Translates {@code input} and deserializes it into a MiniMessage parse tree,
   * resolving tags against {@code target} and applying {@code tagResolver}.
   *
   * @param input       the raw input string
   * @param target      the pointered context used for tag resolution
   * @param tagResolver additional tag resolver
   * @return the root node of the parse tree
   */
  Node.@NotNull Root parseToTree(@NotNull String input, @NotNull Pointered target, @NotNull TagResolver tagResolver);

  /**
   * Translates {@code input} and deserializes it into a MiniMessage parse tree,
   * applying zero or more {@code tagResolvers}.
   *
   * @param input        the raw input string
   * @param tagResolvers additional tag resolvers
   * @return the root node of the parse tree
   */
  Node.@NotNull Root parseToTree(@NotNull String input, TagResolver... tagResolvers);

  /**
   * Translates {@code input} and deserializes it into a MiniMessage parse tree,
   * resolving tags against {@code target} and applying zero or more
   * {@code tagResolvers}.
   *
   * @param input        the raw input string
   * @param target       the pointered context used for tag resolution
   * @param tagResolvers additional tag resolvers
   * @return the root node of the parse tree
   */
  Node.@NotNull Root parseToTree(@NotNull String input, @NotNull Pointered target, TagResolver... tagResolvers);

  /**
   * Returns a new {@link Builder} pre-populated with this parser's translator chain
   * and {@link MiniMessage} instance, allowing a modified copy to be built without
   * starting from scratch.
   *
   * @return a new builder initialized with this parser's configuration
   */
  Builder toBuilder();

  /**
   * Builds a {@link ComponentParser} with a custom translator chain and
   * MiniMessage instance.
   *
   * <p>Use {@link #addTranslator(MiniMessageTranslator)} to append translators
   * one at a time, or {@link #withTranslators(MiniMessageTranslator...)} to
   * replace the entire chain at once. Translators are applied in the order they
   * were added. Any {@link MiniMessageTranslator} implementation is accepted,
   * including the pre-built {@link MiniMessageTranslators} constants.
   */
  interface Builder {

    /**
     * Appends {@code translator} to the end of the translator chain.
     *
     * @param translator the translator to add
     * @return this builder
     */
    Builder addTranslator(MiniMessageTranslator translator);

    /**
     * Replaces the entire translator chain with the given {@code translators},
     * in the order provided.
     *
     * @param translators the translators to use, in application order
     * @return this builder
     */
    Builder withTranslators(MiniMessageTranslator... translators);

    /**
     * Overrides the {@link MiniMessage} instance used for deserialization.
     * Defaults to {@link MiniMessage#miniMessage()} if not set.
     *
     * @param miniMessage the MiniMessage instance to use
     * @return this builder
     */
    Builder withMiniMessage(MiniMessage miniMessage);

    /**
     * Constructs and returns the {@link ComponentParser}.
     *
     * @return a new {@code ComponentParser}
     */
    ComponentParser build();
  }
}
