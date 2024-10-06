// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.rt.animation;

import java.util.ArrayList;
import java.util.List;

public interface Animation {
    boolean apply(long now);

    default Animation andThen(Animation another) {
        return new SequenceAnimation(this).andThen(another);
    }

    default void reset(long now) {
    }

    static Animation from(Animation anim) {
        return anim;
    }

    class SequenceAnimation implements Animation {
        List<Animation> sequence = new ArrayList<>();
        int index = 0;
        int prevIndex = 0;

        public SequenceAnimation(Animation first) {
            sequence.add(first);
        }

        @Override
        public void reset(long now) {
            index = 0;
            prevIndex = 0;
            sequence.getFirst().reset(now);
        }

        @Override
        public boolean apply(long now) {
            while (index < sequence.size()) {
                final var current = sequence.get(index);

                if (index != prevIndex) {
                    prevIndex = index;
                    current.reset(now);
                }
                final var result = current.apply(now);
                if (!result) {
                    ++index;
                    if (current instanceof ActionAnimation) {
                        continue;
                    }
                    if (index < sequence.size() && sequence.get(index) instanceof ActionAnimation) {
                        continue;
                    }
                }
                return index < sequence.size();
            }
            return false;
        }

        @Override
        public Animation andThen(Animation another) {
            sequence.add(another);
            return this;
        }
    };
}
