package com.florn.Config;

import com.florn.IO;
import com.florn.Util;
import com.florn.Vars;

import java.io.IOException;
import java.util.List;

public class BotSettings {
    public static Result setValue(String variableName, String value) throws IOException {
        //List to make sure people don't break the bot
        String[] invalidStrings = { "\n", };

        if(Util.stringContainsItemFromList(value, invalidStrings))
            return Result.INVALID_CHARACTERS;

        List<String> settings = IO.readSmallTextFile(Vars.settingsFile);
        boolean exists = false;
        String setting = "";
        int settingIndex = 0;

        //Check if the keyName exists as setting in the file
        for (String s : settings) {
            if (s.contains("=")) {
                if (s.split("=")[0].equalsIgnoreCase(variableName)) {
                    exists = true;
                    setting = s.split("=")[0];

                    //I know I could've used a normal for-loop but I made it this way and I'm to lazy to fix it
                    settingIndex = settings.indexOf(s);
                }
            }
        }

        //If the keyName doesn't exist, exit the function and return an error
        if (!exists)
            return Result.KEY_DOES_NOT_EXIST;

        //Check if the value is the correct datatype for keyName
        //These are the worst if-statements I've ever made. I'm sorry
        if (setting.equalsIgnoreCase("random_points_per_message")) {
            //Check if the value is a valid Range
            try {
                int min = Integer.parseInt(value.split(",")[0]);
                int max = Integer.parseInt(value.split(",")[1]);

                if(min > max)
                    throw new Exception();
            } catch (Exception e) {
                return Result.INVALID_VALUE;
            }
        } else if (setting.equalsIgnoreCase("give_message_points_delay") ||
                setting.equalsIgnoreCase("give_call_points_delay") ||
                setting.equalsIgnoreCase("higher_people_points") ||
                setting.equalsIgnoreCase("super_people_points")) {
            //Check if the value is a valid int
            try {
                Integer.parseInt(value);
            } catch (Exception e) {
                return Result.INVALID_VALUE;
            }
        }

        //Set the specified setting to the requested value
        settings.set(settingIndex, variableName + "=" + value);

        //Write the new settings to the file
        IO.writeSmallTextFile(settings, Vars.settingsFile);

        return Result.SUCCESS;
    }

    public static String getValueString(String variableName) throws IOException {
        //Get all the settings
        List<String> settings = IO.readSmallTextFile(Vars.settingsFile);

        //Find the requested setting and return it
        for (String setting : settings) {
            if (setting.contains("=")) {
                if (setting.split("=")[0].equalsIgnoreCase(variableName)) {
                    return setting.split("=")[1];
                }
            }
        }

        //No setting found, return nothing
        return "";
    }

    public static int getValueInt(String variableName) throws IOException {
        //Get all the settings
        List<String> settings = IO.readSmallTextFile(Vars.settingsFile);

        //Find the requested setting and return it
        for (String setting : settings) {
            if (setting.contains("=")) {
                if (setting.split("=")[0].equalsIgnoreCase(variableName)) {
                    return Integer.parseInt(setting.split("=")[1]);
                }
            }
        }

        //No setting found, return nothing
        return 0;
    }

    public static Range getValueRange(String variableName) throws IOException {
        //Get all the settings
        List<String> settings = IO.readSmallTextFile(Vars.settingsFile);

        //Find the requested setting and return it
        for (String setting : settings) {
            if (setting.contains("=")) {
                if (setting.split("=")[0].equalsIgnoreCase(variableName)) {
                    String[] values = setting.split("=")[1].split(",");
                    return new Range(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
                }
            }
        }

        //No setting found, return something
        return new Range(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public enum Result {
        KEY_DOES_NOT_EXIST,
        SUCCESS,
        INVALID_VALUE,
        INVALID_CHARACTERS
    }
}
