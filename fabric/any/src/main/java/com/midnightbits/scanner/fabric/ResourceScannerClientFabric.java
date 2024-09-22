package com.midnightbits.scanner.fabric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.midnightbits.scanner.ResourceScannerClientMod;
import com.midnightbits.scanner.rt.core.ScannerClientMod;
import com.midnightbits.scanner.rt.networking.fabric.FabricClientPlayMessaging;

import net.fabricmc.api.ClientModInitializer;

public class ResourceScannerClientFabric implements ClientModInitializer {
	private final ScannerClientMod mod = new ResourceScannerClientMod();
	private final FabricClientPlayMessaging playMessaging = new FabricClientPlayMessaging();

	static final Logger logger = LoggerFactory.getLogger("resource-scanner/client");

	@Override
	public void onInitializeClient() {
		mod.onInitializeClient(playMessaging);
		playMessaging.setup();
	}
}
