package com.konasclient.konas.event.events.client;

public class ModuleInitialisationEvent {

    public static class Pre extends ModuleInitialisationEvent {
        private static final Pre INSTANCE = new Pre();

        public static Pre get() {
            return INSTANCE;
        }
    }

    public static class Post extends ModuleInitialisationEvent {
        private static final Post INSTANCE = new Post();

        public static Post get() {
            return INSTANCE;
        }
    }

}