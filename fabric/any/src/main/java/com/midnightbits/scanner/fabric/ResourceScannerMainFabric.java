package com.midnightbits.scanner.fabric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.midnightbits.scanner.ResourceScannerServerMod;
import com.midnightbits.scanner.rt.core.ScannerServerMod;
import com.midnightbits.scanner.rt.networking.fabric.FabricServerPlayMessaging;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class ResourceScannerMainFabric implements ModInitializer {
	private final ScannerServerMod mod = new ResourceScannerServerMod();
	private final FabricServerPlayMessaging playMessaging = new FabricServerPlayMessaging();

	static final Logger logger = LoggerFactory.getLogger("resource-scanner/main");

	@Override
	public void onInitialize() {
		mod.onInitializeServer(playMessaging);
		playMessaging.setup();
	}
}
