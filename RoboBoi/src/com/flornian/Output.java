package com.flornian;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public class Output {
    public static String getError(int code) {
        final String[] codes = {"-1 [Internal/Other Error]", "0 [Success]", "1 [Unknown Command]", "2 [Unknown Arguments]", "3 [No Permission]", "4 [Unknown UID]", "5 [Function Disabled]"};
        return codes[code + 1];
    }

    public static void Log(String command, String fullMessage, Member executor, TextChannel executionChannel, boolean recognized, int success) {
        //Get the bot log text channel
        TextChannel channel = Vars.guild.getTextChannelById(Vars.botLogChannel);

        //Make an embed to make it look nicer
        EmbedBuilder b = new EmbedBuilder();

        //Set embed information
        b.setTitle(Vars.guild.getJDA().getSelfUser().getName() + " log message");
        b.setColor(executor.getColor());
        b.setFooter(Vars.botName + " made by " + Vars.botOwner);

        //Add actual message to the embed
        b.addField("Command", Vars.botPrefix + command, true);
        b.addField("Full Message", fullMessage, true);
        b.addField("Executor", executor.getAsMention(), true);
        b.addField("Recognized", recognized ? "Yes" : "No", true);
        b.addField("Error Code", getError(success), true);
        b.addField("Channel", executionChannel.getAsMention(), true);

        //Send the embed to the text channel
        channel.sendMessage(b.build()).queue();
    }

    public static int unknownArguments() {
        return 2;
    }

    public static int noPermission() {
        return 3;
    }

    public static int unknownUid() {
        return 4;
    }

    public static int functionDisabled() {
        return 5;
    }

    public static void ConsoleLog(String operation, Member m, String info) {
        System.out.println(operation + ":" + m.getId() + "(" + m.getEffectiveName() + ")->" + info);
    }
}
