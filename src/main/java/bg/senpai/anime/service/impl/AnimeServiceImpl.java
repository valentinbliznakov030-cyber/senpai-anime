package bg.senpai.anime.service.impl;

import bg.senpai.anime.client.NodeClient;
import bg.senpai.anime.exception.M3U8LinkNotFoundException;
import bg.senpai.anime.exception.VideoNotCreatedException;
import bg.senpai.anime.service.AnimeService;
import bg.senpai.anime.service.SessionProcessManager;
import bg.senpai.anime.tasks.SessionTask;
import bg.senpai.anime.utils.M3U8Fetcher;
import bg.senpai.anime.utils.VideoConverter;
import bg.senpai.common.dtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class AnimeServiceImpl implements AnimeService {

    private final NodeClient nodeClient;
    private final M3U8Fetcher m3u8Fetcher;
    private final VideoConverter videoConverter;
    private final SessionProcessManager sessionProcessManager;

    @Override
    public String getM3U8Link(String animeUrl, String sessionId) {
        System.out.println("🎥 Getting m3u8 for: " + animeUrl);

        String m3U8Link = m3u8Fetcher.fetchM3U8Link(animeUrl, sessionId);

        if(m3U8Link == null){
            throw new M3U8LinkNotFoundException("M3u8Link not found");
        }

        return m3U8Link;
    }

    @Override
    public void createVideo(VideoCreationRequestDto dto, String sessionId) {
        System.out.println(dto.getM3u8Link());
        System.out.println(dto.getVidName());
        videoConverter.convertVideoFromM3U8Link(dto.getM3u8Link(), dto.getVidName());

        System.out.println(Paths.get(System.getProperty("user.dir"), "videos", sessionId + ".mp4").toString());
        System.out.println(Files.exists(Paths.get(System.getProperty("user.dir"), "videos", sessionId + ".mp4")));
        if(!Files.exists(Paths.get(System.getProperty("user.dir"), "videos", sessionId + ".mp4"))){
            throw new VideoNotCreatedException();
        }

    }
}