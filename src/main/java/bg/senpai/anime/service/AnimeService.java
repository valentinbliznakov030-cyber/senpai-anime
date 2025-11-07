package bg.senpai.anime.service;

import bg.senpai.common.dtos.AnimeInfoRequestDto;
import bg.senpai.common.dtos.AnimeM3U8LinkDto;
import bg.senpai.common.dtos.VideoCreationRequestDto;
import bg.senpai.common.dtos.VideoCreationResponseDto;
import org.springframework.core.io.Resource;

public interface AnimeService {
    AnimeM3U8LinkDto getM3U8Link(String animeUrl);
    VideoCreationResponseDto createVideo(VideoCreationRequestDto videoCreationRequestDto);
}
