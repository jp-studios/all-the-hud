package com.jpstudios.allthehud;

import net.fabricmc.api.ClientModInitializer;

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

		// Register world change detection
		net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.world != null) {
				POIStorage.onWorldChange();
			}
		});
	}
}
