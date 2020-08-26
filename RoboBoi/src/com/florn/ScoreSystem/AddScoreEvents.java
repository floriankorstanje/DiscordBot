package com.florn.ScoreSystem;

import com.florn.Util;
import com.florn.Vars;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.io.IOException;

public class AddScoreEvents extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        //If the user is a bot, don't give them points
        if (event.getMember().getUser().isBot())
            return;

        //Add the user to the hasSent array to give them some points
        if (!ScoreSystem.hasSent.contains(event.getMember()))
            ScoreSystem.hasSent.add(event.getMember());
    }

    @Override
    public void onGuildVoiceJoin(@Nonnull GuildVoiceJoinEvent event) {
        //Start a loop that will add a point every 5 minutes the user is in a call
        ScoreSystem.VoiceChannelScoreThread(event);
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        //If the user is a bot, don't give them points
        if (event.getMember().getUser().isBot())
            return;

        try {
            //Add 26 points to the user because they accepted the server rules
            if (event.getMessageId().equals(Vars.ruleAcceptMessage))
                ScoreSystem.addScore(event.getMember(), 26);

            //This is a bad way of preventing point farming, but this will only give the user a 20% chance (default, can be modified) of getting a point from a reaction
            double reactionDoesntGivePointsChance = Util.getScoreSettings().getReactionDoesntGivePointsChance();
            ScoreSystem.addScore(event.getMember(), Vars.random.nextDouble() > reactionDoesntGivePointsChance ? 1 : 0);

            //Update the role
            ScoreSystem.updateUserRole(event.getMember(), event.getGuild());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGuildMessageReactionRemove(@Nonnull GuildMessageReactionRemoveEvent event) {
        //If the user is a bot, don't give them points
        if (event.getMember().getUser().isBot())
            return;

        try {
            //Remove 25 points from the user because they didn't accept the rules
            if (event.getMessageId().equals(Vars.ruleAcceptMessage))
                ScoreSystem.addScore(event.getMember(), -26);

            //Update the role
            ScoreSystem.updateUserRole(event.getMember(), event.getGuild());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGuildMemberUpdateBoostTime(@Nonnull GuildMemberUpdateBoostTimeEvent event) {
        try {
            //No need to check if it is a bot this time, since bots can't boost a server

            //Add 100 points because they boosted the server
            ScoreSystem.addScore(event.getMember(), 100);

            //Update the role
            ScoreSystem.updateUserRole(event.getMember(), event.getGuild());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
