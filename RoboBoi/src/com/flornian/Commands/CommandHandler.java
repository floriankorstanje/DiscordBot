package com.flornian.Commands;

import com.flornian.Output;
import com.flornian.Util;
import com.flornian.Vars;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;

public class CommandHandler extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        CommandEvent commandEvent = new CommandEvent(event);
        HandleCommand(commandEvent);
    }

    @Override
    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {
        if(!Vars.executeCommandOnMessageEdit)
            return;

        CommandEvent commandEvent = new CommandEvent(event);

        HandleCommand(commandEvent);
    }

    private void HandleCommand(CommandEvent event) {
        //Make sure Vars.guild is set to the right one
        Vars.guild = event.getGuild();

        //Get the message received
        String msg = event.getMessage().getContentRaw();

        //Set the prefix to and easier accessible variable
        String p = Vars.botPrefix;

        if (msg.startsWith(p)) {
            //Boolean to indicate if the command is recognized and if the command executed successfully
            boolean recognized = true;
            int errcode;

            //Remove the bot prefix from the message and save the original message for logging
            String originalMessage = msg;
            msg = msg.replace(p, "");

            //Split the message up in a command and its arguments.
            String cmd = msg.split("\\s+")[0];
            String[] args = Util.removeTheElement(msg.replace(cmd, "").split(" "), 0);

            //Check if the command is known and then execute it.
            //Commands return a boolean if they succeeded or not
            if (cmd.equalsIgnoreCase("help")) {
                //Command $help
                errcode = Commands.help(event);
            } else if (cmd.equalsIgnoreCase("score")) {
                //Command $score
                try {
                    errcode = Commands.Score(event, args);
                } catch (IOException e) {
                    errcode = -1;
                }
            } else if (cmd.equalsIgnoreCase("modscore")) {
                //Command $modscore
                errcode = Commands.modScore(event, args);
            } else if (cmd.equalsIgnoreCase("say")) {
                //Command $say
                errcode = Commands.say(event, args);
            } else if (cmd.equalsIgnoreCase("info")) {
                //Command $info
                errcode = Commands.info(event);
            } else if (cmd.equalsIgnoreCase(("config"))) {
                //Command $config
                errcode = Commands.config(event, args);
            } else if (cmd.equalsIgnoreCase("leaderboard")) {
                //Command $leaderboard
                errcode = Commands.leaderboard(event);
            } else if (cmd.equalsIgnoreCase("userinfo")) {
                //Command $userinfo
                errcode = Commands.userinfo(event, args);
            } else if(cmd.equalsIgnoreCase("ping")) {
                //Command $ping
                errcode = Commands.ping(event);
            } else {
                //If the command is unrecognized, set recognized and success to false
                recognized = false;
                errcode = 1;
            }

            //Log the command, tell the user if the command succeeded or not and output to console
            Output.Log(cmd, originalMessage, event.getMember(), event.getChannel(), recognized, errcode);
            if (errcode != 0)
                event.getChannel().sendMessage("An error occurred while processing your command: " + Output.getError(errcode)).queue();
            Output.ConsoleLog("Command", event.getMember(), "\"$" + cmd + " [" + args.length + "]\"");
        }
    }
}
