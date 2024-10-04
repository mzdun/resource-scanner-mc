// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.midnightbits.scanner.utils.NotificationConsumer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import com.midnightbits.scanner.rt.core.ClientCore;
import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.core.Services;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.rt.text.MutableText;
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

    public Sonar(int blockDistance, int blockRadius, Set<Id> blocks, int maxEchoes) {
        this.blockDistance = blockDistance;
        this.blockRadius = blockRadius;
        this.blocks = blocks;
        this.echoes = new BlockEchoes(maxEchoes);
    }

    public Sonar(int blockDistance, int blockRadius, Set<Id> blocks) {
        this(blockDistance, blockRadius, blocks, BlockEchoes.MAX_SIZE);
    }

    public Sonar() {
        this(BLOCK_DISTANCE, BLOCK_RADIUS, Set.of(INTERESTING_IDS));
    }

    public void setEchoConsumer(@Nullable Consumer<BlockEcho> echoConsumer) {
        this.echoConsumer = echoConsumer;
    }

    public void refresh(int blockDistance, int blockRadius, Set<Id> blocks, int maxEchoes) {
        this.blockDistance = blockDistance;
        this.blockRadius = blockRadius;
        this.blocks = blocks;
        this.echoes.refresh(maxEchoes);
    }

    public boolean ping(ClientCore client) {
        var blockListChanged = false;

        final var reflections = Reflections.fromPlayerPov(echoes, client, blockDistance, blockRadius);
        final var cone = reflections.coneOfBlocksFromCamera(blockDistance, blockRadius);

        for (final var line : cone.iterate()) {
            for (final var pos : line.iterate()) {
                final var info = client.getBlockInfo(pos);
                if (info.isAir()) {
                    LOGGER.debug("({}) is air", pos);
                    continue;
                }

                final var id = info.getId();
                final var dist = (int) Math.round(Math.sqrt(reflections.center.getSquaredDistance(pos)));
                LOGGER.debug("({}) > {}m {}", pos, dist, id);
                if (!blocks.contains(id))
                    continue;

                reflections.echoFrom(pos, id, info.getName());
                blockListChanged = true;
            }
        }

        return blockListChanged;
    }

    public interface SliceSpacing {
        void registerCallback(Predicate<Long> cb);
    }

    public boolean sendPing(ClientCore client, SliceSpacing spacer, ScanWaveConsumer waveConsumer,
            @Nullable NotificationConsumer pingEnd) {
        if (reflections != null)
            return false;
        reflections = Reflections.fromPlayerPov(echoes, client, blockDistance, blockRadius);
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
        private final BlockEchoes echoes;
        private final ClientCore client;
        private final V3i center;
        private final Set<V3i> seen = new TreeSet<>();
        private final ConeOfBlocks.Slicer slices;

        Reflections(BlockEchoes echoes, ClientCore client, V3i center, int blockDistance, int blockRadius) {
            this.echoes = echoes;
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

            for (final var pos : slice.items()) {
                final var info = client.getBlockInfo(pos);
                if (info.isAir()) {
                    // LOGGER.debug("({}) is air", pos);
                    continue;
                }

                final var id = info.getId();
                if (!blocks.contains(id))
                    continue;

                // LOGGER.debug("({}) > {}m {}", pos, dist, id);

                var message = Services.TEXT
                        .literal(MessageFormatter.format("> {}m ", dist).getMessage())
                        .append(info.getName().formattedGold());
                client.sendPlayerMessage(message, false);

                // this.echoes.echoFrom(pos, id);
                echoes.add(new BlockEcho.Partial(pos, id));
            }
            waveConsumer.advance(slice.items(), echoes.stream().toList());
        }

        static Reflections fromPlayerPov(BlockEchoes echoes, ClientCore client, int blockDistance, int blockRadius) {
            return new Reflections(echoes, client, client.getPlayerPos(), blockDistance, blockRadius);
        }

        private ConeOfBlocks coneOfBlocksFromCamera(int blockDistance, int blockRadius) {
            return ConeOfBlocks.fromCamera(center, client.getCameraPitch(), client.getCameraYaw(),
                    blockDistance, blockRadius);
        }

        public void echoFrom(V3i pos, Id id, MutableText name) {
            if (!seen.add(pos))
                return;

            var dist = (int) Math.round(Math.sqrt(center.getSquaredDistance(pos)));
            var message = Services.TEXT.literal(
                    MessageFormatter.format("> {}m ", dist).getMessage()).append(name.formattedGold());
            client.sendPlayerMessage(message, false);

            echoes.echoFrom(pos, id);
        }
    };
}
