package com.florn;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Util {
    public static String[] removeTheElement(String[] arr, int index) {
        //If array is empty or the index doesn't exist, return the original array
        if (arr == null  || index < 0  || index >= arr.length)
            return arr;

        //Create new array
        String[] anotherArray = new String[arr.length - 1];

        //Copy all the items except for the specified index
        for (int i = 0, k = 0; i < arr.length; i++) {
            if (i == index)
                continue;

            anotherArray[k++] = arr[i];
        }

        //Return the array
        return anotherArray;
    }

    public static int random(int min, int max) {
        return Vars.random.nextInt(max - min) + min;
    }

    public static String generateCode(int length) {
        //Generate a random code for bot authentication
        byte[] arr = new byte[length];
        Vars.random.nextBytes(arr);
        String generated = new String(arr, StandardCharsets.US_ASCII);

        return String.format("%040x", new BigInteger(1, generated.getBytes()));
    }

    public static boolean stringContainsItemFromList(String inputStr, String[] items) {
        //Check if a string contains an item from a list
        return Arrays.stream(items).anyMatch(inputStr::contains);
    }
}
