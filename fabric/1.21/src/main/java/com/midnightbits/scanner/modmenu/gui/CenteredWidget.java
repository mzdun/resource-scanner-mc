// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.modmenu.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.ScreenRect;

@Environment(value = EnvType.CLIENT)
public abstract class CenteredWidget extends FocusedWidget implements Drawable {
    protected final int contentWidth;
    protected final int contentHeight;

    CenteredWidget(int width, int height, int contentWidth, int contentHeight) {
        super(width, height);
        this.contentWidth = contentWidth;
        this.contentHeight = contentHeight;
    }

    public int centerX() {
        return x + (width - contentWidth) / 2;
    }

    public int centerY() {
        return y + (height - contentHeight) / 2;
    }

    // Widget
    @Override
    public ScreenRect getNavigationFocus() {
        return new ScreenRect(centerX(), centerY(), contentWidth, contentHeight);
    }

    // Drawable
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        RenderSystem.disableDepthTest();

        hovered = context.scissorContains(mouseX, mouseY) &&
                mouseInside(mouseX, mouseY, centerX(), centerY(), contentWidth, contentHeight);

        final var centerX = this.centerX();
        final var centerY = this.centerY();
        context.getMatrices().push();
        context.getMatrices().translate(centerX, centerY, 0.0F);

        mouseX -= centerX;
        mouseY -= centerY;

        renderWidget(context, mouseX, mouseY, delta);

        context.getMatrices().pop();
        RenderSystem.enableDepthTest();
    }

    protected abstract void renderWidget(DrawContext context, int mouseX, int mouseY, float delta);
}
