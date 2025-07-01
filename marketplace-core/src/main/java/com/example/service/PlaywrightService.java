package com.example.service;

import com.microsoft.playwright.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class PlaywrightService {

    public String getCookiesAfterJsChallenge(String targetUrl) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            page.navigate(targetUrl);

            // 等待 JS challenge 結束（Cloudflare 5 秒、可自訂）
            page.waitForTimeout(7000); // 可微調

            // 拿 cookie

            return context.cookies().stream()
                .map(c -> c.name + "=" + c.value)
                .collect(Collectors.joining("; "));
        }
    }
}
