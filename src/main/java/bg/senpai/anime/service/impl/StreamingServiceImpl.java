package bg.senpai.anime.service.impl;

import bg.senpai.anime.service.SessionProcessManager;
import bg.senpai.anime.service.StreamingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class StreamingServiceImpl implements StreamingService {
    private static final int CHUNK_SIZE = 1024 * 1024 * 10; // 10 MB chunk
    private final SessionProcessManager sessionProcessManager;

    @Override
    public void streamVideo(String vidName, HttpServletRequest request, HttpServletResponse response) {
        if (vidName == null || vidName.isBlank()) {
            try {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Missing parameter: vidName");
            } catch (IOException ignored) {}
            return;
        }

        Path videoPath = Paths.get(System.getProperty("user.dir"), "videos", vidName + ".mp4");

        if (!Files.exists(videoPath)) {
            try {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Video not found");
            } catch (IOException ignored) {}
            return;
        }

        try {
            long fileSize = Files.size(videoPath);
            String range = request.getHeader("Range");

            long start = 0;
            long end = fileSize - 1;

            // -----------------------------
            // RANGE PARSING
            // -----------------------------
            if (range != null && range.startsWith("bytes=")) {
                String[] parts = range.substring(6).split("-");

                start = Long.parseLong(parts[0]);

                if (parts.length > 1 && !parts[1].isEmpty()) {
                    end = Long.parseLong(parts[1]);
                } else {
                    end = Math.min(start + CHUNK_SIZE - 1, fileSize - 1);
                }
            }

            long contentLength = end - start + 1;

            // -----------------------------
            // RESPONSE HEADERS
            // -----------------------------
            response.setStatus(range != null ? HttpServletResponse.SC_PARTIAL_CONTENT : HttpServletResponse.SC_OK);
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Content-Type", "video/mp4");
            response.setHeader("Content-Length", String.valueOf(contentLength));
            response.setHeader("Content-Range", String.format("bytes %d-%d/%d", start, end, fileSize));


            try (
                    RandomAccessFile raf = new RandomAccessFile(videoPath.toFile(), "r");
                    OutputStream out = response.getOutputStream()
            ) {
                raf.seek(start);

                byte[] buffer = new byte[8192];
                long bytesLeft = contentLength;

                while (bytesLeft > 0) {
                    int bytesToRead = (int) Math.min(buffer.length, bytesLeft);
                    int bytesRead = raf.read(buffer, 0, bytesToRead);

                    if (bytesRead == -1) break;

                    try {
                        out.write(buffer, 0, bytesRead);
                        out.flush();
                    } catch (IOException clientDisconnect) {
                        System.out.println("⚠️ Client disconnected from stream → Killing session: " + vidName);
                        return;
                    }

                    bytesLeft -= bytesRead;
                }
            }

        } catch (Exception e) {
            System.out.println("❌ StreamVideo Exception: " + e.getMessage());
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Error streaming video.");
            } catch (IOException ignored) {}
        }
    }


}
