package com.midnightbits.scanner.fabric;

import com.midnightbits.scanner.sonar.graphics.AbstractAnimatorHost;
import com.midnightbits.scanner.utils.Clock;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class FabricAnimationHost extends AbstractAnimatorHost {
    public static final FabricAnimationHost INSTANCE = new FabricAnimationHost();

    private FabricAnimationHost() {
        super();
    }

    public void initialize() {
        ClientTickEvents.START_WORLD_TICK.register((client) -> {
            this.tick(Clock.currentTimeMillis());
        });
    }
}
