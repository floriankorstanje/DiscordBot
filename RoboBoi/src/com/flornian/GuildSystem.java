package com.flornian;

import com.flornian.ScoreSystem.ScoreSystem;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;

public class GuildSystem extends ListenerAdapter {
    public static void announceUserNewRole(Member m, Role r, Guild g) {
        if (!Vars.enableSystemMessages)
            return;

        //Announce user new role
        g.getSystemChannel().sendMessage(getMessage(Vars.roleGetMessage,
                m.getEffectiveName(),
                m.getAsMention(),
                g.getName(),
                r.getName()))
                .queue();
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        if (!Vars.enableSystemMessages)
            return;

        //Announce user join
        event.getGuild().getSystemChannel().sendMessage(getMessage(Vars.joinMessage,
                event.getMember().getEffectiveName(),
                event.getMember().getAsMention(),
                event.getGuild().getName(),
                ""))
                .queue();
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        if (!Vars.enableSystemMessages)
            return;

        //Announce user leave
        event.getGuild().getSystemChannel().sendMessage(getMessage(Vars.leaveMessage,
                event.getMember().getEffectiveName(),
                event.getMember().getAsMention(),
                event.getGuild().getName(),
                ""))
                .queue();
    }

    @Override
    public void onGuildBan(@Nonnull GuildBanEvent event) {
        if (!Vars.enableSystemMessages)
            return;

        //Announce user ban
        event.getGuild().getSystemChannel().sendMessage(getMessage(Vars.banMessage,
                event.getUser().getName(),
                event.getUser().getAsMention(),
                event.getGuild().getName(),
                ""))
                .queue();
    }

    @Override
    public void onGuildUnban(@Nonnull GuildUnbanEvent event) {
        if (!Vars.enableSystemMessages)
            return;

        //Announce user unban
        event.getGuild().getSystemChannel().sendMessage(getMessage(Vars.unbanMessage,
                event.getUser().getName(),
                event.getUser().getAsMention(),
                event.getGuild().getName(),
                ""))
                .queue();
    }

    @Override
    public void onGuildMemberUpdateBoostTime(@Nonnull GuildMemberUpdateBoostTimeEvent event) {
        if (!Vars.enableSystemMessages)
            return;

        //Announce user boost
        event.getGuild().getSystemChannel().sendMessage(getMessage(Vars.boostMessage,
                event.getMember().getEffectiveName(),
                event.getMember().getAsMention(),
                event.getGuild().getName(),
                ""))
                .queue();
    }

    private static String getMessage(String settingValue, String userName, String userMention, String serverName, String roleName) {
        return settingValue
                .replace("{SERVER_NAME}", serverName)
                .replace("{USER_MENTION}", userMention)
                .replace("{ROLE_NAME}", roleName)
                .replace("{USER_NAME}", userName);
    }
}
