package com.konasclient.konas.module.modules.movement;

import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;

public class Jesus extends Module {
    public Jesus() {
        super("jesus", "Lets you walk on water", 0xFF58C6D0, Category.Movement);
    }

    private enum Denomination {
        Baptist,
        Methodist,
        Presbyterian,
        Orthodox,
        Catholic,
        Christian,
        Lutheran,
        Spirit,
        Reformed,
        Episcopal
    }
}
