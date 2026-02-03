package com.jpstudios.allthehud;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

public class AllTheHUDClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		AllTheHUD.LOGGER.info("All the HUD client initializing!");

		// Load saved POI data
		POIStorage.load();

		// Register our HUD renderer
		HudRenderer.register();

		// Register death tracking
		DeathTracker.register();

		// Register bed tracking
		BedTracker.register();

		// Register portal tracking (nether/end portals)
		PortalTracker.register();

		// Register lodestone tracking
		LodestoneTracker.register();

		// Register respawn anchor tracking
		RespawnAnchorTracker.register();

		// Register world change detection (throttled to reduce overhead)
		net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(new net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick() {
			private int worldChangeTickCounter = 0;
			private static final int WORLD_CHECK_INTERVAL = 20;

			@Override
			public void onEndTick(MinecraftClient client) {
				if (++worldChangeTickCounter % WORLD_CHECK_INTERVAL != 0) return;
				if (client.world != null) {
					POIStorage.onWorldChange();
				}
			}
		});
	}
}
