package com.florn;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public class Output {
    public static void Log(String command, String fullMessage, Member executor, TextChannel executionChannel, boolean recognized, boolean success) {
        //Get the bot log text channel
        TextChannel channel = Vars.guild.getTextChannelById(Vars.botLogChannel);

        //Make an embed to make it look nicer
        EmbedBuilder b = new EmbedBuilder();

        //Set embed information
        b.setTitle(Vars.guild.getJDA().getSelfUser().getName() + " log message");
        b.setColor(Util.random(0x0, 0xFFFFFF));
        b.setFooter(Vars.botName + " made by " + Vars.botOwner);

        //Add actual message to the embed
        b.addField("Command", Vars.botPrefix + command, true);
        b.addField("Full Message", fullMessage, true);
        b.addField("Executor", executor.getAsMention(), true);
        b.addField("Recognized", recognized ? "Yes" : "No", true);
        b.addField("Succeeded", success ? "Yes" : "No", true);
        b.addField("Channel", "#" + executionChannel.getAsMention(), true);

        //Send the embed to the text channel
        channel.sendMessage(b.build()).queue();
    }

    public static void unknownArguments(TextChannel channel, String command, String usage) {
        channel.sendMessage("Unknown argument(s) for command \"" + Vars.botPrefix + command + "\".\nUsage: " + Vars.botPrefix + usage).queue();
    }

    public static void noPermission(TextChannel channel, String command) {
        channel.sendMessage("You do not have the permission to execute command \"" + Vars.botPrefix + command + "\"").queue();
    }

    public static void unknownUid(TextChannel channel, String uid) {
        channel.sendMessage("\"" + uid + "\" is not a valid uid.").queue();
    }

    public static void ConsoleLog(String operation, Member m, String info) {
        System.out.println(operation + ":" + m.getId() + "(" + m.getEffectiveName() + ")->" + info);
    }
}
