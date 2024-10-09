// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.modmenu.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import org.jetbrains.annotations.Nullable;

@Environment(value = EnvType.CLIENT)
public abstract class SingleColumnOptions extends Screen {
    private final Screen parent;
    @Nullable
    protected WidgetList body;
    public final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

    protected SingleColumnOptions(Screen parent, Text title) {
        super(title);
        this.parent = parent;
    }

    @Override
    protected void init() {
        initHeader();
        initBody();
        initFooter();
        layout.forEachChild(this::addDrawableChild);
        initTabNavigation();
    }

    protected void initHeader() {
        layout.addHeader(title, textRenderer);
    }

    protected void initBody() {
        body = layout.addBody(new WidgetList(client, layout));
        addOptions();
    }

    protected abstract void addOptions();

    protected void initFooter() {
        layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
            close();
        }).width(200).build());
    }

    protected void initTabNavigation() {
        layout.refreshPositions();
        if (body != null) {
            body.position(width, layout);
        }
    }

    public void removed() {
        if (client != null)
            client.options.write();
    }

    public void close() {
        if (this.body != null) {
            this.body.applyAllPendingValues();
        }

        if (client != null)
            client.setScreen(parent);
    }
}
