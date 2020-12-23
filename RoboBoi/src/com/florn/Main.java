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

        //Check if directory for files exists.
        File folder = new File(Vars.fileFolder);
        if(!folder.exists())
            folder.mkdirs();

        //Check if settings file exists.
        if (!new File(Vars.settingsFile).exists()) {
            //Tell the user to first download a config file and set the bot up
            System.out.println("Please visit http://fkorstanje.nl/aa/RoboBoi to set the bot up for first usage. You only need to do this once.");
            return;
        }

        //Check if score file exists.
        if (!new File(Vars.scoreFile).exists()) {
            //Create score file with default settings if it doesn't exist
            Files.createFile(Paths.get(Vars.scoreFile));
        }

        //Check if the authentication file exists
        if (!new File(Vars.tokenFile).exists()) {
            //Create auth file and add a auth key to it
            Files.createFile(Paths.get(Vars.tokenFile));

            //Add the auth key to the file
            ArrayList<String> lines = new ArrayList<>();
            lines.add("YOUR DISCORD BOT TOKEN HERE");

            IO.writeSmallTextFile(lines, Vars.tokenFile);
        }

        //Get the bot token from the auth file (which is ignored by github)
        String token = IO.readSmallTextFile(Vars.tokenFile).get(0);

        //Initialize the client with the bot token
        jda = new JDABuilder().setToken(token).build();

        //Set some variables
        Vars.appInfo = jda.retrieveApplicationInfo().complete();
        Vars.botName = jda.getSelfUser().getName();
        Vars.botOwner = Vars.appInfo.getOwner().getName();

        //Make sure all the events get handled
        jda.addEventListener(new GuildSystem());
        jda.addEventListener(new CommandHandler());
        jda.addEventListener(new AddScoreEvents());

        //Start threads
        ScoreSystem.messageScoreThread();
        updateVariablesThread();

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
                (int) ((elapsed / (1000 * 60 * 60))),
                (int) ((elapsed / (1000 * 60)) % 60));
    }

    //Make sure all the config variables are up to date
    public static void updateVariablesThread() {
        new Thread(() -> {
            //Loop while the thread isn't interrupted
            while (!Thread.interrupted()) {
                try {
                    //Set the variables from the settings file
                    Vars.botPrefix = BotSettings.getValueString("bot_prefix");
                    Vars.systemMessagesChannel = BotSettings.getValueString("system_messages_channel");
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
                    Vars.roleGetMessage = BotSettings.getValueString("role_get_message");
                    Vars.boostMessage = BotSettings.getValueString("boost_message");

                    //Wait 5s
                    Thread.sleep(5000);
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
