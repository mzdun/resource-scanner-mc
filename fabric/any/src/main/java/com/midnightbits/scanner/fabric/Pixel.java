// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.fabric;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.joml.Matrix4f;

import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.sonar.BlockEcho;
import com.midnightbits.scanner.sonar.graphics.Shimmers;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;

public class Pixel {
    private record Vertex(int dx, int dy, int dz) {
        void apply(BufferBuilder buffer, Matrix4f matrix, int argb) {
            buffer.vertex(matrix, dx, dy, dz).color(argb);
        }
    }

    private static final Vertex v000 = new Vertex(0, 0, 0);
    private static final Vertex v001 = new Vertex(0, 0, 1);
    private static final Vertex v010 = new Vertex(0, 1, 0);
    private static final Vertex v011 = new Vertex(0, 1, 1);
    private static final Vertex v100 = new Vertex(1, 0, 0);
    private static final Vertex v101 = new Vertex(1, 0, 1);
    private static final Vertex v110 = new Vertex(1, 1, 0);
    private static final Vertex v111 = new Vertex(1, 1, 1);

    private static final Vertex[][] triangles = new Vertex[][] {
            new Vertex[] { v000, v100, v010, v100, v110, v010 },
            new Vertex[] { v001, v101, v000, v101, v100, v000 },
            new Vertex[] { v011, v111, v001, v111, v101, v001 },
            new Vertex[] { v010, v110, v011, v110, v111, v011 },
            new Vertex[] { v001, v000, v011, v000, v010, v011 },
            new Vertex[] { v100, v101, v110, v101, v111, v110 },
    };

    final static int SIDE_X0 = 1 << 4;
    final static int SIDE_X1 = 1 << 5;
    final static int SIDE_Y0 = 1 << 1;
    final static int SIDE_Y1 = 1 << 3;
    final static int SIDE_Z0 = 1;
    final static int SIDE_Z1 = 1 << 2;
    final static int ALL_SIDES = SIDE_X0 | SIDE_X1 | SIDE_Y0 | SIDE_Y1 | SIDE_Z0 | SIDE_Z1;

    final V3i position;
    final int argb;
    int sides = ALL_SIDES;

    Pixel(V3i position, int argb) {
        this.position = position;
        this.argb = argb;
    }

    public V3i position() {
        return position;
    }

    static Pixel of(BlockEcho echo) {
        return new Pixel(echo.position(), echo.argb32());
    }

    void draw(BufferBuilder buffer, MatrixStack stack, Camera camera) {
        final var cam = camera.getPos();
        final var x = position.getX() - cam.x;
        final var y = position.getY() - cam.y;
        final var z = position.getZ() - cam.z;
        stack.push();
        stack.translate(x, y, z);
        final var m = stack.peek().getPositionMatrix();

        for (var side = 0; side < triangles.length; ++side) {
            final var flag = 1 << side;
            if ((sides & flag) == 0) {
                continue;
            }

            final var wall = triangles[side];
            for (final var vertex : wall) {
                vertex.apply(buffer, m, argb);
            }
        }

        stack.pop();
    }

    public static void renderLevel(WorldRenderContext context, Iterable<BlockEcho> echoes, List<Shimmers> shimmers) {
        final var frustum = context.frustum();
        assert frustum != null;

        final var allPixels = StreamSupport
                .stream(echoes.spliterator(), false)
                .map(Pixel::of)
                .collect(Collectors.toSet());

        for (final var shimmer : shimmers) {
            final var alpha = (int) (shimmer.alpha() * 255.0 / 8.0 + .5);
            int argbWalls = ColorHelper.Argb.withAlpha(alpha, 0x8080FF);

            for (final var pos : shimmer.blocks()) {
                allPixels.add(new Pixel(pos, argbWalls));
            }
        }

        Mesh.cleanPixels(allPixels);

        final var visiblePixels = allPixels
                .stream()
                .filter((pixel) -> {
                    if ((pixel.sides & Pixel.ALL_SIDES) == 0) {
                        return false;
                    }
                    final var pos = pixel.position();
                    final var box = new Box(pos.getX(), pos.getY(), pos.getZ(), (pos.getX() + 1), (pos.getY() + 1),
                            (pos.getZ() + 1));
                    return frustum.isVisible(box);
                })
                .collect(Collectors.toSet());

        if (visiblePixels.isEmpty() && shimmers.isEmpty()) {
            return;
        }

        final var camera = context.camera();
        final var stack = context.matrixStack();
        assert stack != null;

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(
                GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        final var tessellator = Tessellator.getInstance();
        final var buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        for (final var pixel : visiblePixels) {
            pixel.draw(buffer, stack, camera);
        }

        final var end = buffer.endNullable();
        if (end != null)
            BufferRenderer.drawWithGlobalProgram(end);

        RenderSystem.enableDepthTest();
    }
}
