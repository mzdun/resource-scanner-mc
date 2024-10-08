// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.midnightbits.scanner.sonar.graphics.Colors;
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

    public Sonar(int blockDistance, int blockRadius, int lifetime, Set<Id> interestingIds) {
        this.blockDistance = blockDistance;
        this.blockRadius = blockRadius;
        this.blocks = interestingIds;
        this.echoes = new BlockEchoes(lifetime);
    }

    public Sonar(Settings settings) {
        this(settings.blockDistance(),
                settings.blockRadius(),
                settings.lifetime(),
                settings.interestingIds());
    }

    public Sonar() {
        this(BLOCK_DISTANCE, BLOCK_RADIUS, BlockEchoes.ECHO_LIFETIME, Set.of(INTERESTING_IDS));
    }

    public void setEchoConsumer(@Nullable Consumer<BlockEcho> echoConsumer) {
        this.echoConsumer = echoConsumer;
    }

    public void refresh(Settings settings) {
        refresh(settings.blockDistance(),
                settings.blockRadius(),
                settings.lifetime(),
                settings.interestingIds());
    }

    public void refresh(int blockDistance, int blockRadius, int lifetime, Set<Id> blocks) {
        this.blockDistance = blockDistance;
        this.blockRadius = blockRadius;
        this.blocks = blocks;
        this.echoes.refresh(lifetime);
    }

    public boolean sendPing(ClientCore client, SlicePacer pacer, ScanWaveConsumer waveConsumer,
            @Nullable NotificationConsumer pingEnd) {
        if (reflections != null)
            return false;
        reflections = Reflections.fromPlayerPov(client, blockDistance, blockRadius);
        if (reflections == null)
            return false;
        pacer.registerCallback((now) -> {
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

    public void remove(Predicate<BlockEcho> whichOnes) {
        echoes.remove(whichOnes);
    }

    public Predicate<BlockEcho> oldEchoes(ClientCore client) {
        return echoes.oldEchoes(client);
    }

    private static final class Reflections {
        private final ClientCore client;
        private final V3i center;
        private final ConeOfBlocks.Slicer slices;
        private final Colors.Proxy VANILLA = new Colors.DirectValue(Colors.VANILLA);
        private final Map<Id, Echo> echoCache = new HashMap<>();

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
                if (info == null) {
                    break;
                }
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

                echoCache.computeIfAbsent(id, (k) -> {
                    var color = VANILLA;
                    for (final var entry : Colors.BLOCK_TAG_COLORS.entrySet()) {
                        if (info.inTag(entry.getKey())) {
                            color = entry.getValue();
                            break;
                        }
                    }

                    return new Echo(k, color);
                });

                echoes.add(new BlockEcho.Partial(pos, echoCache.get(id)));
            }

            waveConsumer.advance(slice.items(), echoes.stream().toList());
        }

        static @Nullable Reflections fromPlayerPov(ClientCore client, int blockDistance, int blockRadius) {
            final var pos = client.getPlayerPos();
            if (pos == null) {
                return null;
            }
            return new Reflections(client, pos, blockDistance, blockRadius);
        }

        private ConeOfBlocks coneOfBlocksFromCamera(int blockDistance, int blockRadius) {
            return ConeOfBlocks.fromCamera(center, client.getCameraPitch(), client.getCameraYaw(),
                    blockDistance, blockRadius);
        }
    };

    public interface SlicePacer {
        void registerCallback(Predicate<Long> cb);
    }
}
