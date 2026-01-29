# Changelog

All notable changes to this project will be documented in this file.

## [0.2.0-beta] - 2026-01-28

### Added
- **Nether Portal tracking**: Automatically tracks portal locations in Overworld and Nether when you travel between dimensions
- **End Portal tracking**: Tracks stronghold portals in Overworld and return portals in The End
- **End Gateway tracking**: Tracks outer End island gateways (teleports >100 blocks detected)
- **Lodestone tracking**: Tracks lodestone location when holding a lodestone compass in either hand (off-hand takes priority)
- **Respawn Anchor tracking**: Tracks nether respawn points when setting spawn at a charged respawn anchor
- **Death location tracking**: Automatically marks death location with dimension context, auto-clears when returning within 10 blocks
- **World Spawn POI**: Always displays world spawn point on compass bar
- **POI stacking system**: Back/middle/front icons offset vertically when overlapping (supports up to 3 icons)
- **POI icon variants**: 6 new icon textures (lodestone, respawn anchor, nether portal, end portal, end gateway, death marker) with normal and distant variants
- **Dimensional POI tracking**: POIs are tracked per-dimension with automatic visibility filtering

### Changed
- **Condensed codebase**: 46% reduction in lines (1,535 → 836 lines)
- **Improved POI rendering**: Unified rendering system with overlap detection and distance-based icon switching
- **Enhanced bed tracking**: Improved geometric validation and always normalizes to bed foot position
- **POI storage system**: Now supports multi-dimensional POI tracking with per-world JSON files

### Fixed
- Respawn anchor: Now correctly detects spawn setting when fully charged (4 charges) with glowstone
- Lodestone compass: Message no longer re-triggers when switching main hand away from compass
- POI icon scaling: Middle icons now scale to 90% like front/back when 3+ icons overlap
- Death tracking: Only triggers on actual death (not respawn), includes dimension information in chat message

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
