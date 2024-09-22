package com.midnightbits.scanner.utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Version implements Comparable<Version> {
    public interface PreReleaseSegment extends Comparable<PreReleaseSegment> {
    };

    private static record StringPreReleaseSegment(String tok) implements PreReleaseSegment {
        @Override
        public String toString() {
            return tok;
        }

        @Override
        public int compareTo(PreReleaseSegment other) {
            if (!(other instanceof StringPreReleaseSegment strToken)) {
                // 11.4.3 Numeric identifiers always have lower precedence than non-numeric
                // identifiers.
                return 1;
            }
            return tok.compareTo(strToken.tok);
        }
    };

    private static record NumberPreReleaseSegment(BigInteger tok) implements PreReleaseSegment {
        @Override
        public String toString() {
            return String.valueOf(tok);
        }

        @Override
        public int compareTo(PreReleaseSegment other) {
            if (!(other instanceof NumberPreReleaseSegment numToken)) {
                // 11.4.3 Numeric identifiers always have lower precedence than non-numeric
                // identifiers.
                return -1;
            }
            return tok.compareTo(numToken.tok);
        }
    };

    public static PreReleaseSegment preReleaseSegment(String tok) {
        return new StringPreReleaseSegment(tok);
    }

    public static PreReleaseSegment preReleaseSegment(BigInteger tok) {
        return new NumberPreReleaseSegment(tok);
    }

    private static class PreRelease implements Comparable<PreRelease> {
        final List<PreReleaseSegment> items;

        static PreRelease empty() {
            return new PreRelease(new ArrayList<>());
        }

        public PreRelease(List<PreReleaseSegment> items) {
            this.items = items;
        }

        public int size() {
            return items.size();
        }

        public Collection<PreReleaseSegment> items() {
            return items;
        }

        @Override
        public String toString() {
            return items.stream().map(String::valueOf).collect(Collectors.joining("."));
        }

        @Override
        public int compareTo(PreRelease other) {
            final var length = Math.min(size(), other.size());
            for (var index = 0; index < length; ++index) {
                final var cmp = items.get(index).compareTo(other.items.get(index));
                if (cmp != 0)
                    return cmp;
            }
            return size() - other.size();
        }

    };

    final BigInteger major;
    final BigInteger minor;
    final BigInteger patch;
    final PreRelease preRelease;
    final String buildMetaData;

    Version(BigInteger major, BigInteger minor, BigInteger patch, PreRelease preRelease,
            String buildMetaData) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.preRelease = preRelease;
        this.buildMetaData = buildMetaData;
    }

    public BigInteger major() {
        return major;
    }

    public BigInteger minor() {
        return minor;
    }

    public BigInteger patch() {
        return patch;
    }

    public Collection<PreReleaseSegment> preRelease() {
        return preRelease.items();
    }

    public String buildMetaData() {
        return buildMetaData;
    }

    @Override
    public int compareTo(Version other) {
        int cmp = major.compareTo(other.major);
        if (cmp != 0)
            return cmp;

        cmp = minor.compareTo(other.minor);
        if (cmp != 0)
            return cmp;

        cmp = patch.compareTo(other.patch);
        if (cmp != 0)
            return cmp;

        // 11.3 When major, minor, and patch are equal, a pre-release version has lower
        // precedence than a normal version:
        if (preRelease.size() == 0 && other.preRelease.size() > 0) {
            return 1;
        }

        if (preRelease.size() > 0 && other.preRelease.size() == 0) {
            return -1;
        }

        return preRelease.compareTo(other.preRelease);
    }

    @Override
    public String toString() {
        final var coreVersion = major + "." + minor + "." + patch;
        final var preReleaseValue = String.valueOf(preRelease);
        final var preReleaseStr = preReleaseValue.isEmpty() ? "" : "-" + preReleaseValue;
        final var buildMetaDataStr = buildMetaData.isEmpty() ? "" : "+" + buildMetaData;

        return coreVersion + preReleaseStr + buildMetaDataStr;
    }

    private static final Pattern versionParser = Pattern.compile(
            "^"
                    + "(0|[1-9]\\d*)\\." // major
                    + "(0|[1-9]\\d*)\\." // minor
                    + "(0|[1-9]\\d*)" // patch
                    + "(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?" // prerelease
                    + "(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?" // meta
                    + "$");
    private static final Pattern segmentSplitter = Pattern.compile("\\.");
    private static final Pattern digitsFinder = Pattern.compile("^\\d+$");

    public static Version parse(String string) {
        if (string == null)
            return null;
        final var matcher = versionParser.matcher(string);
        if (!matcher.matches())
            return null;
        final var major = new BigInteger(matcher.group(1));
        final var minor = new BigInteger(matcher.group(2));
        final var patch = new BigInteger(matcher.group(3));
        final var prerelease = matcher.group(4);
        final var buildMetaData = matcher.group(5);

        return new Version(major, minor, patch, parsePreRelease(prerelease),
                buildMetaData == null ? "" : buildMetaData);
    }

    private static PreRelease parsePreRelease(String prerelease) {
        if (prerelease == null)
            return PreRelease.empty();

        final var segments = segmentSplitter.splitAsStream(prerelease).map(Version::asSegment).toList();
        return new PreRelease(segments);
    }

    private static PreReleaseSegment asSegment(String segment) {
        final var matcher = digitsFinder.matcher(segment);
        if (matcher.matches()) {
            return preReleaseSegment(new BigInteger(segment));
        }
        return preReleaseSegment(segment);
    }
}
