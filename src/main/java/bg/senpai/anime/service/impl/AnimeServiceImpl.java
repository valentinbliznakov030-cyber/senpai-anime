package bg.senpai.anime.service.impl;

import bg.senpai.anime.client.NodeClient;
import bg.senpai.anime.entity.Anime;
import bg.senpai.anime.exception.M3U8LinkNotFoundException;
import bg.senpai.anime.exception.VideoNotCreatedException;
import bg.senpai.anime.repository.AnimeRepository;
import bg.senpai.anime.service.AnimeService;
import bg.senpai.anime.service.SessionProcessManager;
import bg.senpai.anime.utils.M3U8Fetcher;
import bg.senpai.anime.utils.VideoConverter;
import bg.senpai.common.dtos.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnimeServiceImpl implements AnimeService {
    private final AnimeRepository animeRepository;
    private static final Logger logger = LoggerFactory.getLogger(AnimeServiceImpl.class);
    private final NodeClient nodeClient;
    private final SessionProcessManager sessionProcessManager;


    @Override
    public Anime createAnime(AnimeInfoRequestDto dto) {
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
    public Anime getAnime(AnimeInfoRequestDto dto) {
        return animeRepository.findByTitle(dto.getAnimeTitle().trim()).orElseGet(() -> createAnime(dto));
    }

    @Override
    public Anime findByHiAnimeId(String hiAnimeId) {
        return animeRepository.findByHiAnimeId(hiAnimeId)
                .orElseThrow(() -> new EntityNotFoundException("Anime not found by hiAnimeId"));
    }
}