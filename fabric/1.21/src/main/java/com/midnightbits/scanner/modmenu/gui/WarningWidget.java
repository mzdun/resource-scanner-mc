// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.modmenu.gui;

import api.compat.DrawContextCompat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Environment(value = EnvType.CLIENT)
public class WarningWidget extends ClickableWidget implements MoveableWidget {
    final MinecraftClient client;

    public WarningWidget(MinecraftClient client, int width, int height, String message) {
        super(0, 0, width, height, Text.translatable(message).formatted(Formatting.ITALIC));
        this.client = client;
        this.active = false;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        final var color = isFocused() ? 0xFFFFFF : 0x80FFFFFF;
        final var compat = new DrawContextCompat(context);
        compat.drawTextWrapped(client.textRenderer, getMessage(), getX(), getY(), getWidth(), color);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        final var message = Text.translatable("option.resource-scanner.message_narrate", getMessage());
        builder.put(NarrationPart.TITLE, message);
    }

    @Override
    public int preferredSeparator() {
        return Constants.WARNING_OPTION_SEP;
    }

    @Override
    public int preferredHeight(int parentHeight) {
        return 40;
    }
}
