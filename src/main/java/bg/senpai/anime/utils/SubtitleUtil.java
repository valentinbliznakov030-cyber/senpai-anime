package bg.senpai.anime.utils;

import bg.senpai.anime.client.TranslationClient;
import bg.senpai.anime.exception.SubtitlesNotFoundException;
import bg.senpai.anime.exception.SubtitlesTranslationException;
import bg.senpai.anime.service.SessionProcessManager;
import bg.senpai.anime.tasks.SessionTask;
import bg.senpai.common.dtos.SubtitlesDownloadRequestDTO;
import bg.senpai.common.dtos.TranslateSubtitleRequestDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
public class SubtitleUtil {

    private static final Logger logger = LoggerFactory.getLogger(SubtitleUtil.class);

    private final SessionProcessManager sessionProcessManager;
    private final TranslationClient translationClient;

    public Path downloadSubtitle(SubtitlesDownloadRequestDTO dto, String sessionId)
            throws InterruptedException, ExecutionException {

        SessionTask sessionTask = sessionProcessManager.getSession(sessionId);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Path> future = executor.submit(() -> {

            URLConnection connection = null;

            String subtitleName = sessionId;
            Path subsDir = Paths.get(System.getProperty("user.dir"), "subtitles");

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            String subLink = dto.getSubtitleUrl();
            Path subPath = subsDir.resolve(subtitleName + ".vtt");

            try {
                URL url = new URL(subLink);
                connection = url.openConnection();

                if (connection instanceof HttpURLConnection httpConnection) {
                    httpConnection.setRequestProperty("Referer", "https://hianime.to");
                    httpConnection.setRequestProperty("Origin", "https://hianime.to");
                    httpConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
                }

                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Interrupted before download");
                }

                try (InputStream in = connection.getInputStream();
                     FileOutputStream out = new FileOutputStream(subPath.toFile())) {

                    byte[] buffer = new byte[8192];
                    int len;

                    while ((len = in.read(buffer)) != -1) {

                        if (Thread.currentThread().isInterrupted()) {
                            logger.warn("Interrupt: closing subtitle stream...");

                            if (connection instanceof HttpURLConnection httpConnection) {
                                httpConnection.disconnect();
                            }

                            throw new InterruptedException("Download interrupted");
                        }

                        out.write(buffer, 0, len);
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException("IO Error during processing or download.", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Task interrupted.", e);
            } finally {

                if (connection instanceof HttpURLConnection httpConnection) {
                    httpConnection.disconnect();
                }
            }

            return subPath;
        });

        sessionTask.addFuture(future);

        return future.get();
    }

    public Path translateSubtitle(TranslateSubtitleRequestDto request, String sessionId) {

        SessionTask sessionTask = sessionProcessManager.getSession(sessionId);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Path> future = executor.submit(() -> {

            String subtitleName = request.getSubtitleName();
            logger.debug("From method translateSubtitle: {}", subtitleName);

            Path inputPath = Paths.get("subtitles", subtitleName + ".vtt");

            if (Files.exists(inputPath)) {
                logger.debug("Subtitle file exists: {}", inputPath);
            }

            String outputName = subtitleName + "-bg.vtt";
            Path outputPath = Paths.get("subtitles", outputName);
            logger.debug("Output path: {}", outputPath);

            try (
                    BufferedReader br = new BufferedReader(new FileReader(inputPath.toFile()));
                    BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath.toFile(), false))
            ) {
                String firstLine = br.readLine();

                if (firstLine != null && !firstLine.startsWith("WEBVTT")) {
                    bw.write("WEBVTT");
                    bw.newLine();
                    bw.newLine();
                }

                if (firstLine != null) {
                    processAndWriteLine(firstLine, bw);
                }

                String currentLine;
                while ((currentLine = br.readLine()) != null) {

                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Translation interrupted in loop");
                    }

                    processAndWriteLine(currentLine, bw);
                }
            } catch (IOException e) {
                logger.error("I/O Error during subtitles translation processing: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to process subtitle files", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("The translation process was interrupted.", e);
            }

            return outputPath;
        });

        sessionTask.addFuture(future);

        try {
            return future.get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();

            throw new SubtitlesNotFoundException(
                    "Translation failed: " + (cause != null ? cause.getMessage() : "Unknown error"),
                    cause
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SubtitlesTranslationException("The translation process was interrupted.", e);
        }
    }

    private void processAndWriteLine(String line, BufferedWriter bw)
            throws IOException, InterruptedException {

        if (line.trim().isEmpty()
                || line.matches("^[0-9]+$")
                || line.matches("^\\d{2}:\\d{2}:\\d{2}[.,]?\\d{2,3}\\s*-->\\s*\\d{2}:\\d{2}:\\d{2}[.,]?\\d{2,3}$")) {

            bw.write(line);
            bw.newLine();
            return;
        }

        String safeLine = line.replaceAll("(\\d{2}:\\d{2}:\\d{2})[ ,](\\d{2,3})", "$1.$2");
        String translatedText = translateLine(safeLine);

        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Translation interrupted after request");
        }

        bw.write(translatedText);
        bw.newLine();

        logger.debug("Translated text: {}", translatedText);
    }

    private String translateLine(String text) throws InterruptedException {

        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Interrupted before translation");
        }

        try {
            String translated = translationClient.translate(text);
            return translated != null ? translated : text;

        } catch (Exception e) {

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Interrupted during translation");
            }

            logger.error("Translation error: {}", e.getMessage(), e);
            return text;
        }
    }
}