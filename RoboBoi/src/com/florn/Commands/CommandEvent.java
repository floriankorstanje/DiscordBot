package com.florn.Commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;

public class CommandEvent {
    private Guild guild;
    private TextChannel channel;
    private Member member;
    private Message message;
    private User author;
    private JDA jda;

    public CommandEvent(GuildMessageReceivedEvent eventMessage) {
        guild = eventMessage.getGuild();
        channel = eventMessage.getChannel();
        member = eventMessage.getMember();
        message = eventMessage.getMessage();
        author = eventMessage.getAuthor();
        jda = eventMessage.getJDA();
    }

    public CommandEvent(GuildMessageUpdateEvent eventEdit) {
        guild = eventEdit.getGuild();
        channel = eventEdit.getChannel();
        member = eventEdit.getMember();
        message = eventEdit.getMessage();
        author = eventEdit.getAuthor();
        jda = eventEdit.getJDA();
    }

    public Guild getGuild() {
        return guild;
    }

    public TextChannel getChannel() {
        return channel;
    }

    public Member getMember() {
        return member;
    }

    public Message getMessage() {
        return message;
    }

    public User getAuthor() {
        return author;
    }

    public JDA getJDA() {
        return jda;
    }
}
