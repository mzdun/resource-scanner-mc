// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar.graphics;

import com.midnightbits.scanner.sonar.EchoNugget;
import com.midnightbits.scanner.sonar.EchoState;

public interface FrustumFilter {
    default boolean contains(EchoNugget nugget) {
        return contains(nugget.getBounds());
    }
    default boolean contains(EchoState echoState) {
        return contains(echoState.getBounds());
    }
    boolean contains(EchoState.AABB bounds);
}
