// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package api.compat.common;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;

public class DrawContextCommon {
    protected final DrawContext context;

    protected DrawContextCommon(final DrawContext context) {
        this.context = context;
    }

    public MatrixStack getMatrices() {
        return context.getMatrices();
    }

    public void fill(int x1, int y1, int x2, int y2, int color) {
        context.fill(x1, y1, x2, y2, color);
    }

    public void drawItemWithoutEntity(ItemStack stack, int x, int y, int seed) {
        context.drawItemWithoutEntity(stack, x, y, seed);
    }

    public void drawItem(ItemStack item, int x, int y) {
        context.drawItem(item, x, y, 0);
    }

    public void drawItem(ItemStack item, int x, int y, int seed) {
        context.drawItem(item, x, y, seed);
    }

    public void drawTooltip(TextRenderer textRenderer, List<Text> text, Optional<TooltipData> data, int x, int y) {
        context.drawTooltip(textRenderer, text, data, x, y);
    }

    public void drawText(TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow) {
        context.drawText(textRenderer, text, x, y, color, shadow);
    }

    public void fillGradient(RenderLayer layer, int startX, int startY, int endX, int endY, int colorStart,
            int colorEnd, int z) {
        context.fillGradient(layer, startX, startY, endX, endY, colorStart, colorEnd, z);
    }
}
