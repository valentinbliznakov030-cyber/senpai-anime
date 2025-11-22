package bg.senpai.anime.service;

import bg.senpai.anime.tasks.SessionTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@Service
public class SessionProcessManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionProcessManager.class);
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
            task.cancel();
            cleanup(sessionId);
            activeSessions.remove(sessionId);
        }
    }

    public void cleanup(String sessionId) {
        Path downloadedSub = Paths.get(System.getProperty("user.dir"), "subtitles", sessionId + ".vtt");
        logger.debug("Cleaning up subtitle file: {}", downloadedSub);
        Path translatedSub = Paths.get(System.getProperty("user.dir"), "subtitles", sessionId + "-bg.vtt");
        logger.debug("Cleaning up translated subtitle file: {}", translatedSub);
        Path downloadedVideo = Paths.get(System.getProperty("user.dir"), "videos", sessionId + ".mp4");
        logger.debug("Cleaning up video file: {}", downloadedVideo);

        try {
            boolean deletedDownloadedSub = Files.deleteIfExists(downloadedSub);
            if (deletedDownloadedSub) {
                logger.info("Deleted: {}", downloadedSub.getFileName());
            }

            boolean deletedTranslatedSub = Files.deleteIfExists(translatedSub);
            if (deletedTranslatedSub) {
                logger.info("Deleted: {}", translatedSub.getFileName());
            }

            boolean deletedDownloadedVideo = Files.deleteIfExists(downloadedVideo);
            if (deletedDownloadedVideo) {
                logger.info("Deleted: {}", downloadedVideo.getFileName());
            }

        } catch (IOException e) {
            logger.error("Critical error deleting file: {}", e.getMessage(), e);
        }
    }
}
