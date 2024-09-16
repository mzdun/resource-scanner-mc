package com.midnightbits.scanner.fabric;

import com.midnightbits.scanner.ResourceScannerMod;
import com.midnightbits.scanner.rt.core.ScannerMod;

import net.fabricmc.api.ClientModInitializer;

public class ResourceScannerFabric implements ClientModInitializer {
	private final ScannerMod scanner = new ResourceScannerMod();

	@Override
	public void onInitializeClient() {
		scanner.onInitializeClient();
	}
}
