package com.jpstudios.allthehud;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BedBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class BedTracker {
    
    private static long lastProcessedTime = 0;
    
    public static void register() {
        // Detect when player right-clicks on a bed block
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            BlockPos bedPos = hitResult.getBlockPos();
            
            // Check if the block is a bed
            if (world.getBlockState(bedPos).getBlock() instanceof BedBlock) {
                // Prevent duplicate processing - only allow once per second
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastProcessedTime < 1000) {
                    return ActionResult.PASS; // Skip duplicate
                }
                lastProcessedTime = currentTime;
                
                // Get bed properties
                BedPart bedPart = world.getBlockState(bedPos).get(BedBlock.PART);
                Direction bedFacing = world.getBlockState(bedPos).get(BedBlock.FACING);
                
                // Always use the FOOT of the bed as the reference point
                BlockPos bedFoot = bedPart == BedPart.FOOT ? bedPos : bedPos.offset(bedFacing.getOpposite());
                
                // Get player position
                Vec3d playerPos = player.getPos();
                
                // Calculate relative position to bed foot
                double relX = playerPos.x - (bedFoot.getX() + 0.5);
                double relY = playerPos.y - bedFoot.getY();
                double relZ = playerPos.z - (bedFoot.getZ() + 0.5);
                
                // Rotate coordinates based on bed facing direction
                // Bed faces positive Z (south) by default, so we rotate relative coordinates
                double rotatedX, rotatedZ;
                switch (bedFacing) {
                    case SOUTH: // Default, no rotation needed
                        rotatedX = relX;
                        rotatedZ = relZ;
                        break;
                    case WEST: // Rotate 90° clockwise
                        rotatedX = relZ;
                        rotatedZ = -relX;
                        break;
                    case NORTH: // Rotate 180°
                        rotatedX = -relX;
                        rotatedZ = -relZ;
                        break;
                    case EAST: // Rotate 270° clockwise (90° counter-clockwise)
                        rotatedX = -relZ;
                        rotatedZ = relX;
                        break;
                    default:
                        rotatedX = relX;
                        rotatedZ = relZ;
                }
                
                // Check if within rectangular prism (relative to bed foot, facing south)
                // x: -2.5 to 2.5, y: -2.0 to 2.0, z: -2.5 to 3.5
                boolean withinPrism = 
                    rotatedX >= -2.5 && rotatedX <= 2.5 &&
                    relY >= -2.0 && relY <= 2.0 &&
                    rotatedZ >= -2.5 && rotatedZ <= 3.5;
                
                if (withinPrism) {
                    POIData.setBedLocation(bedFoot);
                    // Send success message in green
                    player.sendMessage(
                        Text.literal("§aYour home has been added to your compass bar."),
                        false
                    );
                    AllTheHUD.LOGGER.info("Player interacted with bed at: {} (rotX: {}, Y: {}, rotZ: {})", 
                        bedFoot, 
                        String.format("%.2f", rotatedX), 
                        String.format("%.2f", relY), 
                        String.format("%.2f", rotatedZ));
                } else {
                    // Send warning message to player
                    player.sendMessage(
                        Text.literal("§cCompass bed spawn location not updated. Please move closer and click the bed again."),
                        false  // Not an action bar message
                    );
                    AllTheHUD.LOGGER.debug("Bed interaction rejected - outside valid area (rotX: {}, Y: {}, rotZ: {})", 
                        String.format("%.2f", rotatedX), 
                        String.format("%.2f", relY), 
                        String.format("%.2f", rotatedZ));
                }
            }
            
            return ActionResult.PASS; // Don't interfere with normal bed interaction
        });
    }
}
