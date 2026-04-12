# Component

A small Adventure library that bridges legacy Minecraft formatting codes and non-standard hex color syntaxes into [MiniMessage](https://docs.advntr.dev/minimessage/format.html).

Minecraft plugins often receive player-facing strings that mix MiniMessage tags with older `§`/`&` color codes or Mojang-style hex colors (`&#RRGGBB`, `<&#RRGGBB>`). Adventure's built-in MiniMessage parser does not understand these formats. This library normalizes them into standard MiniMessage before deserialization, so you get a proper Adventure `Component` without preprocessing strings yourself.

## Supported input formats

| Format | Example | Normalized to |
|---|---|---|
| Section-sign legacy codes | `§cHello` | `<red>Hello</red>` |
| Ampersand legacy codes | `&cHello` | `<red>Hello</red>` |
| Mojang boxed hex | `<&#FF0000>` | `<#FF0000>` |
| Mojang unboxed hex | `&#FF0000` | `<#FF0000>` |
| Bare hex | `#FF0000` | `<#FF0000>` |

All formats are safe to mix with regular MiniMessage tags - the translators are tag-aware and will not corrupt existing MiniMessage syntax.

## Usage

Two standard parsers are provided out of the box:

```java
// §-prefixed legacy codes
ComponentParser parser = ComponentParser.componentParser();

// &-prefixed legacy codes
ComponentParser parserAmpersand = ComponentParser.componentParserAmpersand();

Component component = parser.parse("§cHello <bold>World</bold>");
```

`parse()` mirrors the full `MiniMessage.deserialize()` API, including overloads for `Pointered` and `TagResolver`:

```java
Component component = parser.parse(input, tagResolver);
Component component = parser.parse(input, viewer, tagResolver);
Node.Root tree      = parser.parseToTree(input);
```

## Custom parsers

Use the builder to select exactly which translators to apply, and in what order:

```java
ComponentParser parser = ComponentParser.builder()
    .withTranslators(
        MiniMessageTranslators.MOJANG_BOXED_HEX,
        MiniMessageTranslators.MOJANG_UNBOXED_HEX,
        MiniMessageTranslators.LEGACY_CODE_SECTION)
    .build();
```

You can also derive a new parser from an existing one via `toBuilder()`:

```java
ComponentParser extended = parser.toBuilder()
    .addTranslator(MiniMessageTranslators.UNBOXED_HEX)
    .build();
```

To use a custom `MiniMessage` instance (e.g. with additional tags registered):

```java
ComponentParser parser = ComponentParser.builder()
    .withTranslators(MiniMessageTranslators.LEGACY_CODE_SECTION)
    .withMiniMessage(myCustomMiniMessage)
    .build();
```

## Dependencies

This library depends on [Adventure](https://github.com/PaperMC/adventure) with `provided` scope, your platform is expected to supply them.
