package com.jpstudios.allthehud;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DeathTracker {
    private static boolean wasAlive = true;

    public static void register() {
        // Check every tick if player died
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
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
                String dimName = client.world.getRegistryKey().getValue().toString();
                if (dimName.contains("overworld")) {
                    dimName = "Overworld";
                } else if (dimName.contains("nether")) {
                    dimName = "Nether";
                } else if (dimName.contains("end")) {
                    dimName = "End";
                }

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
