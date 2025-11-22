package bg.senpai.anime.service.impl;

import bg.senpai.anime.client.StreamApiClient;
import bg.senpai.anime.exception.SubtitlesTranslationException;
import bg.senpai.anime.exception.SubtitlesNotFoundException;
import bg.senpai.anime.service.SessionProcessManager;
import bg.senpai.anime.service.SubtitleService;
import bg.senpai.anime.tasks.SessionTask;
import bg.senpai.anime.utils.FuzzyMatcher;
import bg.senpai.common.dtos.SubtitlesDownloadRequestDto;
import bg.senpai.common.dtos.TranslateSubtitleRequestDto;
import bg.senpai.common.dtos.TranslationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class SubtitleServiceImpl implements SubtitleService{
    private static final Logger logger = LoggerFactory.getLogger(SubtitleServiceImpl.class);
    private final StreamApiClient streamApiClient;
    private final WebClient webClient = WebClient.create("http://localhost:5000");
    private final ObjectMapper objectMapper;
    private final SessionProcessManager sessionProcessManager;

    @Override
    public void downloadSubtitles(SubtitlesDownloadRequestDto dto, String sessionId) throws ExecutionException, InterruptedException {

        SessionTask sessionTask = sessionProcessManager.getSession(sessionId);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<?> future = executor.submit(() -> {

            HttpURLConnection connection = null;

            try {
                String subtitleName = dto.getSubtitleName();

                Path subsDir = Paths.get(System.getProperty("user.dir"), "subtitles");

                if (Thread.currentThread().isInterrupted()) throw new InterruptedException();

                String subLink = dto.getSubtitleUrl();

                Path subPath = subsDir.resolve(subtitleName + ".vtt");

                URL url = new URL(subLink);
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestProperty("Referer", "https://hianime.to");
                connection.setRequestProperty("Origin", "https://hianime.to");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");

                if (Thread.currentThread().isInterrupted())
                    throw new InterruptedException("Interrupted before download");

                try (InputStream in = connection.getInputStream();
                     FileOutputStream out = new FileOutputStream(subPath.toFile())) {

                    byte[] buffer = new byte[8192];
                    int len;

                    while ((len = in.read(buffer)) != -1) {

                        if (Thread.currentThread().isInterrupted()) {
                            logger.warn("Interrupt: closing subtitle stream...");
                            in.close();
                            connection.disconnect();
                            throw new InterruptedException("Download interrupted");
                        }

                        out.write(buffer, 0, len);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("IO Error during processing or download.", e);
            }catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Task interrupted.", e);
            }finally {
                if (connection != null) connection.disconnect();
            }
        });

        sessionTask.addFuture(future);

        future.get();
    }

    @Override
    public String translateSubtitle(TranslateSubtitleRequestDto request, String sessionId) {

        SessionTask sessionTask = sessionProcessManager.getSession(sessionId);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<String> future = executor.submit(() -> {
            String subtitleName = request.getSubtitleName();
            logger.debug("From method translateSubtitle: {}", subtitleName);

            Path inputPath = Paths.get("subtitles", subtitleName + ".vtt");
            if(Files.exists(inputPath)){
                logger.debug("Subtitle file exists: {}", inputPath);
            }
            String outputName = subtitleName + "-bg.vtt";
            Path outputPath = Paths.get("subtitles", outputName);
            logger.debug("Output path: {}", outputPath);

            List<String> lines = Files.readAllLines(inputPath);
            List<String> translatedLines = new ArrayList<>();

            for (String line : lines) {

                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Translation interrupted in loop");
                }

                if (line.trim().isEmpty()
                        || line.matches("^[0-9]+$")
                        || line.matches("^\\d{2}:\\d{2}:\\d{2}[.,]?\\d{2,3}\\s*-->\\s*\\d{2}:\\d{2}:\\d{2}[.,]?\\d{2,3}$")) {

                    translatedLines.add(line);
                    continue;
                }

                String safeLine = line.replaceAll("(\\d{2}:\\d{2}:\\d{2})[ ,](\\d{2,3})", "$1.$2");
                String translatedText = translateLine(safeLine);

                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Translation interrupted after request");
                }

                translatedLines.add(translatedText);
                logger.debug("Translated text: {}", translatedText);
            }

            if (!translatedLines.get(0).startsWith("WEBVTT")) {
                translatedLines.add(0, "WEBVTT");
                translatedLines.add(1, "");
            }

            Files.write(outputPath, translatedLines, StandardCharsets.UTF_8);

            return outputName;
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

    private String translateLine(String text) throws InterruptedException {

        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Interrupted before translation");
        }

        try {
            Map<String, Object> body = Map.of(
                    "q", text,
                    "source", "en",
                    "target", "bg",
                    "format", "text"
            );

            TranslationResponse responseDto = webClient.post()
                    .uri("/translate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(TranslationResponse.class)
                    .block();

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Interrupted after translation");
            }

            if (responseDto != null && responseDto.getTranslatedText() != null) {
                return responseDto.getTranslatedText();
            }

            return text;

        }catch (WebClientException e) {
            throw e;
        }catch (Exception e) {

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Interrupted during translation");
            }

            logger.error("Translation error: {}", e.getMessage(), e);
            return text;
        }
    }
}
