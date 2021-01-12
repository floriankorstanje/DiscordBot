package com.flornian;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class IO {
    public static List<String> readSmallTextFile(String fileName) throws IOException {
        Path path = Paths.get(fileName);
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    public static void writeSmallTextFile(List<String> lines, String fileName) throws IOException {
        Path path = Paths.get(fileName);
        Files.write(path, lines, StandardCharsets.UTF_8);
    }
}
