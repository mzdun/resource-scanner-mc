// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.modmenu.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;

import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class WidgetList extends ElementListWidget<WidgetList.WidgetEntry> {
    public WidgetList(MinecraftClient client, ThreePartsLayoutWidget layout) {
        super(client, Constants.INVENTORY_WIDTH, layout.getContentHeight(), layout.getHeaderHeight(),
                Constants.ITEM_HEIGHT);
        this.centerListVertically = false;
    }

    public void addOption(SimpleOption<?> option) {
        this.addEntry(OptionWidgetEntry.wrap(this.client.options, option));
    }

    public void addWidget(Widget widget) {
        this.addEntry(WidgetEntry.wrap(widget));
    }

    public int getRowWidth() {
        return Constants.INVENTORY_WIDTH;
    }

    public void applyAllPendingValues() {
        for (final var entry : children()) {
            if (entry instanceof OptionWidgetEntry optionWidgetEntry) {
                if (optionWidgetEntry.widget instanceof SimpleOption.OptionSliderWidgetImpl<?> optionSliderWidgetImpl) {
                    optionSliderWidgetImpl.applyPendingValue();
                }
            } else if (entry.widget instanceof InventoryWidget inventory) {
                inventory.applyPendingValue();
            }
        }
    }

    public Optional<Element> getHoveredWidget(double mouseX, double mouseY) {
        for (final var entry : children()) {
            for (final var element : entry.children()) {
                if (element.isMouseOver(mouseX, mouseY)) {
                    return Optional.of(element);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public void position(int width, int height, int y) {
        super.position(width, height, y);
        layout();
    }

    public void layout() {
        var width = Constants.CONTENT_WIDTH;
        var availableHeight = getHeight();
        final var screenWidth = getWidth();

        final var x = (screenWidth - width) / 2;
        var y = getY();

        for (final var entry : children()) {
            final var heightUsed = entry.moveTo(x, y, width, availableHeight);
            y += heightUsed + 2;
            availableHeight -= heightUsed + 2;
        }
    }

    @Environment(EnvType.CLIENT)
    protected static class WidgetEntry extends ElementListWidget.Entry<WidgetEntry> {
        final Widget widget;

        WidgetEntry(Widget widget) {
            this.widget = widget;
        }

        public static WidgetEntry wrap(Widget widget) {
            return new WidgetEntry(widget);
        }

        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
                int mouseY, boolean hovered, float tickDelta) {
            if (widget instanceof Drawable drawable) {
                drawable.render(context, mouseX, mouseY, tickDelta);
            }
        }

        public List<? extends Element> children() {
            if (widget instanceof Element element) {
                return List.of(element);
            }
            return List.of();
        }

        public List<? extends Selectable> selectableChildren() {
            if (widget instanceof Selectable selectable) {
                return List.of(selectable);
            }
            return List.of();
        }

        public int moveTo(int x, int y, int width, int availableHeight) {
            var preferredSep = Constants.OPTION_SEP;
            var preferredHeight = Constants.ITEM_CONTENT_HEIGHT;
            if (widget instanceof MoveableWidget moveable) {
                preferredSep = moveable.preferredSeparator();

                final var available = Math.max(0, availableHeight - preferredSep);

                preferredHeight = moveable.preferredHeight(available);
                preferredHeight = preferredHeight < 0 ? widget.getHeight() : preferredHeight;
            }

            if (widget instanceof FocusedWidget resizeable)
                resizeable.setDimensions(width, preferredHeight);
            else if (widget instanceof ClickableWidget resizeable)
                resizeable.setDimensions(width, preferredHeight);
            widget.setPosition(x, y + preferredSep);
            return preferredHeight + preferredSep;
        }
    }

    @Environment(EnvType.CLIENT)
    protected static class OptionWidgetEntry extends WidgetEntry {
        final SimpleOption<?> option;

        private OptionWidgetEntry(SimpleOption<?> option, ClickableWidget widget) {
            super(widget);
            this.option = option;
        }

        public static OptionWidgetEntry wrap(GameOptions gameOptions, SimpleOption<?> option) {
            final var widget = option.createWidget(gameOptions, 0, 0, Constants.INVENTORY_WIDTH);
            return new OptionWidgetEntry(option, widget);
        }
    }
}
