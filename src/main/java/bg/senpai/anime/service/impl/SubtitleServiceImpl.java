package bg.senpai.anime.service.impl;

import bg.senpai.anime.client.StreamApiClient;
import bg.senpai.anime.exception.SubtitlesNotFoundException;
import bg.senpai.anime.service.SubtitleService;
import bg.senpai.anime.utils.FuzzyMatcher;
import bg.senpai.common.dtos.SubtitlesDownloadRequestDto;
import bg.senpai.common.dtos.SubtitlesDownloadedResponseDto;
import bg.senpai.common.dtos.TranslateSubtitleRequestDto;
import bg.senpai.common.dtos.TranslationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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


@Service
@RequiredArgsConstructor
public class SubtitleServiceImpl implements SubtitleService{
    private final StreamApiClient streamApiClient;
    private final WebClient webClient = WebClient.create("http://localhost:5000");
    private final ObjectMapper objectMapper;
    @Override
    public SubtitlesDownloadedResponseDto downloadSubtitles(SubtitlesDownloadRequestDto dto) {
        SubtitlesDownloadedResponseDto response = new SubtitlesDownloadedResponseDto();

        try {
            String animeTitle = dto.getAnimeTitle();
            String subtitleName = dto.getSubtitleName();
            int episodeNumber = dto.getEpisodeNumber();

            // 🗂 Създаваме директория subtitles/
            Path subsDir = Paths.get(System.getProperty("user.dir"), "subtitles");
            Files.createDirectories(subsDir);

            System.out.println("🔍 Searching anime: " + animeTitle);

            // 1️⃣ Търсене на аниме по име
            Map<String, Object> animeResponse = streamApiClient.searchAnime(animeTitle, 1);
            Map<String, Object> data = (Map<String, Object>) animeResponse.get("data");
            List<Map<String, Object>> results = (List<Map<String, Object>>) data.get("response");

            if (results == null || results.isEmpty())
                throw new RuntimeException("Anime not found");

            // 2️⃣ Избираме най-добро съвпадение чрез FuzzyMatcher
            Map<String, Object> bestMatch = FuzzyMatcher.findBestMatch(results, animeTitle);
            String animeId = bestMatch.get("id").toString();

            System.out.println("✅ Best match ID: " + animeId);

            // 3️⃣ Вземаме епизодите
            Map<String, Object> episodesData = streamApiClient.getEpisodes(animeId);
            List<Map<String, Object>> episodes = (List<Map<String, Object>>) episodesData.get("data");

            if (episodes == null || episodes.isEmpty())
                throw new RuntimeException("Episodes not found");

            Map<String, Object> selectedEpisode = episodes.get(episodeNumber - 1);
            String episodeId = selectedEpisode.get("id").toString();

            System.out.println("🎯 Episode ID: " + episodeId);

            // 4️⃣ Взимаме стрийм данни
            Map<String, Object> streamData = streamApiClient.getStreamData(episodeId, "sub", "hd-2");
            List<Map<String, Object>> tracks = (List<Map<String, Object>>) ((Map<String, Object>) streamData.get("data")).get("tracks");

            if (tracks == null || tracks.isEmpty())
                throw new RuntimeException("No subtitles found");

            // 5️⃣ Търсим English субтитри
            Map<String, Object> englishSub = tracks.stream()
                    .filter(t -> "English".equals(t.get("label")))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("English subtitles not found"));

            String subLink = englishSub.get("file").toString();
            System.out.println("🧩 Subtitle link: " + subLink);

            // 6️⃣ Теглим субтитрите
            Path subPath = subsDir.resolve(subtitleName + ".vtt");

            try {
                URL url = new URL(subLink);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestProperty("Referer", "https://hianime.to");
                connection.setRequestProperty("Origin", "https://hianime.to");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

                connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                connection.setRequestProperty("Connection", "keep-alive");

                connection.setInstanceFollowRedirects(true);

                try (InputStream in = connection.getInputStream();
                     FileOutputStream out = new FileOutputStream(subPath.toFile())) {
                    in.transferTo(out);
                }

                // Проверка на response code
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    System.out.println("HTTP код: " + responseCode);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("✅ Subtitles saved: " + subPath);

            response.setSuccess(true);
            response.setStatusCode(201);
            response.setMessage("Downloaded subtitles successfully");
            response.setSubtitleName(subtitleName);

        } catch (Exception e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setStatusCode(500);
            response.setMessage("Error: " + e.getMessage());
            response.setSubtitleName("");
            throw new SubtitlesNotFoundException(e.getMessage());
        }

        return response;
    }

    @Override
    public String translateSubtitle(TranslateSubtitleRequestDto request) {
        try {
            String subtitleName = request.getSubtitleName();
            Path inputPath = Paths.get("subtitles", subtitleName);
            System.out.println(inputPath.toString());
            String outputName = subtitleName.replace(".vtt", "-bg.vtt");
            Path outputPath = Paths.get("subtitles", outputName);

            List<String> lines = Files.readAllLines(inputPath);
            List<String> translatedLines = new ArrayList<>();

            for (String line : lines) {
                // Пропуска тайминги, номера и празни редове
                if (line.trim().isEmpty()
                        || line.matches("^[0-9]+$")
                        || line.matches("^\\d{2}:\\d{2}:\\d{2}[.,]?\\d{2,3}\\s*-->\\s*\\d{2}:\\d{2}:\\d{2}[.,]?\\d{2,3}$")) {
                    translatedLines.add(line);
                    continue;
                }

                String safeLine = line.replaceAll("(\\d{2}:\\d{2}:\\d{2})[ ,](\\d{2,3})", "$1.$2");
                String translatedText = translateLine(safeLine);
                translatedLines.add(translatedText);
            }

            // Добавяме WEBVTT в началото, ако липсва
            if (!translatedLines.get(0).startsWith("WEBVTT")) {
                translatedLines.add(0, "WEBVTT");
                translatedLines.add(1, "");
            }

            Files.write(outputPath, translatedLines, StandardCharsets.UTF_8);
            return outputName;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String translateLine(String text) {
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

            // Проверка за null, ако блокне или заявката е неуспешна
            if (responseDto != null && responseDto.getTranslatedText() != null) {
                // Връщаш преведения текст чрез getter
                return responseDto.getTranslatedText();
            } else {
                // Ако парсването е неуспешно, връщаш оригиналния текст
                return text;
            }

        } catch (Exception e) {
            System.out.println("⚠️ Translation error: " + e.getMessage());
            return text;
        }
    }
}
