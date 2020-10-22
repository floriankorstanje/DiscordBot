package com.florn;

import com.florn.Config.Range;
import com.florn.Config.BotSettings;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {
    //Shamelessly stolen from https://www.geeksforgeeks.org/remove-an-element-at-specific-index-from-an-array-in-java/
    public static String[] removeTheElement(String[] arr, int index) {
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

    public static int random(int min, int max) {
        return Vars.random.nextInt(max - min) + min;
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

    public static String generateCode(int length) {
        //Generate a random code for bot authentication
        byte[] arr = new byte[length];
        Vars.random.nextBytes(arr);
        String generated = new String(arr, StandardCharsets.US_ASCII);

        return String.format("%040x", new BigInteger(1, generated.getBytes()));
    }
}
