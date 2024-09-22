package com.midnightbits.scanner.utils.test;

import java.math.BigInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.midnightbits.scanner.utils.Version;

public class VersionTest {
    interface CompareVersions {
        boolean compare(Version lhs, Version rhs);
    };

    static CompareVersions LT = (lhs, rhs) -> lhs.compareTo(rhs) < 0;
    static CompareVersions GT = (lhs, rhs) -> lhs.compareTo(rhs) > 0;
    static CompareVersions LE = (lhs, rhs) -> lhs.compareTo(rhs) <= 0;
    static CompareVersions GE = (lhs, rhs) -> lhs.compareTo(rhs) >= 0;
    static CompareVersions EQ = (lhs, rhs) -> lhs.compareTo(rhs) == 0;
    static CompareVersions NE = (lhs, rhs) -> lhs.compareTo(rhs) != 0;

    static record CompareTest(String lhs, CompareVersions comp, String rhs, boolean neg) {
        public boolean runTest() {
            final var lhsVersion = Version.parse(lhs);
            final var rhsVersion = Version.parse(rhs);

            final var ret = comp.compare(lhsVersion, rhsVersion);
            return neg ? !ret : ret;
        }

        static public CompareTest of(String lhs, CompareVersions comp, String rhs, boolean neg) {
            return new CompareTest(lhs, comp, rhs, neg);
        }

        static public CompareTest of(String lhs, CompareVersions comp, String rhs) {
            return of(lhs, comp, rhs, false);
        }

        static public CompareTest ofNot(String lhs, CompareVersions comp, String rhs) {
            return of(lhs, comp, rhs, true);
        }

        public String toString() {
            final var value = lhs + " " + op(comp) + " " + rhs;
            if (neg)
                return "!(" + value + ")";
            return value;
        }

        static private String op(CompareVersions comp) {
            if (comp == LT)
                return "<";
            if (comp == GT)
                return ">";
            if (comp == LE)
                return "<=";
            if (comp == GE)
                return ">=";
            if (comp == EQ)
                return "==";
            if (comp == NE)
                return "!=";
            return String.valueOf(comp);
        }
    };

    private static BigInteger big(int value) {
        return big(String.valueOf(value));
    }

    private static BigInteger big(String value) {
        return new BigInteger(value);
    }

    @Test
    void checkGetters() {
        final var version = Version.parse("1.2.99-alpha.0valid");
        final var preRelease = version.preRelease();
        final var preReleaseIter = preRelease.iterator();

        Assertions.assertNotNull(version);
        Assertions.assertEquals(big(1), version.major());
        Assertions.assertEquals(big(2), version.minor());
        Assertions.assertEquals(big(99), version.patch());
        Assertions.assertEquals(2, preRelease.size());

        Assertions.assertTrue(preReleaseIter.hasNext());
        Assertions.assertEquals(Version.preReleaseSegment("alpha"), preReleaseIter.next());

        Assertions.assertTrue(preReleaseIter.hasNext());
        Assertions.assertEquals(Version.preReleaseSegment("0valid"), preReleaseIter.next());

        Assertions.assertEquals("", version.buildMetaData());
    }

    @Test
    void checkGettersWithMeta() {
        final var version = Version.parse("1.2.99-alpha.0.99+meta");
        final var preRelease = version.preRelease();
        final var preReleaseIter = preRelease.iterator();

        Assertions.assertNotNull(version);
        Assertions.assertEquals(big(1), version.major());
        Assertions.assertEquals(big(2), version.minor());
        Assertions.assertEquals(big(99), version.patch());
        Assertions.assertEquals(3, preRelease.size());

        Assertions.assertTrue(preReleaseIter.hasNext());
        Assertions.assertEquals(Version.preReleaseSegment("alpha"), preReleaseIter.next());

        Assertions.assertTrue(preReleaseIter.hasNext());
        Assertions.assertEquals(Version.preReleaseSegment(big(0)), preReleaseIter.next());

        Assertions.assertTrue(preReleaseIter.hasNext());
        Assertions.assertEquals(Version.preReleaseSegment(big(99)), preReleaseIter.next());

        Assertions.assertEquals("meta", version.buildMetaData());
    }

    @ParameterizedTest
    @MethodSource("provideVersionTests")
    void compareVersions(CompareTest data) {
        Assertions.assertTrue(data.runTest());
    }

    @ParameterizedTest
    @MethodSource("provideParserTests")
    void provideParserTests(String version, boolean valid) {
        final var ver = Version.parse(version);
        if (valid) {
            Assertions.assertNotNull(ver);
            Assertions.assertEquals(version, ver.toString(), version + " vs " + ver.toString());
        } else {
            Assertions.assertNull(ver);
        }
    }

    static Arguments validVersion(String version) {
        return Arguments.of(version, true);
    }

    static Arguments invalidVersion(String version) {
        return Arguments.of(version, false);
    }

    static Stream<Arguments> provideVersionTests() {
        return Stream.of(
                // semver.org
                Arguments.of(CompareTest.of("1.9.0", LT, "1.10.0")),
                Arguments.of(CompareTest.of("1.10.0", LT, "1.11.0")),
                Arguments.of(CompareTest.ofNot("1.10.0", EQ, "1.11.0")),
                Arguments.of(CompareTest.of("1.11.0", GT, "1.9.0")),
                Arguments.of(CompareTest.of("1.10.0", LT, "1.11.0")),
                Arguments.of(CompareTest.of("1.0.0", LT, "2.0.0")),
                Arguments.of(CompareTest.ofNot("1.0.0", GT, "2.0.0")),
                Arguments.of(CompareTest.ofNot("1.0.0", EQ, "2.0.0")),
                Arguments.of(CompareTest.of("2.0.0", LT, "2.1.0")),
                Arguments.of(CompareTest.ofNot("2.0.0", GT, "2.1.0")),
                Arguments.of(CompareTest.ofNot("2.0.0", EQ, "2.1.0")),
                Arguments.of(CompareTest.of("2.1.0", LT, "2.1.1")),
                Arguments.of(CompareTest.ofNot("2.1.0", GT, "2.1.1")),
                Arguments.of(CompareTest.ofNot("2.1.0", EQ, "2.1.1")),
                Arguments.of(CompareTest.of("1.0.0-alpha", LT, "1.0.0")),
                Arguments.of(CompareTest.ofNot("1.0.0-alpha", GT, "1.0.0")),
                Arguments.of(CompareTest.ofNot("1.0.0-alpha", EQ, "1.0.0")),
                Arguments.of(CompareTest.ofNot("1.0.0", EQ, "1.0.0-alpha")),
                Arguments.of(CompareTest.ofNot("1.0.0-alpha", GE, "1.0.0-alpha.1")),
                Arguments.of(CompareTest.ofNot("1.0.0-alpha.1", GE, "1.0.0-alpha.beta")),
                Arguments.of(CompareTest.of("1.0.0-alpha.beta", GE, "1.0.0-alpha.1")),
                Arguments.of(CompareTest.ofNot("1.0.0-alpha.beta", GE, "1.0.0-beta")),
                Arguments.of(CompareTest.ofNot("1.0.0-beta", GE, "1.0.0-beta.2")),
                Arguments.of(CompareTest.ofNot("1.0.0-beta.2", GE, "1.0.0-beta.11")),
                Arguments.of(CompareTest.ofNot("1.0.0-beta.11", GE, "1.0.0-rc.1")),
                Arguments.of(CompareTest.ofNot("1.0.0-rc.1", GE, "1.0.0")),
                // other
                Arguments.of(CompareTest.of("1.0.0-alpha.11", GT, "1.0.0-alpha.1")),
                Arguments.of(CompareTest.of("1.0.0-alpha.text-11", LT, "1.0.0-alpha.text-1a")),
                Arguments.of(CompareTest.of("1.0.0", EQ, "1.0.0+meta.1")),
                Arguments.of(CompareTest.of("1.0.0+meta.1", EQ, "1.0.0+meta.2"))
        // done
        );
    }

    static Stream<Arguments> provideParserTests() {
        return Stream.of(
                validVersion("1.9.0"),
                validVersion("1.10.0"),
                validVersion("1.10.0"),
                validVersion("1.11.0"),
                validVersion("1.0.0"),
                validVersion("2.0.0"),
                validVersion("2.1.0"),
                validVersion("2.1.1"),
                validVersion("1.0.0-alpha"),
                validVersion("1.0.0-alpha.1"),
                validVersion("1.0.0-alpha.beta"),
                validVersion("1.0.0-beta"),
                validVersion("1.0.0-beta.2"),
                validVersion("1.0.0-beta.11"),
                validVersion("1.0.0-rc.1"),
                validVersion("1.0.0-alpha.text-11"),
                validVersion("1.0.0-alpha.text-1a"),
                validVersion("1.0.0+meta.1"),
                validVersion("1.0.0+meta.2"),

                validVersion("0.0.4"),
                validVersion("1.2.3"),
                validVersion("10.20.30"),
                validVersion("1.1.2-prerelease+meta"),
                validVersion("1.1.2+meta"),
                validVersion("1.1.2+meta-valid"),
                validVersion("1.0.0-alpha"),
                validVersion("1.0.0-beta"),
                validVersion("1.0.0-alpha.beta"),
                validVersion("1.0.0-alpha.beta.1"),
                validVersion("1.0.0-alpha.1"),
                validVersion("1.0.0-alpha0.valid"),
                validVersion("1.0.0-alpha.0valid"),
                validVersion("1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay"),
                validVersion("1.0.0-rc.1+build.1"),
                validVersion("2.0.0-rc.1+build.123"),
                validVersion("1.2.3-beta"),
                validVersion("10.2.3-DEV-SNAPSHOT"),
                validVersion("1.2.3-SNAPSHOT-123"),
                validVersion("1.0.0"),
                validVersion("2.0.0"),
                validVersion("1.1.7"),
                validVersion("2.0.0+build.1848"),
                validVersion("2.0.1-alpha.1227"),
                validVersion("1.0.0-alpha+beta"),
                validVersion("1.2.3----RC-SNAPSHOT.12.9.1--.12+788"),
                validVersion("1.2.3----R-S.12.9.1--.12+meta"),
                validVersion("1.2.3----RC-SNAPSHOT.12.9.1--.12"),
                validVersion("1.0.0+0.build.1-rc.10000aaa-kk-0.1"),
                validVersion("99999999999999999999999.999999999999999999.99999999999999999"),
                validVersion("1.0.0-0A.is.legal"),

                invalidVersion("1"),
                invalidVersion("1.2"),
                invalidVersion("1.2.3-0123"),
                invalidVersion("1.2.3-0123.0123"),
                invalidVersion("1.1.2+.123"),
                invalidVersion("+invalid"),
                invalidVersion("-invalid"),
                invalidVersion("-invalid+invalid"),
                invalidVersion("-invalid.01"),
                invalidVersion("alpha"),
                invalidVersion("alpha.beta"),
                invalidVersion("alpha.beta.1"),
                invalidVersion("alpha.1"),
                invalidVersion("alpha+beta"),
                invalidVersion("alpha_beta"),
                invalidVersion("alpha."),
                invalidVersion("alpha.."),
                invalidVersion("beta"),
                invalidVersion("1.0.0-alpha_beta"),
                invalidVersion("-alpha."),
                invalidVersion("1.0.0-alpha.."),
                invalidVersion("1.0.0-alpha..1"),
                invalidVersion("1.0.0-alpha...1"),
                invalidVersion("1.0.0-alpha....1"),
                invalidVersion("1.0.0-alpha.....1"),
                invalidVersion("1.0.0-alpha......1"),
                invalidVersion("1.0.0-alpha.......1"),
                invalidVersion("01.1.1"),
                invalidVersion("1.01.1"),
                invalidVersion("1.1.01"),
                invalidVersion("1.2"),
                invalidVersion("1.2.3.DEV"),
                invalidVersion("1.2-SNAPSHOT"),
                invalidVersion("1.2.31.2.3----RC-SNAPSHOT.12.09.1--..12+788"),
                invalidVersion("1.2-RC-SNAPSHOT"),
                invalidVersion("-1.0.3-gamma+b7718"),
                invalidVersion("+justmeta"),
                invalidVersion("9.8.7+meta+meta"),
                invalidVersion("9.8.7-whatever+meta+meta"),
                invalidVersion("99999999999999999999999.999999999999999999.99999999999999999"
                        + "----RC-SNAPSHOT.12.09.1--------------------------------..12")
        // done
        );
    }
}
