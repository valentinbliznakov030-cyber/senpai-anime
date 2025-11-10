package bg.senpai.anime.service;

import bg.senpai.common.dtos.*;
import org.springframework.core.io.Resource;

public interface AnimeService {
    AnimeM3U8LinkDto getM3U8Link(String animeUrl);
    VideoCreationResponseDto createVideo(VideoCreationRequestDto videoCreationRequestDto);

    SubtitlesDownloadedResponseDto downloadSubtitles(SubtitlesDownloadRequestDto subtitlesDownloadRequestDto);
}
