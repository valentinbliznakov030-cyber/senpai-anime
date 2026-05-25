package bg.senpai.anime.service.impl;

import bg.senpai.anime.dto.AnimeCreateRequestDTO;
import bg.senpai.anime.entity.Anime;
import bg.senpai.anime.repository.AnimeRepository;
import bg.senpai.anime.service.AnimeService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnimeServiceImpl implements AnimeService {
    private final AnimeRepository animeRepository;

    @Override
    public Anime createAnime(AnimeCreateRequestDTO dto) {
        if(animeRepository.existsByHiAnimeId(dto.getHiAnimeId())){
            throw new EntityExistsException("Anime already exists");
        }

        Anime anime = Anime.builder()
                    .title(dto.getAnimeTitle().trim())
                    .hiAnimeId(dto.getHiAnimeId().trim())
                .build();

        return animeRepository.save(anime);

    }

    @Override
    public Anime findByTitle(String animeName) {
        return animeRepository.findByTitle(animeName)
                .orElseThrow(() -> new EntityNotFoundException("Anime not found by title"));
    }

    @Override
    public Anime findById(UUID animeId) {
        return animeRepository.findById(animeId)
                .orElseThrow(() -> new EntityNotFoundException("Anime not found"));

    }

    @Override
    public Anime getAnime(UUID animeId) {
        return animeRepository.findById(animeId).orElseThrow(() -> new EntityNotFoundException("Anime not found"));
    }

    @Override
    public Anime findByHiAnimeId(String hiAnimeId) {
        return animeRepository.findByHiAnimeId(hiAnimeId)
                .orElseThrow(() -> new EntityNotFoundException("Anime not found by hiAnimeId"));
    }
}