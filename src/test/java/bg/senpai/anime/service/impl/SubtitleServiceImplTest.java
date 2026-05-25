package bg.senpai.anime.service.impl;

import bg.senpai.anime.exception.SubtitlesNotFoundException;
import bg.senpai.anime.exception.SubtitlesTranslationException;
import bg.senpai.anime.utils.SubtitleUtil;
import bg.senpai.common.dtos.SubtitlesDownloadRequestDTO;
import bg.senpai.common.dtos.TranslateSubtitleRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubtitleServiceImplTest {

    @Mock
    private SubtitleUtil subtitleUtil;

    @InjectMocks
    private SubtitleServiceImpl subtitleService;

    private String sampleSessionId;
    private Path mockPath;

    @BeforeEach
    void setUp() {
        sampleSessionId = "session-xyz-123";
        mockPath = Paths.get("subtitles", sampleSessionId + ".vtt");
    }

    @Test
    void downloadSubtitles_ShouldReturnFileName_WhenFileExists() throws Exception {
        SubtitlesDownloadRequestDTO dto = new SubtitlesDownloadRequestDTO();
        dto.setSubtitleUrl("https://hianime.to/sub.vtt");

        when(subtitleUtil.downloadSubtitle(dto, sampleSessionId)).thenReturn(mockPath);

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(mockPath)).thenReturn(true);

            String result = subtitleService.downloadSubtitles(dto, sampleSessionId);

            assertNotNull(result);
            assertEquals(sampleSessionId + ".vtt", result);
            verify(subtitleUtil, times(1)).downloadSubtitle(dto, sampleSessionId);
        }
    }

    @Test
    void downloadSubtitles_ShouldThrowSubtitlesNotFoundException_WhenFileDoesNotExist() throws Exception {
        SubtitlesDownloadRequestDTO dto = new SubtitlesDownloadRequestDTO();
        dto.setSubtitleUrl("https://hianime.to/sub.vtt");

        when(subtitleUtil.downloadSubtitle(dto, sampleSessionId)).thenReturn(mockPath);

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(mockPath)).thenReturn(false);

            SubtitlesNotFoundException exception = assertThrows(SubtitlesNotFoundException.class, () -> {
                subtitleService.downloadSubtitles(dto, sampleSessionId);
            });

            assertEquals("Subtitle not found", exception.getMessage());
        }
    }


    @Test
    void translateSubtitle_ShouldReturnTranslatedFileName_WhenFileExists() {
        TranslateSubtitleRequestDto dto = new TranslateSubtitleRequestDto();
        dto.setSubtitleName(sampleSessionId);

        Path translatedMockPath = Paths.get("subtitles", sampleSessionId + "-bg.vtt");

        when(subtitleUtil.translateSubtitle(dto, sampleSessionId)).thenReturn(translatedMockPath);

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(translatedMockPath)).thenReturn(true);

            String result = subtitleService.translateSubtitle(dto, sampleSessionId);

            assertNotNull(result);
            assertEquals(sampleSessionId + "-bg.vtt", result);
            verify(subtitleUtil, times(1)).translateSubtitle(dto, sampleSessionId);
        }
    }

    @Test
    void translateSubtitle_ShouldThrowSubtitlesTranslationException_WhenFileDoesNotExist() {
        TranslateSubtitleRequestDto dto = new TranslateSubtitleRequestDto();
        dto.setSubtitleName(sampleSessionId);

        Path translatedMockPath = Paths.get("subtitles", sampleSessionId + "-bg.vtt");

        when(subtitleUtil.translateSubtitle(dto, sampleSessionId)).thenReturn(translatedMockPath);

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(translatedMockPath)).thenReturn(false);

            SubtitlesTranslationException exception = assertThrows(SubtitlesTranslationException.class, () -> {
                subtitleService.translateSubtitle(dto, sampleSessionId);
            });

            assertEquals("Translated subtitle fil not found", exception.getMessage());
        }
    }
}