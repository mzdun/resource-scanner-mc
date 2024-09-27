// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.rt.core.fabric;

import com.midnightbits.scanner.rt.core.BlockInfo;
import com.midnightbits.scanner.rt.core.ClientCore;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.rt.text.Text;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class MinecraftClientCore implements ClientCore {
    private final MinecraftClient client;

    public MinecraftClientCore(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public BlockInfo getBlockInfo(V3i pos) {
        assert client.world != null;
        BlockState state = client.world.getBlockState(new BlockPos(Minecraft.vec3iOf(pos)));
        Block block = state == null ? null : state.getBlock();
        return new MinecraftBlockInfo(state, block);
    }

    @Override
    public V3i getPlayerPos() {
        assert client.player != null;
        return Minecraft.v3iOf(client.player.getBlockPos());
    }

    @Override
    public float getCameraPitch() {
        assert client.player != null;
        return client.player.getPitch();
    }

    @Override
    public float getCameraYaw() {
        assert client.player != null;
        return client.player.getYaw();
    }

    @Override
    public void sendPlayerMessage(Text message, boolean overlay) {
        assert client.player != null;
        client.player.sendMessage(Minecraft.unpackText(message), overlay);
    }
}
