package com.jpstudios.allthehud;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DeathTracker {
    private static final int CHECK_INTERVAL = 10; // Check every 10 ticks (0.5 seconds)

    private static boolean wasAlive = true;
    private static int tickCounter = 0;

    public static void register() {
        // Check periodically if player died
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Throttle checks to reduce overhead
            if (++tickCounter % CHECK_INTERVAL != 0) return;
            if (client.player == null) {
                wasAlive = true;
                return;
            }

            // Check if player just died (was alive, now dead)
            boolean isAlive = client.player.isAlive();

            if (wasAlive && !isAlive && client.world != null) {
                // Player just died - record location
                BlockPos deathPos = client.player.getBlockPos();
                var dimKey = client.world.getRegistryKey();
                POIData.setDeathLocation(deathPos, dimKey);

                // Get dimension name for message
                String dimName = dimKey == World.OVERWORLD ? "Overworld" :
                                 dimKey == World.NETHER ? "Nether" :
                                 dimKey == World.END ? "End" : dimKey.getValue().toString();

                // Send death message in grey
                client.player.sendMessage(
                    net.minecraft.text.Text.literal(String.format(
                        "§7Your death has been added to your compass bar at x%d y%d z%d in the %s.",
                        deathPos.getX(),
                        deathPos.getY(),
                        deathPos.getZ(),
                        dimName
                    )),
                    false
                );
            }

            wasAlive = isAlive;

            // Check if player is near death location and clear if so
            if (isAlive && POIData.hasDeathLocation() && client.world != null) {
                var currentDim = client.world.getRegistryKey();
                if (currentDim != null && currentDim.equals(POIData.getDeathDimension())) {
                    POIData.checkAndClearDeathLocation(client.player.getBlockPos());
                }
            }
        });
    }
}
