// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.rt.core;

import java.util.Optional;
import java.util.ServiceLoader;

import org.slf4j.LoggerFactory;

import com.midnightbits.scanner.platform.PlatformInterface;
import com.midnightbits.scanner.rt.text.TextSupportInterface;

public interface Services {
    PlatformInterface PLATFORM = load(PlatformInterface.class);
    TextSupportInterface TEXT = load(TextSupportInterface.class);

    String TAG = ScannerMod.MOD_ID + "/services";

    static <T> T load(Class<T> clazz) {
        final ClassLoader targetClassLoader = clazz.getClassLoader();
        final Thread currentThread = Thread.currentThread();
        final ClassLoader contextClassLoader = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(targetClassLoader);
            ServiceLoader<T> loader = ServiceLoader.load(clazz);
            Optional<T> serviceCandidate = loader.findFirst();
            T loadedService = serviceCandidate.get();
            LoggerFactory.getLogger(TAG).debug("Loaded {} for service {}", loadedService, clazz.getName());
            return loadedService;
        } finally {
            currentThread.setContextClassLoader(contextClassLoader);
        }
    }
}
