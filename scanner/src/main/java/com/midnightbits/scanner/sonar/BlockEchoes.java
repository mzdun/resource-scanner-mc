// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar;

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.midnightbits.scanner.rt.core.ClientCore;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.utils.Clock;

import org.jetbrains.annotations.NotNull;

public final class BlockEchoes implements Iterable<BlockEcho> {
    private final TreeSet<BlockEcho> echoes = new TreeSet<>();
    private List<EchoNugget> nuggets = List.of();
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
     * @param x    axis of where the ping was registered
     * @param y    axis of where the ping was registered
     * @param z    axis of where the ping was registered
     * @param echo what did the echo bounced off of
     * @return resulting block
     */
    public BlockEcho echoFrom(int x, int y, int z, Echo echo) {
        return echoFrom(new BlockEcho.Partial(new V3i(x, y, z), echo));
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

    public boolean remove(Predicate<BlockEcho> whichOnes) {
        return evictBlocks(stream().filter(whichOnes));
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

    List<EchoNugget> nuggets() {
        return nuggets;
    }

    public void splitToNuggets() {
        nuggets = EchoNugget.group(echoes);
    }

    private Stream<BlockEcho> stream() {
        return echoes.stream();
    }

    private boolean evictBlocks(Stream<BlockEcho> stream) {
        final var oldSize = echoes.size();
        List<BlockEcho> evictions = stream.toList();
        for (BlockEcho evicted : evictions) {
            echoes.remove(evicted);
        }
        return oldSize != echoes.size();
    }

    @NotNull
    @Override
    public Iterator<BlockEcho> iterator() {
        return echoes.iterator();
    }
}
