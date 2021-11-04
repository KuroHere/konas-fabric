package com.konasclient.konas.interfaceaccessors;

import net.minecraft.util.math.Quaternion;

public interface IMatrix4f {
    Quaternion mult(Quaternion q);
}