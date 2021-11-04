package com.konasclient.konas.event.events.player;

public class BlockReachDistanceEvent {
    private static final BlockReachDistanceEvent INSTANCE = new BlockReachDistanceEvent();

    public float reachDistance;

    public static BlockReachDistanceEvent get(float reachDistance) {
        INSTANCE.reachDistance = reachDistance;
        return INSTANCE;
    }
}
