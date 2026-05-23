package bg.senpai.anime.service;

import bg.senpai.anime.dto.EpisodeCreationRequestDTO;
import bg.senpai.anime.entity.Anime;
import bg.senpai.anime.entity.Episode;
import bg.senpai.common.dtos.VideoCreationRequestDto;
import bg.senpai.common.dtos.VideoCreationResponseDto;

import java.util.UUID;

public interface EpisodeService {

    String getM3U8Link(String animeUrl, String sessionId);

    Episode createEpisode(EpisodeCreationRequestDTO dto, String sessionId);
    Episode findById(UUID episodeId);

    void convertVideo(VideoCreationRequestDto dto, String sessionId);

    Episode getEpisode(EpisodeCreationRequestDTO dto, String sessionId);

}
