package com.midnightbits.scanner.fabric;

import com.midnightbits.scanner.sonar.Sonar;
import com.midnightbits.scanner.sonar.graphics.AbstractAnimatorHost;
import com.midnightbits.scanner.utils.Clock;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class FabricAnimationHost extends AbstractAnimatorHost {
    public static final FabricAnimationHost INSTANCE = new FabricAnimationHost();
    private Sonar source;

    public void initialize(Sonar source) {
        this.source = source;
        ClientTickEvents.START_WORLD_TICK.register((client) -> {
            this.tick(Clock.currentTimeMillis());
        });
    }

    @Override
    public void tick(long now) {
        super.tick(now);
        this.source.removeOldEchoes();
    }
}
