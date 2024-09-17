package com.midnightbits.scanner.sonar;

import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import com.midnightbits.scanner.rt.core.BlockInfo;
import com.midnightbits.scanner.rt.core.ClientCore;
import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.core.Services;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.rt.text.MutableText;
import com.midnightbits.scanner.utils.ConeOfBlocks;
import com.midnightbits.scanner.utils.LineOfBlocks;

public class Sonar {
    public static final int BLOCK_RADIUS = 4;
    public static final int BLOCK_DISTANCE = 32;
    public static Id[] INTERESTING_IDS = new Id[] {
            Id.ofVanilla("coal_ore"),
            Id.ofVanilla("deepslate_coal_ore"),
            Id.ofVanilla("iron_ore"),
            Id.ofVanilla("deepslate_iron_ore"),
            Id.ofVanilla("diamond_ore"),
            Id.ofVanilla("deepslate_diamond_ore"),
            Id.ofVanilla("netherite_block"),
    };

    private final BlockEchoes echoes;
    private int blockDistance;
    private int blockRadius;
    private Set<Id> blocks;

    public static final Logger LOGGER = LoggerFactory.getLogger("Sonar");

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

    public static Sonar narrow() {
        return narrow(BLOCK_DISTANCE, Set.of(INTERESTING_IDS));
    }

    public static Sonar narrow(int blockDistance, Set<Id> blocks) {
        return new Sonar(blockDistance, 0, blocks, BlockEchoes.MAX_SIZE);
    }

    public void refresh(int blockDistance, int blockRadius, Set<Id> blocks, int maxEchoes) {
        this.blockDistance = blockDistance;
        this.blockRadius = blockRadius;
        this.blocks = blocks;
        this.echoes.refresh(maxEchoes);
    }

    public boolean ping(ClientCore client) {
        boolean blockListChanged = false;

        Reflections reflections = Reflections.fromPlayerPov(echoes, client);
        ConeOfBlocks cone = reflections.coneOfBlocksFromCamera(blockDistance, blockRadius);

        for (LineOfBlocks line : cone.iterate()) {
            for (V3i pos : line.iterate()) {
                BlockInfo info = client.getBlockInfo(pos);
                if (info.isAir()) {
                    LOGGER.debug("({}) is air", pos.toString());
                    continue;
                }

                Id id = info.getId();
                int dist = (int) Math.round(Math.sqrt(reflections.center.getSquaredDistance(pos)));
                LOGGER.debug("({}) > {}m {}", pos.toString(), dist, id);
                if (!blocks.contains(id))
                    continue;

                reflections.echoFrom(pos, id, info.getName());
                blockListChanged = true;
            }
        }

        return blockListChanged;
    }

    public Iterable<BlockEcho> echoes() {
        return echoes;
    }

    private static class Reflections {
        private final BlockEchoes echoes;
        private final ClientCore client;
        private final V3i center;
        private final Set<V3i> seen = new TreeSet<V3i>();

        Reflections(BlockEchoes echoes, ClientCore client, V3i center) {
            this.echoes = echoes;
            this.client = client;
            this.center = center;
        }

        static Reflections fromPlayerPov(BlockEchoes echoes, ClientCore client) {
            return new Reflections(echoes, client, client.getPlayerPos());
        }

        ConeOfBlocks coneOfBlocksFromCamera(int blockDistance, int blockRadius) {
            return ConeOfBlocks.fromCamera(center, client.getCameraPitch(), client.getCameraYaw(),
                    blockDistance, blockRadius);
        }

        public void echoFrom(V3i pos, Id id, MutableText name) {
            if (!seen.add(pos))
                return;

            int dist = (int) Math.round(Math.sqrt(center.getSquaredDistance(pos)));
            MutableText message = Services.TEXT.literal(
                    MessageFormatter.format("> {}m ", dist).getMessage()).append(name.formattedGold());
            client.sendPlayerMessage(message, false);

            echoes.echoFrom(pos, id);
        }
    };
}
