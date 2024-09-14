package com.midnightbits.scanner.rt.core;

import com.midnightbits.scanner.rt.math.V3i;
import com.midnightbits.scanner.rt.text.Text;

public interface ClientCore {
    public BlockInfo getBlockInfo(V3i pos);

    public V3i getPlayerPos();

    public float getCameraPitch();

    public float getCameraYaw();

    public void sendPlayerMessage(Text message, boolean overlay);
}
