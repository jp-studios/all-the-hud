package com.jpstudios.allthehud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class POIStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File configDir;
    private static String currentWorldId = null;
    
    public static class SavedPOIData {
        public BlockPos bedLocation;
        public BlockPos deathLocation;
    }
    
    /**
     * Get a unique identifier for the current world
     */
    private static String getWorldId() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        
        if (world == null) {
            return "unknown";
        }
        
        // Use dimension name
        String dimension = world.getRegistryKey().getValue().toString().replace(':', '_').replace('/', '_');
        
        // For singleplayer, use world folder name (unique); for multiplayer, use server address
        String worldName;
        if (client.isInSingleplayer() && client.getServer() != null) {
            // Use the actual folder name (session directory name) which is unique
            // Example: "New World" becomes "New_World-1234567890" based on creation timestamp
            worldName = client.getServer().getSaveProperties().getLevelName();
            
            // Add world seed for additional uniqueness in case of same-named worlds
            long seed = client.getServer().getOverworld().getSeed();
            worldName = worldName.replace(' ', '_') + "_" + Long.toHexString(seed).substring(0, 8);
        } else if (client.getCurrentServerEntry() != null) {
            // Multiplayer - use server address
            worldName = client.getCurrentServerEntry().address.replace(':', '_').replace('.', '_');
        } else {
            worldName = "unknown";
        }
        
        return worldName + "_" + dimension;
    }
    
    /**
     * Get the save file for the current world
     */
    private static File getSaveFile() {
        if (configDir == null) {
            MinecraftClient client = MinecraftClient.getInstance();
            File gameDir = client.runDirectory;
            configDir = new File(gameDir, "config/allthehud");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
        }
        
        String worldId = getWorldId();
        return new File(configDir, "pois_" + worldId + ".json");
    }
    
    /**
     * Save current POI data to file
     */
    public static void save() {
        File saveFile = getSaveFile();
        
        SavedPOIData data = new SavedPOIData();
        data.bedLocation = POIData.getBedLocation();
        data.deathLocation = POIData.getDeathLocation();
        
        try (FileWriter writer = new FileWriter(saveFile)) {
            GSON.toJson(data, writer);
            AllTheHUD.LOGGER.debug("Saved POI data to {}", saveFile.getAbsolutePath());
        } catch (IOException e) {
            AllTheHUD.LOGGER.error("Failed to save POI data", e);
        }
    }
    
    /**
     * Load POI data from file for the current world
     */
    public static void load() {
        File saveFile = getSaveFile();
        
        if (!saveFile.exists()) {
            AllTheHUD.LOGGER.debug("No saved POI data found for this world");
            return;
        }
        
        try (FileReader reader = new FileReader(saveFile)) {
            SavedPOIData data = GSON.fromJson(reader, SavedPOIData.class);
            if (data != null) {
                if (data.bedLocation != null) {
                    POIData.setBedLocationSilent(data.bedLocation); // Silent to avoid re-saving
                }
                if (data.deathLocation != null) {
                    POIData.setDeathLocationSilent(data.deathLocation); // Silent to avoid re-saving
                }
                AllTheHUD.LOGGER.info("Loaded POI data from {}", saveFile.getAbsolutePath());
            }
        } catch (IOException e) {
            AllTheHUD.LOGGER.error("Failed to load POI data", e);
        }
    }
    
    /**
     * Clear POI data when switching worlds
     */
    public static void onWorldChange() {
        String newWorldId = getWorldId();
        if (!newWorldId.equals(currentWorldId)) {
            AllTheHUD.LOGGER.info("World changed, loading new POI data");
            currentWorldId = newWorldId;
            // Clear current data and load new world's data
            POIData.clearAllSilent();
            load();
        }
    }
}
