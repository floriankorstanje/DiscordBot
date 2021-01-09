package com.flornian.Commands;

import com.flornian.Config.BotSettings;
import com.flornian.IO;
import com.flornian.Output;
import com.flornian.Util;
import com.flornian.Vars;
import com.flornian.ScoreSystem.Rank;
import com.flornian.ScoreSystem.ScoreSystem;
import com.flornian.ScoreSystem.UserScore;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Commands {
    public static int help(CommandEvent e) {
        //Make an embed from the help list
        EmbedBuilder builder = new EmbedBuilder();

        //Add basic stuff to the embed
        builder.setTitle("Help for " + Vars.botName + " v" + Vars.version);
        builder.setFooter(Vars.botName + " made by " + Vars.botOwner);
        builder.setColor(Util.random(0x000000, 0xFFFFFF));
        builder.addField("[] - Optional argument; <> - Required argument", "", false);

        //Add all the commands
        builder.addField(Vars.botPrefix + "modscore <uid> <add|remove|set> <score>", "[*ADMIN ONLY*] Modifies a user's score.", false);
        builder.addField(Vars.botPrefix + "say <channel-id> <message>", "[*ADMIN ONLY*] Send a message as the bot in the specified channel.", false);
        builder.addField(Vars.botPrefix + "config <set|get|help> [variable-name|*] [value-to-set]", "[*ADMIN ONLY*] Modifies or returns a config variable for the bot.", false);
        builder.addField(Vars.botPrefix + "score [uid]", "Shows your score. Shows someone else's score when given an argument.", false);
        builder.addField(Vars.botPrefix + "leaderboard", "Shows the score leaderboard.", false);
        builder.addField(Vars.botPrefix + "info", "Shows some bot info.", false);
        builder.addField(Vars.botPrefix + "userinfo [uid]", "Shows info about the user.", false);
        builder.addField(Vars.botPrefix + "help", "Shows this.", false);

        //Send the help message
        e.getChannel().sendMessage(builder.build()).queue();

        return 0;
    }

    public static int Score(CommandEvent e, String[] args) throws IOException {
        if(!Vars.enableScoreSystem)
            return Output.functionDisabled();

        //Get some basic variables needed for the command
        Member m;
        EmbedBuilder b = new EmbedBuilder();

        //Check if the command got arguments
        if (args.length == 1) {
            //Check if args[0] contains uid
            try {
                m = e.getGuild().getMemberById(args[0]);
            } catch (Exception ex) {
                //Tell user that the uid is unknown
                return Output.unknownUid();
            }
        } else if (args.length == 0) {
            m = e.getMember();
        } else {
            //Tell user command is wrong
            return Output.unknownArguments();
        }

        //Get the user's name and figure out if it has to end in s or 's
        String userName = m.getEffectiveName() + (m.getEffectiveName().toCharArray()[m.getEffectiveName().length() - 1] == 's' ? "'" : "'s");

        //Get the role names
        String higherPeople = e.getGuild().getRoleById(Vars.higherPeopleRole).getName();
        String superPeople = e.getGuild().getRoleById(Vars.superPeopleRole).getName();

        //Set some things in the embed
        b.setTitle(userName + " Score");
        b.setFooter(Vars.botName + " made by " + Vars.botOwner);

        //Set a random color for the embed
        b.setColor(Util.random(0x0, 0xFFFFFF));

        //Get the selected user's rank
        Rank rank = com.flornian.ScoreSystem.ScoreSystem.getRank(m, e.getGuild());

        //Add all the fields of the embed builder
        b.addField("User", m.getAsMention(), false);

        b.addField(higherPeople + " Progress", rank.getScore() + "/" + BotSettings.getValueInt("higher_people_points") + "\n" + rank.getHigherPeepsProgress() + "% progress", false);
        b.addField(superPeople + " Progress", rank.getScore() + "/" + BotSettings.getValueInt("super_people_points") + "\n" + rank.getSuperPeepsProgress() + "% progress", false);

        b.addField("Overall Progress",
                m.getEffectiveName() + " " + (rank.getUserAchievedHigherPeeps() ? "achieved " + higherPeople + "!" : "hasn't achieved " + higherPeople + " yet.") +
                        "\n" + m.getEffectiveName() + " " + (rank.getUserAchievedSuperPeeps() ? "achieved " + superPeople + "!" : "hasn't achieved " + superPeople + " yet."), false);

        b.addField("Rank", rank.getPosition() + " out of " + rank.getTotalMembers() + "\nTop " + rank.getTopPercentage() + "%", false);

        b.addField("Below you",
                rank.getBelow().getScore() != -1 ? e.getGuild().getMemberById(rank.getBelow().getUID()).getAsMention() + " is " + (rank.getScore() - rank.getBelow().getScore()) + " points below you. (" + com.flornian.ScoreSystem.ScoreSystem.getPoints(e.getGuild().getMemberById(rank.getBelow().getUID())) + ")" :
                        "You are on the bottom of the leaderboard. There is no-one below you :(", false);

        b.addField("Above you",
                rank.getAbove().getScore() != -1 ? e.getGuild().getMemberById(rank.getAbove().getUID()).getAsMention() + " is " + (rank.getAbove().getScore() - rank.getScore()) + " points above you. (" + com.flornian.ScoreSystem.ScoreSystem.getPoints(e.getGuild().getMemberById(rank.getAbove().getUID())) + ")" :
                        "You are on the top of the leaderboard. There is no-one above you :)", false);

        //Send the message
        e.getChannel().sendMessage(b.build()).queue();

        return 0;
    }

    public static int modScore(CommandEvent e, String[] args) {
        if(!Vars.enableScoreSystem)
            return Output.functionDisabled();

        //Check if the user has permissions to execute this command
        if (!e.getMember().hasPermission(Vars.adminCommandPermission))
            return Output.noPermission();

        //Check if the arguments are correct
        if (args.length != 3)
            return Output.unknownArguments();

        //Make a Member object
        Member toModify;

        //Try to give toModify a value, if this fails, the uid given is wrong
        try {
            toModify = e.getGuild().getMemberById(args[0]);
        } catch (Exception _ignored) {
            //Tell the user the given uid is unknown
            return Output.unknownUid();
        }

        //Check if the 2nd arguments is a valid operator
        if (!args[1].equals("add") && !args[1].equals("remove") && !args[1].equals("set"))
            return Output.unknownArguments();

        //Check if the 3rd argument is an integer
        try {
            Integer.parseInt(args[2]);
        } catch (Exception _ignored) {
            return Output.unknownArguments();
        }

        //Execute the actual command requested
        if (args[1].equals("add")) {
            //Check if the executor isn't adding negative score
            if (Integer.parseInt(args[2]) < 0) {
                e.getChannel().sendMessage("Can't add negative score. To remove points use the <remove> argument.").queue();
                return -1;
            }

            //Add score to the user
            try {
                com.flornian.ScoreSystem.ScoreSystem.addScore(toModify, Integer.parseInt(args[2]));
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            //Send a message that the user got points
            try {
                e.getChannel().sendMessage(e.getMember().getAsMention() + " added " + args[2] + " to " + toModify.getAsMention() + "'s score (Now " + com.flornian.ScoreSystem.ScoreSystem.getPoints(toModify) + ").").queue();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (args[1].equals("remove")) {
            //Remove the points
            try {
                //If the user's points will go below 0, cancel
                if (com.flornian.ScoreSystem.ScoreSystem.getPoints(toModify) - Integer.parseInt(args[2]) < 0) {
                    e.getChannel().sendMessage("Unable to remove " + args[2] + " points from " + toModify.getEffectiveName() + ". Lowest score value is " + (Integer.parseInt(args[2]) - (26 - (com.flornian.ScoreSystem.ScoreSystem.getPoints(toModify) - Integer.parseInt(args[2]))))).queue();
                    return -1;
                }

                //Check if the executor isn't adding negative score
                if(Integer.parseInt(args[2]) < 0) {
                    e.getChannel().sendMessage("Can't remove negative score. To add points use the <add> argument.").queue();
                    return -1;
                }

                //Add negative points
                com.flornian.ScoreSystem.ScoreSystem.addScore(toModify, -Integer.parseInt(args[2]));
            } catch (IOException ex) {
                ex.printStackTrace();
                return -1;
            }

            //Send a message that points have been removed from the user
            try {
                e.getChannel().sendMessage(e.getMember().getAsMention() + " removed " + args[2] + " from " + toModify.getAsMention() + "'s score (Now " + com.flornian.ScoreSystem.ScoreSystem.getPoints(toModify) + ").").queue();
            } catch (IOException ex) {
                ex.printStackTrace();
                return -1;
            }
        } else if (args[1].equals("set")) {
            //If the user's points will go below 0, cancel
            if (Integer.parseInt(args[2]) < 0) {
                e.getChannel().sendMessage("Can't set users score lower than 0.").queue();
                return -1;
            }

            //Calculate the amount of points to add/remove from the user to set the right score
            try {
                com.flornian.ScoreSystem.ScoreSystem.addScore(toModify, Integer.parseInt(args[2]) - com.flornian.ScoreSystem.ScoreSystem.getPoints(toModify));
            } catch (IOException ex) {
                ex.printStackTrace();
                return -1;
            }

            //Send a message that the points of the user have been modified
            try {
                e.getChannel().sendMessage(e.getMember().getAsMention() + " set " + toModify.getAsMention() + "'s score to " + com.flornian.ScoreSystem.ScoreSystem.getPoints(toModify) + ".").queue();
            } catch (IOException ex) {
                ex.printStackTrace();
                return -1;
            }
        }

        return 0;
    }

    public static int say(CommandEvent e, String[] args) {
        //Check if there are arguments
        if (args.length < 2) {
            return Output.unknownArguments();
        }

        //Check if the user has permissions to do this
        if (!e.getMember().hasPermission(Vars.adminCommandPermission)) {
            return Output.noPermission();
        }

        //Add all the arguments together to make one string
        StringBuilder b = new StringBuilder();

        for(int i = 1; i < args.length; i++)
            b.append(args[i]).append(" ");

        //Send the message
        String message = b.toString().trim();
        e.getGuild().getTextChannelById(args[0]).sendMessage(message).queue();

        return 0;
    }

    public static int info(CommandEvent e) {
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

        return 0;
    }

    public static int config(CommandEvent e, String[] args) {
        //Check if the user has permissions to do this
        if (!e.getMember().hasPermission(Vars.adminCommandPermission))
            return Output.noPermission();

        //Check if there are enough arguments
        if (args.length < 1)
            return Output.unknownArguments();

        //Check the 1st argument and run set, get or help
        if (args[0].equalsIgnoreCase("help")) {
            e.getChannel().sendMessage("For config help, please visit: http://fkorstanje.nl/aa/RoboBoi-Help-ConfigCommand.txt").queue();

        } else if (args[0].equalsIgnoreCase("set")) {
            //Check if there is enough arguments to run "$config set"
            if (args.length != 3)
                return Output.unknownArguments();

            BotSettings.Result result;

            //Try to set the value and save the result in a variable
            try {
                result = BotSettings.setValue(args[1], args[2]);
            } catch (IOException ioException) {
                ioException.printStackTrace();
                return -1;
            }

            //Tell the user if the command succeeded or not
            if (result == BotSettings.Result.SUCCESS) {
                //Tell the user the command succeeded
                e.getChannel().sendMessage("Successfully set \"" + args[1] + "\" to \"" + args[2] + "\".").queue();
            } else if (result == BotSettings.Result.INVALID_VALUE) {
                //Tell the user the value isn't the correct datatype for that key
                e.getChannel().sendMessage("\"" + args[2] + "\" is not the correct datatype for \"" + args[1] + "\". Type \"" + Vars.botPrefix + "config help\" for more explanation.").queue();
                return -1;
            } else if (result == BotSettings.Result.KEY_DOES_NOT_EXIST) {
                //Tell the user that key doesn't exist
                e.getChannel().sendMessage("\"" + args[1] + "\" is an invalid key name. Type \"" + Vars.botPrefix + "config help\" for more explanation.").queue();
                return -1;
            } else if (result == BotSettings.Result.INVALID_CHARACTERS) {
                //Tell the user that they can't have newlines in the value
                e.getChannel().sendMessage("Your value contains invalid characters.").queue();
                return -1;
            }
        } else if (args[0].equalsIgnoreCase("get")) {
            //Check if there is enough arguments to run "$config get"
            if (args.length != 2) {
                Output.unknownArguments();
                return -1;
            }

            //Check if the user wants all the values or just one
            if (args[1].equalsIgnoreCase("*")) {
                //Get all the lines in the settings file
                List<String> lines;

                try {
                    lines = IO.readSmallTextFile(Vars.settingsFile);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    return -1;
                }

                //Create an embed builder so we can make the message look nice
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("Config for " + Vars.botName);
                builder.setColor(Util.random(0x000000, 0xFFFFFF));

                //Fill in the embed builder
                for (String line : lines) {
                    if (line.contains("=")) {
                        String[] setting = line.split("=");
                        builder.addField("\"" + setting[0] + "\":", "\"" + setting[1] + "\"", false);
                    }
                }

                //Send the embed message to the channel
                e.getChannel().sendMessage(builder.build()).queue();
            }
        }

        return 0;
    }

    public static int leaderboard(CommandEvent e) {
        if(!Vars.enableScoreSystem)
            return Output.functionDisabled();

        EmbedBuilder b = new EmbedBuilder();
        com.flornian.ScoreSystem.UserScore[] leaderboard = new UserScore[0];
        try {
            leaderboard = com.flornian.ScoreSystem.ScoreSystem.getLeaderboard();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        //Make the embed look good
        b.setTitle("Score Leaderboard");
        b.setFooter(Vars.botName + " made by " + Vars.botOwner);
        b.setColor(Util.random(0x000000, 0xFFFFFF));

        //Add all the users with their score to the leaderboard embed
        for (int i = 0; i < leaderboard.length; i++) {
            String user = e.getGuild().getMemberById(leaderboard[i].getUID()).getAsMention() + (leaderboard[i].getUID().equalsIgnoreCase(e.getAuthor().getId()) ? " (you)" : "");
            int difference = 0;
            boolean senderHasMorePoints;
            try {
                difference = ScoreSystem.getPoints(e.getMember()) - leaderboard[i].getScore();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            senderHasMorePoints = difference <= 0;
            difference = Math.abs(difference);
            b.addField("#" + (i + 1), user + "\n__" + leaderboard[i].getScore() + " points__ *(" + difference + " points " + (senderHasMorePoints ? "more" : "less") + " than you)*", false);
        }

        //Send the embed
        e.getChannel().sendMessage(b.build()).queue();

        return 0;
    }

    public static int userinfo(CommandEvent e, String[] args) {
        if(args.length > 1)
            return Output.unknownArguments();

        Member member = e.getMember();

        if(args.length == 1) {
            try {
                member = e.getGuild().getMemberById(args[0]);
            } catch (Exception exception) {
                return Output.unknownUid();
            }

            if(member == null)
                return Output.unknownUid();
        }

        //Create embed builder and set it up
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(member.getColor());
        builder.setFooter(Vars.botName + " made by " + Vars.botOwner);

        //Add all the information
        builder.setImage(member.getUser().getAvatarUrl());
        builder.addField("User", member.getAsMention(), true);
        builder.addField("User ID", member.getId(), true);
        builder.addField("Member Color", String.format("#%06X", member.getColor().getRGB() & 0xFFFFFF), true);
        builder.addField("Joined Discord", String.format("%s%n", member.getUser().getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME)), true);
        builder.addField("Joined Guild", String.format("%s%n", member.getTimeJoined().format(DateTimeFormatter.RFC_1123_DATE_TIME)), true);
        builder.addField("Owner", member.isOwner() ? "Yes" : "No", true);

        //Get list of all the roles the user has and put them in a string
        StringBuilder b = new StringBuilder();
        List<Role> roles = member.getRoles();
        for(int i = 0; i < roles.size(); i++)
            if(i == roles.size() - 1)
                b.append(roles.get(i).getAsMention());
            else
                b.append(roles.get(i).getAsMention()).append(", ");

        builder.addField("Roles", b.toString(), false);

        //Send the message
        e.getChannel().sendMessage(builder.build()).queue();

        return 0;
    }
}
