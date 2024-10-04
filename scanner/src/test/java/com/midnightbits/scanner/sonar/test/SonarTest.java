// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar.test;

import java.util.Set;

import com.midnightbits.scanner.rt.core.Services;
import com.midnightbits.scanner.sonar.BlockEcho;
import com.midnightbits.scanner.sonar.BlockEchoes;
import com.midnightbits.scanner.sonar.Sonar;
import com.midnightbits.scanner.sonar.graphics.Scene;
import com.midnightbits.scanner.sonar.graphics.SonarAnimation;
import com.midnightbits.scanner.sonar.graphics.WaveAnimator;
import com.midnightbits.scanner.test.mocks.platform.MockAnimatorHost;
import com.midnightbits.scanner.test.mocks.platform.MockPlatform;
import com.midnightbits.scanner.utils.Clock;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.test.mocks.MockClientCore;
import com.midnightbits.scanner.test.mocks.MockWorld;
import com.midnightbits.scanner.test.mocks.MockedClock;
import com.midnightbits.scanner.test.support.Counter;
import com.midnightbits.scanner.test.support.Iterables;

public class SonarTest {
	public final static int TEST_BLOCK_DISTANCE = 32;
	public final static int TEST_BLOCK_RADIUS = 4;
	public static Id[] TEST_INTERESTING_IDS = new Id[] {
			Id.ofVanilla("coal_ore"),
			Id.ofVanilla("deepslate_coal_ore"),
			Id.ofVanilla("iron_ore"),
			Id.ofVanilla("deepslate_iron_ore"),
			Id.ofVanilla("diamond_ore"),
			Id.ofVanilla("deepslate_diamond_ore"),
			Id.ofVanilla("netherite_block"),
	};
	private final MockedClock clock = new MockedClock();
	private final static MockWorld TEST_WORLD = MockWorld.ofResource("test_world.txt");

	private static class Setup {
		Sonar sonar;
		SonarAnimation animation;
		long then = Clock.currentTimeMillis();

		Setup(int blockDistance, int blockRadius, Set<Id> blocks) {
			this(new Sonar(blockDistance, blockRadius, blocks));
		}

		Setup(Sonar sonar) {
			this.sonar = sonar;
			((MockPlatform) Services.PLATFORM).setHostBackend((echoes, shimmers) -> {
			});
			this.animation = new SonarAnimation(sonar);
		}

		public boolean sendPing(MockClientCore client, @Nullable WaveAnimator.StageReporter stageReporter) {
			final var result = animation.sendPing(client, stageReporter);
			if (result) {
				then = Clock.currentTimeMillis();
			}
			return result;
		}

		public boolean sendPing(MockClientCore client) {
			return sendPing(client, this::report);
		}

		public void sendPingBlocking(MockClientCore client, MockedClock clock,
				@Nullable WaveAnimator.StageReporter stageReporter) {
			sendPing(client, stageReporter);
			((MockAnimatorHost) Services.PLATFORM.getAnimatorHost()).runAll(clock);
		}

		public void sendPingBlocking(MockClientCore client, MockedClock clock) {
			sendPingBlocking(client, clock, this::report);
		}

		void report(long now, int id, WaveAnimator.AnimationStep animationStep) {
			final var diff = now - then;
			Scene.LOGGER.warn("{}.{} {}: {}", diff / 1000, String.format("%03d", diff % 1000),
					String.format("%2d", id),
					animationStep.name());
		}
	}

	public static Sonar narrowSonar() {
		return narrowSonar(TEST_BLOCK_DISTANCE, Set.of(TEST_INTERESTING_IDS));
	}

	public static Sonar narrowSonar(int blockDistance, Set<Id> blocks) {
		return new Sonar(blockDistance, 0, blocks, BlockEchoes.MAX_SIZE);
	}

	@Test
	void checkNewSonarIsEmpty() {
		final var empty = new Sonar();
		Iterables.assertEquals(new BlockEcho[] {}, empty.echoes());
	}

	@Test
	void checkDownwardsDirectionFromMiddle_narrow() {
		final var core = new MockClientCore(V3i.ZERO, -90, 0, TEST_WORLD);
		final var counter = new Counter();

		clock.timeStamp = 0x123456;
		final var setup = new Setup(narrowSonar());
		setup.sonar.setEchoConsumer((echo) -> counter.inc());
		setup.sendPingBlocking(core, clock);

		Iterables.assertEquals(new BlockEcho[] {
				new BlockEcho(new V3i(0, 23, 0), Id.ofVanilla("deepslate_iron_ore"), 1195750),
				new BlockEcho(new V3i(0, 25, 0), Id.ofVanilla("deepslate_diamond_ore"), 1195950),
				new BlockEcho(new V3i(0, 27, 0), Id.ofVanilla("diamond_ore"), 1196150),
				new BlockEcho(new V3i(0, 28, 0), Id.ofVanilla("iron_ore"), 1196250),
				new BlockEcho(new V3i(0, 30, 0), Id.ofVanilla("iron_ore"), 1196450),
		}, setup.sonar.echoes());

		Iterables.assertEquals(new String[] {
				"> 23m deepslate_iron_ore", "> 25m deepslate_diamond_ore", "> 27m diamond_ore",
				"> 28m iron_ore", "> 30m iron_ore",
		}, core.getPlayerMessages());

		Assertions.assertEquals(5, counter.get());
	}

	@Test
	void checkDownwardsDirectionFromMiddleButOnlyDiamonds_narrow() {
		final var core = new MockClientCore(V3i.ZERO, -90f, 0f, TEST_WORLD);

		clock.timeStamp = 0x123456;
		final var setup = new Setup(narrowSonar(TEST_BLOCK_DISTANCE, Set.of(
				Id.ofVanilla("diamond_ore"),
				Id.ofVanilla("deepslate_diamond_ore"))));
		setup.sendPingBlocking(core, clock, null);

		Iterables.assertEquals(new BlockEcho[] {
				new BlockEcho(new V3i(0, 25, 0), Id.ofVanilla("deepslate_diamond_ore"), 1195948),
				new BlockEcho(new V3i(0, 27, 0), Id.ofVanilla("diamond_ore"), 1196148),
		}, setup.sonar.echoes());
	}

	@Test
	void searchForGold() {
		clock.timeStamp = 0x123456;

		final var core = new MockClientCore(new V3i(-60, -60, -51), 0f, 0f, TEST_WORLD);
		final var setup = new Setup(TEST_BLOCK_DISTANCE, TEST_BLOCK_RADIUS, Set.of(Id.ofVanilla("gold_ore")));

		final var started = setup.sendPing(core);
		Assertions.assertTrue(started);
		Assertions.assertFalse(setup.sendPing(core));

		((MockAnimatorHost) Services.PLATFORM.getAnimatorHost()).runAll(clock);

		Iterables.assertEquals(new BlockEcho[] {
				new BlockEcho(new V3i(-60, -60, -50), Id.ofVanilla("gold_ore"), 1193550),
				new BlockEcho(new V3i(-60, -59, -39), Id.ofVanilla("gold_ore"), 1194850),
				new BlockEcho(new V3i(-58, -59, -39), Id.ofVanilla("gold_ore"), 1194950),
				new BlockEcho(new V3i(-60, -59, -37), Id.ofVanilla("gold_ore"), 1195250),
				new BlockEcho(new V3i(-59, -59, -35), Id.ofVanilla("gold_ore"), 1195650),
				new BlockEcho(new V3i(-59, -59, -34), Id.ofVanilla("gold_ore"), 1195850),
				new BlockEcho(new V3i(-60, -58, -34), Id.ofVanilla("gold_ore"), 1195850),
				new BlockEcho(new V3i(-60, -60, -33), Id.ofVanilla("gold_ore"), 1196050),
				new BlockEcho(new V3i(-57, -59, -30), Id.ofVanilla("gold_ore"), 1196750),
				new BlockEcho(new V3i(-59, -60, -29), Id.ofVanilla("gold_ore"), 1196850),
				new BlockEcho(new V3i(-57, -60, -28), Id.ofVanilla("gold_ore"), 1197150),
				new BlockEcho(new V3i(-57, -60, -27), Id.ofVanilla("gold_ore"), 1197350),
				new BlockEcho(new V3i(-60, -57, -27), Id.ofVanilla("gold_ore"), 1197350),
				new BlockEcho(new V3i(-58, -59, -26), Id.ofVanilla("gold_ore"), 1197450),
				new BlockEcho(new V3i(-57, -60, -26), Id.ofVanilla("gold_ore"), 1197550),
				new BlockEcho(new V3i(-59, -57, -26), Id.ofVanilla("gold_ore"), 1197550),
				new BlockEcho(new V3i(-58, -60, -25), Id.ofVanilla("gold_ore"), 1197650),
				new BlockEcho(new V3i(-59, -58, -25), Id.ofVanilla("gold_ore"), 1197650),
				new BlockEcho(new V3i(-59, -57, -25), Id.ofVanilla("gold_ore"), 1197750),
				new BlockEcho(new V3i(-58, -60, -24), Id.ofVanilla("gold_ore"), 1197850),
				new BlockEcho(new V3i(-60, -59, -23), Id.ofVanilla("gold_ore"), 1198050),
				new BlockEcho(new V3i(-60, -58, -23), Id.ofVanilla("gold_ore"), 1198050),
				new BlockEcho(new V3i(-56, -58, -23), Id.ofVanilla("gold_ore"), 1198150),
				new BlockEcho(new V3i(-59, -60, -22), Id.ofVanilla("gold_ore"), 1198250),
				new BlockEcho(new V3i(-58, -58, -22), Id.ofVanilla("gold_ore"), 1198350),
				new BlockEcho(new V3i(-59, -57, -22), Id.ofVanilla("gold_ore"), 1198350),
				new BlockEcho(new V3i(-59, -58, -21), Id.ofVanilla("gold_ore"), 1198450),
				new BlockEcho(new V3i(-57, -58, -21), Id.ofVanilla("gold_ore"), 1198550),
				new BlockEcho(new V3i(-56, -59, -20), Id.ofVanilla("gold_ore"), 1198750),
				new BlockEcho(new V3i(-56, -59, -19), Id.ofVanilla("gold_ore"), 1198950),
				new BlockEcho(new V3i(-59, -56, -19), Id.ofVanilla("gold_ore"), 1198950),
		}, setup.sonar.echoes());

		Iterables.assertEquals(new String[] {
				"> 1m gold_ore", "> 12m gold_ore", "> 12m gold_ore", "> 14m gold_ore", "> 16m gold_ore",
				"> 17m gold_ore", "> 17m gold_ore", "> 18m gold_ore", "> 21m gold_ore",
				"> 22m gold_ore", "> 23m gold_ore", "> 24m gold_ore", "> 24m gold_ore",
				"> 25m gold_ore", "> 25m gold_ore", "> 25m gold_ore", "> 26m gold_ore",
				"> 26m gold_ore", "> 26m gold_ore", "> 27m gold_ore", "> 28m gold_ore",
				"> 28m gold_ore", "> 28m gold_ore", "> 29m gold_ore", "> 29m gold_ore",
				"> 29m gold_ore", "> 30m gold_ore", "> 30m gold_ore", "> 31m gold_ore",
				"> 32m gold_ore", "> 32m gold_ore",
		}, core.getPlayerMessages());
	}

	@Test
	void searchForGold_narrow() {
		final var core = new MockClientCore(new V3i(-60, -60, -51), 0f, 0f, TEST_WORLD);

		clock.timeStamp = 0x123456;
		final var setup = new Setup(narrowSonar(TEST_BLOCK_DISTANCE, Set.of(Id.ofVanilla("gold_ore"))));
		setup.sendPingBlocking(core, clock);

		Iterables.assertEquals(new BlockEcho[] {
				new BlockEcho(new V3i(-60, -60, -50), Id.ofVanilla("gold_ore"), 1193550),
				new BlockEcho(new V3i(-60, -60, -33), Id.ofVanilla("gold_ore"), 1195250),
		}, setup.sonar.echoes());

		Iterables.assertEquals(new String[] {
				"> 1m gold_ore", "> 18m gold_ore",
		}, core.getPlayerMessages());
	}

	@Test
	void checkAnotherDirectionFromMiddle_narrow() {
		final var core = new MockClientCore(V3i.ZERO, -75f, 180f, TEST_WORLD);

		clock.timeStamp = 0x123456;
		final var setup = new Setup(narrowSonar());
		setup.sendPingBlocking(core, clock);

		Iterables.assertEquals(new BlockEcho[] {
				new BlockEcho(new V3i(0, 30, -8), Id.ofVanilla("coal_ore"), 1196450),
				new BlockEcho(new V3i(0, 31, -8), Id.ofVanilla("iron_ore"), 1196550),
		}, setup.sonar.echoes());

		Iterables.assertEquals(new String[] {
				"> 31m coal_ore", "> 32m iron_ore",
		}, core.getPlayerMessages());
	}
}
