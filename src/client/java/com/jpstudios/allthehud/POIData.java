package com.jpstudios.allthehud;

import net.minecraft.util.math.BlockPos;

public class POIData {
    private static BlockPos lastDeathLocation = null;
    private static BlockPos bedLocation = null;
    
    /**
     * Set the location where the player last died
     */
    public static void setDeathLocation(BlockPos pos) {
        lastDeathLocation = pos;
        AllTheHUD.LOGGER.info("Death location recorded at: {}", pos);
        POIStorage.save(); // Auto-save when death location changes
    }
    
    /**
     * Set death location without triggering a save (used when loading from file)
     */
    public static void setDeathLocationSilent(BlockPos pos) {
        lastDeathLocation = pos;
    }
    
    /**
     * Get the last death location, or null if none recorded
     */
    public static BlockPos getDeathLocation() {
        return lastDeathLocation;
    }
    
    /**
     * Check if player is within range of death location and clear it if so
     * @param playerPos Current player position
     * @param range Distance in blocks (checks cubic area)
     * @return true if death location was cleared
     */
    public static boolean checkAndClearDeathLocation(BlockPos playerPos) {
        if (lastDeathLocation == null) {
            return false;
        }
        
        // Calculate 3D distance (cubic check)
        int dx = Math.abs(playerPos.getX() - lastDeathLocation.getX());
        int dy = Math.abs(playerPos.getY() - lastDeathLocation.getY());
        int dz = Math.abs(playerPos.getZ() - lastDeathLocation.getZ());
        
        // Check if within 10 block cubic radius
        if (dx <= 10 && dy <= 10 && dz <= 10) {
            AllTheHUD.LOGGER.info("Player reached death location, clearing marker");
            lastDeathLocation = null;
            POIStorage.save(); // Auto-save when death location cleared by proximity
            return true;
        }
        
        return false;
    }
    
    /**
     * Clear the death location marker
     */
    public static void clearDeathLocation() {
        lastDeathLocation = null;
        POIStorage.save(); // Auto-save when death location cleared
    }
    
    /**
     * Check if there is a death location recorded
     */
    public static boolean hasDeathLocation() {
        return lastDeathLocation != null;
    }
    
    // ===== BED LOCATION TRACKING =====
    
    /**
     * Set the player's bed location
     */
    public static void setBedLocation(BlockPos pos) {
        bedLocation = pos;
        AllTheHUD.LOGGER.info("Bed location set at: {}", pos);
        POIStorage.save(); // Auto-save when bed location changes
    }
    
    /**
     * Set bed location without triggering a save (used when loading from file)
     */
    public static void setBedLocationSilent(BlockPos pos) {
        bedLocation = pos;
    }
    
    /**
     * Get the bed location, or null if none set
     */
    public static BlockPos getBedLocation() {
        return bedLocation;
    }
    
    /**
     * Check if there is a bed location recorded
     */
    public static boolean hasBedLocation() {
        return bedLocation != null;
    }
    
    /**
     * Clear the bed location marker
     */
    public static void clearBedLocation() {
        bedLocation = null;
        POIStorage.save(); // Auto-save when bed location cleared
    }
    
    /**
     * Clear all POI data without saving (used when switching worlds)
     */
    public static void clearAllSilent() {
        lastDeathLocation = null;
        bedLocation = null;
    }
}
