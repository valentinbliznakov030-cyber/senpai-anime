package bg.senpai.anime.utils;

import bg.senpai.anime.exception.VideoNotCreatedException;
import bg.senpai.anime.service.SessionProcessManager;
import bg.senpai.anime.tasks.SessionTask;
import com.microsoft.playwright.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
public class M3U8Fetcher {
    private final SessionProcessManager sessionProcessManager;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public String fetchM3U8Link(String episodeUrl, String sessionId) {
        System.out.println("🎥 Getting m3u8 link for: " + episodeUrl);

        SessionTask sessionTask = sessionProcessManager.getSession(sessionId);

        // Node executable (ако node е в PATH → просто "node")
        String nodePath = "node";

        // JS скрипта в root-а на проекта
        String scriptPath = "m3u8fetcher.js";

        ProcessBuilder pb = new ProcessBuilder(
                nodePath,
                scriptPath,
                episodeUrl
        );

        pb.redirectErrorStream(true);

        try {
            // Старт на Puppeteer процеса
            Process process = pb.start(); // <-- Може да хвърли IOException
            sessionTask.addProcess(process);

            // Future слуша stdout на процеса (Callable)
            Future<String> future = executor.submit(() -> {
                // Тъй като е Callable, IOException може да се разпространява.
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {

                    String line;
                    String result = null;

                    while ((line = reader.readLine()) != null) {
                        System.out.println("[PUPPETEER] " + line);

                        if (line.startsWith("RESULT:")) {
                            result = line.substring("RESULT:".length());
                        }
                    }

                    // 1. Проверка дали скриптът е намерил линк
                    if (result == null) {
                        // Хвърляме RuntimeException (ExecutionException ще го увие)
                        throw new RuntimeException("M3U8 link not found in process output.");
                    }

                    return result;

                } // ❗ IOException от BufferedReader ще се разпространи до future.get()
            });

            sessionTask.addFuture(future);

            return future.get(3, TimeUnit.MINUTES);

        } catch (ExecutionException e) {
            // 2. Хваща вътрешни грешки (напр. RuntimeException от result == null)
            Throwable cause = e.getCause();
            System.err.println("❌ Execution failed while fetching m3u8: " + (cause != null ? cause.getMessage() : "Unknown error"));

            throw new VideoNotCreatedException(
                    "Failed to execute M3U8 fetcher process.",
                    cause
            );

        } catch (TimeoutException te) {
            // 3. Хваща таймаут
            System.out.println("❌ Timeout while fetching m3u8. Killing session.");
            sessionProcessManager.killSession(sessionId);

            throw new VideoNotCreatedException("M3U8 fetch timeout.", te);

        } catch (InterruptedException e) {
            // 4. Хваща, ако нишката, която вика get(), е прекъсната
            Thread.currentThread().interrupt();
            throw new VideoNotCreatedException("M3U8 fetch interrupted.", e);

        } catch (IOException e) {
            // 5. Хваща грешки при стартиране на процеса (pb.start())
            System.err.println("💥 Error starting Puppeteer process: " + e.getMessage());
            throw new VideoNotCreatedException("Error starting M3U8 fetcher process.", e);
        }
    }
}
