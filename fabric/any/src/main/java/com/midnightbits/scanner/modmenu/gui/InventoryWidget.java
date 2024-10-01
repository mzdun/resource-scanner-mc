package com.midnightbits.scanner.modmenu.gui;

import com.midnightbits.scanner.modmenu.InventoryHandler;
import com.midnightbits.scanner.modmenu.ScannerInventory;
import com.midnightbits.scanner.rt.core.Id;
import com.mojang.datafixers.kinds.Const;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.function.Consumer;

// TODO: Remaining issues and missing features:
//       - Picking up item stack from upper slots should leave the stack in the inventory
//       - Dropping item stack _anywhere_ should merge with preexisting stack (count still 1)
//       - Add scrolling (low prio, nothing _to_ scroll)
//       - _Nice to have_: Dropping item to on-hand inventory should re-sort that inventory

@Environment(value = EnvType.CLIENT)
public class InventoryWidget extends HandledWidget<InventoryHandler> {
    private final ScannerInventory inventory;
    private float scrollPosition;
    private final Consumer<Set<Id>> changeCallback;

    public InventoryWidget(MinecraftClient client, ScannerInventory inventory, Consumer<Set<Id>> changeCallback) {
        super(new InventoryHandler(inventory), client, Constants.INVENTORY_WIDTH, Constants.INVENTORY_HEIGHT);
        this.inventory = inventory;
        this.changeCallback = changeCallback;
    }

    public int preferredWidth() {
        return Constants.INVENTORY_WIDTH;
    }

    public int preferredHeight(int parentHeight) {
        return Constants.INVENTORY_HEIGHT;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(client.textRenderer, inventory.getName(), 8, 6, 0x404040, false);
    }

    @Override
    protected void drawBackground(DrawContext context) {
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
    }

    private void drawBackgroundSection(DrawContext context, int y, int u, int height) {
        context.drawTexture(Constants.ITEMS_BG, 0, y, 0, u, Constants.INVENTORY_WIDTH, height);
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
