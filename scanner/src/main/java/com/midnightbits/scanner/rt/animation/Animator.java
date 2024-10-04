package com.midnightbits.scanner.rt.animation;

import com.midnightbits.scanner.sonar.graphics.AbstractAnimatorHost;
import com.midnightbits.scanner.utils.Clock;

public class Animator implements AnimationContainer {
    private final TickSet<Animation> animations = new TickSet<>();
    private final AbstractAnimatorHost parent;

    public Animator(AbstractAnimatorHost parent) {
        this.parent = parent;
    }

    public void add(Animation animation) {
        animations.add(animation);
        if (animations.size() == 1) {
            parent.addContainer(this);
            animation.reset(Clock.currentTimeMillis());
        }
    }

    @Override
    public boolean tick(long now) {
        return animations.run(animation -> animation.apply(now));
    }
}
