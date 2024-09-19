package com.midnightbits.scanner.utils.test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.midnightbits.scanner.sonar.BlockEchoes;
import com.midnightbits.scanner.sonar.Sonar;
import com.midnightbits.scanner.utils.Settings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.midnightbits.scanner.rt.core.Services;
import com.midnightbits.scanner.test.support.Counter;
import com.midnightbits.scanner.utils.ConfigFile;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ConfigFileTest {
    final static Path configDir = Services.PLATFORM.getConfigDir();
    final static Path configFile = configDir.resolve("resource-scanner.json");

    @BeforeAll
    static void startupCleanup() {
        try {
            delete();
        } catch (IOException ignored) {
        }
    }

    @AfterEach
    void cleanup() {
        startupCleanup();
    }

    @Test
    void configIsADir() {
        final var counter = new Counter();

        configFile.toFile().mkdirs();

        final var cfg = new ConfigFile();
        cfg.addEventListener((settings) -> counter.inc());
        cfg.setDirectory(configDir);
        cfg.load();

        Assertions.assertEquals(0, counter.get());
    }

    @ParameterizedTest
    @MethodSource("provideBrokenJsons")
    void configIsABrokenJson(String contents) {
        final var counter = new Counter();

        store(contents);

        final var cfg = new ConfigFile();
        cfg.addEventListener((event) -> {
            counter.inc();
            System.out.println(event.settings());
        });
        cfg.setDirectory(configDir);
        cfg.load();

        Assertions.assertEquals(0, counter.get(), contents);
    }

    @Test
    void configIsAnEmptyObject() {
        final List<Settings> settings = new ArrayList<>();

        store("{\"interestingIds\":[]}");

        final var cfg = new ConfigFile();
        cfg.addEventListener((e) -> {
            settings.add(e.settings());
        });
        cfg.setDirectory(configDir);
        cfg.load();

        Assertions.assertEquals(1, settings.size());
        Assertions.assertEquals(new Settings(0, 0, 0, Set.of()), settings.getFirst());
    }

    @Test
    void configDoesNotSerializeToDirectory() {
        final var counter = new Counter();
        configFile.toFile().mkdirs();

        final var cfg = new ConfigFile();
        cfg.addEventListener((settings) -> counter.inc());
        cfg.setDirectory(configDir);
        cfg.setAll(BlockEchoes.MAX_SIZE,
                Sonar.BLOCK_DISTANCE,
                Sonar.BLOCK_RADIUS,
                Set.of(Sonar.INTERESTING_IDS),
                true);

        final var actual = load();

        Assertions.assertNull(actual);
        Assertions.assertEquals(1, counter.get());
    }

    static Stream<Arguments> provideBrokenJsons() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("{}"),
                Arguments.of("{\"interestingIds\":53}"),
                Arguments.of("{\"interestingIds\":null}"),
                Arguments.of("{\"interestingIds\":true}"),
                Arguments.of("{\"interestingIds\":false}"),
                Arguments.of("{\"interestingIds\":[], \"echoesSize\": -5}"),
                Arguments.of("{\"interestingIds\":[], \"blockDistance\": -5}"),
                Arguments.of("{\"interestingIds\":[], \"blockRadius\": -5}"));
    }

    static void delete() throws IOException {
        delete(configFile.toFile());
    }

    static void delete(File f) throws IOException {
        if (f.isDirectory()) {
            final var files = f.listFiles();
            if (files != null) {
                for (File c : files)
                    delete(c);
            }
        }
        f.delete();
    }

    static void store(String contents) {
        store(configFile.toFile(), contents);
    }

    static void store(File f, String contents) {
        try {
            try (OutputStream out = new FileOutputStream(f)) {
                out.write(contents.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException ignored) {
        }
    }

    static String load() {
        return load(configFile.toFile());
    }

    static String load(File f) {
        try {
            try (InputStream out = new FileInputStream(f)) {
                final var bytes = out.readAllBytes();
                return new String(bytes, StandardCharsets.UTF_8);
            }
        } catch (IOException ignored) {
        }

        return null;
    }
}
