// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.fabric;

import java.util.HashMap;
import java.util.Map;

import api.compat.SoundEventCompat;
import com.midnightbits.scanner.ResourceScannerMod;
import com.midnightbits.scanner.platform.PlatformInterface;
import com.midnightbits.scanner.rt.core.ScannerMod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class FabricSoundManager {
    private static final Map<PlatformInterface.Sample, SoundEvent> SAMPLES;
    private static final SoundEvent ACTIVATED;

    private FabricSoundManager() {
    }

    static void playSample(PlatformInterface.Sample id) {
        final var sample = SAMPLES.get(id);
        ResourceScannerMod.LOGGER.info("{} -> {}", id.name(), sample);
        if (sample == null) {
            return;
        }
        final var client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        client.player.playSound(sample, 1F, 3F);
    }

    private static SoundEvent addEvent(PlatformInterface.Sample id, String path) {
        final var event = SoundEvent.of(Identifier.of(ScannerMod.MOD_ID, path));
        SAMPLES.put(id, event);
        return event;
    }

    private static void registerEvent(SoundEvent event) {
        Registry.register(Registries.SOUND_EVENT, SoundEventCompat.idOf(event), event);
    }

    public static void initialize() {
        registerEvent(ACTIVATED);
    }

    static {
        SAMPLES = new HashMap<>();
        ACTIVATED = addEvent(PlatformInterface.Sample.ACTIVATED, "activated");
    }
}
