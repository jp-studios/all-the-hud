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
        public int[] bedLocation, deathLocation, respawnAnchorLocation;
        public int[] netherPortalOverworldLocation, netherPortalNetherLocation;
        public int[] endPortalOverworldLocation, endPortalEndLocation, endGatewayLocation;
        public int[] lodestoneLocation;
        public String deathDimension, lodestoneDimension;
    }

    private static int[] posToArray(BlockPos pos) {
        if (pos == null) return null;
        return new int[] { pos.getX(), pos.getY(), pos.getZ() };
    }

    private static BlockPos arrayToPos(int[] arr) {
        if (arr == null || arr.length != 3) return null;
        return new BlockPos(arr[0], arr[1], arr[2]);
    }

    static String getWorldId() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return null;

        if (client.isInSingleplayer() && client.getServer() != null) {
            return "world_" + Long.toHexString(client.getServer().getOverworld().getSeed());
        } else if (client.getCurrentServerEntry() != null) {
            return client.getCurrentServerEntry().address.replace(':', '_').replace('.', '_');
        }
        return null;
    }

    private static File getSaveFile(String worldId) {
        if (configDir == null) {
            configDir = new File(MinecraftClient.getInstance().runDirectory, "config/allthehud");
            configDir.mkdirs();
        }
        return new File(configDir, "pois_" + worldId + ".json");
    }

    public static void save() {
        // Only save if world is initialized and matches what we expect
        if (currentWorldId == null) return;

        String actualWorldId = getWorldId();
        if (actualWorldId == null || !actualWorldId.equals(currentWorldId)) return;

        File saveFile = getSaveFile(currentWorldId);

        SavedPOIData data = new SavedPOIData();
        data.bedLocation = posToArray(POIData.getBedLocation());
        data.deathLocation = posToArray(POIData.getDeathLocation());
        data.deathDimension = POIData.getDeathDimension() != null ? POIData.getDeathDimension().getValue().toString() : null;
        data.respawnAnchorLocation = posToArray(POIData.getRespawnAnchorLocation());
        data.netherPortalOverworldLocation = posToArray(POIData.getNetherPortalOverworldLocation());
        data.netherPortalNetherLocation = posToArray(POIData.getNetherPortalNetherLocation());
        data.endPortalOverworldLocation = posToArray(POIData.getEndPortalOverworldLocation());
        data.endPortalEndLocation = posToArray(POIData.getEndPortalEndLocation());
        data.endGatewayLocation = posToArray(POIData.getEndGatewayLocation());
        data.lodestoneLocation = posToArray(POIData.getLodestoneLocation());
        data.lodestoneDimension = POIData.getLodestoneDimension() != null ? POIData.getLodestoneDimension().getValue().toString() : null;

        try (FileWriter writer = new FileWriter(saveFile)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            AllTheHUD.LOGGER.error("Failed to save POI data", e);
        }
    }

    public static void load() {
        if (currentWorldId == null) return;

        File saveFile = getSaveFile(currentWorldId);
        if (!saveFile.exists()) return;

        try (FileReader reader = new FileReader(saveFile)) {
            SavedPOIData data = GSON.fromJson(reader, SavedPOIData.class);
            if (data == null) return;

            BlockPos bed = arrayToPos(data.bedLocation);
            BlockPos death = arrayToPos(data.deathLocation);
            BlockPos anchor = arrayToPos(data.respawnAnchorLocation);
            BlockPos npOverworld = arrayToPos(data.netherPortalOverworldLocation);
            BlockPos npNether = arrayToPos(data.netherPortalNetherLocation);
            BlockPos epOverworld = arrayToPos(data.endPortalOverworldLocation);
            BlockPos epEnd = arrayToPos(data.endPortalEndLocation);
            BlockPos gateway = arrayToPos(data.endGatewayLocation);
            BlockPos lodestone = arrayToPos(data.lodestoneLocation);

            if (bed != null) POIData.setBedLocationSilent(bed);
            if (death != null) POIData.setDeathLocationSilent(death, data.deathDimension);
            if (anchor != null) POIData.setRespawnAnchorLocationSilent(anchor);
            if (npOverworld != null) POIData.setNetherPortalOverworldLocationSilent(npOverworld);
            if (npNether != null) POIData.setNetherPortalNetherLocationSilent(npNether);
            if (epOverworld != null) POIData.setEndPortalOverworldLocationSilent(epOverworld);
            if (epEnd != null) POIData.setEndPortalEndLocationSilent(epEnd);
            if (gateway != null) POIData.setEndGatewayLocationSilent(gateway);
            if (lodestone != null) POIData.setLodestoneLocationSilent(lodestone, data.lodestoneDimension);
        } catch (IOException e) {
            AllTheHUD.LOGGER.error("Failed to load POI data", e);
        }
    }

    public static void onWorldChange() {
        String newWorldId = getWorldId();
        if (newWorldId == null) return;

        if (!newWorldId.equals(currentWorldId)) {
            currentWorldId = newWorldId;
            POIData.clearAllSilent();
            PortalTracker.resetState();
            load();
        }
    }
}
