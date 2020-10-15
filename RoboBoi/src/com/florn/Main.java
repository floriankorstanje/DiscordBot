package com.florn;

import com.florn.Commands.CommandHandler;
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
            //Create settings file with default settings if it doesn't exist
            Files.createFile(Paths.get(Vars.settingsFile));

            ArrayList<String> lines = new ArrayList<>();
            lines.add("random_points_per_message:1,5");
            lines.add("give_message_points_delay:60000");
            lines.add("give_call_points_delay:300000");
            lines.add("chance_reaction_does_not_give_points:0.8");

            IO.writeSmallTextFile(lines, Vars.settingsFile);
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
            Activity[] statuses = {Activity.listening(Vars.botPrefix + "help"), Activity.playing("RoboBoi v" + Vars.version), Activity.playing("RoboBoi for " + getRuntime(startTime))};

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
                TimeUnit.MILLISECONDS.toHours(elapsed),
                TimeUnit.MILLISECONDS.toMinutes(elapsed) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsed)));
    }
}
