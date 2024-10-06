// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.test.mocks.platform;

import com.midnightbits.scanner.sonar.graphics.AbstractAnimatorHost;
import com.midnightbits.scanner.sonar.graphics.GraphicContext;
import com.midnightbits.scanner.test.mocks.MockedClock;
import com.midnightbits.scanner.utils.Clock;

public class MockAnimatorHost extends AbstractAnimatorHost {
    public interface GraphicContextFactory {
        GraphicContext make();
    }

    GraphicContextFactory factory;

    public MockAnimatorHost(GraphicContextFactory factory) {
        this.factory = factory;
    }

    public void tick() {
        final var now = Clock.currentTimeMillis();
        final var ctx = factory.make();
        this.run(ctx);
        this.tick(now);
    }

    public void tickWith(MockedClock clock) {
        if (!isEmpty()) {
            tick();
            clock.timeStamp += 1;
        }
    }

    public void runAll(MockedClock clock) {
        while (!isEmpty()) {
            tick();
            clock.timeStamp += 1;
        }
    }
}
