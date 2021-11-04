package com.konasclient.konas.util.chat;

import com.konasclient.konas.Konas;
import com.konasclient.konas.mixin.ChatHudAccessor;
import com.konasclient.konas.util.client.ThreadUtils;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class Chat {

    public static void message(int id, Formatting main, String msg, Object... args) {
        sendMsg(id, formatMsg(msg, main, args), main);
    }

    public static void info(int id, String msg, Object... args) {
        message(id, Formatting.WHITE, msg, args);
    }

    public static void info(String msg, Object... args) {
        info(0, msg, args);
    }

    public static void warning(int id, String msg, Object... args) {
        message(id, Formatting.YELLOW, msg, args);
    }

    public static void warning(String msg, Object... args) {
        warning(0, msg, args);
    }

    public static void error(int id, String msg, Object... args) {
        message(id, Formatting.RED, msg, args);
    }

    public static void error(String msg, Object... args) {
        error(0, msg, args);
    }

    public static void lineBreak(boolean prefix) {
        if (prefix) sendMsg(Integer.MAX_VALUE, "", Formatting.OBFUSCATED);
        else ((ChatHudAccessor) Konas.mc.inGameHud.getChatHud()).add(new LiteralText(""), Integer.MAX_VALUE);
    }

    private static void sendMsg(int id, String msg, Formatting color) {
        if (!ThreadUtils.canUpdate()) return;

        BaseText formattedMessage = new LiteralText(msg);
        formattedMessage.setStyle(formattedMessage.getStyle().withFormatting(color));

        BaseText finalMsg = new LiteralText("");
        finalMsg.append(getPrefix());
        finalMsg.append(formattedMessage);

        ((ChatHudAccessor) Konas.mc.inGameHud.getChatHud()).add(finalMsg, id);
    }

    private static BaseText getPrefix() {
        BaseText konas = new LiteralText("Konas");
        konas.setStyle(konas.getStyle().withFormatting(Formatting.DARK_PURPLE));

        BaseText prefix = new LiteralText("");
        prefix.setStyle(prefix.getStyle().withFormatting(Formatting.GRAY));
        prefix.append("[");
        prefix.append(konas);
        prefix.append("] ");

        return prefix;
    }

    private static String formatMsg(String format, Formatting defaultColor, Object... args) {
        String msg = String.format(format, args);

        msg = msg.replaceAll("\\(red\\)", Formatting.RED.toString())
                .replaceAll("\\(green\\)", Formatting.GREEN.toString())
                .replaceAll("\\(blue\\)", Formatting.AQUA.toString())
                .replaceAll("\\(highlight\\)", Formatting.LIGHT_PURPLE.toString())
                .replaceAll("\\(white\\)", Formatting.WHITE.toString())
                .replaceAll("\\(reset\\)", defaultColor.toString());

        return msg;
    }
}
