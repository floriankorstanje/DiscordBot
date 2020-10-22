package com.florn;

import com.florn.Commands.CommandHandler;
import com.florn.Config.BotSettings;
import com.florn.ScoreSystem.AddScoreEvents;
import com.florn.ScoreSystem.ScoreSystem;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static JDA jda;

    public static void main(String[] args) throws IOException, LoginException {
        System.out.println("Starting RoboBoi version " + Vars.version);

        //Check if settings file exists.
        if (!new File(Vars.settingsFile).exists()) {
            //Tell the user to first download a config file and set the bot up
            System.out.println("UNABLE TO START BOT. NO CONFIG FILE WAS FOUND. PLEASE DOWNLOAD EXAMPLE CONFIG FILE AND PUT IT IN THE SAME DIRECTORY AS THE .jar OF THE BOT. CONFIG FILE EXAMPLE: http://fkorstanje.nl/aa/rb/RoboBoi_Settings.txt\nTHIS IS A ONE-TIME PROCESS, YOU CAN CHANGE THE CONFIG FILE WITH BOT COMMANDS AFTER THE FIRST RUN.\nFOR MORE HELP WITH THE CONFIG FILE, VISIT: http://fkorstanje.nl/aa/rb/RoboBoi-Help-ConfigCommand.txt");
            return;
        }

        //Check if score file exists.
        if (!new File(Vars.scoreFile).exists()) {
            //Create score file with default settings if it doesn't exist
            Files.createFile(Paths.get(Vars.scoreFile));
        }

        //Check if the authentication file exists
        if (!new File(Vars.authFile).exists()) {
            //Create auth file and add a auth key to it
            Files.createFile(Paths.get(Vars.authFile));

            //Generate random hex string
            String authKey = Util.generateCode(64);

            //Add the auth key to the file
            ArrayList<String> lines = new ArrayList<>();
            lines.add(authKey);

            IO.writeSmallTextFile(lines, Vars.authFile);
        }

        //Get the bot token from my website
        String code = IO.readSmallTextFile(Vars.authFile).get(0);
        String token = IO.getPageContents(new URL("http://fkorstanje.mynetgear.com/aa/BotToken.php?code=" + code));

        System.out.println("Code: " + code);
        System.out.println("Token: " + token);

        //Initialize the client with the bot token
        jda = new JDABuilder().setToken(token).build();

        //Set some variables
        Vars.appInfo = jda.retrieveApplicationInfo().complete();
        Vars.botName = jda.getSelfUser().getName();
        Vars.botOwner = Vars.appInfo.getOwner().getName();

        //Set the variables from the settings file
        Vars.botPrefix = BotSettings.getValueString("bot_prefix");
        Vars.systemMessagesChannel = BotSettings.getValueString("system_messages_channel");
        Vars.afkChannel = BotSettings.getValueString("afk_channel");
        Vars.botLogChannel = BotSettings.getValueString("bot_log_channel");
        Vars.rulesChannel = BotSettings.getValueString("rules_channel");
        Vars.ruleAcceptMessage = BotSettings.getValueString("rule_accept_message");
        Vars.normalPeopleRole = BotSettings.getValueString("normal_people_role");
        Vars.higherPeopleRole = BotSettings.getValueString("higher_people_role");
        Vars.superPeopleRole = BotSettings.getValueString("super_people_role");
        Vars.joinMessage = BotSettings.getValueString("join_message");
        Vars.leaveMessage = BotSettings.getValueString("leave_message");
        Vars.banMessage = BotSettings.getValueString("ban_message");
        Vars.unbanMessage = BotSettings.getValueString("unban_message");
        Vars.roleGetMessage= BotSettings.getValueString("role_get_message");
        Vars.boostMessage = BotSettings.getValueString("boost_message");

        //Make sure all the events get handled
        jda.addEventListener(new GuildSystem());
        jda.addEventListener(new CommandHandler());
        jda.addEventListener(new AddScoreEvents());

        //Start thread for scoring message points
        ScoreSystem.messageScoreThread();

        //Get the current time so the bot can keep track of it's uptime
        Instant startTime = Instant.now();

        //Cycle between statuses
        AtomicInteger index = new AtomicInteger();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Runnable task = () -> {
            //List of custom statuses
            Activity[] statuses = {
                    Activity.listening(Vars.botPrefix + "help"),
                    Activity.playing(Vars.botName + " v" + Vars.version),
                    Activity.playing(Vars.botName + " for " + getRuntime(startTime)),
                    Activity.playing(Vars.botName + " by " + Vars.botOwner)
            };

            //Set the status
            jda.getPresence().setActivity(statuses[index.get()]);
            index.getAndIncrement();

            //Make sure the index doesn't get out of bounds of the array
            if (index.get() == statuses.length)
                index.set(0);
        };

        //Start the status loop
        executor.scheduleWithFixedDelay(task, 0, 10, TimeUnit.SECONDS);
    }

    //function to get the runtime of the bot (for the status)
    static String getRuntime(Instant start) {
        Instant finish = Instant.now();
        long elapsed = Duration.between(start, finish).toMillis();
        return String.format("%02d:%02d",
                (int) ((elapsed / (1000*60*60))),
                (int) ((elapsed / (1000 * 60)) % 60));
    }
}
