package bg.senpai.anime.utils;

import bg.senpai.anime.exception.VideoNotCreatedException;
import bg.senpai.anime.service.SessionProcessManager;
import bg.senpai.anime.tasks.SessionTask;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
public class M3U8Fetcher {
    private static final Logger logger = LoggerFactory.getLogger(M3U8Fetcher.class);
    private final SessionProcessManager sessionProcessManager;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public String fetchM3U8Link(String episodeUrl, String sessionId) {
        logger.info("Getting m3u8 link for: {}", episodeUrl);

        SessionTask sessionTask = sessionProcessManager.getSession(sessionId);

        String nodePath = "node";

        String scriptPath = "m3u8fetcher.js";

        ProcessBuilder pb = new ProcessBuilder(
                nodePath,
                scriptPath,
                episodeUrl
        );

        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            sessionTask.addProcess(process);

            Future<String> future = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {

                    String line;
                    String result = null;

                    while ((line = reader.readLine()) != null) {
                        logger.debug("[PUPPETEER] {}", line);

                        if (line.startsWith("RESULT:")) {
                            result = line.substring("RESULT:".length());
                        }
                    }

                    if (result == null) {
                        throw new RuntimeException("M3U8 link not found in process output.");
                    }

                    return result;

                }
            });

            sessionTask.addFuture(future);

            return future.get(3, TimeUnit.MINUTES);

        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            logger.error("Execution failed while fetching m3u8: {}", cause != null ? cause.getMessage() : "Unknown error", e);

            throw new VideoNotCreatedException(
                    "Failed to execute M3U8 fetcher process.",
                    cause
            );

        } catch (TimeoutException te) {
            logger.warn("Timeout while fetching m3u8. Killing session.");
            sessionProcessManager.killSession(sessionId);

            throw new VideoNotCreatedException("M3U8 fetch timeout.", te);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VideoNotCreatedException("M3U8 fetch interrupted.", e);

        } catch (IOException e) {
            logger.error("Error starting Puppeteer process: {}", e.getMessage(), e);
            throw new VideoNotCreatedException("Error starting M3U8 fetcher process.", e);
        }
    }
}
