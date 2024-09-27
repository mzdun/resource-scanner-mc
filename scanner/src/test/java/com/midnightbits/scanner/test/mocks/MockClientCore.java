// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.test.mocks;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.midnightbits.scanner.rt.core.BlockInfo;
import com.midnightbits.scanner.rt.core.ClientCore;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.rt.text.Text;

public final class MockClientCore implements ClientCore {
    static final Logger LOGGER = LoggerFactory.getLogger(MockClientCore.class);
    final V3i playerPos;
    final float cameraPitch;
    final float cameraYaw;
    final MockWorld world;
    final List<String> messages = new ArrayList<>();

    public MockClientCore(V3i playerPos, float cameraPitch, float cameraYaw, MockWorld world) {
        this.playerPos = playerPos;
        this.cameraPitch = cameraPitch;
        this.cameraYaw = cameraYaw;
        this.world = world;
    }

    @Override
    public BlockInfo getBlockInfo(V3i pos) {
        return world.getOrAir(pos);
    }

    @Override
    public V3i getPlayerPos() {
        return playerPos;
    }

    @Override
    public float getCameraPitch() {
        return cameraPitch;
    }

    @Override
    public float getCameraYaw() {
        return cameraYaw;
    }

    @Override
    public void sendPlayerMessage(Text message, boolean overlay) {
        messages.add(message.getString());
        LOGGER.info(message.getString());
    }

    public Iterable<String> getPlayerMessages() {
        return messages;
    }

    public void resetPlayerMessages() {
        messages.clear();
    }
}
