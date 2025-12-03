package bg.senpai.anime.serviceTests;

import bg.senpai.anime.client.NodeClient;
import bg.senpai.anime.exception.M3U8LinkNotFoundException;
import bg.senpai.anime.exception.VideoNotCreatedException;
import bg.senpai.anime.service.SessionProcessManager;
import bg.senpai.anime.service.impl.AnimeServiceImpl;
import bg.senpai.anime.utils.M3U8Fetcher;
import bg.senpai.anime.utils.VideoConverter;
import bg.senpai.common.dtos.VideoCreationRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnimeServiceImplTest {

    @Mock
    private NodeClient nodeClient;

    @Mock
    private M3U8Fetcher m3u8Fetcher;

    @Mock
    private VideoConverter videoConverter;

    @Mock
    private SessionProcessManager sessionProcessManager;

    @InjectMocks
    private AnimeServiceImpl animeService;

    @Test
    void whenGetM3U8LinkSuccessfully_ThenReturnLink() {
        String animeUrl = "https://hianime.to/watch/demon-slayer/episode-1";
        String sessionId = "test-session-123";
        String expectedM3U8Link = "https://example.com/video.m3u8";

        when(m3u8Fetcher.fetchM3U8Link(anyString(), anyString())).thenReturn(expectedM3U8Link);

        String result = animeService.getM3U8Link(animeUrl, sessionId);

        assertThat(result).isEqualTo(expectedM3U8Link);
        verify(m3u8Fetcher).fetchM3U8Link(animeUrl, sessionId);
    }

    @Test
    void whenM3U8LinkNotFound_ThenThrowM3U8LinkNotFoundException() {
        String animeUrl = "https://hianime.to/watch/invalid-anime/episode-1";
        String sessionId = "test-session-123";

        when(m3u8Fetcher.fetchM3U8Link(anyString(), anyString())).thenReturn(null);

        assertThatThrownBy(() -> animeService.getM3U8Link(animeUrl, sessionId))
                .isInstanceOf(M3U8LinkNotFoundException.class)
                .hasMessageContaining("M3u8Link not found");

        verify(m3u8Fetcher).fetchM3U8Link(animeUrl, sessionId);
    }

    @Test
    void whenCreateVideoSuccessfully_ThenNoExceptionThrown() throws Exception {
        String m3u8Link = "https://example.com/video.m3u8";
        String vidName = "demon-slayer-episode-1";
        String sessionId = "test-session-123";

        VideoCreationRequestDto dto = VideoCreationRequestDto.builder()
                .m3u8Link(m3u8Link)
                .vidName(vidName)
                .build();

        // Create a temporary video file for testing
        Path videosDir = Paths.get(System.getProperty("user.dir"), "videos");
        Files.createDirectories(videosDir);
        Path videoPath = videosDir.resolve(sessionId + ".mp4");
        Files.createFile(videoPath);

        try {
            doNothing().when(videoConverter).convertVideoFromM3U8Link(anyString(), anyString());

            animeService.createVideo(dto, sessionId);

            verify(videoConverter).convertVideoFromM3U8Link(m3u8Link, vidName);
        } finally {
            // Cleanup
            Files.deleteIfExists(videoPath);
        }
    }

    @Test
    void whenVideoNotCreated_ThenThrowVideoNotCreatedException() {
        String m3u8Link = "https://example.com/video.m3u8";
        String vidName = "demon-slayer-episode-1";
        String sessionId = "test-session-123";

        VideoCreationRequestDto dto = VideoCreationRequestDto.builder()
                .m3u8Link(m3u8Link)
                .vidName(vidName)
                .build();

        doNothing().when(videoConverter).convertVideoFromM3U8Link(anyString(), anyString());

        // Video file doesn't exist
        assertThatThrownBy(() -> animeService.createVideo(dto, sessionId))
                .isInstanceOf(VideoNotCreatedException.class);

        verify(videoConverter).convertVideoFromM3U8Link(m3u8Link, vidName);
    }
}

