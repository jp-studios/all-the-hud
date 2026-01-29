package com.jpstudios.allthehud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.io.*;

public class POIStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File configDir;
    private static String currentWorldId = null;

    public static class SavedPOIData {
        public BlockPos bedLocation, deathLocation, respawnAnchorLocation;
        public BlockPos netherPortalOverworldLocation, netherPortalNetherLocation;
        public BlockPos endPortalOverworldLocation, endPortalEndLocation, endGatewayLocation;
        public BlockPos lodestoneLocation;
        public String deathDimension, lodestoneDimension;
    }

    private static String getWorldId() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return "unknown";

        if (client.isInSingleplayer() && client.getServer() != null) {
            return "world_" + Long.toHexString(client.getServer().getOverworld().getSeed());
        } else if (client.getCurrentServerEntry() != null) {
            return client.getCurrentServerEntry().address.replace(':', '_').replace('.', '_');
        }
        return "unknown";
    }

    private static File getSaveFile() {
        if (configDir == null) {
            configDir = new File(MinecraftClient.getInstance().runDirectory, "config/allthehud");
            configDir.mkdirs();
        }
        return new File(configDir, "pois_" + getWorldId() + ".json");
    }

    public static void save() {
        SavedPOIData data = new SavedPOIData();
        data.bedLocation = POIData.getBedLocation();
        data.deathLocation = POIData.getDeathLocation();
        data.deathDimension = POIData.getDeathDimension() != null ? POIData.getDeathDimension().getValue().toString() : null;
        data.respawnAnchorLocation = POIData.getRespawnAnchorLocation();
        data.netherPortalOverworldLocation = POIData.getNetherPortalOverworldLocation();
        data.netherPortalNetherLocation = POIData.getNetherPortalNetherLocation();
        data.endPortalOverworldLocation = POIData.getEndPortalOverworldLocation();
        data.endPortalEndLocation = POIData.getEndPortalEndLocation();
        data.endGatewayLocation = POIData.getEndGatewayLocation();
        data.lodestoneLocation = POIData.getLodestoneLocation();
        data.lodestoneDimension = POIData.getLodestoneDimension() != null ? POIData.getLodestoneDimension().getValue().toString() : null;

        try (FileWriter writer = new FileWriter(getSaveFile())) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            AllTheHUD.LOGGER.error("Failed to save POI data", e);
        }
    }

    public static void load() {
        File saveFile = getSaveFile();
        if (!saveFile.exists()) return;

        try (FileReader reader = new FileReader(saveFile)) {
            SavedPOIData data = GSON.fromJson(reader, SavedPOIData.class);
            if (data == null) return;

            if (data.bedLocation != null) POIData.setBedLocationSilent(data.bedLocation);
            if (data.deathLocation != null) POIData.setDeathLocationSilent(data.deathLocation, data.deathDimension);
            if (data.respawnAnchorLocation != null) POIData.setRespawnAnchorLocationSilent(data.respawnAnchorLocation);
            if (data.netherPortalOverworldLocation != null) POIData.setNetherPortalOverworldLocationSilent(data.netherPortalOverworldLocation);
            if (data.netherPortalNetherLocation != null) POIData.setNetherPortalNetherLocationSilent(data.netherPortalNetherLocation);
            if (data.endPortalOverworldLocation != null) POIData.setEndPortalOverworldLocationSilent(data.endPortalOverworldLocation);
            if (data.endPortalEndLocation != null) POIData.setEndPortalEndLocationSilent(data.endPortalEndLocation);
            if (data.endGatewayLocation != null) POIData.setEndGatewayLocationSilent(data.endGatewayLocation);
            if (data.lodestoneLocation != null) POIData.setLodestoneLocationSilent(data.lodestoneLocation, data.lodestoneDimension);
        } catch (IOException e) {
            AllTheHUD.LOGGER.error("Failed to load POI data", e);
        }
    }

    public static void onWorldChange() {
        String newWorldId = getWorldId();
        if (!newWorldId.equals(currentWorldId)) {
            currentWorldId = newWorldId;
            POIData.clearAllSilent();
            load();
        }
    }
}
