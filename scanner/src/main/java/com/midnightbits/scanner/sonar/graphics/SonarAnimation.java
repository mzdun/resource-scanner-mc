package com.midnightbits.scanner.sonar.graphics;

import com.midnightbits.scanner.rt.core.ClientCore;
import com.midnightbits.scanner.rt.core.Services;
import com.midnightbits.scanner.sonar.Sonar;

public class SonarAnimation {
    private final Sonar target;
    private final Scene scene;
    private final SliceSpacing spacer;

    public SonarAnimation(Sonar target) {
        this.target = target;
        this.scene = new Scene(target);
        this.spacer = new SliceSpacing(SliceSpacing.DURATION);
    }

    public boolean sendPing(ClientCore client, WaveAnimator.StageReporter stageReporter) {
        return target.sendPing(client, spacer,
                new WaveAnimator(scene, Services.PLATFORM.getAnimatorHost(), target, stageReporter), null);
    }
}
