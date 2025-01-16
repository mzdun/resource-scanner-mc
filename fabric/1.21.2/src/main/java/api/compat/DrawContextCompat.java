// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package api.compat;

import api.compat.common.DrawContextCommon;
import com.midnightbits.scanner.modmenu.gui.Constants;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.StringVisitable;
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

    public boolean drawSlotSprite(Slot slot, MinecraftClient client) {
        Pair<Identifier, Identifier> pair = slot.getBackgroundSprite();
        if (pair != null && client != null) {
            Sprite sprite = client.getSpriteAtlas(pair.getFirst())
                    .apply(pair.getSecond());
            context.drawSpriteStretched(RenderLayer::getGuiTextured, sprite, slot.x, slot.y, Constants.ICON_SIZE,
                    Constants.ICON_SIZE);
            return true;
        }
        return false;
    }

    public void drawTextWrapped(TextRenderer textRenderer, StringVisitable text, int x, int y, int width, int color) {
        context.drawTextWrapped(textRenderer, text, x, y, width, color);
    }
}
