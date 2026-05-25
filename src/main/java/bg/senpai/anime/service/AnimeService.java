package bg.senpai.anime.service;

import bg.senpai.anime.dto.AnimeCreateRequestDTO;
import bg.senpai.anime.entity.Anime;

import java.util.UUID;

public interface AnimeService {
    Anime createAnime(AnimeCreateRequestDTO dto);

    Anime findByTitle(String animeName);
    Anime findById(UUID animeId);

    Anime getAnime(UUID animeId);

    Anime findByHiAnimeId(String hiAnimeId);


}
