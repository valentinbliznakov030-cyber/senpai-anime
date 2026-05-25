package bg.senpai.anime.service.impl;

import bg.senpai.anime.dto.AnimeCreateRequestDTO;
import bg.senpai.anime.entity.Anime;
import bg.senpai.anime.repository.AnimeRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnimeServiceImplTest {

    @Mock
    private AnimeRepository animeRepository;

    @InjectMocks
    private AnimeServiceImpl animeService;

    private Anime sampleAnime;
    private UUID sampleId;

    @BeforeEach
    void setUp() {
        sampleId = UUID.randomUUID();
        sampleAnime = Anime.builder()
                .id(sampleId)
                .title("Naruto")
                .hiAnimeId("naruto-123")
                .build();
    }

    @Test
    void createAnime_ShouldSaveAnime_WhenAnimeDoesNotExist() {
        AnimeCreateRequestDTO dto = AnimeCreateRequestDTO.builder()
                .animeTitle("  Naruto  ") // Тестваме и .trim()
                .hiAnimeId("naruto-123")
                .build();

        when(animeRepository.existsByHiAnimeId("naruto-123")).thenReturn(false);
        when(animeRepository.save(any(Anime.class))).thenReturn(sampleAnime);

        Anime result = animeService.createAnime(dto);

        assertNotNull(result);
        assertEquals("Naruto", result.getTitle());
        verify(animeRepository, times(1)).save(any(Anime.class));
    }

    @Test
    void createAnime_ShouldThrowEntityExistsException_WhenAnimeAlreadyExists() {
        AnimeCreateRequestDTO dto = AnimeCreateRequestDTO.builder()
                .animeTitle("Bleach")
                .hiAnimeId("bleach-456")
                .build();

        when(animeRepository.existsByHiAnimeId("bleach-456")).thenReturn(true);

        EntityExistsException exception = assertThrows(EntityExistsException.class, () -> {
            animeService.createAnime(dto);
        });

        assertEquals("Anime already exists", exception.getMessage());
        verify(animeRepository, never()).save(any(Anime.class));
    }


    @Test
    void findByTitle_ShouldReturnAnime_WhenTitleExists() {
        when(animeRepository.findByTitle("Naruto")).thenReturn(Optional.of(sampleAnime));

        Anime result = animeService.findByTitle("Naruto");

        assertNotNull(result);
        assertEquals("Naruto", result.getTitle());
        assertEquals(sampleId, result.getId());
    }

    @Test
    void findByTitle_ShouldThrowEntityNotFoundException_WhenTitleDoesNotExist() {
        when(animeRepository.findByTitle("Unknown")).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            animeService.findByTitle("Unknown");
        });

        assertEquals("Anime not found by title", exception.getMessage());
    }

    @Test
    void findById_ShouldReturnAnime_WhenIdExists() {
        when(animeRepository.findById(sampleId)).thenReturn(Optional.of(sampleAnime));

        Anime result = animeService.findById(sampleId);

        assertNotNull(result);
        assertEquals(sampleId, result.getId());
    }

    @Test
    void findById_ShouldThrowEntityNotFoundException_WhenIdDoesNotExist() {
        UUID fakeId = UUID.randomUUID();
        when(animeRepository.findById(fakeId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            animeService.findById(fakeId);
        });

        assertEquals("Anime not found", exception.getMessage());
    }

    @Test
    void getAnime_ShouldReturnAnime_WhenIdExists() {
        when(animeRepository.findById(sampleId)).thenReturn(Optional.of(sampleAnime));

        Anime result = animeService.getAnime(sampleId);

        assertNotNull(result);
        assertEquals(sampleId, result.getId());
    }

    @Test
    void getAnime_ShouldThrowEntityNotFoundException_WhenIdDoesNotExist() {
        UUID fakeId = UUID.randomUUID();
        when(animeRepository.findById(fakeId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            animeService.getAnime(fakeId);
        });

        assertEquals("Anime not found", exception.getMessage());
    }


    @Test
    void findByHiAnimeId_ShouldReturnAnime_WhenHiAnimeIdExists() {
        when(animeRepository.findByHiAnimeId("naruto-123")).thenReturn(Optional.of(sampleAnime));

        Anime result = animeService.findByHiAnimeId("naruto-123");

        assertNotNull(result);
        assertEquals("naruto-123", result.getHiAnimeId());
    }

    @Test
    void findByHiAnimeId_ShouldThrowEntityNotFoundException_WhenHiAnimeIdDoesNotExist() {
        when(animeRepository.findByHiAnimeId("fake-id")).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            animeService.findByHiAnimeId("fake-id");
        });

        assertEquals("Anime not found by hiAnimeId", exception.getMessage());
    }
}