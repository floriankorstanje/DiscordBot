package com.florn;

import com.florn.ScoreSystem.ScoreSystem;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

public class GuildSystem extends ListenerAdapter {
    public static void announceUserNewRole(Member m, Role r, Guild g) {
        //Announce user new role
        g.getTextChannelById(Vars.systemMessagesChannel).sendMessage("**" + m.getAsMention() + " has just earned the " + r.getName() + " role!**").queue();
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        try {
            //Make sure the entire score file is right
            ScoreSystem.fixScoreList(event.getGuild());

            //Give the joined user a role if they have accepted the rules previously
            List<User> reactions = event.getGuild().getTextChannelById(Vars.rulesChannel).retrieveMessageById(Vars.ruleAcceptMessage).complete().getReactions().get(0).retrieveUsers().complete();
            for (User user : reactions) {
                if (user.getId().equals(event.getMember().getId())) {
                    Role role = event.getGuild().getRoleById(Vars.normalPeopleRole);
                    event.getGuild().addRoleToMember(event.getMember().getId(), role).complete();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Announce user join
        event.getGuild().getTextChannelById(Vars.systemMessagesChannel).sendMessage("**Welcome " + event.getUser().getAsMention() + " to " + event.getGuild().getName() + "! To see all the channels, go to " + Vars.guild.getTextChannelById(Vars.rulesChannel).getAsMention() + " and accept the rules.**").queue();
    }

    @Override
    public void onGuildMemberLeave(@Nonnull GuildMemberLeaveEvent event) {
        try {
            //Reset users score when they leave
            ScoreSystem.resetUserScore(event.getMember().getId());
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
        event.getGuild().getTextChannelById(Vars.systemMessagesChannel).sendMessage("__***THE SERVER HAS BEEN BOOSTED BY " + event.getUser().getAsMention() + "! THANKS!***__").queue();
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        //Add role to user
        if (event.getMessageId().equals(Vars.ruleAcceptMessage)) {
            Role role = event.getGuild().getRoleById(Vars.normalPeopleRole);
            event.getGuild().addRoleToMember(event.getMember().getId(), role).complete();
        }
    }

    @Override
    public void onGuildMessageReactionRemove(@Nonnull GuildMessageReactionRemoveEvent event) {
        //Remove role from user
        if (event.getMessageId().equals(Vars.ruleAcceptMessage)) {
            Role role = event.getGuild().getRoleById(Vars.normalPeopleRole);
            event.getGuild().removeRoleFromMember(event.getMember().getId(), role).complete();
        }
    }
}
