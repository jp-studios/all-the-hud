# Changelog

All notable changes to this project will be documented in this file.

## [0.1.0-alpha] - 2026-01-08

### Added

**Compass Bar**
- Scrolling compass showing all 8 cardinal and intercardinal directions (N, NE, E, SE, S, SW, W, NW)
- Smooth rotation following player movement
- White highlighting for current direction (±20° range)
- Rotating tick marks every 15° for precise navigation
- Semi-transparent black background for visibility

**World Spawn Marker**
- Blue globe icon always showing world spawn location

**Bed/Home Marker**
- Red home icon marking your most recent respawn point/bed
- Auto-saves when you interact with any bed (day or night)
- Green chat message on success
- Red warning if you're too far from the bed

**Death Location Marker**
- Black skull icon marking your death location
- Grey chat message with coordinates and dimension name
- Auto-clears when you return within 10 blocks

**Smart Icon Features**
- Overlap detection: overlapping icons scale and offset for visibility
- Points of Interest automatically scales when >500 blocks away and at compass edges for a falloff effect
- World-specific storage (separate POIs per world and dimension)

**User Experience**
- F1 mode support (hides with HUD)
- GUI scale compatibility
- Dimension detection (Overworld, Nether, End)

---

**Note:** This is an alpha release. Features are functional but under active development.
