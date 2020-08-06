package com.florn;

import com.florn.ScoreSystem.Range;
import com.florn.ScoreSystem.ScoreSettings;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

public class Util {
    //Shamelessly stolen from https://www.geeksforgeeks.org/remove-an-element-at-specific-index-from-an-array-in-java/
    public static String[] removeTheElement(String[] arr, int index)
    {
        // If the array is empty
        // or the index is not in array range
        // return the original array
        if (arr == null
                || index < 0
                || index >= arr.length) {

            return arr;
        }

        // Create another array of size one less
        String[] anotherArray = new String[arr.length - 1];

        // Copy the elements except the index
        // from original array to the other array
        for (int i = 0, k = 0; i < arr.length; i++) {

            // if the index is
            // the removal element index
            if (i == index) {
                continue;
            }

            // if the index is not
            // the removal element index
            anotherArray[k++] = arr[i];
        }

        // return the resultant array
        return anotherArray;
    }

    public static void sendPrivateMessage(User user, String content, TextChannel e) {
        user.openPrivateChannel().queue((channel) -> {
            try {
                channel.sendMessage(content).queue();
            } catch (Exception ex) {
                e.sendMessage("I sent the message. If you didn't receive it, it is probably because you have turned off the \"Allow direct messages from server members\" is your discord settings. To turn this off, go to User Settings -> Privacy & Safety -> Allow direct messages from server members, and turn this on.\nIf it still doesn't work after you've done this, please message <@399594813390848002>").queue();
            }
        });
    }

    public static ScoreSettings getScoreSettings() {
        //Set default settings in case the file can't be read
        ScoreSettings toReturn = new ScoreSettings(new Range(1, 5), 60000, 300000);

        try {
            //Put the entire file in a string array
            List<String> fullFile = IO.readSmallTextFile(Vars.settingsFile);
            String[] vars = Arrays.copyOf(fullFile.toArray(), fullFile.toArray().length, String[].class);

            //Remove variable naming
            for(int i = 0; i < vars.length; i++) {
                vars[i] = vars[i].split(":")[1];
            }

            //Set all the values gotten from the file
            String[] range = vars[0].split(",");
            Range r = new Range(Integer.parseInt(range[0]),Integer.parseInt(range[1]));
            toReturn.setRandomPointsPerMessage(r);

            int randomPointsPerMessageDelay = Integer.parseInt(vars[1]);
            toReturn.setRandomPointsPerMessageDelay(randomPointsPerMessageDelay);

            int callPointsDelay = Integer.parseInt(vars[2]);
            toReturn.setCallPointsDelay(callPointsDelay);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return toReturn;
    }

    public static int random(int min, int max) {
        return Vars.r.nextInt(max - min) + min;
    }

    public static boolean pingHost(String host, int port, int timeout) {
        //Try to connect to IP and port
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            // Either timeout or unreachable or failed DNS lookup.
            return false;
        }
    }

    public static String generateRandom(int length) {
        byte[] arr = new byte[length];
        Vars.r.nextBytes(arr);
        String generated = new String(arr, Charset.forName("UTF-8"));

        return String.format("%040x", new BigInteger(1, generated.getBytes()));
    }
}
