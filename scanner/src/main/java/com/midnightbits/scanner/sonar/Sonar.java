// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.midnightbits.scanner.utils.NotificationConsumer;
import com.midnightbits.scanner.utils.Settings;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import com.midnightbits.scanner.rt.core.ClientCore;
import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.core.Services;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.utils.ConeOfBlocks;

public final class Sonar {
    public static final int BLOCK_RADIUS = 2;
    public static final int BLOCK_DISTANCE = 16;
    public static Id[] INTERESTING_IDS = new Id[] {
            Id.ofVanilla("coal_ore"),
            Id.ofVanilla("deepslate_coal_ore"),
    };

    private final BlockEchoes echoes;
    @Nullable
    private Reflections reflections;
    @Nullable
    private Consumer<BlockEcho> echoConsumer;
    private int blockDistance;
    private int blockRadius;
    private Set<Id> blocks;

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonar");

    public Sonar(int echoesSize, int blockDistance, int blockRadius, Set<Id> interestingIds) {
        this.blockDistance = blockDistance;
        this.blockRadius = blockRadius;
        this.blocks = interestingIds;
        this.echoes = new BlockEchoes(echoesSize);
    }

    public Sonar(Settings settings) {
        this(settings.echoesSize(), settings.blockDistance(),
                settings.blockRadius(),
                settings.interestingIds());
    }

    public Sonar() {
        this(BlockEchoes.MAX_SIZE, BLOCK_DISTANCE, BLOCK_RADIUS, Set.of(INTERESTING_IDS));
    }

    public void setEchoConsumer(@Nullable Consumer<BlockEcho> echoConsumer) {
        this.echoConsumer = echoConsumer;
    }

    public void refresh(Settings settings) {
        refresh(settings.echoesSize(),
                settings.blockDistance(),
                settings.blockRadius(),
                settings.interestingIds());
    }

    public void refresh(int maxEchoes, int blockDistance, int blockRadius, Set<Id> blocks) {
        this.blockDistance = blockDistance;
        this.blockRadius = blockRadius;
        this.blocks = blocks;
        this.echoes.refresh(maxEchoes);
    }

    public interface SliceSpacing {
        void registerCallback(Predicate<Long> cb);
    }

    public boolean sendPing(ClientCore client, SliceSpacing spacer, ScanWaveConsumer waveConsumer,
            @Nullable NotificationConsumer pingEnd) {
        if (reflections != null)
            return false;
        reflections = Reflections.fromPlayerPov(client, blockDistance, blockRadius);
        spacer.registerCallback((now) -> {
            if (!reflections.hasNextSlice()) {
                reflections = null;
                if (pingEnd != null)
                    pingEnd.apply();
                return false;
            }
            reflections.processSlice(waveConsumer, blocks);
            return true;
        });

        return true;
    }

    public void echoFrom(BlockEcho.Partial partial) {
        final var echo = echoes.echoFrom(partial);
        if (echoConsumer != null)
            echoConsumer.accept(echo);
    }

    public Iterable<BlockEcho> echoes() {
        return echoes;
    }

    private static final class Reflections {
        private final ClientCore client;
        private final V3i center;
        private final ConeOfBlocks.Slicer slices;

        Reflections(ClientCore client, V3i center, int blockDistance, int blockRadius) {
            this.client = client;
            this.center = center;
            this.slices = coneOfBlocksFromCamera(blockDistance, blockRadius).sliced();
        }

        boolean hasNextSlice() {
            return slices.hasNext();
        }

        public void processSlice(ScanWaveConsumer waveConsumer, Set<Id> blocks) {
            final var slice = slices.next();
            final var dist = (int) Math.round((double) slice.distance() / ConeOfBlocks.Slicer.PRECISION);
            final Set<BlockEcho.Partial> echoes = new HashSet<>();
            final var blockIds = blocks.stream().map(String::valueOf).collect(Collectors.joining(","));

            for (final var pos : slice.items()) {
                final var info = client.getBlockInfo(pos);
                if (info.isAir()) {
                    LOGGER.debug("({}) is air", pos);
                    continue;
                }

                final var id = info.getId();
                if (!blocks.contains(id))
                    continue;

                LOGGER.debug("({}) > {}m {} ({})", pos, dist, id, blockIds);
                var message = Services.TEXT
                        .literal(MessageFormatter.format("> {}m ", dist).getMessage())
                        .append(info.getName().formattedGold());
                client.sendPlayerMessage(message, false);

                echoes.add(new BlockEcho.Partial(pos, id));
            }

            waveConsumer.advance(slice.items(), echoes.stream().toList());
        }

        static Reflections fromPlayerPov(ClientCore client, int blockDistance, int blockRadius) {
            return new Reflections(client, client.getPlayerPos(), blockDistance, blockRadius);
        }

        private ConeOfBlocks coneOfBlocksFromCamera(int blockDistance, int blockRadius) {
            return ConeOfBlocks.fromCamera(center, client.getCameraPitch(), client.getCameraYaw(),
                    blockDistance, blockRadius);
        }
    };
}
