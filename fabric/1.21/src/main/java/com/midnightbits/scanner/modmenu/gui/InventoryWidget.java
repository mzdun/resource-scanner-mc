// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.modmenu.gui;

import api.compat.DrawContextCompat;
import com.midnightbits.scanner.modmenu.InventoryHandler;
import com.midnightbits.scanner.modmenu.ScannerInventory;
import com.midnightbits.scanner.rt.core.Id;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.Set;
import java.util.function.Consumer;

@Environment(value = EnvType.CLIENT)
public class InventoryWidget extends HandledWidget<InventoryHandler> {
    Identifier SCROLLER_TEXTURE = Identifier.ofVanilla("container/creative_inventory/scroller");
    Identifier SCROLLER_DISABLED_TEXTURE = Identifier.ofVanilla("container/creative_inventory/scroller_disabled");

    private static final int SCROLLBAR_TOP = Constants.TOP;
    private static final int SCROLLBAR_BOTTOM = Constants.INVENTORY_HEIGHT - Constants.BOTTOM - 1;
    private static final int SCROLLBAR_RIGHT = Constants.INVENTORY_WIDTH - Constants.DX + 1;
    private static final int SCROLLBAR_LEFT = SCROLLBAR_RIGHT - Constants.SCROLLBAR_WIDTH - 2;

    private final ScannerInventory inventory;
    private float scrollPosition;
    private boolean scrolling;
    private final Consumer<Set<Id>> changeCallback;

    public InventoryWidget(MinecraftClient client, ScannerInventory inventory, Consumer<Set<Id>> changeCallback) {
        super(new InventoryHandler(inventory), client, Constants.INVENTORY_WIDTH, Constants.INVENTORY_HEIGHT);
        this.inventory = inventory;
        this.changeCallback = changeCallback;
    }

    public int preferredHeight(int parentHeight) {
        return Constants.INVENTORY_HEIGHT;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(new DrawContextCompat(context), mouseX, mouseY);
    }

    @Override
    protected void drawForeground(DrawContextCompat context, int mouseX, int mouseY) {
        context.drawText(client.textRenderer, inventory.getName(), 8, 6, 0x404040, false);
    }

    @Override
    protected void drawBackground(DrawContextCompat context) {
        final var mid_bottom_cut = 2;
        final var top_section_y = 0;
        final var top_section_h = Constants.TEXTURE_HEIGHT - Constants.BUTTON_SIZE - Constants.BOTTOM;
        final var middle_section_y = Constants.TOP + Constants.BUTTON_SIZE;
        final var middle_section_h = 2 * Constants.BUTTON_SIZE - mid_bottom_cut;
        final var bottom_section_y = Constants.TEXTURE_HEIGHT - Constants.BOTTOM - mid_bottom_cut;
        final var bottom_section_h = Constants.BOTTOM + mid_bottom_cut;
        drawBackgroundSection(context, 0, top_section_y, top_section_h);
        drawBackgroundSection(context, top_section_h, middle_section_y, middle_section_h);
        drawBackgroundSection(context, top_section_h + middle_section_h, bottom_section_y, bottom_section_h);

        Identifier identifier = handler.shouldShowScrollbar() ? SCROLLER_TEXTURE : SCROLLER_DISABLED_TEXTURE;

        final var scrollableSpace = SCROLLBAR_BOTTOM - SCROLLBAR_TOP - Constants.SCROLLBAR_HEIGHT - 1;
        context.drawGuiTexture(identifier,
                SCROLLBAR_LEFT + 2,
                SCROLLBAR_TOP + (int) ((float) scrollableSpace * this.scrollPosition),
                Constants.SCROLLBAR_WIDTH, Constants.SCROLLBAR_HEIGHT);
    }

    private void drawBackgroundSection(DrawContextCompat context, int y, int v, int height) {
        context.drawTexture(Constants.ITEMS_BG, 0, y, 0, v, Constants.INVENTORY_WIDTH, height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isClickInScrollbar(mouseX, mouseY)) {
            scrolling = handler.shouldShowScrollbar();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.scrolling) {
            final int y = centerY();
            final int top = y + SCROLLBAR_TOP;
            final int bottom = y + SCROLLBAR_BOTTOM;
            final float thumb = (float) Constants.SCROLLBAR_HEIGHT;

            scrollPosition = ((float) mouseY - (float) top - thumb / 2) / ((float) (bottom - top + 1) - thumb);
            scrollPosition = MathHelper.clamp(scrollPosition, 0.0F, 1.0F);
            handler.scrollItems(scrollPosition);

            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.scrolling = false;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isClickInScrollbar(double mouseX, double mouseY) {
        final int y = centerY();
        final int top = y + Constants.TOP;
        final int bottom = y + Constants.INVENTORY_HEIGHT - Constants.BOTTOM;

        final int x = centerX();
        final int left = x + Constants.INVENTORY_WIDTH - Constants.DX;
        final int right = left - 14;
        return mouseX >= (double) right && mouseY >= (double) top && mouseX < (double) left && mouseY < (double) bottom;
    }

    // Inventory widget
    public void applyPendingValue() {
        final var ids = inventory.serialize();
        changeCallback.accept(ids);
    }

    public void refreshStacks() {
        int offset = handler.getRow(this.scrollPosition);
        handler.reset();
        this.scrollPosition = handler.getScrollPosition(offset);
        handler.scrollItems(this.scrollPosition);
    }

    private void frameInside(DrawContext context, int x, int y, int width, int height, int top, int left, int bottom,
            int right, int color) {
        context.fill(x, y, x + width, y + top, color);
        context.fill(x, y + height - bottom, x + width, y + height, color);
        context.fill(x, y + top, x + left, y + height - bottom, color);
        context.fill(x + width - right, y + top, x + width, y + height - bottom, color);
    }

    private void frameInside(DrawContext context, int x, int y, int width, int height, int color) {
        frameInside(context, x, y, width, height, 1, 1, 1, 1, color);
    }
}
