package com.jpstudios.allthehud;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DeathTracker {
    private static boolean wasAlive = true;
    private static int tickCounter = 0;
    private static final int CHECK_INTERVAL = 10; // Check every 10 ticks (0.5 seconds)

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Throttle to reduce overhead on large modpacks
            if (++tickCounter % CHECK_INTERVAL != 0) return;

            if (client.player == null) {
                wasAlive = true;
                return;
            }

            boolean isAlive = client.player.isAlive();

            // Player just died - record location
            if (wasAlive && !isAlive && client.world != null) {
                BlockPos deathPos = client.player.getBlockPos();
                var dimKey = client.world.getRegistryKey();
                String dimName = dimKey == World.OVERWORLD ? "Overworld" :
                                 dimKey == World.NETHER ? "Nether" :
                                 dimKey == World.END ? "End" : dimKey.getValue().toString();

                POIData.setDeathLocation(deathPos, dimKey);
                client.player.sendMessage(Text.literal(String.format(
                    "§7Your death has been added to your compass bar at x%d y%d z%d in the %s.",
                    deathPos.getX(), deathPos.getY(), deathPos.getZ(), dimName)), false);
            }

            wasAlive = isAlive;

            // Clear death marker if player returns to within 10 blocks
            if (isAlive && POIData.hasDeathLocation() && client.world != null) {
                var currentDim = client.world.getRegistryKey();
                if (currentDim != null && currentDim.equals(POIData.getDeathDimension())) {
                    POIData.checkAndClearDeathLocation(client.player.getBlockPos());
                }
            }
        });
    }
}
