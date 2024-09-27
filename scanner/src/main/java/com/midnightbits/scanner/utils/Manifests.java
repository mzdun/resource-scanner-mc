// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.utils;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Manifest;

public final class Manifests {
    public static final String UNKNOWN = "unknown";

    public static String getAttribute(String name) {
        Enumeration<URL> resources = null;
        try {
            resources = Manifests.class.getClassLoader()
                    .getResources("META-INF/MANIFEST.MF");
        } catch (IOException E) {
            return null;
        }
        while (resources.hasMoreElements()) {
            try {
                final var manifest = new Manifest(resources.nextElement().openStream());
                final var attr = manifest.getMainAttributes();
                final var value = attr.getValue(name);
                if (value != null)
                    return value;
            } catch (IOException ignored) {
            }
        }

        return null;
    }

    static public String getTagString(String version) {
        return version == null ? UNKNOWN : "v" + version;
    }

    static public String getProductVersion(String product, String version) {
        return version == null ? UNKNOWN + " version of " + product : product + " " + version;
    }

    private Manifests() {
    }
}
