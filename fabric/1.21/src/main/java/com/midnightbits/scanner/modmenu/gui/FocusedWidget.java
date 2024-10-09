// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.modmenu.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Environment(value = EnvType.CLIENT)
public abstract class FocusedWidget implements Element, MoveableWidget, Widget, Selectable {
    protected boolean focused = false;
    protected boolean hovered = false;
    protected int width;
    protected int height;
    protected int x;
    protected int y;

    public FocusedWidget(int width, int height) {
        this.width = width;
        this.height = height;
        this.x = 0;
        this.y = 0;
    }

    // Element (minus key presses)

    @Nullable
    @Override
    public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        return !this.isFocused() ? GuiNavigationPath.of(this) : null;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    // Widget
    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {
    }

    @Override
    public ScreenRect getNavigationFocus() {
        return new ScreenRect(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    // Narratable (via Selectable)

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
    }

    // Selectable
    @Override
    public SelectionType getType() {
        return isFocused() ? SelectionType.FOCUSED : isHovered() ? SelectionType.HOVERED : SelectionType.NONE;
    }

    // FocusedWidget
    public boolean isHovered() {
        return hovered;
    }

    public void setHovered(boolean hovering) {
        this.hovered = hovering;
    }

    protected static boolean mouseInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return ((double) x) <= mouseX && (double) (x + width) > mouseX && ((double) y) <= mouseY
                && (double) (y + height) > mouseY;

    }
}
