package com.florn;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Random;

public class Vars {
    //Other
    public static String scoreFile = System.getProperty("user.dir") + "/RoboBoi_Scores.txt";
    public static String settingsFile = System.getProperty("user.dir") + "/RoboBoi_Settings.txt";
    public static String authFile = System.getProperty("user.dir") + "/auth.txt";
    public static String botPrefix = "$";
    public static Guild guild = null;
    public static Random random = new Random();
    public static String version = "1.5.10_5";
    public static Permission adminCommandPermission = Permission.MESSAGE_MANAGE;

    //Channel IDs
    public static String systemMessagesChannel = "630799468043829248";
    public static String afkChannel = "662999958273654834";
    public static String botLogChannel = "740619727612805241";
    public static String rulesChannel = "647116578218836018";

    //Message IDs
    public static String ruleAcceptMessage = "737601352921776149";

    //Role IDs
    public static String newPeopleRole = "709023364525719554";
    public static String normalPeopleRole = "647149235568902144";
    public static String higherPeopleRole = "647147137548943422";
    public static String superPeopleRole = "697405963543642122";
}
