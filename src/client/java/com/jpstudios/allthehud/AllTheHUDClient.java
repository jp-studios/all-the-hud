package com.jpstudios.allthehud;

import net.fabricmc.api.ClientModInitializer;

public class AllTheHUDClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		AllTheHUD.LOGGER.info("All the HUD client initializing!");

		// Register our HUD renderer
		HudRenderer.register();

		// Register world change detection FIRST - must run before any trackers
		// Runs every tick (just a string comparison, negligible cost)
		net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.world != null) {
				POIStorage.onWorldChange();
			}
		});

		// Register trackers AFTER world change detection
		DeathTracker.register();
		BedTracker.register();
		PortalTracker.register();
		LodestoneTracker.register();
		RespawnAnchorTracker.register();
	}
}
