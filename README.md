# Item Landing Sound Refabricated

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Minecraft: 26.3](https://img.shields.io/badge/Minecraft-26.3-brightgreen.svg)]()
[![Fabric: API](https://img.shields.io/badge/Fabric-0.161.0%2B26.3-blue.svg)]()
[![CurseForge: 1721457](https://www.curseforge.com/minecraft/mc-mods/item-landing-sound-refabricated/preview)]()
[![Modrinth: wfdflqrM](https://modrinth.com/mod/item-landing-sound-refabricated)]()

Item Landing Sound Refabricated is a client-side Fabric mod for Minecraft 26.3 that plays realistic, block-specific impact sounds whenever dropped items land on physical surfaces.

Originally created by ExiledLizard635, this refabricated edition has been rewritten to fix legacy defects, eliminate performance bottlenecks, and introduce robust configuration persistence.

---

## Technical Architecture and Improvements

### 1. Instantaneous Ground Collision Detection
In the original mod, item tracking was polled inside `ClientTickEvents.END_CLIENT_TICK` using position deltas on items with `isOnGround()`. When items hit the ground with horizontal momentum, `isOnGround()` remained true while the item slid across the surface, causing the impact sound to be delayed until all sliding motion finished.

Item Landing Sound Refabricated replaces tick polling with a dedicated Mixin (`ItemEntityMixin`) hooking directly into `ItemEntity#tick()`:
- At `HEAD` of the tick, airborne status (`!onGround()`) and downward vertical velocity (`deltaMovement.y`) are captured prior to physics calculations.
- At `TAIL` of the tick, if an entity transitioned from airborne to ground contact with a vertical speed satisfying the threshold (`prevVerticalVelocity < -0.04`), the landing sound triggers immediately at tick 0.
- A 4-tick debounce window is applied to absorb micro-bounces and sliding vibrations without suppressing legitimate subsequent drops.

### 2. Zero Memory Leaks and Ephemeral Lifecycle Tracking
The original mod stored entity state in a static `HashMap<UUID, Double>`. Because item despawning, player pickup, or chunk unloading never cleaned up entries from this map, long gameplay sessions or high-drop environments resulted in a permanent memory leak.

In this refabricated release:
- All tracking state (`wasAirborne`, `prevVerticalVelocity`, `landingCooldown`) is injected directly into `ItemEntity` via a duck-typed interface (`ItemLandingTracker`).
- No static maps or collections are maintained.
- When an `ItemEntity` is picked up, despawned, or discarded by the client world, all associated tracking memory is automatically reclaimed by the Java Virtual Machine garbage collector.

### 3. Authentic Block Acoustics and Layer Resolution
Rather than playing the generic and jarring block-break sound (`getBreakSound()`) at pitch 2.0, this mod uses the authentic block fall sound group (`SoundType.getFallSound()`), falling back to step sounds (`SoundType.getStepSound()`) if unavailable:
- Layered blocks resting on top of solid surfaces (such as carpets, moss carpets, and snow layers) are identified via `BlockTags.INSIDE_STEP_SOUND_BLOCKS` and `BlockTags.COMBINATION_STEP_SOUND_BLOCKS`, ensuring that dropping an item onto a carpeted floor produces soft fabric sounds rather than stone sounds.
- Impact volume scales dynamically with vertical collision speed (`speedFactor = min(1.0, |vy| * 1.8)`).
- Natural organic pitch variation (+- 10% by default) prevents audio fatigue during bulk item drops.
- Items landing while submerged in water or lava are automatically suppressed.

### 4. Resilient Atomic Configuration Persistence
Settings are managed through `ConfigManager` and stored in `config/item_landing_sound_refabricated.json`:
- Disk writes use atomic temporary file replacement (`StandardCopyOption.ATOMIC_MOVE`) to prevent file corruption in case of unexpected game termination.
- Corrupted or invalid configuration files are backed up automatically and replaced with validated defaults without crashing the game.
- Full thread safety protects configuration state during asynchronous client operations.

---

## In-Game Commands

The mod includes responsive client-side commands with real-time disk synchronization. Changes take effect immediately without requiring a game restart.

| Command | Description | Example |
| :--- | :--- | :--- |
| `/itemlandingsound:volume` | Displays current active volume | `/itemlandingsound:volume` |
| `/itemlandingsound:volume <value>` | Sets volume (0.0 to 2.0) and persists to config | `/itemlandingsound:volume 1.25` |
| `/itemlandingsound` | Displays mod status, volume, and available subcommands | `/itemlandingsound` |
| `/itemlandingsound volume` | Queries active volume | `/itemlandingsound volume` |
| `/itemlandingsound volume <value>` | Sets active volume (0.0 to 2.0) and persists to config | `/itemlandingsound volume 0.8` |
| `/itemlandingsound toggle` | Toggles mod sound playback on or off | `/itemlandingsound toggle` |
| `/itemlandingsound reload` | Reloads configuration from disk | `/itemlandingsound reload` |

---

## Configuration Reference

The configuration file is located at `config/item_landing_sound_refabricated.json`:

```json
{
  "enabled": true,
  "volume": 1.0,
  "pitch": 1.0,
  "pitchVariation": 0.1,
  "minVelocity": 0.04
}
```

### Parameter Breakdown

| Key | Type | Default | Range | Description |
| :--- | :--- | :--- | :--- | :--- |
| `enabled` | boolean | `true` | `true`, `false` | Master switch to enable or disable item landing sounds. |
| `volume` | float | `1.0` | `0.0` to `2.0` | Master volume multiplier for item landing sounds. Set to 0.0 to mute. |
| `pitch` | float | `1.0` | `0.1` to `2.0` | Base pitch multiplier applied to landing sounds. |
| `pitchVariation` | float | `0.1` | `0.0` to `0.5` | Random pitch variance range applied to each impact (+- variation). |
| `minVelocity` | double | `0.04` | `0.0` to `1.0` | Minimum vertical impact speed required to trigger a landing sound. |

---

## Installation

1. Install **Minecraft 26.3** and **Fabric Loader** (version 0.19.3 or higher).
2. Download and place **Fabric API** (version 0.161.0+26.3 or higher) in your `.minecraft/mods` folder.
3. Download `item_landing_sound_refabricated-0.1.0-fabric-26.3.jar` and place it in your `.minecraft/mods` folder.
4. Launch the game.

Item Landing Sound Refabricated is strictly client-side. It does not need to be installed on servers and is fully compatible with vanilla Minecraft servers and modded multiplayer environments.

---

## Building from Source

### Requirements
- Java 25 JDK (OpenJDK / Eclipse Temurin)
- Git

### Compilation Steps
```bash
# Clone repository
git clone https://github.com/1unarea/Item-Landing-Sound-Refabricated.git
cd Item-Landing-Sound-Refabricated

# Build release jar
./gradlew build
```

The compiled mod JAR will be generated in `build/libs/item_landing_sound_refabricated-0.1.0-fabric-26.3.jar`.

---

## Verification and Testing

The codebase includes an automated JUnit 5 test suite covering:
- Ground impact state transitions and sliding suppression.
- Reflection and bytecode audit confirming 0 static tracking collections.
- GC reclamation tests verifying that tracking state is reclaimed with the entity.
- Property-based fuzz testing across 50,000 random physics frames.
- Config manager serialization, atomic moves, thread safety, and corruption recovery.

Run the test suite with:
```bash
./gradlew check
```

---

## License and Attribution

This project is licensed under the [MIT License](LICENSE).

- Original mod by **ExiledLizard635** ([Modrinth](https://modrinth.com/mod/item-landing-sound) / [CurseForge](https://www.curseforge.com/minecraft/mc-mods/item-landing-sound)).
- Refabricated and modernized for Minecraft 26.3 by **Item Landing Sound Refabricated Contributors**.
