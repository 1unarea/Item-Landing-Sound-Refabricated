# Item Landing Sound Refabricated

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Minecraft: 26.3](https://img.shields.io/badge/Minecraft-26.3-brightgreen.svg)]()
[![Fabric: API](https://img.shields.io/badge/Fabric-0.161.0%2B26.3-blue.svg)]()

**Item Landing Sound Refabricated** is a modern client-side Fabric mod for Minecraft 26.3 that makes dropped items emit realistic landing/impact sounds when making physical contact with blocks, accurately matching the block's material sound group.

Originally created by **ExiledLizard635**, this modern refabricated edition has been completely re-architected to eliminate legacy bugs and deliver crisp, instantaneous audio feedback.

---

## ✨ Features & Modern Improvements

- **⚡ Instantaneous Ground Collision Detection**: Eliminates the legacy sliding sound delay. Sounds play on tick 0 when an item makes physical contact with the ground, rather than waiting for item sliding physics to finish.
- **🛡️ Zero Memory Leaks**: Completely removes legacy unbounded entity tracking (`Map<UUID, Double>`). State is tracked ephemerally on the item entity via a Mixin duck interface (`ItemLandingTracker`) and automatically garbage-collected when entities despawn or are picked up.
- **💾 Configuration Persistence**: Settings are saved to a clean JSON file (`config/item_landing_sound_refabricated.json`), persisted across game sessions, and protected against corruption with atomic writes.
- **💬 Responsive Client Commands**: Query and update volume in real-time with immediate disk synchronization using `/itemlandingsound:volume`.
- **🎧 Natural Audio Variety**: Subtle randomized pitch variation ensures repeated item drops do not sound synthetic or repetitive.
- **🧱 Accurate Block Surface Acoustics**: Correctly resolves carpets, snow layers, slabs, fences, and lily pads, playing authentic fall sound groups (`SoundType.getFallSound()`) rather than jarring block-break crunch sounds.
- **🖥️ 100% Client-Side**: Only required on the client. Fully compatible with vanilla servers and multiplayer networks.

---

## 📦 Installation

1. Ensure **Minecraft 26.3** and **Fabric Loader (>=0.19.3)** are installed.
2. Download and install **Fabric API (0.161.0+26.3)** in your `.minecraft/mods` directory.
3. Download `item_landing_sound_refabricated-<version>.jar` and place it into your `.minecraft/mods` directory.
4. Launch Minecraft!

---

## 🎮 In-Game Commands

| Command | Description | Example |
| :--- | :--- | :--- |
| `/itemlandingsound:volume` | Displays current active volume | `/itemlandingsound:volume` |
| `/itemlandingsound:volume <value>` | Sets volume (0.0 to 2.0) and persists to config | `/itemlandingsound:volume 1.25` |
| `/itemlandingsound` | Shows mod status, volume, and help summary | `/itemlandingsound` |
| `/itemlandingsound volume` | Queries active volume | `/itemlandingsound volume` |
| `/itemlandingsound volume <value>` | Sets active volume and persists to config | `/itemlandingsound volume 0.8` |
| `/itemlandingsound toggle` | Toggles mod sound playback on/off | `/itemlandingsound toggle` |
| `/itemlandingsound reload` | Reloads configuration from disk | `/itemlandingsound reload` |

---

## ⚙️ Configuration File

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

---

## 🛠️ Building from Source

### Prerequisites
- **Java 25 JDK** (OpenJDK / Eclipse Temurin)
- Git

### Build Instructions
```bash
# Clone the repository
git clone https://github.com/aegeada/Item-Landing-Sound-Refabricated.git
cd Item-Landing-Sound-Refabricated

# Build the mod JAR
./gradlew build
```

The compiled mod JAR will be located in `build/libs/item_landing_sound_refabricated-<version>.jar`.

---

## 📄 License & Attribution

This project is licensed under the [MIT License](LICENSE).
- Original Mod by **ExiledLizard635** ([Modrinth](https://modrinth.com/mod/item-landing-sound) / [CurseForge](https://www.curseforge.com/minecraft/mc-mods/item-landing-sound)).
- Refabricated and modernized for Minecraft 26.3 by **Item Landing Sound Refabricated Contributors**.
