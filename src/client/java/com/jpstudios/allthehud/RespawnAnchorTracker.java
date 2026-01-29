package com.jpstudios.allthehud;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RespawnAnchorTracker {

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            // Only track on client side, in nether
            if (world.isClient() && world.getRegistryKey() == World.NETHER) {
                BlockPos pos = hitResult.getBlockPos();
                BlockState blockState = world.getBlockState(pos);

                // Check if the block is a respawn anchor
                if (blockState.getBlock() == Blocks.RESPAWN_ANCHOR) {
                    int charges = blockState.get(RespawnAnchorBlock.CHARGES);
                    boolean holdingGlowstone = player.getStackInHand(hand).getItem() == Items.GLOWSTONE;

                    // Spawn is set when: anchor is full (4 charges) OR has charges and player NOT holding glowstone
                    // (clicking with glowstone adds charge unless full, clicking without sets spawn)
                    if (charges == 4 || (charges > 0 && !holdingGlowstone)) {
                        BlockPos currentAnchor = POIData.getRespawnAnchorLocation();

                        // Only update and notify if it's a different location
                        if (currentAnchor == null || !currentAnchor.equals(pos)) {
                            POIData.setRespawnAnchorLocation(pos);
                            // Clear bed location since respawn anchor takes priority in nether
                            POIData.clearBedLocation();
                            player.sendMessage(Text.literal("§dRespawn anchor added to your compass bar."), false);
                        }
                    }
                }
            }
            return ActionResult.PASS;
        });
    }
}
