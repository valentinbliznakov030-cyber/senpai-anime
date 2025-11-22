package bg.senpai.anime.service.impl;

import bg.senpai.anime.client.NodeClient;
import bg.senpai.anime.exception.M3U8LinkNotFoundException;
import bg.senpai.anime.exception.VideoNotCreatedException;
import bg.senpai.anime.service.AnimeService;
import bg.senpai.anime.service.SessionProcessManager;
import bg.senpai.anime.utils.M3U8Fetcher;
import bg.senpai.anime.utils.VideoConverter;
import bg.senpai.common.dtos.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class AnimeServiceImpl implements AnimeService {
    private static final Logger logger = LoggerFactory.getLogger(AnimeServiceImpl.class);
    private final NodeClient nodeClient;
    private final M3U8Fetcher m3u8Fetcher;
    private final VideoConverter videoConverter;
    private final SessionProcessManager sessionProcessManager;

    @Override
    public String getM3U8Link(String animeUrl, String sessionId) {
        logger.info("Getting m3u8 for: {}", animeUrl);

        String m3U8Link = m3u8Fetcher.fetchM3U8Link(animeUrl, sessionId);

        if(m3U8Link == null){
            throw new M3U8LinkNotFoundException("M3u8Link not found");
        }

        return m3U8Link;
    }

    @Override
    public void createVideo(VideoCreationRequestDto dto, String sessionId) {
        logger.debug("M3U8 link: {}", dto.getM3u8Link());
        logger.debug("Video name: {}", dto.getVidName());
        videoConverter.convertVideoFromM3U8Link(dto.getM3u8Link(), dto.getVidName());

        Path videoPath = Paths.get(System.getProperty("user.dir"), "videos", sessionId + ".mp4");
        logger.debug("Video path: {}", videoPath);
        logger.debug("Video exists: {}", Files.exists(videoPath));
        if(!Files.exists(videoPath)){
            throw new VideoNotCreatedException();
        }

    }
}