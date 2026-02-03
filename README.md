![Header](https://raw.githubusercontent.com/jp-studios/all-the-hud/main/screenshots/Header.png)

# All the HUD: One HUD mod to rule them all

## **<span style="color:#e03e2d">⚠️ Note: This is Still a Beta Release</span>**

**<span style="color:#e03e2d">Available on Fabric for Minecraft 1.20.1, 1.21.1, and 1.21.11. Features are functional but still in development. <a href="https://github.com/jp-studios/all-the-hud/issues" target="_blank" rel="nofollow">Please report bugs!</a></span>**

***

Are you tired of installing several separate HUD mods that bloat your modpack with dependencies that always seem to conflict with other mods? I couldn't take it anymore either, so I made this mod to reduce all the HUD mods I needed in a single, lightweight solution. All the HUD will _(eventually)_ combine the best features from multiple mods into a single, fully client-side solution that should be vanilla-friendly enough to work on any server.

### **:: Planned Features ::**

*   📊 Lightweight, Per-World Storage | ✅ Complete
*   ☠️ Death Coordinates in Private Chat | ✅ Complete
*   🧭 Compass Bar | 🟡 In Development
*   ⚙️ In-game config screen, keybinds, and settings import/export
*   📍 Coordinates, time, biome, and other info displays from F3
*   🎮 Hotbar tweaks to display tooltips, armor info, and more inventory
*   ⚡️ Potion effect HUD
*   💬 Chat area options
*   🎯 Dynamic crosshairs
*   👀 Auto-hide HUD elements
*   💫 And much more. **Want to request a feature?** [Open an issue on GitHub](https://github.com/jp-studios/all-the-hud/issues) and leave a suggestion!

### **<span style="color:#f1c40f">:: Active Development (Next Release) ::</span>**

*   🗺️ Compass Bar - Holding a map indicates the general coordinates to reach the edge of the map area
*   🔄️ Compass Bar - Reorder the stacking importance (lodestone and map on top, nether portal in front of end portal)
*   ⚛️ Compass Bar - More interesting compass layouts (condensed options and experience bar overlays)
*   ⚙️ In-game config screen, keybinds, and settings import/export

***

# **<span style="color:#2dc26b">// CURRENT FEATURES \\\\</span>**

***

# **🧭 Vanilla-Friendly Compass Bar**

Never get lost again. All icons are automatically added as you discover them while playing the game (useful if radar maps aren't allowed on your server).

_Compass bar showing your latest death, your latest bed, and world spawn:_

![Death, Home, and World Spawn](https://raw.githubusercontent.com/jp-studios/all-the-hud/main/screenshots/death_home_and_world_spawn.png)

_Markers shrink when distant, and intelligently handle overlaps so you never lose sight of important locations:_

![Overlapping Icons](https://raw.githubusercontent.com/jp-studios/all-the-hud/main/screenshots/overlapping_icons.png)

### **📍 Compass Bar Icons**

**Automatic Vanilla Points of Interest**
*   Custom-designed icon artwork
*   Scales down when over 500 blocks away
*   Smart stack up to 3 icons on top of each other
*   Intelligently display only in the proper dimension
*   Automatically detects if you sleep in or destroy your bed or swap your spawn to a nether spawn anchor

**When overlapping, they are stacked by importance:**
*   ☠️ **Latest Death** - Skull icon marking your latest death (auto-clears when you return) and even puts your death coordinates in a private chat
*   🏠 **Latest Bed** - House icon marking your most recent bed/respawn point set (Overworld only)
*   🧭 **Lodestone** - Shows lodestone location (if in the current dimension) when holding a lodestone compass in either hand (off-hand will take priority)
*   ⚛️ **Respawn Anchor** - Tracks spawn points when you've set your spawn at a charged respawn anchor (Nether only)
*   🚪 **End Gateway** - Tracks the latest End Gateway travelled to and from the End Islands (End only)
*   🌀 **End Portal** - Tracks your latest stronghold portal in the Overworld and the Main Island return portal in The End (Overworld and End)
*   🌐 **Nether Portal** - Tracks your latest portal location when you travel between dimensions (Overworld and Nether)
*   🌎 **World Spawn** - Blue globe helps keep you oriented (Overworld only)

***

# **💬 Chat Coordinates (completely private)**

**Private Messages in Your Chat**
*   Notifications when a new icon is added or adjusted on your Compass Bar
*   Coordinate values posted privately to chat when you die or when holding a lodestone compass

_Any updates about your death coordinates or the items on the compass are shown privately only to you:_

![Private Update Messages](https://raw.githubusercontent.com/jp-studios/all-the-hud/main/screenshots/private_update_messages.png)

***

# **Technical Details**

**Client-side only:** Works on any server without requiring additional dependencies or configuration (except Fabric). Minimal performance impact with world-specific storage in `config/allthehud/`.

### **License**

_This mod is released as All Rights Reserved | Copyright ©JP Studios_

***

**<span style="color:#2dc26b">✅ YES, You may use this mod freely for personal play and include the unmodified mod in modpacks without needing to ask.</span>**

**🚫 You may not (unless granted permission by me):**

*   Reupload or mirror this mod
*   Distribute modified or forked versions
*   Monetize this mod or derivatives
*   Reuse code, textures, models, or other assets
*   If you’re interested in collaborating, reach out first

**This is a personal passion project with my own artwork. Please respect the time and care that went into it.**

***

**Download:** [Modrinth](https://modrinth.com/user/jpstudios) | [CurseForge](https://www.curseforge.com/members/jpstudios/projects) | [GitHub](https://github.com/jp-studios/all-the-hud)
