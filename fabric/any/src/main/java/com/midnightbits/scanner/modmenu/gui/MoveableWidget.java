// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.modmenu.gui;

public interface MoveableWidget {
    default int preferredSeparator() {
        return Constants.OPTION_SEP;
    }

    default int preferredHeight(int parentHeight) {
        return -1;
    }
}
