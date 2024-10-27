// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.rt.core;
import org.lwjgl.glfw.GLFW;

public interface KeyBindings {
    String MOVEMENT_CATEGORY = "key.categories.movement";
    String MISC_CATEGORY = "key.categories.misc";
    String MULTIPLAYER_CATEGORY = "key.categories.multiplayer";
    String GAMEPLAY_CATEGORY = "key.categories.gameplay";
    String INVENTORY_CATEGORY = "key.categories.inventory";
    String UI_CATEGORY = "key.categories.ui";
    String CREATIVE_CATEGORY = "key.categories.creative";

    enum MOUSE {
        BTN_LEFT(GLFW.GLFW_MOUSE_BUTTON_LEFT),
        BTN_RIGHT(GLFW.GLFW_MOUSE_BUTTON_RIGHT),
        BTN_MIDDLE(GLFW.GLFW_MOUSE_BUTTON_MIDDLE),
        BTN_4(GLFW.GLFW_MOUSE_BUTTON_4),
        BTN_5(GLFW.GLFW_MOUSE_BUTTON_5),
        BTN_6(GLFW.GLFW_MOUSE_BUTTON_6),
        BTN_7(GLFW.GLFW_MOUSE_BUTTON_7),
        BTN_8(GLFW.GLFW_MOUSE_BUTTON_8);

        private final int button;

        MOUSE(int button) {
            this.button = button;
        }

        public int button() { return button; }
    }

    MOUSE SCAN_BUTTON = MOUSE.BTN_5;
    MOUSE OPTIONS_BUTTON = MOUSE.BTN_4;
}
