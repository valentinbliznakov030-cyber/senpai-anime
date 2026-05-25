package bg.senpai.anime.utils;

import bg.senpai.anime.client.TranslationClient;
import bg.senpai.anime.service.SessionProcessManager;
import bg.senpai.anime.tasks.SessionTask;
import bg.senpai.common.dtos.SubtitlesDownloadRequestDTO;
import bg.senpai.common.dtos.TranslateSubtitleRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubtitleUtilTest {

    @Mock
    private SessionProcessManager sessionProcessManager;

    @Mock
    private SessionTask sessionTask;

    @Mock
    private TranslationClient translationClient;

    @InjectMocks
    private SubtitleUtil subtitleUtil;

    private String sampleSessionId;
    private File tempSourceFile;

    @BeforeEach
    void setUp() throws Exception {
        sampleSessionId = "test-session-" + UUID.randomUUID();

        tempSourceFile = File.createTempFile("remote-subs-sample", ".vtt");
        try (FileWriter writer = new FileWriter(tempSourceFile)) {
            writer.write("WEBVTT\n\n1\n00:01:00.000 --> 00:01:05.000\nHello World");
        }

        Files.createDirectories(Paths.get(System.getProperty("user.dir"), "subtitles"));
    }


    @Test
    void downloadSubtitle_ShouldDownloadFileSuccessfully() throws Exception {
        SubtitlesDownloadRequestDTO dto = new SubtitlesDownloadRequestDTO();
        dto.setSubtitleUrl(tempSourceFile.toURI().toURL().toString());

        when(sessionProcessManager.getSession(sampleSessionId)).thenReturn(sessionTask);

        Path resultPath = subtitleUtil.downloadSubtitle(dto, sampleSessionId);

        assertNotNull(resultPath);
        assertTrue(Files.exists(resultPath));
        assertEquals(sampleSessionId + ".vtt", resultPath.getFileName().toString());

        verify(sessionTask, times(1)).addFuture(any());
    }

    @Test
    void translateSubtitle_ShouldTranslateFileSuccessfully() throws Exception {
        TranslateSubtitleRequestDto dto = new TranslateSubtitleRequestDto();
        dto.setSubtitleName(sampleSessionId);

        Path inputPath = Paths.get("subtitles", sampleSessionId + ".vtt");
        Files.writeString(inputPath, "Hello world");

        when(sessionProcessManager.getSession(sampleSessionId)).thenReturn(sessionTask);
        when(translationClient.translate("Hello world")).thenReturn("Здравей свят");

        Path result = subtitleUtil.translateSubtitle(dto, sampleSessionId);

        assertNotNull(result);
        assertTrue(Files.exists(result));
        assertEquals(sampleSessionId + "-bg.vtt", result.getFileName().toString());

        String translatedContent = Files.readString(result);
        assertTrue(translatedContent.contains("Здравей свят"));

        verify(sessionTask, times(1)).addFuture(any());
    }
}