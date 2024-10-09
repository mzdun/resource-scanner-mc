// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.fabric;

import com.midnightbits.scanner.rt.core.fabric.MinecraftClientCore;
import com.midnightbits.scanner.sonar.Sonar;
import com.midnightbits.scanner.sonar.graphics.AbstractAnimatorHost;
import com.midnightbits.scanner.sonar.graphics.ShimmerConsumer;
import com.midnightbits.scanner.sonar.graphics.Shimmers;
import com.midnightbits.scanner.utils.Clock;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import java.util.ArrayList;
import java.util.List;

public class FabricAnimationHost extends AbstractAnimatorHost {
    public static final FabricAnimationHost INSTANCE = new FabricAnimationHost();
    private Sonar source;

    public void initialize(Sonar source) {
        this.source = source;
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            if (client.world == null)
                return;

            this.tick(Clock.currentTimeMillis());
            if (this.source.remove(this.source.oldEchoes(new MinecraftClientCore(client))))
                this.source.splitToNuggets();
        });
        WorldRenderEvents.LAST.register(this::renderLevel);
    }

    private static final class GatherShimmers implements ShimmerConsumer {
        List<Shimmers> cloud = new ArrayList<>();

        @Override
        public void apply(List<Shimmers> shimmers) {
            this.cloud.addAll(shimmers);
        }
    };

    private void renderLevel(WorldRenderContext context) {
        final var shimmers = new GatherShimmers();
        this.run(shimmers);
        Pixels.renderLevel(context, source.nuggets(), shimmers.cloud);
    }
}
