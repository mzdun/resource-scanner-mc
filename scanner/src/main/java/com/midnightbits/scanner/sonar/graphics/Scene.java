// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar.graphics;

import com.midnightbits.scanner.rt.animation.TickSet;
import com.midnightbits.scanner.rt.core.Services;
import com.midnightbits.scanner.sonar.Sonar;
import com.midnightbits.scanner.utils.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Scene {
    public static final Logger LOGGER = LoggerFactory.getLogger("resources-scanner/gfx");
    final Sonar target;
    final TickSet<Shimmers> shimmers = new TickSet<>();
    long then;
    long frames;

    public Scene(Sonar target) {
        this.target = target;
    }

    public void add(Shimmers shimmers) {
        final var attach = isEmpty();
        this.shimmers.add(shimmers);
        if (attach) {
            then = Clock.currentTimeMillis();
            frames = 0;
            Services.PLATFORM.getAnimatorHost().addRenderer(this::render);
        }
    }

    public void remove(Shimmers shimmers) {
        this.shimmers.remove(shimmers);
    }

    public boolean isEmpty() {
        return shimmers.isEmpty();
    }

    private boolean render(GraphicContext context) {
        context.drawScan(List.copyOf(shimmers.copy()));
        ++frames;

        final var done = isEmpty();
        if (done) {
            final var duration = Clock.currentTimeMillis() - then;
            final var fps10 = frames * 1000 * 10 / duration;
            LOGGER.info("duration: {}, frames: {}, fps: {}.{}", duration, frames, fps10 / 10, fps10 % 10);
        }

        return !done;
    }
}
