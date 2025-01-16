// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.modmenu;

import api.compat.ScreenCompat;
import com.midnightbits.scanner.modmenu.gui.InventoryWidget;
import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.core.ScannerMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Consumer;

@Environment(value = EnvType.CLIENT)
public class InventoryScreen extends ScreenCompat {
    private final Screen parent;
    private final Set<Id> oreIds;
    private final Consumer<Set<Id>> inventoryChangeCallback;
    @Nullable
    protected InventoryWidget inventory;
    public final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

    public InventoryScreen(Set<Id> ids, Screen parent, Consumer<Set<Id>> inventoryChangeCallback) {
        super(Text.translatable(ScannerMod.translationKey("screens", "ores")));
        this.parent = parent;
        this.oreIds = ids;
        this.inventoryChangeCallback = inventoryChangeCallback;
    }

    @Override
    protected void init() {
        this.initHeader();
        this.initBody();
        this.initFooter();
        this.layout.forEachChild(this::addDrawableChild);
        if (inventory != null)
            addDrawableChild(inventory);
        this.refreshWidgetPositions();
    }

    @Override
    public final void tick() {
        super.tick();
        if (client != null && client.player != null) {
            if (this.client.player.isAlive() && !this.client.player.isRemoved()) {
                updateDisplayParameters(client.player.networkHandler.getEnabledFeatures(),
                        client.player.getWorld().getRegistryManager(), false);
            } else {
                this.client.player.closeHandledScreen();
            }
        }
    }

    private void updateDisplayParameters(FeatureSet enabledFeatures, RegistryWrapper.WrapperLookup registryLookup,
            boolean newInventory) {
        if (inventory != null) {
            final var updatedDisplay = ItemGroups.updateDisplayContext(enabledFeatures, false, registryLookup);
            if (newInventory || updatedDisplay) {
                inventory.refreshStacks();
            }
        }
    }

    protected void initHeader() {
        layout.addHeader(title, textRenderer);
    }

    protected void initBody() {
        if (client == null || client.world == null)
            return;

        inventory = layout.addBody(new InventoryWidget(client, new ScannerInventory(oreIds), inventoryChangeCallback));
        updateDisplayParameters(client.world.getEnabledFeatures(), client.world.getRegistryManager(), true);
    }

    protected void initFooter() {
        layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
            close();
        }).width(200).build());
    }

    @Override
    protected void refreshWidgetPositions() {
        layout.refreshPositions();
        if (inventory != null) {
            inventory.setDimensions(width, layout.getContentHeight());
            inventory.setPosition(0, layout.getHeaderHeight());
        }
    }

    @Override
    public void removed() {
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }

        if (inventory != null) {
            inventory.applyPendingValue();
        }
    }
}
