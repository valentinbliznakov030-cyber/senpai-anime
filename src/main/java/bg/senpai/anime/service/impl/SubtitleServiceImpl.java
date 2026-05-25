package bg.senpai.anime.service.impl;

import bg.senpai.anime.exception.SubtitlesNotFoundException;
import bg.senpai.anime.exception.SubtitlesTranslationException;
import bg.senpai.anime.service.SubtitleService;
import bg.senpai.anime.utils.SubtitleUtil;
import bg.senpai.common.dtos.SubtitlesDownloadRequestDTO;
import bg.senpai.common.dtos.TranslateSubtitleRequestDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class SubtitleServiceImpl implements SubtitleService {
    private static final Logger logger = LoggerFactory.getLogger(SubtitleServiceImpl.class);
    private final SubtitleUtil subtitleUtil;

    @Override
    public String downloadSubtitles(SubtitlesDownloadRequestDTO dto, String sessionId) throws ExecutionException, InterruptedException {
        Path downloadedSubtitlePath = subtitleUtil.downloadSubtitle(dto, sessionId);

        if(!Files.exists(downloadedSubtitlePath)){
            throw new SubtitlesNotFoundException("Subtitle not found");
        }

        return downloadedSubtitlePath.getFileName().toString();
    }



    @Override
    public String translateSubtitle(TranslateSubtitleRequestDto request, String sessionId) {
        Path outputPath = subtitleUtil.translateSubtitle(request, sessionId);

        if(!Files.exists(outputPath)){
            throw new SubtitlesTranslationException("Translated subtitle fil not found");
        }
        return outputPath.getFileName().toString();
    }

}