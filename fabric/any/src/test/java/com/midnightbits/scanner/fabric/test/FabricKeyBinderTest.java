// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.fabric.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;

import com.midnightbits.scanner.rt.core.KeyBinding;

public class FabricKeyBinderTest {
    @Test
    void keysAreEqual() {
        Assertions.assertEquals(KeyBinding.KEY_M, GLFW.GLFW_KEY_M);
    }
}
