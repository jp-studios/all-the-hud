package com.jpstudios.allthehud.mixin.client;

import com.jpstudios.allthehud.POIData;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BedPart;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class POIBlockBreakMixin {
    @Shadow
    private MinecraftClient client;

    @Inject(at = @At("RETURN"), method = "breakBlock")
    private void onBlockBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        // Only process if block was successfully broken
        if (!cir.getReturnValue() || client.world == null) return;

        // Check bed location (handle both head and foot parts)
        BlockPos bedPos = POIData.getBedLocation();
        if (bedPos != null) {
            // Check if broken block is the stored foot position
            if (bedPos.equals(pos)) {
                POIData.clearBedLocation();
            } else {
                // Check if broken block is the head part of the stored bed
                // We need to check all 4 directions since we don't know the bed's facing
                for (Direction dir : Direction.Type.HORIZONTAL) {
                    if (bedPos.offset(dir).equals(pos)) {
                        POIData.clearBedLocation();
                        break;
                    }
                }
            }
        }

        // Check respawn anchor location
        BlockPos anchorPos = POIData.getRespawnAnchorLocation();
        if (anchorPos != null && anchorPos.equals(pos)) {
            POIData.clearRespawnAnchorLocation();
        }

        // Check lodestone location
        BlockPos lodestonePos = POIData.getLodestoneLocation();
        if (lodestonePos != null && lodestonePos.equals(pos)) {
            POIData.clearLodestoneLocation();
        }
    }
}
