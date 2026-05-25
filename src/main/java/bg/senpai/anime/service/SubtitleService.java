package bg.senpai.anime.service;

import bg.senpai.common.dtos.SubtitlesDownloadRequestDTO;
import bg.senpai.common.dtos.TranslateSubtitleRequestDto;

import java.util.concurrent.ExecutionException;

public interface SubtitleService {
    String downloadSubtitles(SubtitlesDownloadRequestDTO subtitlesDownloadRequestDto, String sessionId) throws ExecutionException, InterruptedException;

    String translateSubtitle(TranslateSubtitleRequestDto translateSubtitleRequestDto, String sessionId);
}
