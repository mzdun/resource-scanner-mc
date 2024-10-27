// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.fabric;

import java.util.List;

import api.compat.ShaderCompat;
import com.midnightbits.scanner.sonar.EchoNugget;
import com.midnightbits.scanner.sonar.EchoState;
import com.midnightbits.scanner.sonar.graphics.*;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Box;
import org.joml.Matrix4f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

public class Pixels {

    private record GlProgramVertexConsumer(VertexConsumer buffer) implements GlProgramConsumer {
        @Override
        public void vertexColor(Matrix4f matrix, float x, float y, float z, int argb32) {
            buffer.vertex(matrix, x, y, z).color(argb32);
        }
    }

    public static void renderLevel(WorldRenderContext context, List<EchoNugget> nuggets, List<Shimmers> shimmers) {
        final var frustum = context.frustum();
        if (frustum == null) {
            return;
        }

        final var contextMatrices = context.matrixStack();
        if (contextMatrices == null) {
            return;
        }

        final var matrices = new MatrixStack(contextMatrices.peek().getPositionMatrix());

        final var camera = context.camera();
        final var cameraPos = camera.getPos().toVector3f();

        final var frustumFilter = new FrustumFilter() {
            @Override
            public boolean contains(EchoState.AABB bounds) {
                return frustum.isVisible(
                        new Box(bounds.minX(), bounds.minY(), bounds.minZ(),
                                bounds.maxX(), bounds.maxY(), bounds.maxZ()));
            }
        };

        final var allShimmers = EchoNugget.group(Shimmers.toEchoStates(shimmers, .5));

        final var visibleNuggets = EchoNugget.filterVisible(nuggets, frustumFilter);
        final var visibleShimmers = EchoNugget.filterVisible(allShimmers, frustumFilter);
        final var central = EchoNugget.theThingImLookingAt(
                visibleNuggets, cameraPos, camera.getPitch(), camera.getYaw());

        if (visibleNuggets.isEmpty() && visibleShimmers.isEmpty()) {
            return;
        }

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(
                GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        ShaderCompat.setShader();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        final var tessellator = Tessellator.getInstance();
        {
            final var buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
            final var glProgram = new GlProgramVertexConsumer(buffer);

            for (final var nugget : visibleShimmers) {
                nugget.draw(glProgram, matrices, cameraPos);
            }

            for (final var nugget : visibleNuggets) {
                nugget.draw(glProgram, matrices, cameraPos);
            }

            final var builtBuffer = buffer.endNullable();
            if (builtBuffer != null)
                BufferRenderer.drawWithGlobalProgram(builtBuffer);
        }

        {
            final var buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            final var glProgram = new GlProgramVertexConsumer(buffer);

            for (final var nugget : visibleShimmers) {
                nugget.sketch(glProgram, matrices, cameraPos);
            }

            final var centralNugget = central == null ? null : central.nugget();
            for (final var nugget : visibleNuggets) {
                if (nugget == centralNugget)
                    continue;
                nugget.sketch(glProgram, matrices, cameraPos);
            }

            if (central != null) {
                central.nugget().sketch(glProgram, matrices, cameraPos, Colors.OPAQUE | Colors.BLACK);
            }

            final var builtBuffer = buffer.endNullable();
            if (builtBuffer != null)
                BufferRenderer.drawWithGlobalProgram(builtBuffer);
        }

        RenderSystem.enableDepthTest();
    }
}
