package com.jpstudios.allthehud;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.registry.RegistryKey;

public class PortalTracker {
    private static RegistryKey<World> lastDimension = null;
    private static BlockPos lastPos = null;
    private static int ticksSinceChange = 0;
    private static boolean needsNetherUpdate = false;
    private static boolean needsOverworldUpdate = false;
    private static boolean needsGatewayUpdate = false;
    private static boolean wasAlive = true;

    public static void resetState() {
        lastDimension = null;
        lastPos = null;
        ticksSinceChange = 0;
        needsNetherUpdate = false;
        needsOverworldUpdate = false;
        needsGatewayUpdate = false;
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            RegistryKey<World> currentDim = client.world.getRegistryKey();
            BlockPos currentPos = client.player.getBlockPos();
            boolean isAlive = client.player.isAlive();
            boolean justRespawned = !wasAlive && isAlive;

            // Detect dimension change (skip if respawning)
            if (lastDimension != null && !lastDimension.equals(currentDim) && !justRespawned) {
                ticksSinceChange = 0;

                if (lastDimension == World.OVERWORLD && currentDim == World.NETHER && lastPos != null) {
                    POIData.setNetherPortalOverworldLocation(lastPos);
                    needsNetherUpdate = true;
                } else if (lastDimension == World.NETHER && currentDim == World.OVERWORLD && lastPos != null) {
                    POIData.setNetherPortalNetherLocation(lastPos);
                    needsOverworldUpdate = true;
                } else if (lastDimension == World.OVERWORLD && currentDim == World.END && lastPos != null) {
                    POIData.setEndPortalOverworldLocation(lastPos);
                    client.player.sendMessage(Text.literal("§aEnd portal added to your compass bar."), false);
                } else if (lastDimension == World.END && currentDim == World.OVERWORLD && lastPos != null) {
                    if (POIData.getEndPortalEndLocation() == null) {
                        POIData.setEndPortalEndLocation(lastPos);
                    }
                }
            }

            // Detect end gateway teleport (>100 blocks in one tick within the end)
            if (currentDim == World.END && lastDimension == World.END && lastPos != null) {
                if (Math.sqrt(currentPos.getSquaredDistance(lastPos)) > 100) {
                    needsGatewayUpdate = true;
                    ticksSinceChange = 0;
                }
            }

            // Update positions after teleport stabilizes
            int netherWait = (needsNetherUpdate && POIData.getNetherPortalNetherLocation() == null) ? 20 : 10;

            if (needsNetherUpdate && currentDim == World.NETHER && ticksSinceChange == netherWait) {
                POIData.setNetherPortalNetherLocation(currentPos);
                client.player.sendMessage(Text.literal("§dNether portal added to your compass bar."), false);
                needsNetherUpdate = false;
            }
            if (ticksSinceChange == 10) {
                if (needsOverworldUpdate && currentDim == World.OVERWORLD) {
                    POIData.setNetherPortalOverworldLocation(currentPos);
                    client.player.sendMessage(Text.literal("§dNether portal added to your compass bar."), false);
                    needsOverworldUpdate = false;
                }
                if (needsGatewayUpdate && currentDim == World.END) {
                    POIData.setEndGatewayLocation(currentPos);
                    client.player.sendMessage(Text.literal("§eEnd gateway added to your compass bar."), false);
                    needsGatewayUpdate = false;
                }
            }

            lastPos = currentPos;
            lastDimension = currentDim;
            wasAlive = isAlive;
            if (ticksSinceChange < 25) ticksSinceChange++;
        });
    }
}
