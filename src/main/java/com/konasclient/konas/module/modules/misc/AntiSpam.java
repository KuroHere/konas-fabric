package com.konasclient.konas.module.modules.misc;

import com.konasclient.konas.event.events.network.PacketEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.Setting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

public class AntiSpam extends Module {
    private static Setting<Boolean> discordLinks = new Setting<>("Discord Invites", true);
    private static Setting<Boolean> domains = new Setting<>("Domains", false);
    private static Setting<Boolean> announcer = new Setting<>("Announcer", true);

    private static String[] discordStringArray =
            {
                    "discord.gg",
            };

    private static String[] domainStringArray =
            {
                    ".com",
                    ".ru",
                    ".net",
                    ".in",
                    ".ir",
                    ".au",
                    ".uk",
                    ".de",
                    ".br",
                    ".xyz",
                    ".org",
                    ".co",
                    ".cc",
                    ".me",
                    ".tk",
                    ".us",
                    ".bar",
                    ".gq",
                    ".nl",
                    ".space"
            };

    private static String[] announcerStringArray =
            {
                    "Looking for new anarchy servers?",
                    "I just walked",
                    "I just flew",
                    "I just placed",
                    "I just ate",
                    "I just healed",
                    "I just took",
                    "I just spotted",
                    "I walked",
                    "I flew",
                    "I walked",
                    "I flew",
                    "I placed",
                    "I ate",
                    "I healed",
                    "I took",
                    "I gained",
                    "I mined",
                    "I lost",
                    "I moved"
            };

    public AntiSpam() {
        super("AntiSpam", "Hides spam in chat", 0xFFAD24DF, Category.Misc);
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {

        if (mc.world == null || mc.player == null) return;

        if (!(event.packet instanceof GameMessageS2CPacket)) {
            return;
        }

        GameMessageS2CPacket chatMessage = (GameMessageS2CPacket) event.packet;


        if (detectSpam(chatMessage.getMessage().getString())) {
            event.setCancelled(true);
        }
    }


    private boolean detectSpam(String message) {
        if (discordLinks.getValue()) {
            for (String discordSpam : discordStringArray) {
                if (message.contains(discordSpam)) {
                    return true;
                }
            }
        }

        if (announcer.getValue()) {
            for (String announcerSpam : announcerStringArray) {
                if (message.contains(announcerSpam)) {
                    return true;
                }
            }
        }

        if(domains.getValue()) {
            for (String domainSpam : domainStringArray) {
                if(message.contains(domainSpam)) {
                    return true;
                }
            }
        }

        return false;
    }
}
