package com.jpstudios.allthehud;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BedBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BedTracker {
    private static long lastProcessedTime = 0;

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.getRegistryKey() != World.OVERWORLD) return ActionResult.PASS;
            if (!(world.getBlockState(hitResult.getBlockPos()).getBlock() instanceof BedBlock)) return ActionResult.PASS;

            // Prevent duplicate processing (1 second cooldown)
            long now = System.currentTimeMillis();
            if (now - lastProcessedTime < 1000) return ActionResult.PASS;
            lastProcessedTime = now;

            BlockPos bedPos = hitResult.getBlockPos();
            var state = world.getBlockState(bedPos);
            Direction facing = state.get(BedBlock.FACING);

            // Always use bed foot as reference
            BlockPos bedFoot = state.get(BedBlock.PART) == BedPart.FOOT ? bedPos : bedPos.offset(facing.getOpposite());

            // Calculate relative position and rotate based on bed facing
            double relX = player.getX() - (bedFoot.getX() + 0.5);
            double relY = player.getY() - bedFoot.getY();
            double relZ = player.getZ() - (bedFoot.getZ() + 0.5);

            // Rotate coordinates: SOUTH=0, WEST=90, NORTH=180, EAST=270
            double rotatedX = facing == Direction.SOUTH ? relX : facing == Direction.NORTH ? -relX :
                              facing == Direction.WEST ? relZ : -relZ;
            double rotatedZ = facing == Direction.SOUTH ? relZ : facing == Direction.NORTH ? -relZ :
                              facing == Direction.WEST ? -relX : relX;

            // Check bounds: x: -2.5 to 2.5, y: -2.0 to 2.0, z: -2.5 to 3.5
            boolean withinPrism = rotatedX >= -2.5 && rotatedX <= 2.5 &&
                                  relY >= -2.0 && relY <= 2.0 &&
                                  rotatedZ >= -2.5 && rotatedZ <= 3.5;

            if (withinPrism) {
                POIData.setBedLocation(bedFoot);
                POIData.clearRespawnAnchorLocation();
                player.sendMessage(Text.literal("§aYour home has been added to your compass bar."), false);
            } else {
                player.sendMessage(Text.literal("§cCompass bed spawn location not updated. Please move closer and click the bed again."), false);
            }

            return ActionResult.PASS;
        });
    }
}
