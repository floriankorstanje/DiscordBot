package com.florn.Commands;

import com.florn.*;
import com.florn.Config.BotSettings;
import com.florn.ScoreSystem.Rank;
import com.florn.ScoreSystem.ScoreSystem;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Commands {
    public static boolean help(GuildMessageReceivedEvent e) {
        //ArrayList to easily add new lines in the help message
        ArrayList<String> help = new ArrayList<>();
        help.add("**Help for " + e.getJDA().getSelfUser().getName() + " version " + Vars.version + "**");
        help.add("modscore <uid> <add|remove|set> <score> - ADMIN ONLY - Modifies a user's score.");
        help.add("say <message> - ADMIN ONLY - Sends a message as the bot");
        help.add("help - Shows this.");
        help.add("version - Shows the bot version.");
        help.add("score [uid|*] - Without arguments, this will show your score. Argument UID will show someone else's score. * will send you the score file.");
        help.add("config <set|get|help> [variable_name|*] [value_to_set] - ADMIN ONLY - Modifies the config for this bot.");

        //Add all the item in the ArrayList to one string to send it to the user
        StringBuilder b = new StringBuilder();

        for (String helpString : help) {
            if (help.indexOf(helpString) == 0) {
                b.append(helpString + "\n");
            } else {
                b.append(Vars.botPrefix + helpString + "\n");
            }
        }

        //PM the help message to the user
        Util.sendPrivateMessage(e.getMember().getUser(), b.toString(), e.getChannel());

        return true;
    }

    public static boolean Score(GuildMessageReceivedEvent e, String[] args) throws IOException {
        //Get some basic variables needed for the command
        Member m = null;
        EmbedBuilder b = new EmbedBuilder();

        //Check if the command got arguments
        if (args.length == 1) {
            if (args[0].equals("*")) {
                e.getChannel().sendFile(new File(Vars.scoreFile)).queue();
                return true;
            }

            //Check if args[0] contains uid
            try {
                m = e.getGuild().getMemberById(args[0]);
            } catch (Exception ex) {
                //Tell user that the uid is unknown
                Output.unknownUid(e.getChannel(), args[0]);
                return false;
            }
        } else if (args.length == 0) {
            m = e.getMember();
        } else {
            //Tell user command is wrong
            Output.unknownArguments(e.getChannel(), "score", "score [uid]");
            return false;
        }

        //Get the user's name and figure out if it has to end in s or 's
        String userName = m.getEffectiveName() + (m.getEffectiveName().toCharArray()[m.getEffectiveName().length() - 1] == 's' ? "'" : "'s");

        //Set some things in the embed
        b.setTitle(userName + " Score");
        b.setFooter(Vars.guild.getJDA().getSelfUser().getName() + " made by florn");

        //Set a random color for the embed
        b.setColor(Util.random(0x0, 0xFFFFFF));

        //Get the selected user's rank
        Rank rank = ScoreSystem.getRank(m, e.getGuild());

        //Add all the fields of the embed builder
        b.addField("User", m.getAsMention(), false);

        b.addField("Higher Peeps Progress", rank.getScore() + "/500\n" + rank.getHigherPeepsProgress() + "% progress", false);
        b.addField("Super Peeps Progress", rank.getScore() + "/2000\n" + rank.getSuperPeepsProgress() + "% progress", false);

        b.addField("Overall Progress",
                m.getEffectiveName() + " " + (rank.getUserAchievedHigherPeeps() ? "achieved Higher Peeps!" : "hasn't achieved Higher Peeps yet.") +
                        "\n" + m.getEffectiveName() + " " + (rank.getUserAchievedSuperPeeps() ? "achieved Super Peeps!" : "hasn't achieved Super Peeps yet."), false);

        b.addField("Rank", rank.getPosition() + " out of " + rank.getTotalMembers() + "\nTop " + rank.getTopPercentage() + "%", false);

        b.addField("Below you",
                rank.getBelow().getScore() != -1 ? e.getGuild().getMemberById(rank.getBelow().getId()).getAsMention() + " is " + (rank.getScore() - rank.getBelow().getScore()) + " below you." :
                        "You are on the bottom of the leaderboard. There is no-one below you :(", false);

        b.addField("Above you",
                rank.getAbove().getScore() != -1 ? e.getGuild().getMemberById(rank.getAbove().getId()).getAsMention() + " is " + (rank.getAbove().getScore() - rank.getScore()) + " above you." :
                        "You are on the top of the leaderboard. There is no-one above you :)", false);

        e.getChannel().sendMessage(b.build()).queue();

        return true;
    }

    public static boolean modScore(GuildMessageReceivedEvent e, String[] args) {
        //Check if the user has permissions to execute this command
        if (!e.getMember().hasPermission(Vars.adminCommandPermission)) {
            Output.noPermission(e.getChannel(), "modscore");
            return false;
        }

        //Check if the arguments are correct
        if (args.length != 3) {
            Output.unknownArguments(e.getChannel(), "modscore", "modscore <uid> <add|remove|set> <score>");
            return false;
        }

        //Make a Member object
        Member toModify = null;

        //Try to give toModify a value, if this fails, the uid given is wrong
        try {
            toModify = e.getGuild().getMemberById(args[0]);
        } catch (Exception _ignored) {
            //Tell the user the given uid is unknown
            Output.unknownUid(e.getChannel(), args[0]);
            return false;
        }

        //Check if the 2nd arguments is a valid operator
        if (!args[1].equals("add") && !args[1].equals("remove") && !args[1].equals("set")) {
            Output.unknownArguments(e.getChannel(), "modscore", "modscore <uid> <add|remove|set> <score>");
            return false;
        }

        //Check if the 3rd argument is an integer
        try {
            Integer.parseInt(args[2]);
        } catch (Exception _ignored) {
            Output.unknownArguments(e.getChannel(), "modscore", "modscore <uid> <add|remove|set> <score>");
            return false;
        }

        //Execute the actual command requested
        if (args[1].equals("add")) {
            //Add score to the user
            try {
                ScoreSystem.addScore(toModify, Integer.parseInt(args[2]));
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            //Send a message that the user got points
            try {
                e.getChannel().sendMessage(e.getMember().getAsMention() + " added " + args[2] + " to " + toModify.getAsMention() + "'s score (Now " + ScoreSystem.getPoints(toModify) + ").").queue();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (args[1].equals("remove")) {
            //Remove the points
            try {
                //If the user's points will go below 26, cancel
                if (ScoreSystem.getPoints(toModify) - Integer.parseInt(args[2]) < 26) {
                    e.getChannel().sendMessage("Unable to remove " + args[2] + " points from " + toModify.getEffectiveName() + ". Lowest score value is " + (Integer.parseInt(args[2]) - (26 - (ScoreSystem.getPoints(toModify) - Integer.parseInt(args[2]))))).queue();
                    return false;
                }

                //Add negative points
                ScoreSystem.addScore(toModify, -Integer.parseInt(args[2]));
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            }

            //Send a message that points have been removed from the user
            try {
                e.getChannel().sendMessage(e.getMember().getAsMention() + " removed " + args[2] + " from " + toModify.getAsMention() + "'s score (Now " + ScoreSystem.getPoints(toModify) + ").").queue();
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            }
        } else if (args[1].equals("set")) {
            //If the user's points will go below 26, cancel
            if (Integer.parseInt(args[2]) < 26) {
                e.getChannel().sendMessage("Can't set users score lower than 26.").queue();
                return false;
            }

            //Calculate the amount of points to add/remove from the user to set the right score
            try {
                ScoreSystem.addScore(toModify, Integer.parseInt(args[2]) - ScoreSystem.getPoints(toModify));
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            }

            //Send a message that the points of the user have been modified
            try {
                e.getChannel().sendMessage(e.getMember().getAsMention() + " set " + toModify.getAsMention() + "'s score to " + ScoreSystem.getPoints(toModify) + ".").queue();
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            }
        }

        return true;
    }

    public static boolean say(GuildMessageReceivedEvent e, String[] args) {
        //Check if there are arguments
        if (args.length == 0) {
            Output.unknownArguments(e.getChannel(), "say", "say <message>");
            return false;
        }

        //Check if the user has permissions to do this
        if (!e.getMember().hasPermission(Vars.adminCommandPermission)) {
            Output.noPermission(e.getChannel(), "say");
            return false;
        }

        //Add all the arguments together to make one string
        StringBuilder b = new StringBuilder();

        for (String arg : args) {
            b.append(arg + " ");
        }

        //Send the message
        String message = b.toString().trim();
        e.getChannel().sendMessage(message).queue();

        return true;
    }

    public static boolean version(GuildMessageReceivedEvent e) {
        //Get versions
        String botVersion = Vars.version;
        String javaVersion = System.getProperty("java.version");

        //Make an embed and make it look nice
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(Util.random(0x0, 0xFFFFFF));
        b.setTitle(e.getJDA().getSelfUser().getName() + " Version");
        b.setFooter(Vars.botName + " made by " + Vars.botOwner);

        //Add the versions to the embed
        b.addField("Bot Version", "v" + botVersion, false);
        b.addField("Java Version", javaVersion, false);

        //Send the embed
        e.getChannel().sendMessage(b.build()).queue();

        return true;
    }

    public static boolean test(GuildMessageReceivedEvent e, String[] args) {
        if (!e.getJDA().retrieveApplicationInfo().complete().getOwner().getId().equals(e.getMember().getId())) {
            Output.noPermission(e.getChannel(), "test");
            return false;
        }

        List<User> reaction = e.getGuild().getTextChannelById(Vars.rulesChannel).retrieveMessageById(Vars.ruleAcceptMessage).complete().getReactions().get(0).retrieveUsers().complete();
        e.getChannel().sendMessage(reaction.toString()).queue();


        return true;
    }

    public static boolean config(GuildMessageReceivedEvent e, String[] args) {
        //Check if the user has permissions to do this
        if (!e.getMember().hasPermission(Vars.adminCommandPermission)) {
            Output.noPermission(e.getChannel(), "config");
            return false;
        }

        //Check if there are enough arguments
        if(args.length < 1) {
            Output.unknownArguments(e.getChannel(), "config", "config <set|get|help> [key_name|*] [value_to_set]");
            return false;
        }

        //Check the 1st argument and run set, get or help
        if(args[0].equalsIgnoreCase("help")) {
            e.getChannel().sendMessage("For config help, please visit: http://fkorstanje.nl/aa/RoboBoi-Help-ConfigCommand.txt").queue();

        } else if(args[0].equalsIgnoreCase("set")) {
            //Check if there is enough arguments to run "$config set"
            if(args.length != 3) {
                Output.unknownArguments(e.getChannel(), "config", "config <set|get|help> [variable_name|*] [value_to_set]");
                return false;
            }

            BotSettings.Result result;

            //Try to set the value and save the result in a variable
            try {
                 result = BotSettings.setValue(args[1], args[2]);
            } catch (IOException ioException) {
                ioException.printStackTrace();
                return  false;
            }

            //Tell the user if the command succeeded or not
            if(result == BotSettings.Result.SUCCESS) {
                //Tell the user the command succeeded
                e.getChannel().sendMessage("Successfully set \"" + args[1] + "\" to \"" + args[2] + "\".").queue();
            } else if(result == BotSettings.Result.INVALID_VALUE) {
                //Tell the user the value isn't the correct datatype for that key
                e.getChannel().sendMessage("\"" + args[2] + "\" is not the correct datatype for \"" + args[1] + "\". Type \"" + Vars.botPrefix + "config help\" for more explanation.").queue();
                return false;
            } else if(result == BotSettings.Result.KEY_DOES_NOT_EXIST) {
                //Tell the user that key doesn't exist
                e.getChannel().sendMessage("\"" + args[1] + "\" is an invalid key name. Type \"" + Vars.botPrefix + "config help\" for more explanation.").queue();
                return false;
            } else if(result == BotSettings.Result.NO_NEWLINE_ALLOWED) {
                //Tell the user that they can't have newlines in the value
                e.getChannel().sendMessage("You can't put newlines in a value.").queue();
                return false;
            }
        } else if(args[0].equalsIgnoreCase("get")) {
            //Check if there is enough arguments to run "$config get"
            if(args.length != 2) {
                Output.unknownArguments(e.getChannel(), "config", "config <set|get|help> [variable_name|*] [value_to_set]");
                return false;
            }

            //Check if the user wants all the values or just one
            if(args[1].equalsIgnoreCase("*")) {
                //Get all the lines in the settings file
                List<String> lines;

                try {
                    lines = IO.readSmallTextFile(Vars.settingsFile);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    return false;
                }

                //Create an embedbuilder so we can make the message look nice
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("Config for " + Vars.botName);
                builder.setColor(Vars.random.nextInt(0xFFFFFF));

                //Fill in the embed builder
                for(String line : lines) {
                    if(line.contains("=")) {
                        String[] setting = line.split("=");
                        builder.addField("\"" + setting[0] + "\":", "\"" + setting[1] + "\"", false);
                    }
                }

                //Send the embed message to the channel
                e.getChannel().sendMessage(builder.build()).queue();
            }
        }

        return true;
    }
}
