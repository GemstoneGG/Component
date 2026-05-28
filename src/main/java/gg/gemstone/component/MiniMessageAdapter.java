package gg.gemstone.component;

import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tree.Node;
import org.jetbrains.annotations.NotNull;

/**
 * Adapts a {@link ComponentParser} to the {@link MiniMessage} interface.
 */
final class MiniMessageAdapter implements MiniMessage {

  private final ComponentParser parser;
  private final MiniMessage miniMessage;

  /**
   * Instantiates a {@link MiniMessageAdapter}.
   *
   * @param parser the parser to adapt method calls to
   * @param miniMessage the underlying {@link MiniMessage} object the parser is using (used for
   *                    functionality that the {@link MiniMessage} interface expects, but is
   *                    not provided by the {@link ComponentParser} interface)
   */
  MiniMessageAdapter(ComponentParser parser, MiniMessage miniMessage) {
    this.parser = parser;
    this.miniMessage = miniMessage;
  }

  @Override
  public @NotNull String escapeTags(@NotNull String input) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NotNull String escapeTags(@NotNull String input, @NotNull TagResolver tagResolver) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NotNull String stripTags(@NotNull String input) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NotNull String stripTags(@NotNull String input, @NotNull TagResolver tagResolver) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NotNull Component deserialize(@NotNull String input) {
    return parser.parse(input);
  }

  @Override
  public @NotNull Component deserialize(@NotNull String input, @NotNull Pointered target) {
    return parser.parse(input, target);
  }

  @Override
  public @NotNull Component deserialize(@NotNull String input, @NotNull TagResolver tagResolver) {
    return parser.parse(input, tagResolver);
  }

  @Override
  public @NotNull Component deserialize(@NotNull String input, @NotNull Pointered target, @NotNull TagResolver tagResolver) {
    return parser.parse(input, target, tagResolver);
  }

  @Override
  public Node.@NotNull Root deserializeToTree(@NotNull String input) {
    return parser.parseToTree(input);
  }

  @Override
  public Node.@NotNull Root deserializeToTree(@NotNull String input, @NotNull Pointered target) {
    return parser.parseToTree(input, target);
  }

  @Override
  public Node.@NotNull Root deserializeToTree(@NotNull String input, @NotNull TagResolver tagResolver) {
    return parser.parseToTree(input, tagResolver);
  }

  @Override
  public Node.@NotNull Root deserializeToTree(@NotNull String input, @NotNull Pointered target, @NotNull TagResolver tagResolver) {
    return parser.parseToTree(input, target, tagResolver);
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
