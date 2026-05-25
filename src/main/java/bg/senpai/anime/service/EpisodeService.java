package bg.senpai.anime.service;

import bg.senpai.anime.dto.EpisodeCreateOrGetRequestDTO;
import bg.senpai.anime.entity.Episode;
import bg.senpai.common.dtos.VideoCreationRequestDto;

import java.util.UUID;

public interface EpisodeService {
    Episode createEpisode(EpisodeCreateOrGetRequestDTO dto, String sessionId);
    Episode findById(UUID episodeId);

    void convertVideo(VideoCreationRequestDto dto, String sessionId);

    Episode getEpisode(EpisodeCreateOrGetRequestDTO dto, String sessionId);
}
