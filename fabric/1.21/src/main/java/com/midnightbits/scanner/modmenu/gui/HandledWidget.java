// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.modmenu.gui;

import api.compat.DrawContextCompat;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.midnightbits.scanner.modmenu.InventoryHandler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

@Environment(value = EnvType.CLIENT)
public abstract class HandledWidget<T extends ScreenHandler> extends CenteredWidget {
    private static final int HIGHLIGHT = 0x80FFFFFF;
    private static final long DOUBLECLICK_MS = 250L;

    protected final T handler;
    protected MinecraftClient client;
    @Nullable
    protected Slot focusedSlot;
    @Nullable
    private Slot touchDragSlotStart;
    @Nullable
    private Slot touchDropOriginSlot;
    @Nullable
    private Slot touchHoveredSlot;
    @Nullable
    private Slot lastClickedSlot;
    private boolean touchIsRightClickDrag;
    private ItemStack touchDragStack;
    private int touchDropX;
    private int touchDropY;
    private long touchDropTime;
    private ItemStack touchDropReturningStack;
    private long touchDropTimer;
    protected final Set<Slot> cursorDragSlots;
    protected boolean cursorDragging;
    private int heldButtonType;
    private int heldButtonCode;
    private boolean cancelNextRelease;
    private long lastButtonClickTime;
    private int lastClickedButton;
    private boolean lastClickOutsideBounds;
    private boolean doubleClicking;
    private ItemStack quickMovingStack;

    public HandledWidget(T handler, MinecraftClient client, int width, int height) {
        super(width, height, Constants.INVENTORY_WIDTH, Constants.INVENTORY_HEIGHT);
        this.handler = handler;
        this.client = client;
        this.touchDragStack = ItemStack.EMPTY;
        this.touchDropReturningStack = ItemStack.EMPTY;
        this.cursorDragSlots = Sets.newHashSet();
        this.quickMovingStack = ItemStack.EMPTY;
        this.cancelNextRelease = true;
    }

    // Element (minus key presses)

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean isMouseButtonItemPicker = client.options.pickItemKey.matchesMouse(button);
        Slot slot = getSlotAt(mouseX, mouseY);

        long now = Util.getMeasuringTimeMs();
        long sinceLastClick = now - lastButtonClickTime;

        doubleClicking = lastClickedSlot == slot && sinceLastClick < DOUBLECLICK_MS && lastClickedButton == button;
        cancelNextRelease = false;
        if (button != 0 && button != 1 && !isMouseButtonItemPicker) {
            onMouseClick(button);
        } else {
            final var touchscreenSupported = client.options.getTouchscreen().getValue();
            final var droppingOutside = isClickOutsideBounds(mouseX, mouseY, button);
            var slotId = slot != null ? slot.id : -1;

            if (droppingOutside) {
                slotId = -999;
            }

            if (touchscreenSupported && droppingOutside && handler.getCursorStack().isEmpty()) {
                if (client.currentScreen != null)
                    client.currentScreen.close();
                return true;
            }

            if (slotId != -1) {
                if (touchscreenSupported) {
                    if (slot != null && slot.hasStack()) {
                        touchDragSlotStart = slot;
                        touchDragStack = ItemStack.EMPTY;
                        touchIsRightClickDrag = button == 1;
                    } else {
                        touchDragSlotStart = null;
                    }
                } else if (!cursorDragging) {
                    if (handler.getCursorStack().isEmpty()) {
                        if (isMouseButtonItemPicker) {
                            onMouseClick(slot, slotId, button, SlotActionType.CLONE);
                        } else {
                            boolean shiftPressed = slotId != -999 && Screen.hasShiftDown();
                            SlotActionType slotActionType = SlotActionType.PICKUP;
                            if (shiftPressed) {
                                quickMovingStack = slot.hasStack() ? slot.getStack().copyWithCount(1) : ItemStack.EMPTY;
                                slotActionType = SlotActionType.QUICK_MOVE;
                            } else if (slotId == -999) {
                                slotActionType = SlotActionType.THROW;
                            }

                            onMouseClick(slot, slotId, button, slotActionType);
                        }

                        cancelNextRelease = true;
                    } else {
                        cursorDragging = true;
                        heldButtonCode = button;
                        cursorDragSlots.clear();
                        if (button == 0) {
                            heldButtonType = 0;
                        } else if (button == 1) {
                            heldButtonType = 1;
                        } else {
                            heldButtonType = 2;
                        }
                    }
                }
            }
        }

        lastClickedSlot = slot;
        lastButtonClickTime = now;
        lastClickedButton = button;
        return true;
    }

    private void onMouseClick(int button) {
        if (focusedSlot != null && handler.getCursorStack().isEmpty()) {
            if (client.options.swapHandsKey.matchesMouse(button)) {
                onMouseClick(focusedSlot, focusedSlot.id, 40, SlotActionType.SWAP);
                return;
            }

            for (int i = 0; i < 9; ++i) {
                if (client.options.hotbarKeys[i].matchesMouse(button)) {
                    onMouseClick(focusedSlot, focusedSlot.id, i, SlotActionType.SWAP);
                }
            }
        }

    }

    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int button) {
        lastClickOutsideBounds = !mouseInside(mouseX, mouseY, centerX(), centerY(), contentWidth, contentHeight);
        return lastClickOutsideBounds;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        Slot slot = getSlotAt(mouseX, mouseY);
        if (touchDragSlotStart != null && client.options.getTouchscreen().getValue()) {
            if (button == 0 || button == 1) {
                if (touchDragStack.isEmpty()) {
                    if (slot != touchDragSlotStart && !touchDragSlotStart.getStack().isEmpty()) {
                        touchDragStack = touchDragSlotStart.getStack().copyWithCount(1);
                    }
                } else if (touchDragStack.getCount() > 1 && slot != null
                        && ScreenHandler.canInsertItemIntoSlot(slot, touchDragStack, false)) {
                    long l = Util.getMeasuringTimeMs();
                    if (touchHoveredSlot == slot) {
                        if (l - touchDropTimer > 500L) {
                            onMouseClick(touchDragSlotStart, touchDragSlotStart.id, 0, SlotActionType.PICKUP);
                            onMouseClick(slot, slot.id, 1, SlotActionType.PICKUP);
                            onMouseClick(touchDragSlotStart, touchDragSlotStart.id, 0, SlotActionType.PICKUP);
                            touchDropTimer = l + 750L;
                            touchDragStack.decrement(1);
                        }
                    } else {
                        touchHoveredSlot = slot;
                        touchDropTimer = l;
                    }
                }
            }
        }

        return true;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Slot slot = getSlotAt(mouseX, mouseY);
        int x = centerX();
        int y = centerY();
        final var droppingOutside = isClickOutsideBounds(mouseX, mouseY, button);
        var slotId = slot != null ? slot.id : -1;

        if (droppingOutside) {
            slotId = -999;
        }

        if (doubleClicking && slot != null && button == 0 && handler.canInsertIntoSlot(ItemStack.EMPTY, slot)) {
            if (Screen.hasShiftDown()) {
                if (!quickMovingStack.isEmpty()) {

                    for (Slot handlerSlot : handler.slots) {
                        if (handlerSlot != null && handlerSlot.canTakeItems(client.player) && handlerSlot.hasStack()
                                && handlerSlot.inventory == slot.inventory
                                && ScreenHandler.canInsertItemIntoSlot(handlerSlot, quickMovingStack, true)) {
                            onMouseClick(handlerSlot, handlerSlot.id, button, SlotActionType.QUICK_MOVE);
                        }
                    }
                }
            } else {
                onMouseClick(slot, slotId, button, SlotActionType.PICKUP_ALL);
            }

            doubleClicking = false;
            lastButtonClickTime = 0L;
        } else {
            if (cursorDragging && heldButtonCode != button) {
                cursorDragging = false;
                cursorDragSlots.clear();
                cancelNextRelease = true;
                return true;
            }

            if (cancelNextRelease) {
                cancelNextRelease = false;
                return true;
            }

            if (touchDragSlotStart != null && (Boolean) client.options.getTouchscreen().getValue()) {
                if (button == 0 || button == 1) {
                    if (touchDragStack.isEmpty() && slot != touchDragSlotStart) {
                        touchDragStack = touchDragSlotStart.getStack();
                    }

                    final var canInsert = ScreenHandler.canInsertItemIntoSlot(slot, touchDragStack, false);
                    if (slotId != -1 && !touchDragStack.isEmpty() && canInsert) {
                        onMouseClick(touchDragSlotStart, touchDragSlotStart.id, button, SlotActionType.PICKUP);
                        onMouseClick(slot, slotId, 0, SlotActionType.PICKUP);
                        if (handler.getCursorStack().isEmpty()) {
                            touchDropReturningStack = ItemStack.EMPTY;
                        } else {
                            onMouseClick(touchDragSlotStart, touchDragSlotStart.id, button, SlotActionType.PICKUP);
                            touchDropX = MathHelper.floor(mouseX - (double) x);
                            touchDropY = MathHelper.floor(mouseY - (double) y);
                            touchDropOriginSlot = touchDragSlotStart;
                            touchDropReturningStack = touchDragStack;
                            touchDropTime = Util.getMeasuringTimeMs();
                        }
                    } else if (!touchDragStack.isEmpty()) {
                        touchDropX = MathHelper.floor(mouseX - (double) x);
                        touchDropY = MathHelper.floor(mouseY - (double) y);
                        touchDropOriginSlot = touchDragSlotStart;
                        touchDropReturningStack = touchDragStack;
                        touchDropTime = Util.getMeasuringTimeMs();
                    }

                    endTouchDrag();
                }
            } else if (cursorDragging && !cursorDragSlots.isEmpty()) {
                onMouseClick(null, -999, ScreenHandler.packQuickCraftData(0, heldButtonType),
                        SlotActionType.QUICK_CRAFT);

                for (Slot cursorDragSlot : cursorDragSlots) {
                    onMouseClick(cursorDragSlot, cursorDragSlot.id,
                            ScreenHandler.packQuickCraftData(1, this.heldButtonType), SlotActionType.QUICK_CRAFT);
                }

                this.onMouseClick(null, -999, ScreenHandler.packQuickCraftData(2, this.heldButtonType),
                        SlotActionType.QUICK_CRAFT);
            } else if (!handler.getCursorStack().isEmpty()) {
                if (client.options.pickItemKey.matchesMouse(button)) {
                    onMouseClick(slot, slotId, button, SlotActionType.CLONE);
                } else {
                    final var shiftPressed = slotId != -999 && Screen.hasShiftDown();
                    if (shiftPressed) {
                        quickMovingStack = slot != null && slot.hasStack() ? slot.getStack().copyWithCount(1)
                                : ItemStack.EMPTY;
                    }

                    onMouseClick(slot, slotId, button,
                            shiftPressed ? SlotActionType.QUICK_MOVE : SlotActionType.PICKUP);
                }
            }
        }

        if (handler.getCursorStack().isEmpty()) {
            lastButtonClickTime = 0L;
        }

        cursorDragging = false;
        return true;
    }

    public void endTouchDrag() {
        touchDragStack = ItemStack.EMPTY;
        touchDragSlotStart = null;
    }

    @Nullable
    private Slot getSlotAt(double x, double y) {
        for (final var slot : handler.slots) {
            if (isPointOverSlot(slot, x, y) && slot.isEnabled()) {
                return slot;
            }
        }

        return null;
    }

    private boolean isPointOverSlot(Slot slot, double pointX, double pointY) {
        return mouseInside(pointX, pointY, centerX() + slot.x, centerY() + slot.y, Constants.ICON_SIZE,
                Constants.ICON_SIZE);
    }

    protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
        actionType = slotId == -999 && actionType == SlotActionType.PICKUP ? SlotActionType.THROW : actionType;
        ItemStack itemStack;
        if (slot == null && actionType != SlotActionType.QUICK_CRAFT) {
            if (!(handler.getCursorStack().isEmpty() && lastClickOutsideBounds)) {
                if (button == 0) {
                    handler.setCursorStack(ItemStack.EMPTY);
                }
            }
        } else {
            if (slot != null && !slot.canTakeItems(this.client.player)) {
                return;
            }

            handler.onSlotClick(slot == null ? slotId : slot.id, button, actionType, client.player);
        }

    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
            if (client.currentScreen != null)
                client.currentScreen.close();
            return true;
        } else {
            handleHotbarKeyPressed(keyCode, scanCode);
            if (focusedSlot != null && focusedSlot.hasStack()) {
                if (client.options.pickItemKey.matchesKey(keyCode, scanCode)) {
                    onMouseClick(focusedSlot, focusedSlot.id, 0, SlotActionType.CLONE);
                } else if (client.options.dropKey.matchesKey(keyCode, scanCode)) {
                    onMouseClick(focusedSlot, focusedSlot.id, Screen.hasControlDown() ? 1 : 0, SlotActionType.THROW);
                }
            }

            return true;
        }
    }

    protected void handleHotbarKeyPressed(int keyCode, int scanCode) {
        if (handler.getCursorStack().isEmpty() && focusedSlot != null) {
            if (client.options.swapHandsKey.matchesKey(keyCode, scanCode)) {
                onMouseClick(focusedSlot, focusedSlot.id, 40, SlotActionType.SWAP);
                return; // true;
            }

            for (int i = 0; i < 9; ++i) {
                if (client.options.hotbarKeys[i].matchesKey(keyCode, scanCode)) {
                    onMouseClick(focusedSlot, focusedSlot.id, i, SlotActionType.SWAP);
                    return; // true;
                }
            }
        }

        // return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseInside(mouseX, mouseY, centerX(), centerY(), contentWidth, contentHeight);
    }

    // CenteredWidget

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        final var compat = new DrawContextCompat(context);
        drawBackground(compat);
        drawSlots(compat, mouseX, mouseY);
        drawForeground(compat, mouseX, mouseY);
        drawDragAndDrop(compat, mouseX, mouseY, delta);
    }

    protected void drawBackground(DrawContextCompat context) {
        final var x = 0;
        final var y = 0;

        context.drawTexture(Constants.ITEMS_BG, x, y, 0, 0, contentWidth, contentHeight);
    }

    protected void drawSlots(DrawContextCompat context, int mouseX, int mouseY) {
        focusedSlot = null;
        for (final Slot slot : handler.slots) {
            if (!slot.isEnabled()) {
                continue;
            }

            drawSlot(context, slot);

            final var D = (Constants.BUTTON_SIZE - Constants.ICON_SIZE) / 2;
            if (mouseInside(mouseX, mouseY, slot.x - D, slot.y - D, Constants.BUTTON_SIZE, Constants.BUTTON_SIZE)) {
                focusedSlot = slot;
                if (focusedSlot.canBeHighlighted()) {
                    drawSlotHighlight(context, slot.x, slot.y, 0);
                }
            }
        }
    }

    protected void drawForeground(DrawContextCompat context, int mouseX, int mouseY) {

    }

    protected void drawDragAndDrop(DrawContextCompat context, int mouseX, int mouseY, float delta) {
        ItemStack itemStack = touchDragStack.isEmpty() ? handler.getCursorStack() : touchDragStack;
        if (!itemStack.isEmpty()) {
            itemStack = itemStack.copyWithCount(1);
            drawItem(context, itemStack, mouseX - 8, mouseY - (touchDragStack.isEmpty() ? 8 : 16));
        }

        if (!touchDropReturningStack.isEmpty()) {
            var duration = (float) (Util.getMeasuringTimeMs() - touchDropTime) / 100.0F;
            if (duration >= 1.0F) {
                duration = 1.0F;
                touchDropReturningStack = ItemStack.EMPTY;
            }

            final var origX = touchDropOriginSlot == null ? touchDropX : touchDropOriginSlot.x;
            final var origY = touchDropOriginSlot == null ? touchDropY : touchDropOriginSlot.y;

            final var distX = origX - touchDropX;
            final var distY = origY - touchDropY;

            final var x = touchDropX + (int) ((float) distX * duration);
            final var y = touchDropY + (int) ((float) distY * duration);

            drawItem(context, touchDropReturningStack, x, y);
        }
    }

    protected void drawSlot(DrawContextCompat context, Slot slot) {
        var itemStack = slot.getStack();
        var bl = false;
        var alreadyDrawn = slot == touchDragSlotStart && !touchDragStack.isEmpty() && !touchIsRightClickDrag;
        var cursorStack = handler.getCursorStack();

        if (slot == touchDragSlotStart && !touchDragStack.isEmpty() && touchIsRightClickDrag && !itemStack.isEmpty()) {
            itemStack = itemStack.copyWithCount(1);
        } else if (cursorDragging && cursorDragSlots.contains(slot) && !cursorStack.isEmpty()) {
            if (cursorDragSlots.size() == 1) {
                return;
            }

            if (ScreenHandler.canInsertItemIntoSlot(slot, cursorStack, true) && handler.canInsertIntoSlot(slot)) {
                bl = true;
                itemStack = cursorStack.copyWithCount(1);
            } else {
                cursorDragSlots.remove(slot);
            }
        }

        context.getMatrices().push();
        context.getMatrices().translate(0.0F, 0.0F, 100.0F);
        if (itemStack.isEmpty() && slot.isEnabled()) {
            alreadyDrawn = context.drawSlotSprite(slot, client);
        }

        if (!alreadyDrawn) {
            if (bl) {
                context.fill(slot.x, slot.y, slot.x + Constants.ICON_SIZE, slot.y + Constants.ICON_SIZE, HIGHLIGHT);
            }

            final var seed = slot.x + slot.y * contentWidth;
            if (slot.disablesDynamicDisplay()) {
                context.drawItemWithoutEntity(itemStack, slot.x, slot.y, seed);
            } else {
                context.drawItem(itemStack, slot.x, slot.y, seed);
            }
        }

        context.getMatrices().pop();
    }

    public static void drawSlotHighlight(DrawContextCompat context, int x, int y, int z) {
        context.fillGradient(RenderLayer.getGuiOverlay(), x, y, x + Constants.ICON_SIZE, y + Constants.ICON_SIZE,
                HIGHLIGHT, HIGHLIGHT, z);
    }

    private void drawItem(DrawContextCompat context, ItemStack stack, int x, int y) {
        context.getMatrices().push();
        context.getMatrices().translate(0.0F, 0.0F, 232.0F);
        context.drawItem(stack, x, y);
        context.getMatrices().pop();
    }

    protected void drawMouseoverTooltip(DrawContextCompat context, int x, int y) {
        if (handler.getCursorStack().isEmpty() && focusedSlot != null && focusedSlot.hasStack() && client != null) {
            ItemStack itemStack = focusedSlot.getStack();
            context.drawTooltip(client.textRenderer, getTooltipFromItem(client, itemStack), itemStack.getTooltipData(),
                    x, y);
        }
    }

    public List<Text> getTooltipFromItem(MinecraftClient client, ItemStack stack) {
        boolean isOreSlot = focusedSlot != null && focusedSlot instanceof InventoryHandler.OreSlot;
        final var default_ = client.options.advancedItemTooltips ? TooltipType.Default.ADVANCED
                : TooltipType.Default.BASIC;
        final var tooltipType = isOreSlot ? default_.withCreative() : default_;
        final var list = stack.getTooltip(Item.TooltipContext.create(client.world), client.player, tooltipType);
        final var result = Lists.newArrayList(list);

        int i = 1;

        for (ItemGroup itemGroup : ItemGroups.getGroupsToDisplay()) {
            if (itemGroup.getType() != ItemGroup.Type.SEARCH && itemGroup.contains(stack)) {
                result.add(i++, itemGroup.getDisplayName().copy().formatted(Formatting.BLUE));
            }
        }

        return result;
    }
}
