# Changelog

All notable changes to this project will be documented in this file.

## [0.2.2-beta] - 2026-02-07

### Fixed
- **World memory corruption**: POI data no longer mixes between worlds when switching saves
- **POI persistence**: POI markers now correctly persist after restarting Minecraft
- **World change detection**: Now runs every tick and registers before trackers to prevent race conditions
- **PortalTracker state reset**: Static tracker state is now cleared on world switch to prevent false dimension-change detection
- **JSON serialization**: BlockPos now serialized as `int[]` arrays instead of obfuscated field names for reliable cross-version storage
- **Old config compatibility**: Gracefully handles old-format config files by deleting and starting fresh instead of crashing

### Changed
- **POIStorage rewrite**: `save()` now validates world ID matches before writing, `getSaveFile()` takes explicit world ID parameter, `getWorldId()` returns null instead of "unknown"

## [0.2.1-beta] - 2026-02-02

### Added
- **Automatic POI cleanup**: POIs are now automatically removed when their blocks are broken (beds, respawn anchors, lodestones)
- **POIBlockBreakMixin**: New mixin system detects when POI blocks are destroyed and clears markers

### Changed
- **Performance optimizations**: All tick event handlers now use throttling to reduce overhead on large modpacks
  - World change detection: Checks every 20 ticks (1 second) instead of every tick
  - Death tracking: Checks every 10 ticks (0.5 seconds) instead of every tick
  - Lodestone tracking: Scans inventory every 20 ticks (1 second) instead of every tick
- **Simplified dimension handling**: Cleaner dimension name detection for death messages
- **Version management**: fabric.mod.json now uses Gradle version placeholder for easier multi-version support

### Fixed
- **Color rendering**: Fixed HUD text color values (0xFFFFFFFF : 0xFFAAAAAA) for proper alpha channel support

## [0.2.0-beta] - 2026-01-28

### Added
- **Nether Portal tracking**: Automatically tracks portal locations in Overworld and Nether when you travel between dimensions
- **End Portal tracking**: Tracks stronghold portals in Overworld and return portals in The End
- **End Gateway tracking**: Tracks outer End island gateways (teleports >100 blocks detected)
- **Lodestone tracking**: Tracks lodestone location when holding a lodestone compass in either hand (off-hand takes priority)
- **Respawn Anchor tracking**: Tracks nether respawn points when setting spawn at a charged respawn anchor and clears the bed icon in the overworld
- **Death location tracking**: Automatically marks death location with dimension context, auto-clears when returning within 10 blocks
- **POI icon variants**: 6 new icon textures (lodestone, respawn anchor, nether portal, end portal, end gateway, death marker) with normal and distant variants
- **Dimensional POI tracking**: POIs are tracked per-dimension with automatic visibility filtering

### Changed
- **Condensed codebase**: 46% reduction in lines (1,535 → 836 lines)
- **Improved POI rendering**: Unified rendering system with overlap detection and distance-based icon switching
- **Triple POI stacking system**: Back/middle/front icons offset vertically when overlapping (supports up to 3 icons)
- **Enhanced bed tracking**: Improved geometric validation and always normalizes to bed foot position, and clears the respawn anchor location if it was active
- **Condensed POI storage system**: Now supports multi-dimensional POI tracking with per-world JSON files (a single file per world)

---

## [0.1.2] - 2026-01-10

### Added

- Multi-version support for Minecraft 1.20.1 and 1.21.1
- Separate build configurations for each supported version
- Lowered Fabric Loader requirement to 0.14.0 for broader compatibility

### Changed

- Mixin compatibility level changed to JAVA_17 for multi-version support

---

## [0.1.1-alpha] - 2026-01-09

### Added

- Lowered Fabric Loader requirement from 0.18.4 to 0.16.0 for better modpack compatibility

This makes the mod compatible with more modpacks and older Fabric installations.

---

**Note:** This is an alpha release. Features are functional but under active development.
