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

                // 5) DOWNLOAD
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

                        // ❗ ПЕРФЕКТНОТО МЯСТО ЗА ПРЕКЪСВАНЕ
                        if (Thread.currentThread().isInterrupted()) {
                            System.out.println("🛑 Interrupt: closing subtitle stream...");
                            in.close();        // прекъсва read()
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

        // submit worker thread
        Future<String> future = executor.submit(() -> {
            // ❗ Няма try-catch тук! Оставяме грешките да се разпространят.
            // InterruptedException, IOException, RuntimeException - всички ще бъдат
            // увити в ExecutionException от future.get()

            String subtitleName = request.getSubtitleName();
            System.out.println("From method trnaslteSubtitle:" + subtitleName);

            Path inputPath = Paths.get("subtitles", subtitleName + ".vtt");
            if(Files.exists(inputPath)){
                System.out.println("yes, it exists: " + inputPath.toString());//subtitles\4df43495-15fe-4c5e-ae8b-55afc781e17e_20251119_155218.vtt
            }
            String outputName = subtitleName + "-bg.vtt";
            Path outputPath = Paths.get("subtitles", outputName);
            System.out.println(outputPath.toString());

            List<String> lines = Files.readAllLines(inputPath);
            List<String> translatedLines = new ArrayList<>();

            for (String line : lines) {

                // ❗ Прекъсване точно в лупа
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Translation interrupted in loop");
                }

                // Пропуск на тайминги / номера / празни редове
                if (line.trim().isEmpty()
                        || line.matches("^[0-9]+$")
                        || line.matches("^\\d{2}:\\d{2}:\\d{2}[.,]?\\d{2,3}\\s*-->\\s*\\d{2}:\\d{2}:\\d{2}[.,]?\\d{2,3}$")) {

                    translatedLines.add(line);
                    continue;
                }

                // Превод
                String safeLine = line.replaceAll("(\\d{2}:\\d{2}:\\d{2})[ ,](\\d{2,3})", "$1.$2");
                String translatedText = translateLine(safeLine); // вътре също има interrupt check

                // ❗ Проверяваме interrupt след всяка заявка!
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Translation interrupted after request");
                }

                translatedLines.add(translatedText);
                System.out.println(translatedText);
            }

            // Добавя WEBVTT header ако липсва
            if (!translatedLines.get(0).startsWith("WEBVTT")) {
                translatedLines.add(0, "WEBVTT");
                translatedLines.add(1, "");
            }

            Files.write(outputPath, translatedLines, StandardCharsets.UTF_8);

            return outputName;
        }); // ❗ Край на submit без catch блокове

        // регистрираме във SessionTask, за да го kill-ваме
        sessionTask.addFuture(future);

        try {
            return future.get();
        } catch (ExecutionException e) {
            // 1. Хваща всички грешки, хвърлени вътре (IOException, RuntimeException, InterruptedException)
            Throwable cause = e.getCause();

            // Трансформираме грешката в нашето специфично изключение
            // (Може да създадете и SubtitlesTranslationException, ако искате по-голяма прецизност)
            throw new SubtitlesNotFoundException(
                    "Translation failed: " + (cause != null ? cause.getMessage() : "Unknown error"),
                    cause
            );
        } catch (InterruptedException e) {
            // 2. Хваща, ако нишката, която вика get(), е прекъсната
            Thread.currentThread().interrupt();
            throw new SubtitlesTranslationException("The translation process was interrupted.", e);
        }
    }


    private String translateLine(String text) throws InterruptedException {

        // ❗ Прекъсване ПРЕДИ да започнем
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
                    .block(); // ❗ Potentially blocking

            // ❗ Прекъсване СЛЕД block()
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

            System.out.println("⚠️ Translation error: " + e.getMessage());
            return text;
        }
    }

}
