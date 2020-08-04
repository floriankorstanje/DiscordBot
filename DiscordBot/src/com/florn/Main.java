package com.florn;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {
    public static JDA jda;

    public static void main(String[] args) throws LoginException, IOException {
        System.out.println("Starting bot...");

        if(!new File(RoleSystem.scoreFile).exists()) {
            try {
                Files.createFile(Paths.get(RoleSystem.scoreFile));

                ArrayList<String> lines = new ArrayList<>();
                lines.add("randompointsmessage:1,5&randompointsmessagedelay:60000&callpointsdelay:300000");

                CoolIO.writeSmallTextFile(lines, RoleSystem.scoreFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String computerName = InetAddress.getLocalHost().getHostName();
        String token = CoolIO.getPageContents(new URL("http://10.0.0.8/aa/BotToken.php?code=" + computerName));

        System.out.println("Computer Name: " + computerName);
        System.out.println("Token: " + token);

        jda = new JDABuilder(AccountType.BOT).setToken(token).build();

        jda.getPresence().setActivity(Activity.listening("$help"));

        jda.addEventListener(new SystemMessages());
        jda.addEventListener(new Commands());
        jda.addEventListener(new RoleSystem());

        //Guild server = jda.getGuilds().get(0);
        //Console.WriteLine("Server " + server.getName() + ":" + server.getId() + "currently has " + server.getMembers().size() + " members.");

        //Console.WriteLine(jda.getGuilds().size());

        Commands.g = jda.getGuildById("630797688253972511"); /*Get discord server Snaekyy*/

        RoleSystem.MsgThread();
        //MemberCount.MemberCountThread();
    }
}
