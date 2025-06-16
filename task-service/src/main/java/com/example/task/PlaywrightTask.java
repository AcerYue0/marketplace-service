package com.example.task;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class PlaywrightTask {
    public void runTask() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("node", "automation/fetch_with_playwright.js");
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while((line =reader.readLine())!=null) {
            // TODO
        }
    }
}
