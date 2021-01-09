package com.flornian.ScoreSystem;

import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.io.IOException;

public class AddScoreEvents extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
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
