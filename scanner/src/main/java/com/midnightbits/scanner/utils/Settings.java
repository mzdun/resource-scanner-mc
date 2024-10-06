// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.utils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.sonar.BlockEchoes;
import org.jetbrains.annotations.Nullable;

public record Settings(int blockDistance, int blockRadius, int lifetime, Set<Id> interestingIds) {

    private static final Gson GSON = new Gson();

    private record JsonSettings(int blockDistance, int blockRadius, @Nullable Double lifetime,
            String[] interestingIds) {
    };

    String serialize() {
        final var ids = interestingIds.stream().map(Id::toString).sorted().toArray(String[]::new);
        final var lifetimeInSec = lifetime / 1000.0;
        return GSON.toJson(new JsonSettings(blockDistance, blockRadius, lifetimeInSec, ids));
    }

    static Settings deserialize(String content) {
        try {
            var settings = GSON.fromJson(content, JsonSettings.class);
            if (settings == null || settings.interestingIds == null || settings.blockDistance < 0
                    || settings.blockRadius < 0 || (settings.lifetime != null && settings.lifetime < 0)) {
                return null;
            }
            var ids = Arrays.stream(settings.interestingIds).map(Id::of).collect(Collectors.toSet());
            var lifetime = settings.lifetime == null ? BlockEchoes.ECHO_LIFETIME
                    : (int) (settings.lifetime * 1000 + .5);
            return new Settings(settings.blockDistance, settings.blockRadius, lifetime, ids);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public Settings withBlockDistance(int value) {
        return new Settings(value, blockRadius, lifetime, interestingIds);
    }

    public Settings withBlockRadius(int value) {
        return new Settings(blockDistance, value, lifetime, interestingIds);
    }

    public Settings withLifetime(int value) {
        return new Settings(blockDistance, blockRadius, value, interestingIds);
    }

    public Settings withIds(Set<Id> value) {
        return new Settings(blockDistance, blockRadius, lifetime, value);
    }

    public static class Event extends com.midnightbits.scanner.rt.event.Event {
        private final Settings settings;

        public Event(Settings settings) {
            this.settings = settings;
        }

        public Settings settings() {
            return settings;
        }
    };
};
