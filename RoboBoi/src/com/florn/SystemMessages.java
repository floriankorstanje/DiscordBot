package com.florn;

import com.florn.ScoreSystem.ScoreSystem;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
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
    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        Role role = event.getGuild().getRoleById(Vars.newPeepsRole);

        event.getGuild().addRoleToMember(event.getMember(), role).complete();

        try {
            //Make sure the entire score file is right
            ScoreSystem.fixScoreList(event.getGuild());
            throw new IOException();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Announce user join
        event.getGuild().getTextChannelById(Vars.systemMessagesChannel).sendMessage("**Welcome " + event.getUser().getName() + " to " + event.getGuild().getName() + "!**").queue();
    }

    @Override
    public void onGuildMemberLeave(@Nonnull GuildMemberLeaveEvent event) {
        try {
            //Remove the left user's ID from the score file
            ScoreSystem.removeUserId(event.getMember().getId());
            throw new IOException();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Announce user leave
        event.getGuild().getTextChannelById(Vars.systemMessagesChannel).sendMessage("**" + event.getUser().getName() + " has left " + event.getGuild().getName() + ".**").queue();
    }

    @Override
    public void onGuildBan(@Nonnull GuildBanEvent event) {
        //Announce user ban
        event.getGuild().getTextChannelById(Vars.systemMessagesChannel).sendMessage("**" + event.getUser().getName() + " has been banned.**").queue();
    }

    @Override
    public void onGuildUnban(@Nonnull GuildUnbanEvent event) {
        //Announce user unban
        event.getGuild().getTextChannelById(Vars.systemMessagesChannel).sendMessage("**" + event.getUser().getName() + " has been unbanned.**").queue();
    }

    @Override
    public void onGuildMemberUpdateBoostTime(@Nonnull GuildMemberUpdateBoostTimeEvent event) {
        //Announce user boost
        event.getGuild().getTextChannelById(Vars.systemMessagesChannel).sendMessage("__***THE SERVER HAS BEEN BOOSTED BY " + event.getUser().getAsMention() + ". THANKS!***__").queue();
    }

    public static void announceUserNewRole(Member m, Role r, Guild g) {
        //Announce user new role
        g.getTextChannelById(Vars.systemMessagesChannel).sendMessage("**" + m.getAsMention() + " has just earned the " + r.getName() + " role!**").queue();
    }
}
