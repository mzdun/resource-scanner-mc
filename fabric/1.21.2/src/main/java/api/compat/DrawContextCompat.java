// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package api.compat;

import api.compat.common.DrawContextCommon;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

public class DrawContextCompat extends DrawContextCommon {
    public DrawContextCompat(DrawContext context) {
        super(context);
    }

    public void drawTexture(Identifier texture, int x, int y, int u, int v, int width, int height) {
        context.drawTexture(RenderLayer::getGuiTextured, texture, x, y, u, v, width, height, 256, 256);
    }

    public void drawGuiTexture(Identifier texture, int x, int y, int width, int height) {
        context.drawGuiTexture(RenderLayer::getGuiTextured, texture, x, y, width, height);
    }


    public void drawSprite(Sprite sprite, int x, int y, int width, int height) {
        context.drawSpriteStretched(RenderLayer::getGuiTextured, sprite, x, y, width, height);
    }
}
