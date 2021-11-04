package com.konasclient.konas.event.events.world;

public class GameLeftEvent {

    private static final GameLeftEvent INSTANCE = new GameLeftEvent();

    public static GameLeftEvent get() {
        return INSTANCE;
    }

}