package bg.senpai.anime.service.impl;

import bg.senpai.anime.dto.EpisodeCreateOrGetRequestDTO;
import bg.senpai.anime.entity.Anime;
import bg.senpai.anime.entity.Episode;
import bg.senpai.anime.exception.M3U8LinkNotFoundException;
import bg.senpai.anime.exception.VideoNotCreatedException;
import bg.senpai.anime.repository.EpisodeRepository;
import bg.senpai.anime.service.AnimeService;
import bg.senpai.anime.utils.M3U8Fetcher;
import bg.senpai.anime.utils.VideoConverter;
import bg.senpai.common.dtos.VideoCreationRequestDto;
import jakarta.persistence.EntityNotFoundException;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EpisodeServiceImplTest {

    @Mock
    private EpisodeRepository episodeRepository;
    @Mock
    private AnimeService animeService;
    @Mock
    private M3U8Fetcher m3u8Fetcher;
    @Mock
    private VideoConverter videoConverter;

    @InjectMocks
    private EpisodeServiceImpl episodeService;

    private UUID sampleEpisodeId;
    private UUID sampleAnimeId;
    private Anime sampleAnime;
    private Episode sampleEpisode;
    private String sampleSessionId;

    @BeforeEach
    void setUp() {
        sampleEpisodeId = UUID.randomUUID();
        sampleAnimeId = UUID.randomUUID();
        sampleSessionId = "session-123";

        sampleAnime = Anime.builder()
                .id(sampleAnimeId)
                .title("Naruto")
                .hiAnimeId("naruto-123")
                .build();

        sampleEpisode = Episode.builder()
                .id(sampleEpisodeId)
                .episodeNumber(1)
                .anime(sampleAnime)
                .m3u8Link("http://stream.link/master.m3u8")
                .build();
    }


    @Test
    void findById_ShouldReturnEpisode_WhenExists() {
        when(episodeRepository.findById(sampleEpisodeId)).thenReturn(Optional.of(sampleEpisode));

        Episode result = episodeService.findById(sampleEpisodeId);

        assertNotNull(result);
        assertEquals(sampleEpisodeId, result.getId());
    }

    @Test
    void findById_ShouldThrowEntityNotFoundException_WhenDoesNotExist() {
        when(episodeRepository.findById(sampleEpisodeId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> episodeService.findById(sampleEpisodeId));
    }

    @Test
    void createEpisode_ShouldSaveAndReturnEpisode_WhenSuccessful() {
        EpisodeCreateOrGetRequestDTO dto = EpisodeCreateOrGetRequestDTO.builder()
                .animeId(sampleAnimeId)
                .episodeNumber(1)
                .episodeUrl("https://hianime.to/watch/naruto-1")
                .build();

        when(animeService.findById(sampleAnimeId)).thenReturn(sampleAnime);
        when(m3u8Fetcher.fetchM3U8Link(dto.getEpisodeUrl(), sampleSessionId)).thenReturn("http://stream.link/master.m3u8");
        when(episodeRepository.save(any(Episode.class))).thenReturn(sampleEpisode);

        Episode result = episodeService.createEpisode(dto, sampleSessionId);

        assertNotNull(result);
        assertEquals("http://stream.link/master.m3u8", result.getM3u8Link());
        verify(episodeRepository, times(1)).save(any(Episode.class));
    }

    @Test
    void createEpisode_ShouldThrowM3U8LinkNotFoundException_WhenFetcherReturnsNull() {
        EpisodeCreateOrGetRequestDTO dto = EpisodeCreateOrGetRequestDTO.builder()
                .animeId(sampleAnimeId)
                .episodeNumber(1)
                .episodeUrl("https://hianime.to/watch/naruto-1")
                .build();

        when(animeService.findById(sampleAnimeId)).thenReturn(sampleAnime);
        when(m3u8Fetcher.fetchM3U8Link(dto.getEpisodeUrl(), sampleSessionId)).thenReturn(null);

        assertThrows(M3U8LinkNotFoundException.class, () -> episodeService.createEpisode(dto, sampleSessionId));
        verify(episodeRepository, never()).save(any(Episode.class));
    }


    @Test
    void convertVideo_ShouldCompleteSuccessfully_WhenFileIsCreated() {
        VideoCreationRequestDto dto = new VideoCreationRequestDto("http://stream.link/master.m3u8");

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(true);

            assertDoesNotThrow(() -> episodeService.convertVideo(dto, sampleSessionId));
            verify(videoConverter, times(1)).convertVideoFromM3U8Link(dto.getM3u8Link(), sampleSessionId);
        }
    }

    @Test
    void convertVideo_ShouldThrowVideoNotCreatedException_WhenFileDoesNotExist() {
        VideoCreationRequestDto dto = new VideoCreationRequestDto("http://stream.link/master.m3u8");

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(false);

            assertThrows(VideoNotCreatedException.class, () -> episodeService.convertVideo(dto, sampleSessionId));
        }
    }


    @Test
    void getEpisode_ShouldReturnExistingEpisode_WhenFoundInRepository() {
        EpisodeCreateOrGetRequestDTO dto = EpisodeCreateOrGetRequestDTO.builder()
                .animeId(sampleAnimeId)
                .build();

        when(episodeRepository.findByAnime_Id(sampleAnimeId)).thenReturn(Optional.of(sampleEpisode));

        Episode result = episodeService.getEpisode(dto, sampleSessionId);

        assertNotNull(result);
        assertEquals(sampleEpisodeId, result.getId());
        verify(episodeRepository, never()).save(any(Episode.class));
    }
}