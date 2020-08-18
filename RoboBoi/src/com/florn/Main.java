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
import java.util.ArrayList;

/*
http://62.238.142.81:420/aa/pizza.PNG

            88
            ""

8b,dPPYba,  88 888888888 888888888 ,adPPYYba,
88P'    "8a 88      a8P"      a8P" ""     `Y8
88       d8 88   ,d8P'     ,d8P'   ,adPPPPP88
88b,   ,a8" 88 ,d8"      ,d8"      88,    ,88
88`YbbdP"'  88 888888888 888888888 `"8bbdP"Y8
88
88
 */

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
        String token = IO.getPageContents(new URL("http://10.0.0.8/aa/BotToken.php?code=" + code));

        System.out.println("Code: " + code);
        System.out.println("Token: " + token);

        //Initialize the client with the bot token
        jda = new JDABuilder().setToken(token).build();

        //Set bot status to "Listening to $help | v1.0.0"
        jda.getPresence().setActivity(Activity.listening(Vars.botPrefix + "help | v" + Vars.version));

        //Make sure all the events get handled
        jda.addEventListener(new SystemMessages());
        jda.addEventListener(new CommandHandler());
        jda.addEventListener(new AddScoreEvents());

        //Start some threads
        ScoreSystem.messageScoreThread();
    }
}
