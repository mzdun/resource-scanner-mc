// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar.graphics;

import com.midnightbits.scanner.rt.animation.AnimationContainer;
import com.midnightbits.scanner.rt.animation.TickSet;

import java.util.function.Predicate;

public class AbstractAnimatorHost {
    private final TickSet<Predicate<GraphicContext>> renderers = new TickSet<>();
    private final TickSet<Predicate<Long>> animations = new TickSet<>();
    private final TickSet<AnimationContainer> containers = new TickSet<>();

    public void addRenderer(Predicate<GraphicContext> renderer) {
        renderers.add(renderer);
    }

    public void addContainer(AnimationContainer animator) {
        containers.add(animator);
    }

    public void addAnimation(Predicate<Long> animation) {
        animations.add(animation);
    }

    public boolean isEmpty() {
        return renderers.isEmpty() && animations.isEmpty() && containers.isEmpty();
    }

    public void tick(long now) {
        animations.run(anim -> anim.test(now));
        containers.run(container -> container.tick(now));
    }

    public void run(GraphicContext ctx) {
        renderers.run(renderer -> renderer.test(ctx));
    }
}
