package com.florn;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Random;

public class Vars {
    //Other
    public static String scoreFile = System.getProperty("user.dir") + "/RoboBoi_Scores.txt";
    public static String settingsFile = System.getProperty("user.dir") + "/RoboBoi_Settings.txt";
    public static String authFile = System.getProperty("user.dir") + "/auth.txt";
    public static String botPrefix;
    public static Guild guild;
    public static Random random = new Random();
    public static String version = "1.7.1_1";
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
    public static String afkChannel;
    public static String botLogChannel;
    public static String rulesChannel;

    //Message IDs
    public static String ruleAcceptMessage;

    //Role IDs
    public static String normalPeopleRole;
    public static String higherPeopleRole;
    public static String superPeopleRole;
}
