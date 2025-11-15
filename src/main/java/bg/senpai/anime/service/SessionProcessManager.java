package bg.senpai.anime.service;

import bg.senpai.anime.tasks.SessionTask;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@Service
public class SessionProcessManager {
    private final Map<String, SessionTask> activeSessions = new ConcurrentHashMap<>();

    public String createOrGetSession(String sessionId) {
        if (activeSessions.containsKey(sessionId)) return sessionId;

        activeSessions.put(sessionId, new SessionTask(sessionId));
        return sessionId;
    }

    public SessionTask getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

    public void registerProcess(String sessionId, Process process) {
        SessionTask task = activeSessions.get(sessionId);
        if (task != null) {
            task.addProcess(process);
        }
    }


    public void registerFuture(String sessionId, Future<?> future) {
        SessionTask task = activeSessions.get(sessionId);
        if (task != null) {
            task.addFuture(future);
        }
    }

    public void killSession(String sessionId) {
        SessionTask task = activeSessions.get(sessionId);

        if (task != null) {
            task.cancel();      // убива процеси + futures
            cleanup(sessionId); // трие файловете
            activeSessions.remove(sessionId);
        }
    }

    public void cleanup(String sessionId) {
        Path downloadedSub = Paths.get(System.getProperty("user.dir"), "subtitles", sessionId + ".vtt");
        System.out.println(downloadedSub.toString());
        Path translatedSub = Paths.get(System.getProperty("user.dir"), "subtitles", sessionId + "-bg.vtt");
        System.out.println(translatedSub.toString());
        Path downloadedVideo = Paths.get(System.getProperty("user.dir"), "videos", sessionId + ".mp4");
        System.out.println(downloadedVideo.toString());

        try {
            boolean deletedDownloadedSub = Files.deleteIfExists(downloadedSub);
            if (deletedDownloadedSub) {
                System.out.println("Изтрит: " + downloadedSub.getFileName());
            }

            boolean deletedTranslatedSub = Files.deleteIfExists(translatedSub);
            if (deletedTranslatedSub) {
                System.out.println("Изтрит: " + translatedSub.getFileName());
            }

            boolean deletedDownloadedVideo = Files.deleteIfExists(downloadedVideo);
            if (deletedDownloadedVideo) {
                System.out.println("Изтрит: " + downloadedVideo.getFileName());
            }

        } catch (IOException e) {
            System.err.println("❌ Критична грешка при изтриване на файл: " + e.getMessage());
        }
    }

}
