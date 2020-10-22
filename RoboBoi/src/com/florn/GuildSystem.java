package com.florn;

import com.florn.Config.BotSettings;
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
        g.getTextChannelById(Vars.systemMessagesChannel).sendMessage(getMessage(Vars.roleGetMessage,
                m.getEffectiveName(),
                m.getAsMention(),
                g.getName(),
                "",
                r.getName()))
                .queue();
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
        event.getGuild().getTextChannelById(Vars.systemMessagesChannel).sendMessage(getMessage(Vars.joinMessage,
                event.getMember().getEffectiveName(),
                event.getMember().getAsMention(),
                event.getGuild().getName(),
                "#" + event.getGuild().getGuildChannelById(Vars.rulesChannel).getName(),
                ""))
                .queue();
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
        event.getGuild().getTextChannelById(Vars.systemMessagesChannel).sendMessage(getMessage(Vars.leaveMessage,
                event.getMember().getEffectiveName(),
                event.getMember().getAsMention(),
                event.getGuild().getName(),
                "#" + event.getGuild().getGuildChannelById(Vars.rulesChannel).getName(),
                ""))
                .queue();
    }

    @Override
    public void onGuildBan(@Nonnull GuildBanEvent event) {
        //Announce user ban
        event.getGuild().getTextChannelById(Vars.systemMessagesChannel).sendMessage(getMessage(Vars.banMessage,
                event.getUser().getName(),
                event.getUser().getAsMention(),
                event.getGuild().getName(),
                "#" + event.getGuild().getGuildChannelById(Vars.rulesChannel).getName(),
                ""))
                .queue();
    }

    @Override
    public void onGuildUnban(@Nonnull GuildUnbanEvent event) {
        //Announce user unban
        event.getGuild().getTextChannelById(Vars.systemMessagesChannel).sendMessage(getMessage(Vars.unbanMessage,
                event.getUser().getName(),
                event.getUser().getAsMention(),
                event.getGuild().getName(),
                "#" + event.getGuild().getGuildChannelById(Vars.rulesChannel).getName(),
                ""))
                .queue();
    }

    @Override
    public void onGuildMemberUpdateBoostTime(@Nonnull GuildMemberUpdateBoostTimeEvent event) {
        //Announce user boost
        event.getGuild().getTextChannelById(Vars.systemMessagesChannel).sendMessage(getMessage(Vars.boostMessage,
                event.getMember().getEffectiveName(),
                event.getMember().getAsMention(),
                event.getGuild().getName(),
                "#" + event.getGuild().getGuildChannelById(Vars.rulesChannel).getName(),
                ""))
                .queue();
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

    private static String getMessage(String settingValue, String userName, String userMention, String serverName, String rulesChannel, String roleName) {
        return settingValue
                .replace("{SERVER_NAME}", serverName)
                .replace("{USER_MENTION}", userMention)
                .replace("{RULES_CHANNEL}", rulesChannel)
                .replace("{ROLE_NAME}", roleName)
                .replace("{USER_NAME}", userName);
    }
}
