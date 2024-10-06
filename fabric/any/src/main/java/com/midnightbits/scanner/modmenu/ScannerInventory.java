// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.modmenu;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.core.ScannerMod;
import com.midnightbits.scanner.rt.core.fabric.Minecraft;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Nameable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Environment(value = EnvType.CLIENT)
public class ScannerInventory implements Inventory, Nameable {
    public static final TagKey<Item> ORES_TAG = TagKey.of(Registries.ITEM.getKey(), Identifier.of("c:ores"));

    List<ItemStack> items = Arrays.asList(
            ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
            ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
            ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
            ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);

    public ScannerInventory(Set<Id> ids) {
        final var maxCount = Math.min(ids.size(), items.size());

        final var itemStacks = ids.stream()
                .limit(maxCount)
                .map(Minecraft::identifierOf)
                .map(Registries.ITEM::get)
                .map(ItemStack::new).iterator();

        for (var index = 0; index < items.size(); ++index) {
            if (!itemStacks.hasNext()) {
                break;
            }
            final var item = itemStacks.next();
            items.set(index, item);
        }
    }

    @Override
    public Text getName() {
        return Text.translatable(ScannerMod.translationKey("inventory", "ores"));
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        // all or nothing
        return removeStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot) {
        final var current = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        return current;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        items.set(slot, stack.copyWithCount(1));
    }

    @Override
    public void markDirty() {

    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        items.replaceAll(ignored -> ItemStack.EMPTY);
    }

    public Set<Id> serialize() {
        return items.stream()
                .filter((stack) -> !stack.isEmpty())
                .map(ItemStack::getItem)
                .map(Registries.ITEM::getId)
                .map(Minecraft::idOf)
                .collect(Collectors.toSet());
    }
}
