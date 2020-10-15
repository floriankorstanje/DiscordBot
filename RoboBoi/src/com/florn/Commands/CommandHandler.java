package com.florn.Commands;

import com.florn.Output;
import com.florn.Util;
import com.florn.Vars;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.io.IOException;

public class CommandHandler extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        //Make sure Vars.guild is set to snaekyy and not to null
        Vars.guild = event.getGuild();

        //Get the message received
        String msg = event.getMessage().getContentRaw();

        //Set the prefix to and easier accessible variable
        String p = Vars.botPrefix;

        if (msg.startsWith(p)) {
            //Boolean to indicate if the command is recognized and if the command executed successfully
            boolean recognized = true;
            boolean success;

            //Remove the bot prefix from the message and save the original message for logging
            String originalMessage = msg;
            msg = msg.replace(p, "");

            //Split the message up in a command and its arguments.
            String cmd = msg.split("\\s+")[0];
            String[] args = Util.removeTheElement(msg.replace(cmd, "").split(" "), 0);

            //Check if the command is known and then execute it.
            //Commands return boolean if they succeeded or not
            if (cmd.equalsIgnoreCase("help")) {
                //Command $help
                success = Commands.help(event);
            } else if (cmd.equalsIgnoreCase("score")) {
                //Command $score
                try {
                    success = Commands.Score(event, args);
                } catch (IOException e) {
                    e.printStackTrace();
                    success = false;
                }
            } else if (cmd.equalsIgnoreCase("modscore")) {
                //Command $modscore
                success = Commands.modScore(event, args);
            } else if (cmd.equalsIgnoreCase("say")) {
                //Command $say
                success = Commands.say(event, args);
            } else if (cmd.equalsIgnoreCase("version")) {
                //Command $version
                success = Commands.version(event);
            } else if (cmd.equalsIgnoreCase("test")) {
                //Command $test
                success = Commands.test(event, args);
            } else {
                //If the command is unrecognized, set recognized and success to false
                recognized = false;
                success = false;
            }

            //Log the command
            Output.Log(cmd, originalMessage, event.getMember(), event.getChannel(), recognized, success, event.getJDA().retrieveApplicationInfo().complete().getOwner().getName());
        }
    }
}
