package bg.senpai.anime.utils;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class VideoConverter {
    public boolean convertVideoFromM3U8Link(String m3u8Link, String vidName) {
        System.out.println("🎬 Starting video conversion for: " + vidName);
        boolean success = false;

        try {
            // 🗂 Създаваме папка "videos" ако не съществува
            Path videosDir = Paths.get(System.getProperty("user.dir"), "videos");
            Files.createDirectories(videosDir);

            // 🔗 Подготвяме командата
            String decodedLink = java.net.URLDecoder.decode(m3u8Link, "UTF-8");
            String exePath = "N_m3u8DL-RE.exe"; // увери се, че е в същата директория като jar-а
            Path outputPath = videosDir.resolve(vidName + ".mp4");

            List<String> command = List.of(
                    exePath,
                    decodedLink,
                    "--save-dir", videosDir.toString(),
                    "--save-name", vidName,
                    "-H", "Referer: https://animepahe.si",
                    "--auto-select",
                    "--del-after-done"
            );

            System.out.println("▶️ Executing: " + String.join(" ", command));

            // 🚀 Стартираме процеса
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // 🧾 Четем output (stdout)
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();

            if (exitCode == 0 && Files.exists(outputPath)) {
                System.out.println("✅ Video successfully downloaded at: " + outputPath);
                success = true;
            } else {
                System.out.println("❌ Process failed with exit code: " + exitCode);
            }

        } catch (Exception e) {
            System.out.println("💥 Error during video conversion: " + e.getMessage());
        }

        return success;

    }
}
