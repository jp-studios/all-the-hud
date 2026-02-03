package com.jpstudios.allthehud;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraft.registry.RegistryKey;

public class LodestoneTracker {
    private static final int MESSAGE_DELAY_TICKS = 15;
    private static final int CHECK_INTERVAL = 20; // Check every 20 ticks (1 second)

    private static BlockPos trackedPos = null;
    private static RegistryKey<World> trackedDimension = null;
    private static boolean trackedBothHands = false;
    private static int holdTicks = 0;
    private static boolean messageSent = false;
    private static int tickCounter = 0;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Throttle inventory scanning to reduce overhead on large modpacks
            if (++tickCounter % CHECK_INTERVAL != 0) return;

            if (client.player == null || client.world == null) return;

            // Get lodestone data from both hands (off-hand priority)
            LodestoneData offHand = getLodestoneData(client.player.getOffHandStack());
            LodestoneData mainHand = getLodestoneData(client.player.getMainHandStack());
            LodestoneData active = offHand != null ? offHand : mainHand;
            boolean bothHands = offHand != null && mainHand != null;

            // Not holding any lodestone compass - clear and reset
            if (active == null) {
                if (trackedPos != null) {
                    POIData.clearLodestoneLocation();
                    trackedPos = null;
                    trackedDimension = null;
                }
                trackedBothHands = false;
                holdTicks = 0;
                messageSent = false;
                return;
            }

            // Reset if lodestone changed or added second compass (to show offhand priority message)
            boolean posChanged = !active.pos.equals(trackedPos) || !active.dimension.equals(trackedDimension);
            boolean addedSecondCompass = bothHands && !trackedBothHands;

            if (posChanged || addedSecondCompass) {
                trackedPos = active.pos;
                trackedDimension = active.dimension;
                holdTicks = 0;
                messageSent = false;
            }
            trackedBothHands = bothHands;
            holdTicks += CHECK_INTERVAL; // Increment by interval since we're throttled

            // Send message after delay
            if (holdTicks >= MESSAGE_DELAY_TICKS && !messageSent) {
                messageSent = true;
                String dimName = active.dimension == World.OVERWORLD ? "Overworld" :
                                 active.dimension == World.NETHER ? "Nether" :
                                 active.dimension == World.END ? "End" : "Unknown";
                String msg = String.format("§7Lodestone in the %s at x%d y%d z%d.",
                    dimName, active.pos.getX(), active.pos.getY(), active.pos.getZ());
                if (bothHands) msg += " The compass in your off hand is shown.";

                client.player.sendMessage(Text.literal(msg), false);
                POIData.setLodestoneLocation(active.pos, active.dimension);
            }
        });
    }

    private static LodestoneData getLodestoneData(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != Items.COMPASS) return null;

        LodestoneTrackerComponent component = stack.get(DataComponentTypes.LODESTONE_TRACKER);
        if (component == null || !component.target().isPresent()) return null;

        GlobalPos globalPos = component.target().get();
        BlockPos pos = globalPos.pos();
        RegistryKey<World> dim = globalPos.dimension();

        return new LodestoneData(pos, dim);
    }

    private record LodestoneData(BlockPos pos, RegistryKey<World> dimension) {}
}
