package com.jpstudios.allthehud;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.registry.RegistryKey;

public class POIData {
    // Death tracking
    private static BlockPos deathLocation = null;
    private static RegistryKey<World> deathDimension = null;

    // Respawn tracking
    private static BlockPos bedLocation = null;
    private static BlockPos respawnAnchorLocation = null;

    // Portal tracking
    private static BlockPos netherPortalOverworld = null;
    private static BlockPos netherPortalNether = null;
    private static BlockPos endPortalOverworld = null;
    private static BlockPos endPortalEnd = null;
    private static BlockPos endGateway = null;

    // Lodestone tracking
    private static BlockPos lodestoneLocation = null;
    private static RegistryKey<World> lodestoneDimension = null;

    // ===== HELPER =====

    private static RegistryKey<World> parseDimension(String s) {
        if (s == null) return null;
        return s.equals("minecraft:overworld") ? World.OVERWORLD :
               s.equals("minecraft:the_nether") ? World.NETHER :
               s.equals("minecraft:the_end") ? World.END : null;
    }

    // ===== DEATH =====

    public static void setDeathLocation(BlockPos pos, RegistryKey<World> dim) {
        deathLocation = pos;
        deathDimension = dim;
        POIStorage.save();
    }

    public static void setDeathLocationSilent(BlockPos pos, String dimStr) {
        deathLocation = pos;
        deathDimension = parseDimension(dimStr);
    }

    public static BlockPos getDeathLocation() { return deathLocation; }
    public static RegistryKey<World> getDeathDimension() { return deathDimension; }
    public static boolean hasDeathLocation() { return deathLocation != null; }

    public static void clearDeathLocation() {
        deathLocation = null;
        deathDimension = null;
        POIStorage.save();
    }

    public static boolean checkAndClearDeathLocation(BlockPos playerPos) {
        if (deathLocation == null) return false;
        int dx = Math.abs(playerPos.getX() - deathLocation.getX());
        int dy = Math.abs(playerPos.getY() - deathLocation.getY());
        int dz = Math.abs(playerPos.getZ() - deathLocation.getZ());
        if (dx <= 10 && dy <= 10 && dz <= 10) {
            deathLocation = null;
            deathDimension = null;
            POIStorage.save();
            return true;
        }
        return false;
    }

    // ===== BED =====

    public static void setBedLocation(BlockPos pos) { bedLocation = pos; POIStorage.save(); }
    public static void setBedLocationSilent(BlockPos pos) { bedLocation = pos; }
    public static BlockPos getBedLocation() { return bedLocation; }
    public static boolean hasBedLocation() { return bedLocation != null; }
    public static void clearBedLocation() { bedLocation = null; POIStorage.save(); }

    // ===== RESPAWN ANCHOR =====

    public static void setRespawnAnchorLocation(BlockPos pos) { respawnAnchorLocation = pos; POIStorage.save(); }
    public static void setRespawnAnchorLocationSilent(BlockPos pos) { respawnAnchorLocation = pos; }
    public static BlockPos getRespawnAnchorLocation() { return respawnAnchorLocation; }
    public static boolean hasRespawnAnchorLocation() { return respawnAnchorLocation != null; }
    public static void clearRespawnAnchorLocation() { respawnAnchorLocation = null; POIStorage.save(); }

    // ===== NETHER PORTAL =====

    public static void setNetherPortalOverworldLocation(BlockPos pos) { netherPortalOverworld = pos; POIStorage.save(); }
    public static void setNetherPortalOverworldLocationSilent(BlockPos pos) { netherPortalOverworld = pos; }
    public static BlockPos getNetherPortalOverworldLocation() { return netherPortalOverworld; }

    public static void setNetherPortalNetherLocation(BlockPos pos) { netherPortalNether = pos; POIStorage.save(); }
    public static void setNetherPortalNetherLocationSilent(BlockPos pos) { netherPortalNether = pos; }
    public static BlockPos getNetherPortalNetherLocation() { return netherPortalNether; }

    public static BlockPos getNetherPortalLocation(boolean inNether) {
        return inNether ? netherPortalNether : netherPortalOverworld;
    }
    public static boolean hasNetherPortalLocation(boolean inNether) {
        return inNether ? netherPortalNether != null : netherPortalOverworld != null;
    }

    // ===== END PORTAL =====

    public static void setEndPortalOverworldLocation(BlockPos pos) { endPortalOverworld = pos; POIStorage.save(); }
    public static void setEndPortalOverworldLocationSilent(BlockPos pos) { endPortalOverworld = pos; }
    public static BlockPos getEndPortalOverworldLocation() { return endPortalOverworld; }

    public static void setEndPortalEndLocation(BlockPos pos) { endPortalEnd = pos; POIStorage.save(); }
    public static void setEndPortalEndLocationSilent(BlockPos pos) { endPortalEnd = pos; }
    public static BlockPos getEndPortalEndLocation() { return endPortalEnd; }

    public static BlockPos getEndPortalLocation(boolean inEnd) {
        return inEnd ? endPortalEnd : endPortalOverworld;
    }
    public static boolean hasEndPortalLocation(boolean inEnd) {
        return inEnd ? endPortalEnd != null : endPortalOverworld != null;
    }

    // ===== END GATEWAY =====

    public static void setEndGatewayLocation(BlockPos pos) { endGateway = pos; POIStorage.save(); }
    public static void setEndGatewayLocationSilent(BlockPos pos) { endGateway = pos; }
    public static BlockPos getEndGatewayLocation() { return endGateway; }
    public static boolean hasEndGatewayLocation() { return endGateway != null; }

    // ===== LODESTONE =====

    public static void setLodestoneLocation(BlockPos pos, RegistryKey<World> dim) {
        lodestoneLocation = pos;
        lodestoneDimension = dim;
        POIStorage.save();
    }

    public static void setLodestoneLocationSilent(BlockPos pos, String dimStr) {
        lodestoneLocation = pos;
        lodestoneDimension = parseDimension(dimStr);
    }

    public static BlockPos getLodestoneLocation() { return lodestoneLocation; }
    public static RegistryKey<World> getLodestoneDimension() { return lodestoneDimension; }
    public static boolean hasLodestoneLocation() { return lodestoneLocation != null; }

    public static void clearLodestoneLocation() {
        lodestoneLocation = null;
        lodestoneDimension = null;
        POIStorage.save();
    }

    public static boolean isLodestoneVisibleInDimension(RegistryKey<World> currentDim) {
        return lodestoneLocation != null && lodestoneDimension != null && lodestoneDimension.equals(currentDim);
    }

    // ===== CLEAR ALL =====

    public static void clearAllSilent() {
        deathLocation = null;
        deathDimension = null;
        bedLocation = null;
        respawnAnchorLocation = null;
        netherPortalOverworld = null;
        netherPortalNether = null;
        endPortalOverworld = null;
        endPortalEnd = null;
        endGateway = null;
        lodestoneLocation = null;
        lodestoneDimension = null;
    }
}
