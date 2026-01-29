package com.jpstudios.allthehud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HudRenderer {

    // POI icon pairs (normal, distant)
    private static final Identifier[] WORLD_SPAWN_ICONS = {
        Identifier.of("allthehud", "textures/poi-world-spawn.png"),
        Identifier.of("allthehud", "textures/poi-world-spawn-distant.png")
    };
    private static final Identifier[] BED_ICONS = {
        Identifier.of("allthehud", "textures/poi-bed.png"),
        Identifier.of("allthehud", "textures/poi-bed-distant.png")
    };
    private static final Identifier[] DEATH_ICONS = {
        Identifier.of("allthehud", "textures/poi-death.png"),
        Identifier.of("allthehud", "textures/poi-death-distant.png")
    };
    private static final Identifier[] NETHER_PORTAL_ICONS = {
        Identifier.of("allthehud", "textures/poi-nether.png"),
        Identifier.of("allthehud", "textures/poi-nether-distant.png")
    };
    private static final Identifier[] END_PORTAL_ICONS = {
        Identifier.of("allthehud", "textures/poi-end.png"),
        Identifier.of("allthehud", "textures/poi-end-distant.png")
    };
    private static final Identifier[] END_GATEWAY_ICONS = {
        Identifier.of("allthehud", "textures/poi-gateway.png"),
        Identifier.of("allthehud", "textures/poi-gateway-distant.png")
    };
    private static final Identifier[] LODESTONE_ICONS = {
        Identifier.of("allthehud", "textures/poi-lodestone.png"),
        Identifier.of("allthehud", "textures/poi-lodestone-distant.png")
    };
    private static final Identifier[] RESPAWN_ANCHOR_ICONS = {
        Identifier.of("allthehud", "textures/poi-respawn-beacon.png"),
        Identifier.of("allthehud", "textures/poi-respawn-beacon-distant.png")
    };

    // Compass bar constants
    private static final int BAR_Y = 20;
    private static final int SPACING = 60;
    private static final float MAX_VISIBLE_ANGLE = 90.0f;
    private static final int OVERLAP_THRESHOLD = 12;

    public static void register() {
        HudRenderCallback.EVENT.register(HudRenderer::renderHud);
    }

    private static void renderHud(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden || client.player == null || client.world == null) return;

        int screenWidth = client.getWindow().getScaledWidth();
        float yaw = ((client.player.getYaw() % 360) + 360) % 360; // Normalize to 0-360

        renderCompassBar(drawContext, client, screenWidth, yaw);
    }

    private static void renderCompassBar(DrawContext drawContext, MinecraftClient client, int screenWidth, float yaw) {
        int centerX = screenWidth / 2;
        int edgePadding = screenWidth / 5;

        // Draw background
        int bgWidth = (int)(MAX_VISIBLE_ANGLE * 2 * SPACING / 45.0) + 12;
        drawContext.fill(centerX - bgWidth / 2, BAR_Y - 6, centerX + bgWidth / 2, BAR_Y + 14, 0x4D000000);

        // Draw tick marks
        drawTickMarks(drawContext, centerX, yaw, edgePadding, screenWidth);

        // Draw direction labels
        String[] directions = {"S", "SW", "W", "NW", "N", "NE", "E", "SE", "S", "SW", "W", "NW", "N"};
        float[] angles = {0, 45, 90, 135, 180, 225, 270, 315, 360, 405, 450, 495, 540};

        for (int i = 0; i < directions.length; i++) {
            float angleDiff = wrapAngle(angles[i] - yaw);
            if (Math.abs(angleDiff) > MAX_VISIBLE_ANGLE) continue;

            int x = centerX + (int)(angleDiff * SPACING / 45.0);
            if (x > edgePadding && x < screenWidth - edgePadding) {
                String dir = directions[i];
                int textWidth = client.textRenderer.getWidth(dir);
                boolean focused = Math.abs(angleDiff) < 20;
                drawContext.drawText(client.textRenderer, dir, x - textWidth / 2, BAR_Y,
                    focused ? 0xFFFFFF : 0xAAAAAA, focused);
            }
        }

        // Draw center marker
        drawContext.fill(centerX, BAR_Y - 4, centerX + 1, BAR_Y - 2, 0xFFFFFFFF);
        drawContext.fill(centerX, BAR_Y + 10, centerX + 1, BAR_Y + 12, 0xFFFFFFFF);

        // Draw POIs
        drawPOIs(drawContext, client, centerX, yaw, edgePadding, screenWidth);
    }

    private static void drawTickMarks(DrawContext drawContext, int centerX, float yaw, int edgePadding, int screenWidth) {
        for (int i = 0; i < 24; i++) {
            float angle = i * 15;
            if (angle % 45 == 0) continue; // Skip cardinal/intercardinal directions

            float angleDiff = wrapAngle(angle - yaw);
            if (Math.abs(angleDiff) > MAX_VISIBLE_ANGLE) continue;

            int x = centerX + (int)(angleDiff * SPACING / 45.0);
            if (x > edgePadding && x < screenWidth - edgePadding) {
                drawContext.fill(x, BAR_Y + 2, x + 1, BAR_Y + 6, 0xB3B3B3B3);
            }
        }
    }

    private static void drawPOIs(DrawContext drawContext, MinecraftClient client, int centerX, float yaw, int edgePadding, int screenWidth) {
        var dimKey = client.world.getRegistryKey();
        boolean isOverworld = dimKey == World.OVERWORLD;
        boolean isNether = dimKey == World.NETHER;
        boolean isEnd = dimKey == World.END;

        // Calculate all POI X positions for overlap detection
        Integer spawnX = isOverworld ? calcPOIX(client, client.world.getSpawnPos(), centerX, yaw, edgePadding, screenWidth) : null;
        Integer bedX = isOverworld ? calcPOIX(client, POIData.getBedLocation(), centerX, yaw, edgePadding, screenWidth) : null;
        Integer netherX = (isOverworld || isNether) ? calcPOIX(client, POIData.getNetherPortalLocation(isNether), centerX, yaw, edgePadding, screenWidth) : null;
        Integer endX = (isOverworld || isEnd) ? calcPOIX(client, POIData.getEndPortalLocation(isEnd), centerX, yaw, edgePadding, screenWidth) : null;
        Integer gatewayX = isEnd ? calcPOIX(client, POIData.getEndGatewayLocation(), centerX, yaw, edgePadding, screenWidth) : null;
        Integer anchorX = isNether ? calcPOIX(client, POIData.getRespawnAnchorLocation(), centerX, yaw, edgePadding, screenWidth) : null;
        Integer lodestoneX = POIData.isLodestoneVisibleInDimension(dimKey) ? calcPOIX(client, POIData.getLodestoneLocation(), centerX, yaw, edgePadding, screenWidth) : null;
        Integer deathX = dimKey.equals(POIData.getDeathDimension()) ? calcPOIX(client, POIData.getDeathLocation(), centerX, yaw, edgePadding, screenWidth) : null;

        // Draw POIs by dimension (back to front order)
        // Stacking: back=+2x/-2y, middle=no offset, front=-2x/+2y; all overlapping icons scale to 90%
        if (isOverworld) {
            // Order: spawn, nether, end, lodestone, bed, death
            Integer[] after = {netherX, endX, lodestoneX, bedX, deathX};
            drawPOI(drawContext, client, client.world.getSpawnPos(), WORLD_SPAWN_ICONS, centerX, yaw, edgePadding, screenWidth,
                false, anyOverlap(spawnX, after));

            Integer[] beforeN = {spawnX};
            Integer[] afterN = {endX, lodestoneX, bedX, deathX};
            drawPOI(drawContext, client, POIData.getNetherPortalLocation(false), NETHER_PORTAL_ICONS, centerX, yaw, edgePadding, screenWidth,
                anyOverlap(netherX, beforeN), anyOverlap(netherX, afterN));

            Integer[] beforeE = {spawnX, netherX};
            Integer[] afterE = {lodestoneX, bedX, deathX};
            drawPOI(drawContext, client, POIData.getEndPortalLocation(false), END_PORTAL_ICONS, centerX, yaw, edgePadding, screenWidth,
                anyOverlap(endX, beforeE), anyOverlap(endX, afterE));

            if (lodestoneX != null) {
                Integer[] beforeL = {spawnX, netherX, endX};
                Integer[] afterL = {bedX, deathX};
                drawPOI(drawContext, client, POIData.getLodestoneLocation(), LODESTONE_ICONS, centerX, yaw, edgePadding, screenWidth,
                    anyOverlap(lodestoneX, beforeL), anyOverlap(lodestoneX, afterL));
            }

            BlockPos bedPos = POIData.getBedLocation();
            if (bedPos != null && !bedPos.equals(client.world.getSpawnPos())) {
                Integer[] beforeB = {spawnX, netherX, endX, lodestoneX};
                Integer[] afterB = {deathX};
                drawPOI(drawContext, client, bedPos, BED_ICONS, centerX, yaw, edgePadding, screenWidth,
                    anyOverlap(bedX, beforeB), anyOverlap(bedX, afterB));
            }

            if (deathX != null) {
                Integer[] beforeD = {spawnX, netherX, endX, lodestoneX, bedX};
                drawPOI(drawContext, client, POIData.getDeathLocation(), DEATH_ICONS, centerX, yaw, edgePadding, screenWidth,
                    anyOverlap(deathX, beforeD), false);
            }

        } else if (isNether) {
            // Order: nether, lodestone, anchor, death
            Integer[] afterN = {lodestoneX, anchorX, deathX};
            drawPOI(drawContext, client, POIData.getNetherPortalLocation(true), NETHER_PORTAL_ICONS, centerX, yaw, edgePadding, screenWidth,
                false, anyOverlap(netherX, afterN));

            if (lodestoneX != null) {
                Integer[] beforeL = {netherX};
                Integer[] afterL = {anchorX, deathX};
                drawPOI(drawContext, client, POIData.getLodestoneLocation(), LODESTONE_ICONS, centerX, yaw, edgePadding, screenWidth,
                    anyOverlap(lodestoneX, beforeL), anyOverlap(lodestoneX, afterL));
            }

            Integer[] beforeA = {netherX, lodestoneX};
            Integer[] afterA = {deathX};
            drawPOI(drawContext, client, POIData.getRespawnAnchorLocation(), RESPAWN_ANCHOR_ICONS, centerX, yaw, edgePadding, screenWidth,
                anyOverlap(anchorX, beforeA), anyOverlap(anchorX, afterA));

            if (deathX != null) {
                Integer[] beforeD = {netherX, lodestoneX, anchorX};
                drawPOI(drawContext, client, POIData.getDeathLocation(), DEATH_ICONS, centerX, yaw, edgePadding, screenWidth,
                    anyOverlap(deathX, beforeD), false);
            }

        } else if (isEnd) {
            // Order: end, gateway, lodestone, death
            Integer[] afterE = {gatewayX, lodestoneX, deathX};
            drawPOI(drawContext, client, POIData.getEndPortalLocation(true), END_PORTAL_ICONS, centerX, yaw, edgePadding, screenWidth,
                false, anyOverlap(endX, afterE));

            Integer[] beforeG = {endX};
            Integer[] afterG = {lodestoneX, deathX};
            drawPOI(drawContext, client, POIData.getEndGatewayLocation(), END_GATEWAY_ICONS, centerX, yaw, edgePadding, screenWidth,
                anyOverlap(gatewayX, beforeG), anyOverlap(gatewayX, afterG));

            if (lodestoneX != null) {
                Integer[] beforeL = {endX, gatewayX};
                Integer[] afterL = {deathX};
                drawPOI(drawContext, client, POIData.getLodestoneLocation(), LODESTONE_ICONS, centerX, yaw, edgePadding, screenWidth,
                    anyOverlap(lodestoneX, beforeL), anyOverlap(lodestoneX, afterL));
            }

            if (deathX != null) {
                Integer[] beforeD = {endX, gatewayX, lodestoneX};
                drawPOI(drawContext, client, POIData.getDeathLocation(), DEATH_ICONS, centerX, yaw, edgePadding, screenWidth,
                    anyOverlap(deathX, beforeD), false);
            }
        }
    }

    /** Check if pos overlaps with any position in the array */
    private static boolean anyOverlap(Integer pos, Integer[] others) {
        if (pos == null) return false;
        for (Integer other : others) {
            if (other != null && Math.abs(pos - other) < OVERLAP_THRESHOLD) return true;
        }
        return false;
    }

    /** Wrap angle difference to -180..180 range */
    private static float wrapAngle(float angle) {
        if (angle > 180) return angle - 360;
        if (angle < -180) return angle + 360;
        return angle;
    }

    /** Calculate POI X position (null if not visible) */
    private static Integer calcPOIX(MinecraftClient client, BlockPos pos, int centerX, float yaw, int edgePadding, int screenWidth) {
        if (pos == null) return null;

        double dx = pos.getX() - client.player.getX();
        double dz = pos.getZ() - client.player.getZ();
        float angleDiff = wrapAngle((float)(((Math.toDegrees(Math.atan2(-dx, dz)) % 360) + 360) % 360 - yaw));

        if (Math.abs(angleDiff) > MAX_VISIBLE_ANGLE) return null;

        int x = centerX + (int)(angleDiff * SPACING / 45.0);
        return (x > edgePadding + 5 && x < screenWidth - edgePadding - 5) ? x : null;
    }

    /** Draw a POI icon with overlap-based stacking */
    private static void drawPOI(DrawContext drawContext, MinecraftClient client, BlockPos pos, Identifier[] icons,
                                int centerX, float yaw, int edgePadding, int screenWidth,
                                boolean overlapsBefore, boolean overlapsAfter) {
        if (pos == null) return;

        double dx = pos.getX() - client.player.getX();
        double dz = pos.getZ() - client.player.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        float angleDiff = wrapAngle((float)(((Math.toDegrees(Math.atan2(-dx, dz)) % 360) + 360) % 360 - yaw));

        if (Math.abs(angleDiff) > MAX_VISIBLE_ANGLE) return;

        int x = centerX + (int)(angleDiff * SPACING / 45.0);
        if (x <= edgePadding + 5 || x >= screenWidth - edgePadding - 5) return;

        // Select icon variant (distant if >500 blocks or near edge)
        Identifier icon = (distance > 500.0 || Math.abs(angleDiff) > 75) ? icons[1] : icons[0];

        // Calculate offset based on overlap position in stack
        int xOff = 0, yOff = 0;
        boolean isOverlapping = overlapsBefore || overlapsAfter;
        if (overlapsBefore && !overlapsAfter) {
            xOff = -2; yOff = 2;  // Front
        } else if (overlapsAfter && !overlapsBefore) {
            xOff = 2; yOff = -2;  // Back
        }
        // Middle (both) = no offset but still scales

        int drawX = x - 8 + xOff;
        int drawY = BAR_Y - 19 + yOff;

        if (isOverlapping) {
            drawContext.getMatrices().push();
            float iconCenterX = x + xOff;
            float iconCenterY = BAR_Y - 19 + yOff + 8.5f;
            drawContext.getMatrices().translate(iconCenterX, iconCenterY, 0);
            drawContext.getMatrices().scale(0.9f, 0.9f, 1.0f);
            drawContext.getMatrices().translate(-iconCenterX, -iconCenterY, 0);
            drawContext.drawTexture(icon, drawX, drawY, 0, 0, 17, 17, 17, 17);
            drawContext.getMatrices().pop();
        } else {
            drawContext.drawTexture(icon, drawX, drawY, 0, 0, 17, 17, 17, 17);
        }
    }
}
