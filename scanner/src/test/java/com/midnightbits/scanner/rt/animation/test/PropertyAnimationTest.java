package com.midnightbits.scanner.rt.animation.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.midnightbits.scanner.rt.animation.Animation;
import com.midnightbits.scanner.rt.animation.AnimationDeclaration;
import com.midnightbits.scanner.rt.animation.PropertyAnimation;
import com.midnightbits.scanner.rt.animation.TimeFunction;

public class PropertyAnimationTest {
    final AnimationDeclaration decl = new AnimationDeclaration(250, TimeFunction.LINEAR);

    private class Prop {
        double value;

        Prop(double value) {
            this.value = value;
        }

        AnimatedProp animate(double start, double target, AnimationDeclaration decl) {
            return new AnimatedProp(this,
                    new PropertyAnimation(this::set, start, target, decl));
        }

        void set(double v) {
            value = v;
        }
    };

    private class AnimatedProp {
        final Prop prop;
        final Animation animation;

        AnimatedProp(Prop prop, Animation animation) {
            this.prop = prop;
            this.animation = animation;
        }

        void assertValue(long time, double value) {
            prop.set(value - 100);
            animation.apply(time);
            Assertions.assertEquals(value, prop.value);
        }
    };

    @Test
    void declarationKeepsWithinBounds() {
        Assertions.assertEquals(250, decl.duration());
        Assertions.assertEquals(TimeFunction.LINEAR, decl.timeFunction());
        Assertions.assertEquals(0.0, decl.apply(0));
        Assertions.assertEquals(0.248, decl.apply(62));
        Assertions.assertEquals(0.5, decl.apply(125));
        Assertions.assertEquals(0.8, decl.apply(200));
        Assertions.assertEquals(1.0, decl.apply(250));
        Assertions.assertEquals(1.0, decl.apply(400));
    }

    @Test
    void propertyAnimationKeepsWithinBounds() {
        final long now = 0xcafebabe;
        final double start = -300.0;
        final double target = 500.0;
        final var prop = new Prop(start - 100.0);
        final var test = prop.animate(start, target, decl);

        test.animation.reset(now);

        test.assertValue(now + 0, start);
        test.assertValue(now + 62, start + (target - start) * 62 / 250);
        test.assertValue(now + 125, start + (target - start) / 2);
        test.assertValue(now + 200, start + (target - start) * 4 / 5);
        test.assertValue(now + 250, target);
        test.assertValue(now + 400, target);
    }
}
