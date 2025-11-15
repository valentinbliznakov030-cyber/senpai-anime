package bg.senpai.anime.service;

import bg.senpai.common.dtos.*;
import org.springframework.core.io.Resource;

public interface AnimeService {
    String getM3U8Link(String animeUrl, String sessionId);
     void createVideo(VideoCreationRequestDto videoCreationRequestDto, String sessionId);
}
