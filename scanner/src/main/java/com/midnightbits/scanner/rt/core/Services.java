package com.midnightbits.scanner.rt.core;

import java.util.Optional;
import java.util.ServiceLoader;

import org.slf4j.LoggerFactory;

import com.midnightbits.scanner.platform.PlatformInterface;
import com.midnightbits.scanner.rt.text.TextSupportInterface;

public interface Services {
    public static PlatformInterface PLATFORM = load(PlatformInterface.class);
    public static TextSupportInterface TEXT = load(TextSupportInterface.class);

    public static final String TAG = "resource-scanner/services";

    public static <T> T load(Class<T> clazz) {
        final ClassLoader targetClassLoader = clazz.getClassLoader();
        final Thread currentThread = Thread.currentThread();
        final ClassLoader contextClassLoader = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(targetClassLoader);
            ServiceLoader<T> loader = ServiceLoader.load(clazz);
            Optional<T> serviceCandidate = loader.findFirst();
            T loadedService = serviceCandidate.get();
            LoggerFactory.getLogger(TAG).info("Loaded {} for service {}", loadedService, clazz.getName());
            return loadedService;
        } finally {
            currentThread.setContextClassLoader(contextClassLoader);
        }
    }
}
