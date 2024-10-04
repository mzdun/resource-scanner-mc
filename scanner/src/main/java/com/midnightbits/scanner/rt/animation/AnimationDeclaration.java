package com.midnightbits.scanner.rt.animation;

public class AnimationDeclaration {
    long duration;
    TimeFunction timeFunction;

    public AnimationDeclaration(long duration, TimeFunction timeFunction) {
        this.duration = duration;
        this.timeFunction = timeFunction;
    }

    public double apply(long elapsed) {
        if (elapsed > duration) {
            return 1.0;
        }
        final var time = ((double) elapsed) / ((double) duration);
        return timeFunction.apply(time);
    }

    public long duration() {
        return duration;
    }

    public TimeFunction timeFunction() {
        return timeFunction;
    }
}
