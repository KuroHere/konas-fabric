package com.konasclient.konas.util.config;

public class ShutdownHook extends Thread {

    @Override
    public void run() {
        Config.save(Config.currentConfig);
    }

}
