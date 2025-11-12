package bg.senpai.anime.service;

import bg.senpai.common.dtos.SubtitlesDownloadRequestDto;
import bg.senpai.common.dtos.SubtitlesDownloadedResponseDto;
import bg.senpai.common.dtos.TranslateSubtitleRequestDto;

public interface SubtitleService {
    SubtitlesDownloadedResponseDto downloadSubtitles(SubtitlesDownloadRequestDto subtitlesDownloadRequestDto);

    String translateSubtitle(TranslateSubtitleRequestDto translateSubtitleRequestDto);
}
