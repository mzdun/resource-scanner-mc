// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.event.EventEmitterOf;

public final class Options extends EventEmitterOf.Impl<Settings.Event> {
    static String JSON_NAME = "resource-scanner.json";
    static Options instance = null;
    Path configDir = null;
    Settings settings = null;

    private Options() {
    }

    public static synchronized Options getInstance() {
        if (instance == null)
            instance = new Options();
        return instance;
    }

    public static synchronized void resetInstance() {
        instance = null;
    }

    Path getFileName() {
        return configDir.resolve(JSON_NAME);
    }

    public void setDirectory(Path configDir) {
        this.configDir = configDir;
    }

    public boolean load() {
        settings = null;

        final var file = getFileName().toFile();

        InputStream stream = null;
        try {
            try {
                stream = new FileInputStream(file);
                final var bytes = stream.readAllBytes();
                final var content = new String(bytes, StandardCharsets.UTF_8);
                settings = Settings.deserialize(content);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        } catch (IOException e) {
            // empty
        }

        if (settings != null) {
            dispatchSettingsEvent(settings);
            return true;
        }

        return false;
    }

    public Settings settings() {
        return settings;
    }

    public void setAll(int blockDistance, int blockRadius, int lifetime, Set<Id> interestingIds) {
        setAll(blockDistance, blockRadius, lifetime, interestingIds, true);
    }

    public void setAll(int blockDistance, int blockRadius, int lifetime, Set<Id> interestingIds, boolean notify) {
        settings = new Settings(blockDistance, blockRadius, lifetime, interestingIds);
        store();
        if (notify) {
            dispatchSettingsEvent(settings);
        }
    }

    public void store() {
        final var contents = settings.serialize();
        LoggerFactory.getLogger(getClass()).info(contents);

        final var file = getFileName().toFile();

        OutputStream stream = null;
        try {
            try {
                final var bytes = contents.getBytes(StandardCharsets.UTF_8);
                stream = new FileOutputStream(file);
                stream.write(bytes);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        } catch (IOException e) {
            // empty
        }
    }

    private void dispatchSettingsEvent(Settings settings) {
        dispatchEvent(new Settings.Event(settings));
    }
}
