# Modrinth Project Information

- Title: Item Landing Sound Refabricated
- Slug: item-landing-sound-refabricated
- Short Description: Client-side Fabric mod for Minecraft 26.3 playing realistic block-based impact sounds when items hit the ground, featuring instant collision detection and zero memory leaks.
- Project Type: Mod
- Environment: Client only
- Categories: Utility, Technology
- License: MIT
- Source Repository: https://github.com/1unarea/Item-Landing-Sound-Refabricated
- Issues Tracker: https://github.com/1unarea/Item-Landing-Sound-Refabricated/issues

---

# Version Release Details (First Upload)

- Version Number: 0.1.0-fabric-26.3
- Version Title: 0.1.0 Fabric 26.3
- Release Channel: Beta
- Supported Game Versions: 26.3
- Supported Loaders: Fabric
- Dependencies: Fabric API (Required)
- File to Upload: item_landing_sound_refabricated-0.1.0-fabric-26.3.jar

---

# Project Description (Copy and Paste into Modrinth Page Body)

**Item Landing Sound Refabricated** is a client-side Fabric mod for Minecraft 26.3 that makes dropped items produce realistic, material-dependent impact sounds the moment they contact the ground.

Originally created by ExiledLizard635, this refabricated edition has been completely rewritten to eliminate sound delays, remove memory leaks, and add persistent configuration.

---

## Core Improvements

### Instant Ground Collision Detection
The legacy mod polled items during client ticks and checked position differences while `isOnGround()` was true. When items had horizontal speed, this created a noticeable delay where sounds only played after sliding came to a halt.

Item Landing Sound Refabricated detects physical ground impact at tick 0 using targeted Mixin hooks directly inside entity movement calculations. Sounds play instantly upon contact, even when items slide rapidly across ice, packed mud, or smooth surfaces.

### Zero Memory Leaks
The legacy mod stored item UUIDs in an unbounded static collection that was never purged when items were picked up, merged, or despawned.

This edition binds state tracking directly to the `ItemEntity` lifecycle via a lightweight duck interface. There are zero static collections, and all tracking data is automatically garbage-collected when the entity leaves the client world.

### Authentic Block Surface Acoustics
Rather than playing the jarring block-break sound effect (`getBreakSound()`), this mod resolves the authentic fall sound group (`SoundType.getFallSound()`), falling back to step sounds if necessary. It correctly resolves layered blocks such as carpets, moss, and snow layers resting on solid blocks, playing soft fabric sounds instead of stone thuds.

---

## And Other Features...

- Fluid suppression: Items landing in water or lava produce no ground impact sounds.
- Micro-bounce protection: A 4-tick debounce filter prevents audio chatter on bouncing items.
- Dynamic impact volume: Volume scales naturally based on vertical falling velocity.
- Organic pitch variation: Subtle pitch variance prevents repetitive audio fatigue during bulk item drops.
- Persistent configuration: Settings are stored in `config/item_landing_sound_refabricated.json` and saved with atomic file replacement to prevent corruption.
- In-game commands: Full command support via `/itemlandingsound:volume` and `/itemlandingsound`.
- 100% client-side: Fully compatible with vanilla servers and modded multiplayer networks.

---

## Installation

1. Install Minecraft 26.3 and Fabric Loader (0.19.3 or higher).
2. Install Fabric API (0.161.0+26.3 or higher).
3. Place `item_landing_sound_refabricated-0.1.0-fabric-26.3.jar` into your `.minecraft/mods` directory.
4. Launch Minecraft.

---

## In-Game Commands

- `/itemlandingsound:volume`: Queries current volume.
- `/itemlandingsound:volume <0.0 - 2.0>`: Sets volume and persists to config.
- `/itemlandingsound`: Displays status and configuration summary.
- `/itemlandingsound toggle`: Toggles mod sound playback.
- `/itemlandingsound reload`: Reloads configuration from disk.

---

## License and Attribution

This project is licensed under the MIT License.
- Original mod by ExiledLizard635 (Item Landing Sound).
- Refabricated and maintained for Minecraft 26.3 by Item Landing Sound Refabricated Contributors.
