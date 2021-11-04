package com.konasclient.konas.event.events.client;

import com.konasclient.konas.event.Cancellable;

public class KeyEvent extends Cancellable {
    private static final KeyEvent INSTANCE = new KeyEvent();

    public int keyCode;
    public int state;

    public static KeyEvent get(int keyCode, int state) {
        INSTANCE.keyCode = keyCode;
        INSTANCE.state = state;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
