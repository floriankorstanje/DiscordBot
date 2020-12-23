package com.florn;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Random;

public class Vars {
    //Other
    public static String fileFolder = System.getProperty("user.dir") + "/Bot-Files";
    public static String scoreFile = fileFolder + "/Scores.txt";
    public static String settingsFile = fileFolder + "/Settings.txt";
    public static String tokenFile = fileFolder + "/Token.txt";
    public static String botPrefix;
    public static Guild guild;
    public static Random random = new Random();
    public static String version = "1.10.0_1";
    public static Permission adminCommandPermission = Permission.MESSAGE_MANAGE;
    public static String joinMessage;
    public static String leaveMessage;
    public static String banMessage;
    public static String roleGetMessage;
    public static String unbanMessage;
    public static String boostMessage;

    //Bot Info
    public static ApplicationInfo appInfo;
    public static String botName;
    public static String botOwner;

    //Channel IDs
    public static String systemMessagesChannel;
    public static String botLogChannel;
    public static String rulesChannel;

    //Message IDs
    public static String ruleAcceptMessage;

    //Role IDs
    public static String normalPeopleRole;
    public static String higherPeopleRole;
    public static String superPeopleRole;
}
