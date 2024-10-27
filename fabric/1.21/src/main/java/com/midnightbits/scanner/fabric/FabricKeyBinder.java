// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.fabric;

import java.util.ArrayList;

import com.midnightbits.scanner.platform.KeyBinder;

import com.midnightbits.scanner.rt.core.ClientCore;
import com.midnightbits.scanner.rt.core.KeyBindings;
import com.midnightbits.scanner.rt.core.fabric.MinecraftClientCore;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class FabricKeyBinder implements KeyBinder {
    private record BoundKey(KeyBinding binding, KeyBinder.KeyPressHandler handler) {
        public boolean handle(ClientCore client) {
            if (binding.wasPressed()) {
                handler.handle(client);
                return true;
            }
            return false;
        }
    };

    private final ArrayList<BoundKey> keys = new ArrayList<>();

    private boolean handle(ClientCore client) {
        return keys.parallelStream()
                .map(key -> key.handle(client))
                .reduce(false, (identity, wasPressed) -> identity || wasPressed);
    }

    private void attach(KeyBinding registeredKey, KeyPressHandler handler) {
        boolean attachHandler = keys.isEmpty();
        keys.add(new BoundKey(registeredKey, handler));
        if (attachHandler) {
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                ClientCore core = new MinecraftClientCore(client);
                boolean shouldContinue = handle(core);
                while (shouldContinue) {
                    shouldContinue = handle(core);
                }
            });
        }
    }

    @Override
    public void bind(String translationKey, int code, String category, KeyPressHandler handler) {
        KeyBinding key = KeyBindingHelper.registerKeyBinding(new KeyBinding(translationKey, code, category));
        attach(key, handler);
    }

    @Override
    public void bind(String translationKey, KeyBindings.MOUSE code, String category, KeyPressHandler handler) {
        KeyBinding key = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(translationKey, InputUtil.Type.MOUSE, code.button(), category));
        attach(key, handler);
    }
}
