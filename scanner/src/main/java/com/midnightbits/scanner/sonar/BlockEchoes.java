// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar;

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.midnightbits.scanner.rt.core.ClientCore;
import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.utils.Clock;

import org.jetbrains.annotations.NotNull;

public final class BlockEchoes implements Iterable<BlockEcho> {
    private final TreeSet<BlockEcho> echoes = new TreeSet<>();
    public static final int ECHO_LIFETIME = 10000;

    private int lifetime;

    public BlockEchoes(int lifetime) {
        this.lifetime = lifetime;
    }

    public BlockEchoes() {
        this(ECHO_LIFETIME);
    }

    public void refresh(int lifetime) {
        this.lifetime = lifetime;
    }

    /**
     * Adds pinged block to list of all seen blocks.
     *
     * @param position where the ping was registered
     * @param id       what did the echo bounced off of
     * @return resulting block
     */
    public BlockEcho echoFrom(V3i position, Id id, int argb32) {
        return echoFrom(new BlockEcho.Partial(position, id, argb32));
    }

    /**
     * Adds pinged block to list of all seen blocks.
     *
     * @param partial where the ping was registered and what did the echo bounced
     *                off of
     * @return resulting block
     */
    public BlockEcho echoFrom(BlockEcho.Partial partial) {
        evictBlocks(stream().filter(b -> b.position().equals(partial.position())));

        BlockEcho echo = BlockEcho.echoFrom(partial);
        echoes.add(echo);
        return echo;
    }

    public void remove(Predicate<BlockEcho> whichOnes) {
        evictBlocks(stream().filter(whichOnes));
    }

    public Predicate<BlockEcho> oldEchoes(ClientCore client) {
        final var now = Clock.currentTimeMillis();
        return ((block) -> {
            final var blockLifetime = now - block.pingTime();
            if (blockLifetime > lifetime) {
                return true;
            }

            final var info = client.getBlockInfo(block.position());
            if (info == null) {
                return true;
            }
            return !block.id().equals(info.getId());
        });
    }

    private Stream<BlockEcho> stream() {
        return echoes.stream();
    }

    private void evictBlocks(Stream<BlockEcho> stream) {
        List<BlockEcho> evictions = stream.toList();
        for (BlockEcho evicted : evictions) {
            echoes.remove(evicted);
        }
    }

    @NotNull
    @Override
    public Iterator<BlockEcho> iterator() {
        return echoes.iterator();
    }
}
