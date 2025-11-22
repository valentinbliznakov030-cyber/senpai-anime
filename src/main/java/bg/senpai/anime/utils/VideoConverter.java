package bg.senpai.anime.utils;

import bg.senpai.anime.exception.SubtitlesNotFoundException;
import bg.senpai.anime.exception.VideoNotCreatedException;
import bg.senpai.anime.service.SessionProcessManager;
import bg.senpai.anime.tasks.SessionTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class VideoConverter {
    private final SessionProcessManager sessionProcessManager;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public void convertVideoFromM3U8Link(String m3u8Link, String sessionId) {
        System.out.println("Starting video conversion for: " + sessionId);

        SessionTask sessionTask = sessionProcessManager.getSession(sessionId);

        try {
            Path videosDir = Paths.get(System.getProperty("user.dir"), "videos");
            Files.createDirectories(videosDir);

            String exePath = "N_m3u8DL-RE.exe";
            Path outputPath = videosDir.resolve(sessionId + ".mp4");

            List<String> command = List.of(
                    exePath,
                    m3u8Link,
                    "--save-dir", videosDir.toString(),
                    "--save-name", sessionId,
                    "-H", "Referer: https://animepahe.si",
                    "--auto-select",
                    "--del-after-done"
            );

            System.out.println("▶Executing: " + String.join(" ", command));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            sessionTask.addProcess(process);

            AtomicInteger exitCode = new AtomicInteger(-1);

            Future<?> future = executor.submit(() -> {
                try {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()))) {

                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println("[VIDEO] " + line);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Error reading process output stream", e);
                } finally {
                    try {
                        exitCode.set(process.waitFor());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        // Хвърляме RuntimeException, за да уведомим future.get() за неуспех
                        throw new RuntimeException("Process wait interrupted", e);
                    }
                }
            });

            sessionTask.addFuture(future);

            future.get();

            if (exitCode.get() == 0 && Files.exists(outputPath)) {
                System.out.println("Video downloaded at: " + outputPath);
            } else {
                System.out.println("Process failed with exit code: " + exitCode.get());
            }

        }catch(ExecutionException e){
            Throwable cause = e.getCause();

            throw new VideoNotCreatedException(
                    "Video creation failed: " + (cause != null ? cause.getMessage() : "Unknown error"),
                    cause
            );
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VideoNotCreatedException("Video creation was interrupted");
        }catch (Exception e) {
            System.out.println("Error during video conversion: " + e.getMessage());
        }

    }
}
