// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package api.compat;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKeys;

public class ShaderCompat {
    public static void setShader() {
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
    }

    public static void close(ShaderProgram shader) {
        shader.close();
    }
}
