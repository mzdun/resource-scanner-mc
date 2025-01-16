// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package api.compat;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SoundEventCompat {
    public static Identifier idOf(SoundEvent event) {
        return event.id();
    }

    private SoundEventCompat() {
    }
}
