package com.florn;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.io.IOException;

public class SystemMessages extends ListenerAdapter {
    public static String systemMessagesChannelId = "630799468043829248";
    public static String newPeepsRole = "709023364525719554";
    public static String normalPeepsRole = "647149235568902144";
    public static String higherPeepsRole = "647147137548943422";
    public static String superPeepsRole = "697405963543642122";

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        Role role = event.getGuild().getRoleById(newPeepsRole);

        event.getGuild().addRoleToMember(event.getMember(), role).complete();

        try {
            RoleSystem.fixScoreList(event.getGuild());
        } catch (IOException e) {
            e.printStackTrace();
        }

        event.getGuild().getTextChannelById(systemMessagesChannelId).sendMessage("**Welcome " + event.getUser().getName() + " to " + event.getGuild().getName() + "!**").queue();
    }

    @Override
    public void onGuildMemberLeave(@Nonnull GuildMemberLeaveEvent event) {
        try {
            RoleSystem.removeId(event.getMember().getId());
        } catch (IOException e) {
            e.printStackTrace();
        }

        event.getGuild().getTextChannelById(systemMessagesChannelId).sendMessage("**" + event.getUser().getName() + " has left " + event.getGuild().getName() + ".**").queue();
    }

    @Override
    public void onGuildBan(@Nonnull GuildBanEvent event) {
        event.getGuild().getTextChannelById(systemMessagesChannelId).sendMessage("**" + event.getUser().getName() + " has been banned.**").queue();
    }

    @Override
    public void onGuildUnban(@Nonnull GuildUnbanEvent event) {
        event.getGuild().getTextChannelById(systemMessagesChannelId).sendMessage("**" + event.getUser().getName() + " has been unbanned.**").queue();
    }

    @Override
    public void onGuildMemberUpdateBoostTime(@Nonnull GuildMemberUpdateBoostTimeEvent event) {
        event.getGuild().getTextChannelById(systemMessagesChannelId).sendMessage("__***THE SERVER HAS BEEN BOOSTED BY " + event.getUser().getAsMention() + ". THANKS!***__").queue();
    }
}
