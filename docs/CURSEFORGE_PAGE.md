# CurseForge Project Information

- Project Name: Item Landing Sound Refabricated
- Summary: Client-side Fabric mod for Minecraft 26.3 playing realistic block-based impact sounds when items hit the ground, featuring instant collision detection and zero memory leaks.
- Primary Category: Utility & QoL
- Secondary Categories: Audio/Sound, Technology
- Environment: Client
- License: MIT
- Project Visibility: Public
- Source Repository: https://github.com/1unarea/Item-Landing-Sound-Refabricated
- Issue Tracker: https://github.com/1unarea/Item-Landing-Sound-Refabricated/issues

---

# CurseForge File Upload Information

- Display Name: Item Landing Sound Refabricated [Fabric 26.3, Beta 0.1.0]
- File to Upload: item_landing_sound_refabricated-0.1.0-fabric-26.3.jar
- Release Type: Beta
- Supported Minecraft Versions: 26.3
- Supported ModLoader: Fabric
- Required Dependencies: Fabric API (Related Projects -> Add "Fabric API" as Required Dependency)

---

# Description (Copy and Paste into CurseForge Description Editor)

<p><strong>Item Landing Sound Refabricated</strong> is a client-side Fabric mod for Minecraft 26.3 that makes dropped items produce realistic, material-dependent impact sounds the moment they contact the ground.</p>

<p>Originally created by ExiledLizard635, this refabricated edition has been completely rewritten to eliminate sound delays, remove memory leaks, and add persistent configuration.</p>

<hr/>

<h3>Core Improvements</h3>

<h4>Instant Ground Collision Detection</h4>
<p>The legacy mod polled items during client ticks and checked position differences while <code>isOnGround()</code> was true. When items had horizontal speed, this created a noticeable delay where sounds only played after sliding came to a halt.</p>
<p>Item Landing Sound Refabricated detects physical ground impact at tick 0 using targeted Mixin hooks directly inside entity movement calculations. Sounds play instantly upon contact, even when items slide rapidly across ice, packed mud, or smooth surfaces.</p>

<h4>Zero Memory Leaks</h4>
<p>The legacy mod stored item UUIDs in an unbounded static collection that was never purged when items were picked up, merged, or despawned.</p>
<p>This edition binds state tracking directly to the <code>ItemEntity</code> lifecycle via a lightweight duck interface. There are zero static collections, and all tracking data is automatically garbage-collected when the entity leaves the client world.</p>

<h4>Authentic Block Surface Acoustics</h4>
<p>Rather than playing the jarring block-break sound effect (<code>getBreakSound()</code>), this mod resolves the authentic fall sound group (<code>SoundType.getFallSound()</code>), falling back to step sounds if necessary. It correctly resolves layered blocks such as carpets, moss, and snow layers resting on solid blocks, playing soft fabric sounds instead of stone thuds.</p>

<hr/>

<h3>And Other Features...</h3>
<ul>
  <li><strong>Fluid suppression:</strong> Items landing in water or lava produce no ground impact sounds.</li>
  <li><strong>Micro-bounce protection:</strong> A 4-tick debounce filter prevents audio chatter on bouncing items.</li>
  <li><strong>Dynamic impact volume:</strong> Volume scales naturally based on vertical falling velocity.</li>
  <li><strong>Organic pitch variation:</strong> Subtle pitch variance prevents repetitive audio fatigue during bulk item drops.</li>
  <li><strong>Persistent configuration:</strong> Settings are stored in <code>config/item_landing_sound_refabricated.json</code> and saved with atomic file replacement to prevent corruption.</li>
  <li><strong>In-game commands:</strong> Full command support via <code>/itemlandingsound:volume</code> and <code>/itemlandingsound</code>.</li>
  <li><strong>100% client-side:</strong> Fully compatible with vanilla servers and modded multiplayer networks.</li>
</ul>

<hr/>

<h3>Installation</h3>
<ol>
  <li>Install <strong>Minecraft 26.3</strong> and <strong>Fabric Loader</strong> (version 0.19.3 or higher).</li>
  <li>Install <strong>Fabric API</strong> (version 0.161.0+26.3 or higher).</li>
  <li>Place <code>item_landing_sound_refabricated-0.1.0-fabric-26.3.jar</code> into your <code>.minecraft/mods</code> directory.</li>
  <li>Launch Minecraft.</li>
</ol>

<hr/>

<h3>In-Game Commands</h3>
<ul>
  <li><code>/itemlandingsound:volume</code>: Queries current active volume.</li>
  <li><code>/itemlandingsound:volume &lt;0.0 - 2.0&gt;</code>: Sets volume and persists to configuration.</li>
  <li><code>/itemlandingsound</code>: Displays status and configuration summary.</li>
  <li><code>/itemlandingsound toggle</code>: Toggles mod sound playback on/off.</li>
  <li><code>/itemlandingsound reload</code>: Reloads configuration from disk.</li>
</ul>

<hr/>

<h3>License and Attribution</h3>
<p>This project is licensed under the <a href="https://github.com/1unarea/Item-Landing-Sound-Refabricated/blob/main/LICENSE" target="_blank" rel="noopener noreferrer">MIT License</a>.</p>
<ul>
  <li>Original mod by <strong>ExiledLizard635</strong> (<a href="https://www.curseforge.com/minecraft/mc-mods/item-landing-sound" target="_blank" rel="noopener noreferrer">Item Landing Sound</a>).</li>
  <li>Refabricated and maintained for Minecraft 26.3 by <strong>Item Landing Sound Refabricated Contributors</strong>.</li>
</ul>
