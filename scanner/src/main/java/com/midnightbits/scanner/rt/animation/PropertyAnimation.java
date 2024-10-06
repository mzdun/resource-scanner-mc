// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.rt.animation;

import java.util.function.Consumer;

import com.midnightbits.scanner.utils.Clock;

public class PropertyAnimation implements Animation {
    double startValue;
    double targetValue;
    long then;
    AnimationDeclaration animation;
    Consumer<Double> consumer;

    public PropertyAnimation(Consumer<Double> consumer, double startValue, double targetValue,
            AnimationDeclaration animation) {
        this.startValue = startValue;
        this.targetValue = targetValue;
        this.then = Clock.currentTimeMillis();
        this.animation = animation;
        this.consumer = consumer;
    }

    @Override
    public boolean apply(long now) {
        final var elapsed = now - then;
        if (elapsed > animation.duration()) {
            consumer.accept(targetValue);
            return false;
        }
        final var scale = animation.apply(elapsed);
        final var value = startValue + (targetValue - startValue) * scale;
        consumer.accept(value);
        return elapsed < animation.duration();
    }

    @Override
    public void reset(long now) {
        then = now;
    }
}
