package com.jpstudios.allthehud;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

public class LodestoneTracker {
    private static final int MESSAGE_DELAY_TICKS = 15;

    private static BlockPos trackedPos = null;
    private static RegistryKey<World> trackedDimension = null;
    private static boolean trackedBothHands = false;
    private static int holdTicks = 0;
    private static boolean messageSent = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
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
            holdTicks++;

            // Send message after delay
            if (holdTicks == MESSAGE_DELAY_TICKS && !messageSent) {
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

        // In 1.20.1, lodestone data is stored in NBT
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains("LodestonePos") || !nbt.contains("LodestoneDimension")) {
            return null;
        }

        // Check if lodestone is tracked (not broken)
        if (nbt.contains("LodestoneTracked") && !nbt.getBoolean("LodestoneTracked")) {
            return null;
        }

        // Read position from NBT
        BlockPos pos = NbtHelper.toBlockPos(nbt.getCompound("LodestonePos"));

        // Read dimension from NBT
        String dimString = nbt.getString("LodestoneDimension");
        Identifier dimId = Identifier.tryParse(dimString);
        if (dimId == null) return null;

        RegistryKey<World> dim = RegistryKey.of(RegistryKeys.WORLD, dimId);

        return new LodestoneData(pos, dim);
    }

    private record LodestoneData(BlockPos pos, RegistryKey<World> dimension) {}
}
