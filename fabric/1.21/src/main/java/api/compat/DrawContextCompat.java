// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package api.compat;

import com.midnightbits.scanner.modmenu.gui.Constants;
import com.mojang.datafixers.util.Pair;

import api.compat.common.DrawContextCommon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Sprite;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.StringVisitable;
import net.minecraft.util.Identifier;

public class DrawContextCompat extends DrawContextCommon {
    public DrawContextCompat(DrawContext context) {
        super(context);
    }

    public void drawTexture(Identifier texture, int x, int y, int u, int v, int width, int height) {
        context.drawTexture(texture, x, y, u, v, width, height);
    }

    public void drawGuiTexture(Identifier texture, int x, int y, int width, int height) {
        context.drawGuiTexture(texture, x, y, width, height);
    }

    public boolean drawSlotSprite(Slot slot, MinecraftClient client) {
        Pair<Identifier, Identifier> pair = slot.getBackgroundSprite();
        if (pair != null && client != null) {
            Sprite sprite = client.getSpriteAtlas(pair.getFirst())
                    .apply(pair.getSecond());
            context.drawSprite(slot.x, slot.y, 0, Constants.ICON_SIZE, Constants.ICON_SIZE, sprite);
            return true;
        }
        return false;
    }

    public void drawTextWrapped(TextRenderer textRenderer, StringVisitable text, int x, int y, int width, int color) {
        context.drawTextWrapped(textRenderer, text, x, y, width, color);
    }
}
