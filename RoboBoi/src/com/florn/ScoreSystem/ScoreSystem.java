package com.florn.ScoreSystem;

import com.florn.GuildSystem;
import com.florn.IO;
import com.florn.Util;
import com.florn.Vars;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ScoreSystem {
    public static ArrayList<Member> hasSent = new ArrayList<>();

    //A thread that loops and gives users a random score if they sent a message in that time period
    public static void messageScoreThread() {
        Random r = Vars.random;

        new Thread(() -> {
            //Loop while the thread isn't interrupted
            while (!Thread.interrupted()) {
                try {
                    hasSent.clear();
                    int delay = Util.getScoreSettings().getRandomPointsPerMessageDelay();
                    Thread.sleep(delay);

                    //Add score to users that have sent a message
                    if (hasSent != null) {
                        for (Member m : hasSent) {
                            Range rRange = Util.getScoreSettings().getRandomPointsPerMessage();
                            addScore(m, Util.random(rRange.getMin(), rRange.getMax()));
                            updateUserRole(m, Vars.guild);
                        }
                    }

                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void VoiceChannelScoreThread(GuildVoiceJoinEvent e) {
        new Thread(() -> {
            int total = 0;

            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(Util.getScoreSettings().getCallPointsDelay());

                    //If someone joined the AFK channel they will not gain points
                    if (e.getChannelJoined().getId().equals(Vars.afkChannel))
                        return;

                    //If the user left the voice channel the loop will stop
                    if (!e.getChannelJoined().getMembers().contains(e.getMember()))
                        return;

                    //Get all the people in the call, but remove the bots
                    List<Member> members = removeBotsFromMemberList(e.getChannelJoined().getMembers());

                    //Only give points if there is more than 1 person in the call (excluding bots)
                    if (members.size() > 1) {
                        if (total >= 50)
                            return;

                        total++;

                        //If the user is deafened, don't give them points as they're not actively using the channel
                        if (!e.getMember().getVoiceState().isDeafened())
                            addScore(e.getMember(), 1);

                        //Update the users role so if they have enough points for a higher role they instantly get it
                        updateUserRole(e.getMember(), e.getGuild());
                    }
                } catch (InterruptedException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    public static void updateUserRole(Member m, Guild g) throws IOException {
        //Don't do anything if the member is a bot
        if (m.getUser().isBot())
            return;

        //Get the basic information about someone's score
        int userLine = getUserLine(m.getId());
        List<String> lines = IO.readSmallTextFile(Vars.scoreFile);

        //Put all the roles in an easy Role object
        Role normalPeeps = g.getRoleById(Vars.normalPeopleRole);
        Role higherPeeps = g.getRoleById(Vars.higherPeopleRole);
        Role superPeeps = g.getRoleById(Vars.superPeopleRole);

        //If the user does not have line in the score file yet, create one
        if (userLine == -1) {
            lines.add(m.getId() + ":0");
        } else {
            //Get some basic data about the user's score
            String oldLine = lines.get(userLine);
            int score = Integer.parseInt(oldLine.split(":")[1]);

            if (score <= 500) {
                //Remove all the higher roles since the user doesn't have the score for them anymore
                g.removeRoleFromMember(m, higherPeeps).complete();
                g.removeRoleFromMember(m, superPeeps).complete();

                //If the user has between 25 and 500 points, they get normal peeps
                if (!hasRole(m, normalPeeps)) {
                    //Add the normal peeps role
                    g.addRoleToMember(m, normalPeeps).complete();

                    //Announce that the user got a new role
                    GuildSystem.announceUserNewRole(m, normalPeeps, g);
                }
            } else if (score > 500 && score <= 2000) {
                //Remove super peeps since the user doesn't have the score for it anymore
                g.removeRoleFromMember(m, superPeeps).complete();

                //If the user has between 500 and 2000 points, they get higher peeps
                if (!hasRole(m, higherPeeps)) {
                    //Add the normal and higher peeps role
                    g.addRoleToMember(m, normalPeeps).complete();
                    g.addRoleToMember(m, higherPeeps).complete();

                    //Announce that the user got a new role
                    GuildSystem.announceUserNewRole(m, higherPeeps, g);
                }
            } else if (score > 2000) {
                //If the user has more than 2000 points, they get super peeps

                if (!hasRole(m, superPeeps)) {
                    //No roles to remove since the user gets the highest role available

                    //Add all the roles
                    g.addRoleToMember(m, normalPeeps).complete();
                    g.addRoleToMember(m, higherPeeps).complete();
                    g.addRoleToMember(m, superPeeps).complete();

                    //Announce that the user got a new role
                    GuildSystem.announceUserNewRole(m, superPeeps, g);
                }
            }
        }
    }

    private static boolean hasRole(Member m, Role r) {
        return m.getRoles().contains(r);
    }

    private static List<Member> removeBotsFromMemberList(List<Member> list) {
        ArrayList<Member> newList = new ArrayList<>();

        for (Member m : list) {
            if (!m.getUser().isBot())
                newList.add(m);
        }

        return newList;
    }

    public static void fixScoreList(Guild g) throws IOException {
        List<String> lines = IO.readSmallTextFile(Vars.scoreFile);

        for (String line : lines) {
            if (lines.indexOf(line) != 0) {
                try {
                    g.getMemberById(line.split(":")[0]).getAsMention();
                } catch (Exception ignored) {
                    removeUserId(line.split(":")[0]);
                    removeUserId(line.split(":")[0]);
                }
            }
        }

        for (Member m : g.getMembers()) {
            try {
                addScore(m, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void removeUserId(String uid) throws IOException {
        //Remove someone's ID from the score list, so if they leave the server and then join back they have 0 points
        List<String> lines = IO.readSmallTextFile(Vars.scoreFile);
        lines.remove(getUserLine(uid));
        IO.writeSmallTextFile(lines, Vars.scoreFile);
    }

    public static void resetUserScore(String uid) throws IOException {
        //Set the users score back to 25
        List<String> lines = IO.readSmallTextFile(Vars.scoreFile);
        lines.set(getUserLine(uid), uid + ":" + 25);
        IO.writeSmallTextFile(lines, Vars.scoreFile);
    }

    private static int getUserLine(String uid) {
        try {
            //Get all the lines in the file
            List<String> lines = IO.readSmallTextFile(Vars.scoreFile);

            //Loop through all the lines and see if it matches the given UID
            for (int i = 0; i < lines.size(); i++) {
                String user = lines.get(i).split(":")[0];
                if (user.equalsIgnoreCase(uid)) {
                    return i;
                }
            }

            //Nothing found, return -1
            return -1;
        } catch (IOException e) {
            e.printStackTrace();
        }

        //If the program threw an exception, return -1 as well
        return -1;
    }

    public static void addScore(Member m, int score) throws IOException {
        //Check if member is a bot, if so, don't update score
        if (m.getUser().isBot())
            return;

        //Read the file with all the user scores
        int userLine = getUserLine(m.getId());
        List<String> lines = IO.readSmallTextFile(Vars.scoreFile);

        //Update score, if user isn't in the file yet, create the line
        if (userLine == -1) {
            lines.add(m.getId() + ":" + score);
        } else {
            String oldLine = lines.get(userLine);
            int newScore = Integer.parseInt(oldLine.split(":")[1]) + score;

            lines.set(userLine, m.getId() + ":" + newScore);
        }

        //Write the new contents to the score file
        IO.writeSmallTextFile(lines, Vars.scoreFile);
    }

    public static Rank getRank(Member m, Guild g) throws IOException {
        List<String> lines = IO.readSmallTextFile(Vars.scoreFile);
        List<UserScore> userScores = new ArrayList<>();

        //Put all the users and their scores in an UserScore array
        for (String line : lines) {
            String[] splitLine = line.split(":");
            String uid = splitLine[0];
            int score = Integer.parseInt(splitLine[1]);
            userScores.add(new UserScore(uid, score));
        }

        //Sort and reverse the UserScore array (and a lot of array conversion)
        userScores = Arrays.asList(reverseUserScoreArray(sortUserScoreArray(userScores.toArray(new UserScore[0]))));

        //Get rank, person below and person above from sorted array
        int rank = userScores.indexOf(getUserScoreFromId(userScores, m.getId()));
        UserScore scoreBelow = new UserScore("Not Found", -1);
        UserScore scoreAbove = new UserScore("Not Found", -1);

        try {
            scoreBelow = userScores.get(rank + 1);
            scoreAbove = userScores.get(rank - 1);
        } catch (Exception ignored) {
        }

        //Get the person who requested his score his UserScore
        UserScore requester = getUserScoreFromId(userScores, m.getId());

        //Return it all as a nice class
        return new Rank(
                requester.getScore(),
                scoreAbove,
                scoreBelow,
                g.getMembers().size(),
                rank + 1,
                requester.getScore() > 500,
                requester.getScore() > 2000,
                (double) requester.getScore() / 500 * 100,
                (double) requester.getScore() / 2000 * 100
        );
    }

    private static UserScore getUserScoreFromId(List<UserScore> array, String id) {
        for (UserScore score : array)
            if (score.getId().equals(id))
                return score;

        return new UserScore("Not Found", -1);
    }

    private static UserScore[] sortUserScoreArray(UserScore[] array) {
        UserScore temp;
        for (int i = 1; i < array.length; i++) {
            for (int j = i; j > 0; j--) {
                if (array[j].getScore() < array[j - 1].getScore()) {
                    temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;
                }
            }
        }

        return array;
    }

    private static UserScore[] reverseUserScoreArray(UserScore[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            UserScore temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }

        return array;
    }

    public static int getPoints(Member m) throws IOException {
        //Get basic variables for reading and writing score
        int userLine = getUserLine(m.getId());
        List<String> lines = IO.readSmallTextFile(Vars.scoreFile);

        if (userLine == -1)
            return 0;

        return Integer.parseInt(lines.get(userLine).split(":")[1]);
    }
}
