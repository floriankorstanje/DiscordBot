package com.florn;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class IO {
    public static List<String> readSmallTextFile(String fileName) throws IOException {
        Path path = Paths.get(fileName);
        return java.nio.file.Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    public static void writeSmallTextFile(List<String> lines, String fileName) throws IOException {
        Path path = Paths.get(fileName);
        java.nio.file.Files.write(path, lines, StandardCharsets.UTF_8);
    }

    public static String getPageContents(URL url) throws IOException {
        Scanner sc = new Scanner(url.openStream());
        StringBuffer sb = new StringBuffer();

        while(sc.hasNext()) {
            sb.append(sc.next());
        }

        String result = sb.toString();

        result = result.replaceAll("<[^>]*>", "");

        return result;
    }
}
