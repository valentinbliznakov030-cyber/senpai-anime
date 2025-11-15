package bg.senpai.anime.service;

import bg.senpai.common.dtos.SubtitlesDownloadRequestDto;
import bg.senpai.common.dtos.SubtitlesDownloadedResponseDto;
import bg.senpai.common.dtos.TranslateSubtitleRequestDto;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface SubtitleService {
    void downloadSubtitles(SubtitlesDownloadRequestDto subtitlesDownloadRequestDto, String sessionId) throws ExecutionException, InterruptedException;

    String translateSubtitle(TranslateSubtitleRequestDto translateSubtitleRequestDto, String sessionId);
}
