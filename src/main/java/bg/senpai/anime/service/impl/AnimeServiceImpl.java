package bg.senpai.anime.service.impl;

import bg.senpai.anime.client.NodeClient;
import bg.senpai.anime.service.AnimeService;
import bg.senpai.common.dtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AnimeServiceImpl implements AnimeService {

    private final NodeClient nodeClient;

    @Override
    public AnimeM3U8LinkDto getM3U8Link(String animeUrl){
        System.out.println(animeUrl);
        return nodeClient.m3u8Extracting(animeUrl);
    }

    @Override
    public VideoCreationResponseDto createVideo(VideoCreationRequestDto videoCreationRequestDto) {
        System.out.println(videoCreationRequestDto.getM3u8Link());
        System.out.println(videoCreationRequestDto.getVidName());
        return nodeClient.createVideo(videoCreationRequestDto);
    }
}