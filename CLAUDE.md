# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Forge is a comprehensive open-source Magic: The Gathering rules engine written in Java. The project implements the complete MTG rules system, AI opponents, and multiple platform-specific UIs (desktop Swing, mobile LibGDX, Android, iOS).

## Build Commands

**Initial setup:**
```bash
# Windows/Linux build with all dependencies
mvn -U -B clean -P windows-linux install
```

**Run tests:**
```bash
# All tests
mvn -U -B clean test

# Single test class
mvn test -Dtest=ClassName

# Single test method
mvn test -Dtest=ClassName#methodName
```

**Build desktop snapshot:**
```bash
# Profile: windows-linux
mvn clean install -P windows-linux
# Output: forge-gui-desktop/target/forge-gui-desktop-[version]-SNAPSHOT/
```

**Code quality:**
```bash
# Checkstyle validation (runs automatically on validate phase)
mvn checkstyle:check
```

**Run desktop application:**
- In IDE: Run `Main.java` in `forge.view` package (forge-gui-desktop module)
- Command: `java -jar forge-gui-desktop/target/forge-gui-desktop-[version].jar`

**Run mobile dev (desktop preview of mobile UI):**
- In IDE: Run `Main.java` in `forge.app` package (forge-gui-mobile-dev module)

## Architecture

### Module Hierarchy (12 modules)

**Core Layer (bottom-up):**
1. **forge-core**: Immutable card data models, database, deck management
   - `CardRules`: Static card definitions (cached globally)
   - `CardDb`: In-memory card database
   - `StaticData`: Singleton holding all game data (cards, editions, formats)
   - `PaperCard`: Card + edition + art combination
   - `Deck`: Deck structure (main/sideboard)

2. **forge-game**: Complete MTG rules engine implementation
   - `Game`: Central game state singleton
   - `Card`: Mutable card instance in play (references immutable `CardRules`)
   - `SpellAbility`: Spell/ability definitions with cost/effect
   - `PhaseHandler`: Turn structure and phase management
   - `MagicStack`: Stack resolution logic
   - `TriggerHandler`, `ReplacementHandler`, `StaticEffects`: Rules processing
   - `Zone`: Card containers (battlefield, hand, graveyard, etc.)
   - `AbilityFactory`: Parses card text into executable abilities
   - 200+ effect classes in `forge.game.ability.effects/`

3. **forge-ai**: Computer opponent logic
   - `PlayerControllerAi`: Main AI controller
   - `GameStateEvaluator`: Board position evaluation
   - `AiAttackController`, `AiBlockController`: Combat decisions
   - `ComputerUtil*`: Specialized evaluators (ability, card, combat, mana, cost)
   - 150+ ability-specific AI classes (e.g., `AnimateAi`, `CounterAi`)

**UI Layer:**
4. **forge-gui**: Shared game mode logic (match, quest, gauntlet, puzzle, adventure)
   - Game mode implementations
   - Network support
   - Deck editing logic
   - Card resource definitions in `res/`

5. **forge-gui-desktop**: Swing-based desktop UI (Windows/Mac/Linux)
6. **forge-gui-mobile**: LibGDX Scene2D mobile UI (cross-platform)
7. **forge-gui-android**: Android-specific wrapper
8. **forge-gui-ios**: iOS-specific wrapper
9. **forge-gui-mobile-dev**: Desktop development preview for mobile UI

**Utilities:**
10. **forge-lda**: Latent Dirichlet Allocation for deck analysis
11. **adventure-editor**: Tool for creating adventure/quest content
12. **forge-installer**: Build/installation utilities

### Key Design Patterns

**Dual Representation:**
- `CardRules` (forge-core): Immutable, globally cached card definition
- `Card` (forge-game): Mutable instance representing card in play
- This separation enables multiple instances from one definition, card copying, and token generation

**Ability System:**
- Cards define abilities via text-based format parsed by `AbilityFactory`
- Format: `AB: <cost>$ <effect>$ <sub-abilities>`
- Each `ApiType` (100+ types) has corresponding effect class
- Abilities are declarative, not hardcoded per card
- AI evaluates abilities systematically via ability-specific AI classes

**Event-Driven Architecture:**
- `EventBus` publishes `GameEvent` for every state change
- UI subscribes to events for real-time updates
- Loose coupling between game engine and UI layers

**State Machine (PhaseHandler):**
- Game phases managed as state transitions
- Extra turns/phases implemented via stack
- Each phase has pre/post priority sequences

### Data Flow: Playing a Spell

1. **UI**: User activates spell from hand
2. **Game Validation**: Check legality, mana availability
3. **Ability Parsing**: `AbilityFactory` converts card text → `SpellAbility`
4. **Stack Management**: `MagicStack` adds to stack, checks responses
5. **Effect Execution**: `SpellAbilityEffect` runs card logic
6. **Trigger Processing**: `TriggerHandler` processes triggered abilities
7. **State-Based Actions**: Apply game rules (damage, token cleanup, etc.)
8. **Event Publishing**: `EventBus` notifies UI of changes
9. **AI Response**: `PlayerControllerAi` evaluates counter-opportunities
10. **UI Update**: Display updated game state

## Card Scripting

Card definitions are text-based files in `forge-gui/res/cardsfolder/`.

**Format:** Each card has:
- Metadata (Name, ManaCost, Types, PT, etc.)
- Static abilities (S:Mode$ Continuous...)
- Triggered abilities (T:Mode$ ...)
- Activated abilities (A:AB$ ...)
- Spell abilities (for instants/sorceries)

**Resources:**
- Card scripting API: https://github.com/Card-Forge/forge/wiki/Card-scripting-API
- All card resources: `forge-gui/res/`

## Development Guidelines

**Testing:**
- Use TestNG framework (not JUnit)
- Run tests with virtual framebuffer if GUI tests fail headless: `Xvfb :1 -screen 0 800x600x8 &`
- Test framework configured with Java module opens (see pom.xml surefire config)

**Code Style:**
- Checkstyle enforced on validate phase (see `checkstyle.xml`)
- Build fails on checkstyle violations
- Java 17 language level (source/target)

**Platform Considerations:**
- Core game logic (forge-game, forge-ai, forge-core) must be platform-independent
- UI code separated by platform (desktop vs mobile)
- Mobile UI uses LibGDX Scene2D framework, not Swing

**Module Dependencies:**
- forge-core → (no dependencies on other forge modules)
- forge-game → depends on forge-core
- forge-ai → depends on forge-game, forge-core
- forge-gui → depends on forge-game, forge-ai, forge-core
- Platform UIs → depend on forge-gui and below

**Common Pitfalls:**
- Don't confuse `Card` (mutable game instance) with `CardRules` (immutable definition)
- Don't add UI dependencies to forge-game or forge-ai (must remain platform-independent)
- AI timeout is 5 seconds by default - complex evaluations must be optimized
- Card instances are reused across games - always reset state properly

## Requirements

- Java JDK 17 or later (tested with 17 and 21)
- Maven 3.8.1 or later
- For Android: Android SDK with Build-tools 35.0.0, API 35 platform
- For mobile dev: LibGDX familiarity helpful

## Running from IDE

**IntelliJ (recommended):**
- Import as Maven project
- Run configuration: `Main` class in `forge.view` (desktop) or `forge.app` (mobile-dev)

**Eclipse:**
- Import as "Existing Maven Projects"
- Run as Java Application: `Main - forge.view` (desktop) or `Main - forge.app` (mobile-dev)
- Note: Android SDK no longer supported in Eclipse, use IntelliJ

## Key Files & Locations

- `forge-gui/res/cardsfolder/`: Card definitions (text format)
- `forge-game/src/main/java/forge/game/ability/effects/`: Effect implementations
- `forge-ai/src/main/java/forge/ai/ability/`: AI evaluators for abilities
- `forge-game/src/main/java/forge/game/ability/AbilityFactory.java`: Ability parser
- `forge-core/src/main/java/forge/item/`: Card/deck data models
- `checkstyle.xml`: Code style rules (enforced on build)
