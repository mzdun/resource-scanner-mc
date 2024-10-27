// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.fabric;

import com.midnightbits.scanner.ResourceScannerMod;
import com.midnightbits.scanner.modmenu.OptionsScreen;
import com.midnightbits.scanner.rt.core.ClientCore;
import com.midnightbits.scanner.rt.core.KeyBindings;
import com.midnightbits.scanner.rt.core.ScannerMod;
import com.midnightbits.scanner.rt.core.Services;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

public class ResourceScannerFabric implements ClientModInitializer {
	private final ScannerMod scanner = new ResourceScannerMod();

	@Override
	public void onInitializeClient() {
		scanner.onInitializeClient();

		Services.PLATFORM.getKeyBinder().bind(
				ScannerMod.translationKey("key", "options"),
				KeyBindings.OPTIONS_BUTTON,
				KeyBindings.GAMEPLAY_CATEGORY,
				ResourceScannerFabric::onSettings);

		FabricAnimationHost.INSTANCE.initialize(scanner.getSonar());
		FabricSoundManager.initialize();
	}

	private static void onSettings(ClientCore core) {
		final var client = MinecraftClient.getInstance();
		final var screen = new OptionsScreen(client.currentScreen);
		client.setScreen(screen);
	}
}
