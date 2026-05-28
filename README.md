# Component

A small Adventure library that bridges legacy Minecraft formatting codes and non-standard hex color syntaxes into [MiniMessage](https://docs.advntr.dev/minimessage/format.html).

Minecraft plugins often receive player-facing strings that mix MiniMessage tags with older `§`/`&` color codes or Mojang-style hex colors (`&#RRGGBB`, `<&#RRGGBB>`). Adventure's built-in MiniMessage parser does not understand these formats. This library normalizes them into standard MiniMessage before deserialization, so you get a proper Adventure `Component` without preprocessing strings yourself.

## Maven/Gradle
Import this library from [repo.velocityctd.com](https://repo.velocityctd.com/#/).

**Maven**
```xml
<repository>
  <id>velocityctd-releases</id>
  <name>Velocity-CTD Repository</name>
  <url>https://repo.velocityctd.com/releases</url>
</repository>

<dependency>
  <groupId>gg.gemstone</groupId>
  <artifactId>component</artifactId>
  <version>1.0.1</version>
</dependency>
```

**Gradle (kotlin)**
```kotlin
maven {
  name = "velocityctdReleases"
  url = uri("https://repo.velocityctd.com/releases")
}

implementation("gg.gemstone:component:1.0.1")
```

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

## Escaping and stripping

Use `escape()` to quote user input so that no legacy code, hex syntax, or MiniMessage tag is interpreted on a subsequent `parse()` call. Use `strip()` to drop every recognized token and keep only the plain text. Both methods honor the parser's translator chain, so they cover exactly the formats the parser knows about.

```java
String safe = parser.escape("§cHello <bold>World</bold>");
// safe -> "§\cHello \<bold>World\</bold>"
parser.parse(safe); // renders the literal text, no formatting applied

String plain = parser.strip("§cHello <bold>World</bold>");
// plain -> "Hello World"
```

## MiniMessage adapter

If you have code that expects a `MiniMessage` instance, call `asMiniMessage()` to drop this parser in without changing the call sites:

```java
MiniMessage mm = parser.asMiniMessage();
Component component = mm.deserialize("§cHello <bold>World</bold>");

// MiniMessage's MiniMessageTranslationStore expects a MiniMessage:
MiniMessageTranslationStore translationRegistry =
    MiniMessageTranslationStore.create(
        this.translationRegistryKey,
        parser.asMiniMessage());
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
