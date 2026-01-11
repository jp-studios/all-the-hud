package com.jpstudios.allthehud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.List;

public class HudRenderer {

    // Textures for POI icons (normal and distant variants)
    private static final Identifier WORLD_SPAWN_ICON = Identifier.of("allthehud", "textures/poi-world-spawn.png");
    private static final Identifier WORLD_SPAWN_ICON_DISTANT = Identifier.of("allthehud", "textures/poi-world-spawn-distant.png");
    private static final Identifier BED_ICON = Identifier.of("allthehud", "textures/poi-bed.png");
    private static final Identifier BED_ICON_DISTANT = Identifier.of("allthehud", "textures/poi-bed-distant.png");
    private static final Identifier DEATH_ICON = Identifier.of("allthehud", "textures/poi-death.png");
    private static final Identifier DEATH_ICON_DISTANT = Identifier.of("allthehud", "textures/poi-death-distant.png");

    public static void register() {
        HudRenderCallback.EVENT.register(HudRenderer::renderHud);
    }

    private static void renderHud(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Don't render in F1 mode (hidden HUD)
        if (client.options.hudHidden) {
            return;
        }

        // Don't render if no player exists
        if (client.player == null) {
            return;
        }

        // Get screen dimensions
        int screenWidth = client.getWindow().getScaledWidth();

        // Get player's yaw rotation (0-360 degrees)
        float yaw = client.player.getYaw();

        // Normalize to 0-360 range
        yaw = yaw % 360;
        if (yaw < 0) {
            yaw += 360;
        }

        // Render the compass bar
        renderCompassBar(drawContext, client, screenWidth, yaw);
    }

    private static void renderCompassBar(DrawContext drawContext, MinecraftClient client, int screenWidth, float yaw) {
        int barY = 20;  // Distance from top of screen (text top position)
        int centerX = screenWidth / 2;
        int spacing = 60;  // Pixels between each direction
        
        // Maximum visible range: 180 degrees (90 degrees on each side)
        float maxVisibleAngle = 90.0f;
        
        // Add padding from screen edges (20% of screen width on each side)
        int edgePadding = screenWidth / 5;

        // Draw semi-transparent black background behind the entire compass
        // 30% opacity = 0x4D in hex (77/255 ≈ 30%)
        int bgColor = 0x4D000000;  // ARGB: 30% alpha, black
        int bgHeight = 20;  // Height of background bar
        int bgY = barY - 6;  // Start a bit above the text
        
        // Calculate the width for 180 degrees (±90 degrees from center)
        // At 60 pixels per 45 degrees, 180 degrees = 240 pixels
        // Extended by 6px on each side (12px total)
        int bgWidth = (int)(maxVisibleAngle * 2 * spacing / 45.0) + 12; // 90*2 * 60/45 + 12 = 252 pixels
        int bgStartX = centerX - bgWidth / 2;
        int bgEndX = centerX + bgWidth / 2;
        
        drawContext.fill(bgStartX, bgY, bgEndX, bgY + bgHeight, bgColor);

        // Draw tick marks every 15 degrees (these scroll with the compass)
        drawTickMarks(drawContext, centerX, barY, yaw, spacing, maxVisibleAngle, edgePadding, screenWidth);

        // All 8 directions in order (duplicated for smooth wrapping)
        String[] directions = {"S", "SW", "W", "NW", "N", "NE", "E", "SE", "S", "SW", "W", "NW", "N"};
        float[] angles = {0, 45, 90, 135, 180, 225, 270, 315, 360, 405, 450, 495, 540};

        // Draw each direction
        for (int i = 0; i < directions.length; i++) {
            // Calculate offset from center based on angle difference
            float angleDiff = angles[i] - yaw;

            // Wrap around for smooth scrolling
            if (angleDiff > 180) angleDiff -= 360;
            if (angleDiff < -180) angleDiff += 360;

            // Only show directions within 180 degree range (±90 degrees)
            if (Math.abs(angleDiff) > maxVisibleAngle) {
                continue;
            }

            // Calculate X position (pixels per degree)
            int x = centerX + (int)(angleDiff * spacing / 45.0);

            // Only draw if visible on screen (with padding)
            if (x > edgePadding && x < screenWidth - edgePadding) {
                String direction = directions[i];
                int textWidth = client.textRenderer.getWidth(direction);

                // Calculate distance from center
                float distanceFromCenter = Math.abs(angleDiff);

                // Simple two-color system: White when centered, same gray for everything else
                int color;
                boolean drawShadow;
                if (distanceFromCenter < 20) {
                    color = 0xFFFFFF;  // White - within 20 degrees of facing direction
                    drawShadow = true;  // Only draw shadow on white text
                } else {
                    color = 0xAAAAAA;  // All unfocused directions are the same light gray
                    drawShadow = false;
                }

                // Draw the direction text (with shadow only if white)
                drawContext.drawText(client.textRenderer, direction, x - textWidth / 2, barY, color, drawShadow);
            }
        }

        // Draw center marker (double tick marks - one above and one below the text)
        // Top tick mark (above text with small gap)
        drawContext.fill(centerX, barY - 4, centerX + 1, barY - 2, 0xFFFFFFFF);
        
        // Bottom tick mark (below text with small gap)
        drawContext.fill(centerX, barY + 10, centerX + 1, barY + 12, 0xFFFFFFFF);

        // Draw POIs (Points of Interest) AFTER everything else so they're always on top
        // Ordered so world spawn is furthest back (drawn first), death is on top (drawn last)
        // Track X positions for overlap detection
        List<Integer> drawnPOIPositions = new ArrayList<>();
        
        // First pass: calculate all positions and detect overlaps
        Integer spawnX = calculatePOIXPosition(client, client.world != null ? client.world.getSpawnPos() : null, centerX, yaw, spacing, maxVisibleAngle, edgePadding, screenWidth);
        Integer bedX = calculatePOIXPosition(client, POIData.getBedLocation(), centerX, yaw, spacing, maxVisibleAngle, edgePadding, screenWidth);
        Integer deathX = calculatePOIXPosition(client, POIData.getDeathLocation(), centerX, yaw, spacing, maxVisibleAngle, edgePadding, screenWidth);
        
        // Detect overlaps
        boolean spawnOverlapped = false;
        boolean bedOverlapped = false;
        boolean bedOverlaps = false;
        boolean deathOverlaps = false;
        
        // Check if spawn is overlapped by bed or death
        if (spawnX != null) {
            if (bedX != null && Math.abs(spawnX - bedX) < 12) spawnOverlapped = true;
            if (deathX != null && Math.abs(spawnX - deathX) < 12) spawnOverlapped = true;
        }
        
        // Check if bed overlaps spawn or is overlapped by death
        if (bedX != null) {
            if (spawnX != null && Math.abs(bedX - spawnX) < 12) bedOverlaps = true;
            if (deathX != null && Math.abs(bedX - deathX) < 12) bedOverlapped = true;
        }
        
        // Check if death overlaps spawn or bed
        if (deathX != null) {
            if (spawnX != null && Math.abs(deathX - spawnX) < 12) deathOverlaps = true;
            if (bedX != null && Math.abs(deathX - bedX) < 12) deathOverlaps = true;
        }
        
        // Draw world spawn
        // If overlapped by another POI, move UP 2px and scale down
        // If not overlapped, no offset and full size
        int spawnYOffset = spawnOverlapped ? -2 : 0;
        Integer spawnDrawnX = drawWorldSpawnPOI(drawContext, client, centerX, barY, yaw, spacing, maxVisibleAngle, edgePadding, screenWidth, spawnYOffset, spawnOverlapped);
        if (spawnDrawnX != null) {
            drawnPOIPositions.add(spawnDrawnX);
        }
        
        // Draw bed
        // If overlapping spawn, move DOWN 3px and scale
        // If overlapped by death (and not overlapping spawn), move UP 2px and scale
        // If both, the DOWN takes priority (it's in the middle)
        int bedYOffset = 0;
        boolean bedIsScaled = false;
        if (bedOverlaps && bedOverlapped) {
            bedYOffset = 3;  // Middle of stack, move down
            bedIsScaled = true;
        } else if (bedOverlaps) {
            bedYOffset = 3;  // Overlapping spawn, move down
            bedIsScaled = true;
        } else if (bedOverlapped) {
            bedYOffset = -2;  // Overlapped by death, move up
            bedIsScaled = true;
        }
        Integer bedDrawnX = drawBedPOI(drawContext, client, centerX, barY, yaw, spacing, maxVisibleAngle, edgePadding, screenWidth, bedYOffset, bedIsScaled);
        if (bedDrawnX != null) {
            drawnPOIPositions.add(bedDrawnX);
        }
        
        // Draw death
        // If overlapping anything, move DOWN 3px and scale
        int deathYOffset = deathOverlaps ? 3 : 0;
        drawDeathPOI(drawContext, client, centerX, barY, yaw, spacing, maxVisibleAngle, edgePadding, screenWidth, deathYOffset, deathOverlaps);
    }

    private static void drawTickMarks(DrawContext drawContext, int centerX, int barY, float yaw, int spacing, float maxVisibleAngle, int edgePadding, int screenWidth) {
        // Draw tick marks at world angles (they scroll with cardinal directions)
        // We need to iterate through actual world angles just like we do for direction labels
        
        // Create an array of all tick mark angles (every 15 degrees around the full circle)
        // Similar to how we duplicate direction angles for wrapping
        float[] tickAngles = new float[48];  // 360/15 = 24 positions, doubled for wrapping
        for (int i = 0; i < 48; i++) {
            tickAngles[i] = (i * 15) % 360;
        }
        
        for (float angle : tickAngles) {
            // Skip cardinal/intercardinal directions (every 45 degrees) - they have text labels
            if (angle % 45 == 0) {
                continue;
            }
            
            // Calculate offset from center based on angle difference (same logic as direction labels)
            float angleDiff = angle - yaw;
            
            // Wrap around for smooth scrolling
            if (angleDiff > 180) angleDiff -= 360;
            if (angleDiff < -180) angleDiff += 360;
            
            // Only show tick marks within visible range
            if (Math.abs(angleDiff) > maxVisibleAngle) {
                continue;
            }
            
            // Calculate X position (same formula as direction labels)
            int x = centerX + (int)(angleDiff * spacing / 45.0);
            
            // Only draw if visible on screen (with padding)
            if (x > edgePadding && x < screenWidth - edgePadding) {
                // 70% white opacity for rotating tick marks (0xB3 = 179/255 ≈ 70%)
                int tickColor = 0xB3B3B3B3;  // Gray with 70% opacity for differentiation
                
                // Tick mark vertically centered with text (4 pixels tall)
                // Text top is at barY, text is ~8px tall, so center is at barY + 4
                drawContext.fill(x, barY + 2, x + 1, barY + 6, tickColor);
            }
        }
    }

    /**
     * Calculate the X position for a POI without drawing it
     * Returns null if POI is not visible
     */
    private static Integer calculatePOIXPosition(MinecraftClient client, BlockPos targetPos, int centerX, float yaw, int spacing, float maxVisibleAngle, int edgePadding, int screenWidth) {
        if (targetPos == null || client.player == null) {
            return null;
        }
        
        // Calculate angle to target
        double dx = targetPos.getX() - client.player.getX();
        double dz = targetPos.getZ() - client.player.getZ();
        double angleToTarget = Math.toDegrees(Math.atan2(-dx, dz));
        
        // Normalize to 0-360
        angleToTarget = angleToTarget % 360;
        if (angleToTarget < 0) {
            angleToTarget += 360;
        }

        // Calculate angle difference
        float angleDiff = (float)(angleToTarget - yaw);
        if (angleDiff > 180) angleDiff -= 360;
        if (angleDiff < -180) angleDiff += 360;

        // Check if within visible range
        if (Math.abs(angleDiff) > maxVisibleAngle) {
            return null;
        }

        // Calculate X position
        int x = centerX + (int)(angleDiff * spacing / 45.0);

        // Check if visible on screen
        if (x > edgePadding + 5 && x < screenWidth - edgePadding - 5) {
            return x;
        }
        
        return null;
    }

    private static Integer drawWorldSpawnPOI(DrawContext drawContext, MinecraftClient client, int centerX, int barY, float yaw, int spacing, float maxVisibleAngle, int edgePadding, int screenWidth, int yOffset, boolean isOverlapping) {
        if (client.world == null) {
            return null;
        }

        // Get world spawn position
        BlockPos spawnPos = client.world.getSpawnPos();
        
        // Calculate and draw POI icon with distance variants
        return drawPOIIconWithVariants(drawContext, client, spawnPos, WORLD_SPAWN_ICON, WORLD_SPAWN_ICON_DISTANT, centerX, barY, yaw, spacing, maxVisibleAngle, edgePadding, screenWidth, yOffset, isOverlapping);
    }

    private static Integer drawBedPOI(DrawContext drawContext, MinecraftClient client, int centerX, int barY, float yaw, int spacing, float maxVisibleAngle, int edgePadding, int screenWidth, int yOffset, boolean isOverlapping) {
        if (!POIData.hasBedLocation()) {
            return null;
        }
        
        BlockPos bedPos = POIData.getBedLocation();
        if (bedPos != null && client.world != null) {
            // Only show bed icon if it's different from world spawn
            BlockPos worldSpawn = client.world.getSpawnPos();
            if (!bedPos.equals(worldSpawn)) {
                return drawPOIIconWithVariants(drawContext, client, bedPos, BED_ICON, BED_ICON_DISTANT, centerX, barY, yaw, spacing, maxVisibleAngle, edgePadding, screenWidth, yOffset, isOverlapping);
            }
        }
        return null;
    }

    private static void drawDeathPOI(DrawContext drawContext, MinecraftClient client, int centerX, int barY, float yaw, int spacing, float maxVisibleAngle, int edgePadding, int screenWidth, int yOffset, boolean isOverlapping) {
        if (!POIData.hasDeathLocation()) {
            return;
        }

        BlockPos deathPos = POIData.getDeathLocation();
        if (deathPos != null) {
            drawPOIIconWithVariants(drawContext, client, deathPos, DEATH_ICON, DEATH_ICON_DISTANT, centerX, barY, yaw, spacing, maxVisibleAngle, edgePadding, screenWidth, yOffset, isOverlapping);
        }
    }

    /**
     * Generic method to draw any POI icon on the compass bar with distance-based variant switching
     * Returns the X position if drawn, null otherwise
     */
    private static Integer drawPOIIconWithVariants(DrawContext drawContext, MinecraftClient client, BlockPos targetPos, Identifier normalIcon, Identifier distantIcon, int centerX, int barY, float yaw, int spacing, float maxVisibleAngle, int edgePadding, int screenWidth, int yOffset, boolean isOverlapping) {
        // Calculate angle to target from player position
        double dx = targetPos.getX() - client.player.getX();
        double dz = targetPos.getZ() - client.player.getZ();
        
        // Calculate 2D horizontal distance
        double distance = Math.sqrt(dx * dx + dz * dz);
        
        // Convert to angle (atan2 returns radians, convert to degrees)
        // Minecraft's coordinate system: North = -Z, South = +Z, West = -X, East = +X
        // Minecraft yaw: South = 0°, West = 90°, North = 180°, East = 270°
        // We use -dx because X axis is inverted in Minecraft's yaw system
        double angleToTarget = Math.toDegrees(Math.atan2(-dx, dz));
        
        // Normalize to 0-360
        angleToTarget = angleToTarget % 360;
        if (angleToTarget < 0) {
            angleToTarget += 360;
        }

        // Calculate angle difference from player's facing direction
        float angleDiff = (float)(angleToTarget - yaw);
        
        // Wrap around for smooth scrolling
        if (angleDiff > 180) angleDiff -= 360;
        if (angleDiff < -180) angleDiff += 360;

        // Only show if within visible range
        if (Math.abs(angleDiff) > maxVisibleAngle) {
            return null;
        }

        // Calculate X position
        int x = centerX + (int)(angleDiff * spacing / 45.0);

        // Only draw if visible on screen (with padding)
        if (x > edgePadding + 5 && x < screenWidth - edgePadding - 5) {
            // Determine which icon variant to use:
            // Use distant variant if either:
            // 1. Distance is over 500 blocks, OR
            // 2. Icon is within 15 degrees of either edge (|angleDiff| > 75)
            boolean useDistant = distance > 500.0 || Math.abs(angleDiff) > 75;
            Identifier icon = useDistant ? distantIcon : normalIcon;
            
            // Debug logging to verify distance calculation
            if (distance > 500.0) {
                AllTheHUD.LOGGER.debug("POI at distance {} blocks - using distant variant", (int)distance);
            }
            
            // Scale and position for overlapping icons
            if (isOverlapping) {
                // Use matrix transformation to scale the icon by 90%
                drawContext.getMatrices().push();
                
                // Translate to icon center, scale, translate back
                float scale = 0.9f;
                float iconCenterX = x;
                float iconCenterY = barY - 19 + yOffset + 8.5f;  // 8.5 is half of 17
                
                drawContext.getMatrices().translate(iconCenterX, iconCenterY, 0);
                drawContext.getMatrices().scale(scale, scale, 1.0f);
                drawContext.getMatrices().translate(-iconCenterX, -iconCenterY, 0);
                
                // Draw at original size (scaling is handled by matrix)
                drawContext.drawTexture(icon, x - 8, barY - 19 + yOffset, 0, 0, 17, 17, 17, 17);
                
                drawContext.getMatrices().pop();
            } else {
                // Draw at full size without scaling
                drawContext.drawTexture(icon, x - 8, barY - 19 + yOffset, 0, 0, 17, 17, 17, 17);
            }
            return x;  // Return X position so we can track overlaps
        }
        return null;
    }
}
