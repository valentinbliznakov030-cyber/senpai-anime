package bg.senpai.anime.service.impl;

import bg.senpai.anime.client.NodeClient;
import bg.senpai.anime.service.AnimeService;
import bg.senpai.anime.utils.M3U8Fetcher;
import bg.senpai.anime.utils.VideoConverter;
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
    private final M3U8Fetcher m3u8Fetcher;
    private final VideoConverter videoConverter;

    @Override
    public String getM3U8Link(String animeUrl) {
        System.out.println("🎥 Getting m3u8 for: " + animeUrl);

        return m3u8Fetcher.fetchM3U8Link(animeUrl);
    }

    @Override
    public boolean createVideo(VideoCreationRequestDto dto) {
        System.out.println(dto.getM3u8Link());
        System.out.println(dto.getVidName());

       return videoConverter.convertVideoFromM3U8Link(dto.getM3u8Link(), dto.getVidName());
    }

    @Override
    public SubtitlesDownloadedResponseDto downloadSubtitles(SubtitlesDownloadRequestDto subtitlesDownloadRequestDto) {
        System.out.println(subtitlesDownloadRequestDto.getAnimeTitle());
        System.out.println(subtitlesDownloadRequestDto.getSubtitleName());
        return nodeClient.downloadSubtitles(subtitlesDownloadRequestDto);
    }
}