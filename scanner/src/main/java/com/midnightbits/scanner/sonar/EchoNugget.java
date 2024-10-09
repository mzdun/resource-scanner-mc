// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.sonar.graphics.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class EchoNugget {
    private final Map<V3i, EchoState> echoStates;
    public final Id id;
    private final EchoState.AABB bounds;

    private EchoNugget(Id id, Map<V3i, EchoState> echoStates) {
        this.echoStates = echoStates;
        this.id = id;
        this.bounds = makeBounds(echoStates);
    }

    public void draw(GlProgramConsumer buffer, MatrixStack matrices, Vector3f camera) {
        furthestToClosest(buffer, matrices, camera, EchoState::draw);
    }

    public void sketch(GlProgramConsumer buffer, MatrixStack matrices, Vector3f camera) {
        furthestToClosest(buffer, matrices, camera, EchoState::sketch);
    }

    public EchoState.AABB getBounds() {
        return bounds;
    }

    private static EchoState.AABB makeBounds(Map<V3i, EchoState> echoStates) {
        var bounds = new EchoState.AABB(0, 0, 0, 0, 0, 0);

        final var iter = echoStates.values().iterator();
        if (iter.hasNext()) {
            bounds = iter.next().getBounds();
        }

        while (iter.hasNext()) {
            bounds.expand(iter.next().getBounds());
        }

        return bounds;
    }

    private interface DrawingFunction {
        void apply(EchoState self, GlProgramConsumer buffer, MatrixStack matrices, Vector3f camera);
    };

    private void furthestToClosest(GlProgramConsumer buffer, MatrixStack matrices, Vector3f camera,
            DrawingFunction fn) {
        furthestToClosest(echoStates.values().stream(), buffer, matrices, camera, fn);
    }

    private static void furthestToClosest(Stream<EchoState> echoes, GlProgramConsumer buffer, MatrixStack matrices, Vector3f camera,
                                          DrawingFunction fn) {
        final Consumer<Pixel> paint = (pixel) -> fn.apply(pixel.echoState(), buffer, matrices, camera);

        echoes
                .map((echo) -> Pixel.of(echo, camera))
                .sorted((lhs, rhs) -> Double.compare(rhs.distanceSquared(), lhs.distanceSquared()))
                .forEach(paint);
    }

    private List<Double> minMaxTo(Vector3f camera) {
        final var sorted = echoStates.values().stream()
                .map((echo) -> Pixel.of(echo, camera).distanceSquared())
                .sorted((lhs, rhs) -> Double.compare(rhs, lhs)).iterator();

        final var closest = sorted.next();
        var furthest = closest;
        while (sorted.hasNext()) {
            furthest = sorted.next();
        }
        return List.of(closest, furthest);
    }

    private record Distance(EchoNugget nugget, List<Double> minMax) implements Comparable<Distance> {
        @Override
        public int compareTo(@NotNull Distance other) {
            final var lhsMin = minMax.getFirst();
            final var rhsMin = other.minMax.getFirst();
            return Double.compare(rhsMin, lhsMin);
        }
    }

    public static List<EchoNugget> sortForCamera(List<EchoNugget> nuggets, Vector3f camera) {
        return nuggets.stream()
                .map((nugget) -> new Distance(nugget, nugget.minMaxTo(camera)))
                .sorted()
                .map((dist) -> dist.nugget)
                .toList();
    }

    public static List<EchoNugget> group(Collection<EchoState> echoes) {
        final var result = new ArrayList<EchoNugget>();
        final var sorter = new TriColorSorter(echoes);

        for (final var group : sorter) {
            final var id = group.entrySet().iterator().next().getValue().id();
            result.add(new EchoNugget(id, group));
        }

        return result;
    }

    public static List<EchoNugget> group(Stream<EchoState> echoes) {
        final var result = new ArrayList<EchoNugget>();
        final var sorter = new TriColorSorter(echoes);

        for (final var group : sorter) {
            final var id = group.entrySet().iterator().next().getValue().id();
            result.add(new EchoNugget(id, group));
        }

        return result;
    }

    public static List<View> filterVisible(List<EchoNugget> nuggets, FrustumFilter frustum) {
        return nuggets.stream()
                .filter(frustum::contains)
                .map(nugget -> {
                    final var filtered = nugget.echoStates.values().stream().filter(frustum::contains).toList();
                    return nugget.new View(filtered);
                })
                .toList();
    }

    public class View {
        private final List<EchoState> echoes;

        View(List<EchoState> echoes) {
            this.echoes = echoes;
        }

        EchoNugget parent() {
            return EchoNugget.this;
        }

        public void draw(GlProgramConsumer buffer, MatrixStack matrices, Vector3f camera) {
            furthestToClosest(echoes.stream(), buffer, matrices, camera, EchoState::draw);
        }

        public void sketch(GlProgramConsumer buffer, MatrixStack matrices, Vector3f camera) {
            furthestToClosest(echoes.stream(), buffer, matrices, camera, EchoState::sketch);
        }
    }
}
