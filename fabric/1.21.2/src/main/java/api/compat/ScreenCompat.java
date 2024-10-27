// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package api.compat;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class ScreenCompat extends Screen {
    protected ScreenCompat(Text title) {
        super(title);
    }
}
