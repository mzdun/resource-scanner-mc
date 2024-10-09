// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.modmenu;

import com.midnightbits.scanner.modmenu.gui.Constants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;

@Environment(value = EnvType.CLIENT)
public class InventoryHandler extends ScreenHandler {
    private final DefaultedList<ItemStack> itemList = DefaultedList.of();
    private final SimpleInventory lockableInventory;

    public InventoryHandler(ScannerInventory inventory) {
        super(null, 0);

        this.lockableInventory = new SimpleInventory(Constants.ROWS_COUNT * Constants.COLUMNS_COUNT);

        for (int row = 0; row < Constants.ROWS_COUNT; ++row) {
            final var idOffset = row * Constants.COLUMNS_COUNT;
            for (int col = 0; col < Constants.COLUMNS_COUNT; ++col) {
                this.addSlot(new OreSlot(lockableInventory,
                        idOffset + col, Constants.DX + col * Constants.BUTTON_SIZE,
                        Constants.TOP + row * Constants.BUTTON_SIZE));
            }
        }
        final var topRow = Constants.INVENTORY_HEIGHT - Constants.BOTTOM - 2 * Constants.BUTTON_SIZE;
        for (int row = 0; row < Constants.AT_HAND_ROWS_COUNT; ++row) {
            final var idOffset = row * Constants.COLUMNS_COUNT;
            for (int col = 0; col < Constants.COLUMNS_COUNT; ++col) {
                this.addSlot(new Slot(inventory, idOffset + col, Constants.DX + col * Constants.BUTTON_SIZE,
                        topRow + row * Constants.BUTTON_SIZE));
            }
        }
        this.scrollItems(0.0f);
    }

    public void reset() {
        itemList.clear();
        itemList.addAll(
                Registries.ITEM
                        .stream()
                        .map(Registries.ITEM::getEntry)
                        .filter(entry -> entry.isIn(Constants.ORES_TAG))
                        .map(ItemStack::new).toList());
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public int getOverflowRows() {
        return MathHelper.ceilDiv(this.itemList.size(), Constants.COLUMNS_COUNT) - Constants.ROWS_COUNT;
    }

    public int getRow(float scroll) {
        return Math.max((int) ((double) (scroll * (float) this.getOverflowRows()) + 0.5), 0);
    }

    public float getScrollPosition(int row) {
        return MathHelper.clamp((float) row / (float) this.getOverflowRows(), 0.0f, 1.0f);
    }

    public float getScrollPosition(float current, double amount) {
        return MathHelper.clamp(current - (float) (amount / (double) this.getOverflowRows()), 0.0f, 1.0f);
    }

    public void scrollItems(float position) {
        int scrolledRow = this.getRow(position);
        for (int row = 0; row < Constants.ROWS_COUNT; ++row) {
            for (int col = 0; col < Constants.COLUMNS_COUNT; ++col) {
                final var scrolledIndex = col + (row + scrolledRow) * Constants.COLUMNS_COUNT;
                final var slotIndex = col + row * Constants.COLUMNS_COUNT;
                if (scrolledIndex >= 0 && scrolledIndex < this.itemList.size()) {
                    lockableInventory.setStack(slotIndex, this.itemList.get(scrolledIndex));
                } else {
                    lockableInventory.setStack(slotIndex, ItemStack.EMPTY);
                }
            }
        }
    }

    public boolean shouldShowScrollbar() {
        return this.itemList.size() > Constants.ROWS_COUNT * Constants.COLUMNS_COUNT;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        if (slot >= this.slots.size() - Constants.COLUMNS_COUNT && slot < this.slots.size()) {
            Slot dst = this.slots.get(slot);
            final var tgt = dst.getStack();
            if (tgt != null || dst.hasStack()) {
                dst.setStack(ItemStack.EMPTY);
            }
            return tgt;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return slot.inventory != lockableInventory;
    }

    @Override
    public boolean canInsertIntoSlot(Slot slot) {
        return slot.inventory != lockableInventory;
    }

    @Environment(value = EnvType.CLIENT)
    public static class OreSlot
            extends Slot {
        public OreSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            ItemStack itemStack = this.getStack();
            if (super.canTakeItems(playerEntity) && !itemStack.isEmpty()) {
                return itemStack.isIn(Constants.ORES_TAG);
            }
            return itemStack.isEmpty();
        }
    }
}
