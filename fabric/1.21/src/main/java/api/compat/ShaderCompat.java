// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package api.compat;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.GameRenderer;

public class ShaderCompat {
    public static void setShader() {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
    }

    public static void close(Object ignorable) {
    }
}
