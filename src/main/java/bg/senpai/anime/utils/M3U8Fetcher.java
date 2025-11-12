package bg.senpai.anime.utils;

import com.microsoft.playwright.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class M3U8Fetcher {

    public String fetchM3U8Link(String episodeUrl) {
        System.out.println("🎬 Fetching .m3u8 link for: " + episodeUrl);

        AtomicReference<String> m3u8Ref = new AtomicReference<>(null);

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setArgs(java.util.List.of("--no-sandbox", "--disable-setuid-sandbox"))
            );

            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                            + "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
            );

            Page page = context.newPage();

            // 🧠 антибот защита
            page.addInitScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
            page.addInitScript("window.open = () => null;");

            // 🎯 слушаме всички HTTP отговори
            page.onResponse(response -> {
                String url = response.url();
                if (url.endsWith(".m3u8")) {
                    System.out.println("✅ Found .m3u8: " + url);
                    m3u8Ref.set(url);
                }
            });

            // Навигация
            page.navigate(episodeUrl, new Page.NavigateOptions().setTimeout(30000));

            // Клик на бутона за зареждане
            try {
                page.waitForSelector(".click-to-load", new Page.WaitForSelectorOptions().setTimeout(15000));
                page.click(".click-to-load");
                System.out.println("🖱️ Clicked .click-to-load");
            } catch (Exception e) {
                System.out.println("⚠️ Няма бутон '.click-to-load' или вече е зареден.");
            }

            long start = System.currentTimeMillis();
            while (m3u8Ref.get() == null && System.currentTimeMillis() - start < 40000) {
                page.waitForTimeout(500);
            }

            browser.close();

        } catch (Exception e) {
            System.out.println("❌ Error while fetching m3u8 link: " + e.getMessage());
        }

        String m3u8Link = m3u8Ref.get();
        if (m3u8Link == null) {
            System.out.println("❌ No .m3u8 link detected!");
        }

        return m3u8Link;
    }
}
