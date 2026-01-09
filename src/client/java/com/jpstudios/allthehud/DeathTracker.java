package com.jpstudios.allthehud;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

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
            
            if (wasAlive && !isAlive) {
                // Player just died - record location
                BlockPos deathPos = client.player.getBlockPos();
                POIData.setDeathLocation(deathPos);
                
                // Get dimension name
                String dimension = "Unknown";
                if (client.world != null) {
                    String dimKey = client.world.getRegistryKey().getValue().toString();
                    // Format: "minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"
                    if (dimKey.contains("overworld")) {
                        dimension = "Overworld";
                    } else if (dimKey.contains("nether")) {
                        dimension = "Nether";
                    } else if (dimKey.contains("end")) {
                        dimension = "End";
                    } else {
                        // For custom dimensions, use the full key
                        dimension = dimKey;
                    }
                }
                
                // Send death message in grey
                client.player.sendMessage(
                    net.minecraft.text.Text.literal(String.format(
                        "§7Your death has been added to your compass bar at x%d y%d z%d in the %s.",
                        deathPos.getX(),
                        deathPos.getY(),
                        deathPos.getZ(),
                        dimension
                    )),
                    false
                );
            }
            
            wasAlive = isAlive;
            
            // Check if player is near death location and clear if so
            if (isAlive && POIData.hasDeathLocation()) {
                POIData.checkAndClearDeathLocation(client.player.getBlockPos());
            }
        });
    }
}
