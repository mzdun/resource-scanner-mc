package com.midnightbits.scanner.modmenu.gui;

public interface MoveableWidget {
    default int preferredSeparator() {
        return Constants.OPTION_SEP;
    }

    default int preferredHeight(int parentHeight) {
        return -1;
    }
}
