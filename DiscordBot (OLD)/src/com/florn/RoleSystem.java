package com.florn;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

public class RoleSystem extends ListenerAdapter {
    public static String scoreFile = System.getProperty("user.dir") + "/DiscordBotScores.txt";

    public static ArrayList<Member> hasSent = new ArrayList<>();

    public static void MsgThread() {
        Random r = new Random();

        new Thread(new Runnable() {
            public void run() {
                int total = 0;

                while (!Thread.interrupted()) {
                    try {
                        hasSent.clear();
                        int delayTime = Integer.parseInt(getVars()[1]);
                        Thread.sleep(delayTime);

                        int minScore = Integer.parseInt(getVars()[0].split(",")[0]);
                        int maxScore = Integer.parseInt(getVars()[0].split(",")[1]);

                        if(hasSent != null) {
                            for(Member m : hasSent) {
                                addScore(m, r.nextInt(maxScore - minScore) + minScore, "they did something in the last " + (delayTime / 1000) + " seconds.");
                            }
                        }

                        updateAllRoles();
                        //fixScoreList(Commands.g);

                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    static String[] getVars() {
        try {
            String fullFile = CoolIO.readSmallTextFile(scoreFile).get(0);
            String[] vars = fullFile.split("&");

            for(int i = 0; i < vars.length; i++) {
                vars[i] = vars[i].split(":")[1];
            }

            return vars;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new String[0];
    }

    public static Object[] getRank(Member m) throws IOException {
        List<String> lines = CoolIO.readSmallTextFile(scoreFile);
        ArrayList<UserScore> users = new ArrayList<>();

        for(String line : lines)
            if(lines.indexOf(line) != 0)
                users.add(new UserScore(line.split(":")[0], Integer.parseInt(line.split(":")[1])));

        UserScore[] scores = sortUserScoreArray(users.toArray(new UserScore[0]));

        List<UserScore> list = Arrays.asList(reverseUserScoreArray(scores));

        int rank = list.indexOf(getUserScoreFromId(list, m.getId()));
        UserScore scoreBelow = new UserScore("Not Found", -1);
        UserScore scoreBefore = new UserScore("Not Found", -1);

        try {
            scoreBelow = list.get(rank + 1);
        } catch (Exception ignored) {}

        try {
            scoreBefore = list.get(rank - 1);
        } catch (Exception ignored) {}

        return new Object[] { rank + 1, 0, scoreBelow, scoreBefore};
    }

    private static UserScore getUserScoreFromId(List<UserScore> array, String id) {
        for(UserScore score : array)
            if(score.getId().equals(id)) {
                return score;
            }

        return new UserScore("Not Found", -1);
    }

    public static UserScore[] sortUserScoreArray(UserScore[] array) {
        UserScore temp;
        for (int i = 1; i < array.length; i++) {
            for (int j = i; j > 0; j--) {
                if (array[j].getScore() < array [j - 1].getScore()) {
                    temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;
                }
            }
        }

        return array;
    }

    public static UserScore[] reverseUserScoreArray(UserScore[] array) {
        for(int i=0; i<array.length/2; i++){
            UserScore temp = array[i];
            array[i] = array[array.length -i -1];
            array[array.length -i -1] = temp;
        }

        return array;
    }

    @Override
    public void onGuildMessageReactionRemove(@Nonnull GuildMessageReactionRemoveEvent e) {
        try {
            if(Objects.requireNonNull(e.getMember()).getUser().isBot())
                return;

            if(e.getMessageId().equals("737601352921776149")) { // ID Is the message where people can react to get their normal peeps role
                addScore(e.getMember(), -25, "ServerReadRules");
            }

            addScore(e.getMember(), -1, "they removed a reaction");
            updateRole(e.getMember(), e.getGuild());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
        if(Objects.requireNonNull(e.getMember()).getUser().isBot())
            return;

        if(!hasSent.contains(e.getMember()))
            hasSent.add(e.getMember());

        try {
            updateRole(e.getMember(), e.getGuild());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent e) {
        if(e.getMember().getUser().isBot())
            return;

        new Thread(new Runnable() {
            public void run() {
                int total = 0;

                while (!Thread.interrupted()) {
                    try {
                        Thread.sleep(Integer.parseInt(getVars()[2]));

                        if(e.getChannelJoined().getId().equals("662999958273654834")) //Check if people are joining the AFK channel
                            return;

                        if (!e.getChannelJoined().getMembers().contains(e.getMember()))
                            return;

                        if(e.getChannelJoined().getMembers().size() > 2) {
                            if(total >= 50)
                                return;

                            total++;

                            if(!e.getMember().getVoiceState().isDeafened())
                                addScore(e.getMember(), 1, "they have been in a voice channel for 5 minutes");

                            updateRole(e.getMember(), e.getGuild());
                        }
                    } catch (InterruptedException | IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onGuildMemberUpdateBoostTime(@Nonnull GuildMemberUpdateBoostTimeEvent e) {
        try {
            if(e.getMember().getUser().isBot())
                return;

            addScore(e.getMember(), 50, "they boosted the server.");
            updateRole(e.getMember(), e.getGuild());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void fixScoreList(Guild g) throws IOException {
        List<String> lines = CoolIO.readSmallTextFile(scoreFile);

        for(String line : lines) {
            if(lines.indexOf(line) != 0) {
                try {
                    g.getMemberById(line.split(":")[0]).getAsMention();
                } catch (Exception ignored) {
                    removeId(line.split(":")[0]);
                }
            }
        }

        for(Member m : g.getMembers()) {
            try {
                addScore(m, 0, "ScoreFix");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void removeId(String m) throws IOException {
        List<String> lines = CoolIO.readSmallTextFile(scoreFile);
        int userLine = getUserLine(m);
        lines.remove(lines.get(userLine));
        CoolIO.writeSmallTextFile(lines, scoreFile);
    }

    public static int getUserLine(String m) {
        try {
            List<String> lines = CoolIO.readSmallTextFile(scoreFile);

            for(int i = 0; i < lines.size(); i++) {
                String user = lines.get(i).split(":")[0];
                if(user.equalsIgnoreCase(m)) {
                    return i;
                }
            }

            return -1;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent e) {
        try {
            if(e.getMember().getUser().isBot())
                return;

            if(e.getMessageId().equals("737601352921776149")) { //This is the ID of the message where people can react to get their normal peeps role
                addScore(e.getMember(), 25, "ServerReadRules");
            }

            addScore(e.getMember(), 1, "they added a reaction to a message.");
            updateRole(e.getMember(), e.getGuild());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static int getPoints(Member m) throws IOException {
        int userLine = getUserLine(m);
        List<String> lines = CoolIO.readSmallTextFile(scoreFile);

        if(userLine == -1) {
            return 0;
        }

        return  Integer.parseInt(lines.get(userLine).split(":")[1].split(",")[0]);
    }

    public static void updateRole(Member m, Guild g) throws IOException {
        if(m.getUser().isBot())
            return;

        int userLine = getUserLine(m);
        List<String> lines = CoolIO.readSmallTextFile(scoreFile);

        if(userLine == -1) {
            lines.add(m.getId() + ":0");
        } else {
            String oldLine = lines.get(userLine);
            int score = Integer.parseInt(oldLine.split(":")[1].split(",")[0]);
            if(score < 25) {
                g.removeRoleFromMember(m, Objects.requireNonNull(g.getRoleById(SystemMessages.normalPeepsRole))).complete();
                g.removeRoleFromMember(m, Objects.requireNonNull(g.getRoleById(SystemMessages.higherPeepsRole))).complete();
                g.removeRoleFromMember(m, Objects.requireNonNull(g.getRoleById(SystemMessages.superPeepsRole))).complete();
            } else if(score > 25 && score <= 500) {
                if(!hasRole(m, SystemMessages.normalPeepsRole)) {
                    Role role = g.getRoleById(SystemMessages.normalPeepsRole);

                    try {
                        Thread.sleep((int)(Math.random() * 2500));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    g.addRoleToMember(m, role).complete();

                    g.removeRoleFromMember(m, Objects.requireNonNull(g.getRoleById(SystemMessages.higherPeepsRole))).complete();
                    g.removeRoleFromMember(m, Objects.requireNonNull(g.getRoleById(SystemMessages.superPeepsRole))).complete();

                    g.getTextChannelById(SystemMessages.systemMessagesChannelId).sendMessage( "**" + m.getAsMention() + " has just earned the " + role.getName() + " role!**").queue();
                }
            } else if(score > 500 && score <= 2000) {
                if(!hasRole(m, SystemMessages.higherPeepsRole)) {
                    Role role = g.getRoleById(SystemMessages.higherPeepsRole);

                    try {
                        Thread.sleep((int)(Math.random() * 2500));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    g.addRoleToMember(m, role).complete();

                    g.removeRoleFromMember(m, Objects.requireNonNull(g.getRoleById(SystemMessages.superPeepsRole))).complete();

                    g.getTextChannelById(SystemMessages.systemMessagesChannelId).sendMessage( "**" + m.getAsMention() + " has just earned the " + role.getName() + " role!**").queue();
                }
            } else if(score > 2000) {
                if(!hasRole(m, SystemMessages.superPeepsRole)) {
                    Role role = g.getRoleById(SystemMessages.superPeepsRole);

                    try {
                        Thread.sleep((int)(Math.random() * 2500));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    g.addRoleToMember(m, role).complete();

                    g.getTextChannelById(SystemMessages.systemMessagesChannelId).sendMessage( "**" + m.getAsMention() + " has just earned the " + role.getName() + " role!**").queue();
                }
            }
        }
    }

    public static void updateAllRoles() throws IOException {
        Guild g = Commands.g;

        if(g == null) {
            System.out.println("Unable to update all roles because g=null");
            return;
        }

        //Console.WriteLine("Updating all user roles.");


        for(Member m : g.getMembers()) {
            updateRole(m, g);
        }
    }

    private static boolean hasRole(Member member, String roleId) {
        List<Role> roles = member.getRoles();
        for(Role r : roles) {
            if(r.getId().equals(roleId))
                return true;
        }

        return false;
    }

    public static void addScore(Member m, int score, String reason) throws IOException {
        if(m.getUser().isBot())
            return;

        int userLine = getUserLine(m);
        List<String> lines = CoolIO.readSmallTextFile(scoreFile);

        if(userLine == -1) {
            lines.add(m.getId() + ":" + score);
        } else {
            String oldLine = lines.get(userLine);
            lines.set(userLine, m.getId() + ":" + (Integer.parseInt(oldLine.split(":")[1]) + score));
        }

        CoolIO.writeSmallTextFile(lines, scoreFile);

        //Console.WriteLine("Added " + score + " point to user " + m.getEffectiveName() + ":" + m.getId() + "(" + userLine + ") because " + reason);
    }

    private static int getUserLine(Member m) {
        try {
            List<String> lines = CoolIO.readSmallTextFile(scoreFile);

            for(int i = 0; i < lines.size(); i++) {
                String user = lines.get(i).split(":")[0];
                if(user.equalsIgnoreCase(m.getId())) {
                    return i;
                }
            }

            return -1;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }
}
