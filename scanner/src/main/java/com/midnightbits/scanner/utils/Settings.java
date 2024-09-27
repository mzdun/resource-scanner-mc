// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.utils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.midnightbits.scanner.rt.core.Id;

public record Settings(int echoesSize, int blockDistance, int blockRadius, Set<Id> interestingIds) {

    private static final Gson GSON = new Gson();

    private record JsonSettings(int echoesSize, int blockDistance, int blockRadius, String[] interestingIds) {
    };

    String serialize() {
        final var ids = interestingIds.stream().map(Id::toString).sorted().toArray(String[]::new);
        return GSON.toJson(new JsonSettings(echoesSize, blockDistance, blockRadius, ids));
    }

    static Settings deserialize(String content) {
        try {
            var settings = GSON.fromJson(content, JsonSettings.class);
            if (settings == null || settings.interestingIds == null || settings.echoesSize < 0
                    || settings.blockDistance < 0 || settings.blockRadius < 0) {
                return null;
            }
            var ids = Arrays.stream(settings.interestingIds).map(Id::of).collect(Collectors.toSet());
            return new Settings(settings.echoesSize, settings.blockDistance, settings.blockRadius, ids);
        } catch (JsonSyntaxException e) {
            return null;
        }
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
