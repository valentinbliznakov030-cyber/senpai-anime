package bg.senpai.anime.service;

import bg.senpai.anime.entity.Anime;
import bg.senpai.common.dtos.*;
import org.springframework.core.io.Resource;

import java.util.Optional;
import java.util.UUID;

public interface AnimeService {
    Anime createAnime(AnimeInfoRequestDto animeInfoRequestDto);

    Anime findByTitle(String animeName);
    Anime findById(UUID animeId);

    Anime getAnime(AnimeInfoRequestDto dto);

    Anime findByHiAnimeId(String hiAnimeId);


}
