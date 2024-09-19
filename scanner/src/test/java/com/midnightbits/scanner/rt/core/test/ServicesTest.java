package com.midnightbits.scanner.rt.core.test;

import java.util.ServiceConfigurationError;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.midnightbits.scanner.platform.PlatformInterface;
import com.midnightbits.scanner.rt.core.Services;
import com.midnightbits.scanner.test.mocks.platform.MockPlatform;

public class ServicesTest {
    private interface NoSuchService {
    }

    @Test
    public void checkPlatform() {
        PlatformInterface platform = Services.PLATFORM;
        Assertions.assertEquals(MockPlatform.class, platform.getClass());

        MockPlatform.developmentEnvironment = true;
        Assertions.assertEquals("development", platform.getEnvironmentName());

        MockPlatform.developmentEnvironment = false;
        Assertions.assertEquals("production", platform.getEnvironmentName());
    }

    @Test
    public void getIgnoredInterface() {
        Assertions.assertThrows(ServiceConfigurationError.class, () -> Services.load(NoSuchService.class));
    }
}
