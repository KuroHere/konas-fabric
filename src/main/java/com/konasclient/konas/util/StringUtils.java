package com.konasclient.konas.util;

import net.minecraft.client.util.InputUtil;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;

public class StringUtils {

    public static String nameToTitle(String name) {
        return Arrays.stream(name.split("-")).map(org.apache.commons.lang3.StringUtils::capitalize).collect(Collectors.joining(""));
    }

    public static String ticksToElapsedTime(int ticks) {
        int i = ticks / 20;
        int j = i / 60;
        i %= 60;
        return i < 10 ? j + ":0" + i : j + ":" + i;
    }

    public static String[] getKeyNames() {
        String[] keyNames = new String[58];
        for(int i = 39; i < 97; i++) {
            keyNames[i - 39] = glfwGetKeyName(GLFW_KEY_UNKNOWN, i);
        }
        return keyNames;
    }

    // glfwGetKeyName is one way
    public static int getKeyCodeFromKey(String key) {
        for(int i = 39; i < 97; i++) {
            if (key.equalsIgnoreCase(glfwGetKeyName(GLFW_KEY_UNKNOWN, i))) {
                return i;
            }
        }

        return -1;
    }

    public static String getKeyName(int key) {
        switch (key) {
            case GLFW_KEY_UNKNOWN:
                return "NONE";
            case GLFW_KEY_ESCAPE:
                return "ESC";
            case GLFW_KEY_PRINT_SCREEN:
                return "PRTSCRN";
            case GLFW_KEY_GRAVE_ACCENT:
                return "GRAVE";
            case GLFW_KEY_WORLD_1:
                return "WORLD";
            case GLFW_KEY_WORLD_2:
                return "WORLD";
            case GLFW_KEY_PAUSE:
                return "PAUSE";
            case GLFW_KEY_INSERT:
                return "INSERT";
            case GLFW_KEY_DELETE:
                return "DELETE";
            case GLFW_KEY_HOME:
                return "HOME";
            case GLFW_KEY_PAGE_UP:
                return "PAGEUP";
            case GLFW_KEY_PAGE_DOWN:
                return "PAGEDOWN";
            case GLFW_KEY_END:
                return "END";
            case GLFW_KEY_TAB:
                return "TAB";
            case GLFW_KEY_LEFT_CONTROL:
                return "LCONTROL";
            case GLFW_KEY_RIGHT_CONTROL:
                return "RCONTROL";
            case GLFW_KEY_LEFT_ALT:
                return "LALT";
            case GLFW_KEY_RIGHT_ALT:
                return "RALT";
            case GLFW_KEY_LEFT_SHIFT:
                return "LSHIFT";
            case GLFW_KEY_RIGHT_SHIFT:
                return "RSHIFT";
            case GLFW_KEY_UP:
                return "UP";
            case GLFW_KEY_DOWN:
                return "DOWN";
            case GLFW_KEY_LEFT:
                return "LEFT";
            case GLFW_KEY_RIGHT:
                return "RIGHT";
            case GLFW_KEY_BACKSPACE:
                return "BACKSPACE";
            case GLFW_KEY_CAPS_LOCK:
                return "CAPSLOCK";
            case GLFW_KEY_MENU:
                return "MENU";
            case GLFW_KEY_LEFT_SUPER:
                return "LSUPER";
            case GLFW_KEY_RIGHT_SUPER:
                return "RSUPER";
            case GLFW_KEY_ENTER:
                return "ENTER";
            case GLFW_KEY_NUM_LOCK:
                return "NUMLOCK";
            case GLFW_KEY_SPACE:
                return "SPACE";
            case GLFW_KEY_F1:
                return "F1";
            case GLFW_KEY_F2:
                return "F2";
            case GLFW_KEY_F3:
                return "F3";
            case GLFW_KEY_F4:
                return "F4";
            case GLFW_KEY_F5:
                return "F5";
            case GLFW_KEY_F6:
                return "F6";
            case GLFW_KEY_F7:
                return "F7";
            case GLFW_KEY_F8:
                return "F8";
            case GLFW_KEY_F9:
                return "F9";
            case GLFW_KEY_F10:
                return "F10";
            case GLFW_KEY_F11:
                return "F11";
            case GLFW_KEY_F12:
                return "F12";
            case GLFW_KEY_F13:
                return "F13";
            case GLFW_KEY_F14:
                return "F14";
            case GLFW_KEY_F15:
                return "F15";
            case GLFW_KEY_F16:
                return "F16";
            case GLFW_KEY_F17:
                return "F17";
            case GLFW_KEY_F18:
                return "F18";
            case GLFW_KEY_F19:
                return "F19";
            case GLFW_KEY_F20:
                return "F20";
            case GLFW_KEY_F21:
                return "F21";
            case GLFW_KEY_F22:
                return "F22";
            case GLFW_KEY_F23:
                return "F23";
            case GLFW_KEY_F24:
                return "F24";
            case GLFW_KEY_F25:
                return "F25";
            case GLFW_MOUSE_BUTTON_MIDDLE:
                return "MIDDLECLICK";
            case GLFW_MOUSE_BUTTON_4:
                return "MOUSE4";
            case GLFW_MOUSE_BUTTON_5:
                return "MOUSE5";
            case GLFW_MOUSE_BUTTON_6:
                return "MOUSE6";
            case GLFW_MOUSE_BUTTON_7:
                return "MOUSE7";
            case GLFW_MOUSE_BUTTON_8:
                return "MOUSE8";
            default:
                String keyName = glfwGetKeyName(key, 0);
                if (keyName == null) return "UNKNOWN";
                return keyName.toUpperCase();
        }
    }

}