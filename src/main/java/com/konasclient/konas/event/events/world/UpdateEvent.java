package com.konasclient.konas.event.events.world;

public class UpdateEvent {

    public static class Pre extends UpdateEvent {
        private static final Pre INSTANCE = new Pre();

        public static Pre get() {
            return INSTANCE;
        }
    }

    public static class Post extends UpdateEvent {
        private static final Post INSTANCE = new Post();

        public static Post get() {
            return INSTANCE;
        }
    }

}