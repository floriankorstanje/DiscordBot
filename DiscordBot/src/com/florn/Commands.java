package com.florn;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Commands extends ListenerAdapter {
    String[] prefixx = { "$", "!" };
    Random r = new Random();

    public static Guild g = null;

    @Override
    public void onMessageDelete(@Nonnull MessageDeleteEvent event) {
        System.out.println("[MSG_DELETE | in channel #" + event.getTextChannel().getName() + " | in guild " + event.getGuild().getName() + "] Message Id: " + event.getMessageId());
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        g = event.getGuild();

        String message = event.getMessage().getContentRaw();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        System.out.println("[" + dtf.format(now) + " | #" + event.getChannel().getName() + " | " + event.getMessageId() + "] \"" + event.getMember().getEffectiveName() + "\":(" + event.getMember().getUser().getAsTag() + "): \"" + event.getMessage().getContentDisplay().replace('\n', ' ') + "\"");

        for(String p : prefixx) {
            if(message.startsWith(p)) {
                if(message.equalsIgnoreCase(p + "help")) {
                    sendPrivateMessage(event.getMember().getUser(), "**Help for " + event.getJDA().getSelfUser().getName() + "**\n" +
                            "I have " + prefixx.length + " prefixes: " + getPrefixesAsString() + "\n" +
                            p + "clear - Clears a channel. Only for admins.\n" +
                            p + "dm <message> - Sends a dm message to yourself.\n" +
                            p + "say - Lets the bot say something. Only for admins.\n" +
                            p + "mute <user> - mutes a user. Admin only\n" +
                            p + "unmute <user> - Unmutes a user. Admin only\n" +
                            p + "howgay <name> - Tells how gay a person is.\n" +
                            p + "marry - Matches you with a random user.\n" +
                            p + "id - Returns your user ID.\n" +
                                    p + "punishscore <userid> - Removes 50 points from a user if they abused the points system. Only for admins.\n" +
                                    p + "score [userid|*] - Shows your current score. Enter a userid to see someone else's score, enter a * to get all scores.\n" +
                            p + "modscore <UID> <add|remove|set> <score> - Modifies a users score. Only for admins\n" +
                            p + "mcserver - Pings the minecraft server to see whether it is on or off.\n",
                    event.getChannel());
                } else if(message.equalsIgnoreCase(p + "clear")) {
                    if(event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                        clear(event.getChannel(), event.getMember());
                    } else {
                        event.getChannel().sendMessage("You don't have permission to perform this action!").queue();
                    }
                } else if(message.contains(p + "dm ")) {
                    String content = message.replace(p + "dm ", "");
                    sendPrivateMessage(event.getMember().getUser(), content, event.getChannel());
                } else if(message.contains(p + "say ")) {
                    if(event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                        String content = message.replace(p + "say ", "");
                        event.getMessage().delete().queue();
                        event.getChannel().sendMessage(content).queue();
                    } else {
                        event.getChannel().sendMessage("You don't have permission to perform this action!").queue();
                    }
                } else if(message.contains(p + "mute ")) {
                    Member member = event.getGuild().getMemberById(message.replace(p + "mute ", "").replace("<@!", "").replace(">", ""));
                    muteUser(event, member, true);
                } else if(message.contains(p + "unmute ")) {
                    Member member = event.getGuild().getMemberById(message.replace(p + "unmute ", "").replace("<@!", "").replace(">", ""));
                    muteUser(event, member, false);
                } else if(message.contains((p + "howgay "))) {
                    String user = message.replace(p + "howgay ", "");
                    double len = user.length();
                    double max = 8192 * len;
                    double cc = 1;

                    for(char c : user.toCharArray())
                        cc += c;

                    double percent = (cc / max * 100);

                    if(percent < 0)
                        percent = 0;

                    event.getChannel().sendMessage(user + " is " + percent + "% gay.").queue();
                } else if(message.equalsIgnoreCase(p + "marry")) {
                    List<Member> members = event.getGuild().getMembers();

                    Member user = members.get(r.nextInt(members.size()));

                    if(user == event.getMember()) {
                        event.getChannel().sendMessage(event.getAuthor().getAsMention() + " will be single forever.").queue();
                    } else {
                        event.getChannel().sendMessage(event.getMember().getEffectiveName() + " will marry " + user.getEffectiveName() + ".").queue();
                    }
                } else if(message.startsWith(p + "id")) {
                    event.getChannel().sendMessage("Your (" + event.getAuthor().getAsMention()+ ") user ID is " + event.getMember().getId()).queue();
                } else if(message.startsWith(p + "punishscore ")) {
                    if(!event.getMember().hasPermission(Permission.MESSAGE_MANAGE))
                        return;

                    String id = message.split("\\s+")[1];
                    try {
                        int score = -(int)(RoleSystem.getPoints(event.getGuild().getMemberById(id)) * 0.25);
                        RoleSystem.addScore(event.getGuild().getMemberById(id), score, "they got punished.");

                        event.getChannel().sendMessage(event.getGuild().getMemberById(id).getAsMention() + " has been punished by " + event.getMember().getAsMention() + " and lost " + (score * -1) + " points.").queue();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if(message.startsWith(p + "score")) {
                    int score = 0;
                    Member name = null;

                    if(!message.contains(" ")) {
                        try {
                            name = event.getMember();
                            score = RoleSystem.getPoints(name);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        String id = message.split("\\s+")[1];

                        if(id.equals("*")) {
                            getAllScores(event.getMember().getUser(), event.getChannel());
                            return;
                        }

                        try {
                            name = event.getGuild().getMemberById(id);
                            score = RoleSystem.getPoints(name);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    EmbedBuilder b = new EmbedBuilder();
                    String n = name.getEffectiveName() + (name.getEffectiveName().toCharArray()[name.getEffectiveName().length() - 1] == 's' ? "'" : "'s");
                    b.setTitle(n + " Score");
                    b.setColor(new Color(r.nextInt(0xFFFFFF)));

                    b.addField("User", name.getAsMention(), false);

                    b.addField("Higher Peeps progress", score + "/" + "500\n" + Math.round(score / 500f * 100) + "% progress.", false);
                    b.addField("Super Peeps progress", score + "/" + "2000\n" + Math.round(score / 2000f * 100) + "% progress.", false);

                    b.addField("Overall Progress", (score > 500 ? name.getEffectiveName() + " achieved Higher Peeps!" : name.getEffectiveName() + " hasn't achieved Higher peeps yet.") + "\n" + (score > 2000 ? name.getEffectiveName() + " achieved Super Peeps!" : name.getEffectiveName() + " hasn't achieved Super Peeps yet."), false);

                    Object[] rank = new Object[0];
                    try {
                        rank = RoleSystem.getRank(name);
                        rank[1] = event.getGuild().getMembers().size();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    b.addField("Rank", rank[0] + " out of " + rank[1] + "\n" +
                            "Top " + Math.round((float)((double)(int)rank[0] / (double)(int)rank[1]) * 100) + "%", false);

                    UserScore below = (UserScore)rank[2];
                    UserScore above = (UserScore)rank[3];

                    try {
                        b.addField("Below You", (below.getScore() != -1 ?
                                event.getGuild().getMemberById(below.getId()).getAsMention() + " is " + (RoleSystem.getPoints(name) - below.getScore()) + " points below you." :
                                "You are on the bottom of the leaderboard. There is no-one below you :("), false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        b.addField("Above You", (above.getScore() != -1 ?
                                event.getGuild().getMemberById(above.getId()).getAsMention() + " is " + (above.getScore() - RoleSystem.getPoints(name)) + " points above you." :
                                "You are on the top of the leaderboard. There is no-one above you :)"), false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    event.getChannel().sendMessage(b.build()).queue();
                } else if(message.startsWith(p + "modscore ")) {
                    if(!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                        event.getChannel().sendMessage("You do not have permission to perform this action.").queue();
                        return;
                    }

                    String[] args = message.replace(p + "modscore ", "").split("\\s+");

                    if(args.length != 3) {
                        event.getChannel().sendMessage("Invalid arguments. Usage: $modscore <UID> <add|remove|set> <score>").queue();
                        return;
                    }

                    Member member = null;

                    try {
                        member = event.getGuild().getMemberById(args[0]);
                    } catch (Exception e) {
                        event.getChannel().sendMessage("\"" + args[0] + "\" is not a valid UID.").queue();
                        return;
                    }

                    if(!args[1].equals("add") && !args[1].equals("remove") && !args[1].equals("set")) {
                        event.getChannel().sendMessage("\"" + args[1] + "\" is invalid. Usage: $modscore <UID> <add|remove|set> <score>").queue();
                        return;
                    }

                    try {
                        int score = Integer.parseInt(args[2]);
                    } catch (Exception e) {
                        event.getChannel().sendMessage("\"" + args[2] + "\" is not a valid score number.").queue();
                        return;
                    }

                    if(args[1].equals("add")) {
                        try {
                            RoleSystem.addScore(member, Integer.parseInt(args[2]), "$modscore");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            event.getChannel().sendMessage(event.getMember().getAsMention() + " added " + args[2] + " to " + member.getAsMention() + "'s score (Now " + RoleSystem.getPoints(member) + ").").queue();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if(args[1].equals("remove")) {
                        try {
                            if(RoleSystem.getPoints(member) - Integer.parseInt(args[2]) < 26) {
                                // score_to_remove - (26 - (userscore - score_to_remove))
                                event.getChannel().sendMessage("Unable to remove " + args[2] + " points from " + member.getEffectiveName() + ". Lowest score value is " + (Integer.parseInt(args[2]) - (26 - (RoleSystem.getPoints(member) - Integer.parseInt(args[2]))))).queue();
                                return;
                            }

                            RoleSystem.addScore(member, -Integer.parseInt(args[2]), "$modscore");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            event.getChannel().sendMessage(event.getMember().getAsMention() + " removed " + args[2] + " from " + member.getAsMention() + "'s score (Now " + RoleSystem.getPoints(member) + ").").queue();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if(args[1].equals("set")) {
                        if(Integer.parseInt(args[2]) < 26) {
                            event.getChannel().sendMessage("Can't set users score lower than 26.").queue();
                            return;
                        }

                        try {
                            RoleSystem.addScore(member, Integer.parseInt(args[2]) - RoleSystem.getPoints(member), "$modscore");

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            event.getChannel().sendMessage(event.getMember().getAsMention() + " set " + member.getAsMention() + "'s score to " + RoleSystem.getPoints(member) + ".").queue();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if(message.equalsIgnoreCase(p + "mcserver")) {
                    String serverIP = "10.0.0.2"; /*Minecraft server running on PC on local network*/
                    int serverPort = 25565;
                    int timeout = 500;

                    if(pingHost(serverIP, serverPort, timeout)) {
                        event.getChannel().sendMessage("Minecraft server is on!").queue();
                    } else {
                        event.getChannel().sendMessage("Minecraft server is off.").queue();
                    }
                }
            }
        }
    }

    public static boolean pingHost(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }

    private void getAllScores(User u, TextChannel ch) {
        StringBuilder b = new StringBuilder();

        List<String> lines = null;
        try {
            lines = CoolIO.readSmallTextFile(RoleSystem.scoreFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<UserScore> users = new ArrayList<>();

        for(String line : lines)
            if(lines.indexOf(line) != 0)
                users.add(new UserScore(line.split(":")[0], Integer.parseInt(line.split(":")[1])));

        UserScore[] scores = RoleSystem.sortUserScoreArray(users.toArray(new UserScore[0]));

        UserScore[] list = RoleSystem.reverseUserScoreArray(scores);

        for(UserScore score : list) {
            try {
                b.append(g.getMemberById(score.getId()).getEffectiveName()).append(": ").append(score.getScore()).append("\n");
            } catch (Exception ignored) {}
        }

        sendPrivateMessage(u, b.toString(), ch);
    }

    private String getPrefixesAsString() {
        StringBuilder builder = new StringBuilder();

        for(String pref : prefixx) {
            builder.append("\"" + pref + "\", ");
        }

        return builder.toString().substring(0, builder.toString().length() - 2);
    }

    private void muteUser(GuildMessageReceivedEvent e, Member member, boolean mute) {
        if(!e.getMember().hasPermission(Permission.KICK_MEMBERS)) {
            e.getChannel().sendMessage("You don't have permission to perform this action!").queue();
            return;
        }

        Guild guild = e.getGuild();
        Role mutedRole = guild.getRoleById("675750498434940978");
        assert mutedRole != null;
        if(mute) {
            guild.addRoleToMember(member, mutedRole).complete();
            guild.removeRoleFromMember(member, Objects.requireNonNull(guild.getRoleById(new SystemMessages().normalPeepsRole))).complete();
            guild.removeRoleFromMember(member, Objects.requireNonNull(guild.getRoleById(new SystemMessages().higherPeepsRole))).complete();
            guild.removeRoleFromMember(member, Objects.requireNonNull(guild.getRoleById(new SystemMessages().superPeepsRole))).complete();

            e.getChannel().sendMessage("Successfully muted " + member.getAsMention()).queue();
        } else {
            guild.removeRoleFromMember(member, mutedRole).complete();
            guild.addRoleToMember(member, Objects.requireNonNull(guild.getRoleById(new SystemMessages().normalPeepsRole))).complete();

            e.getChannel().sendMessage("Successfully unmuted " + member.getAsMention()).queue();
        }
    }

    private void sendPrivateMessage(User user, String content, TextChannel e) {
        user.openPrivateChannel().queue((channel) -> {
            try {
                channel.sendMessage(content).queue();
            } catch (Exception ex) {
                e.sendMessage("I sent the message. If you didn't receive it, it is probably because you have turned off the \"Allow direct messages from server members\" is your discord settings. To turn this off, go to User Settings -> Privacy & Safety -> Allow direct messages from server members, and turn this on.\nIf it still doesn't work after you've done this, please message <@399594813390848002>").queue();
            }
        });
    }

    public static String getRandom(String[] array) {
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }

    boolean isWorking = false;

    public void clear(TextChannel channel, Member member)
    {
        AtomicInteger count = new AtomicInteger();

        if (isWorking)
        {
            channel.sendMessage("I'm busy right now...").queue();
            return;
        }

        isWorking = true;

        OffsetDateTime twoWeeksAgo = OffsetDateTime.now().minus(2, ChronoUnit.WEEKS);

        System.out.println("Deleting messages in channel: " + channel);

        new Thread(() ->
        {
            while (isWorking)
            {
                List<Message> messages = channel.getHistory().retrievePast(50).complete();

                messages.removeIf(m -> m.getTimeCreated().isBefore(twoWeeksAgo));

                if (messages.isEmpty())
                {
                    isWorking = false;
                    System.out.println("Done deleting: " + channel);
                    return;
                }

                messages.forEach(m -> System.out.println("Deleting: " + m));
                messages.forEach(m -> count.getAndIncrement());
                channel.deleteMessages(messages).complete();
            }
        })
                .run();

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        channel.sendMessage("Cleared " + count + " messages at " + formatter.format(date) + "\nThis action was performed by " + member.getAsMention()).queue();
    }
}
