package bg.senpai.anime.controller;

import bg.senpai.anime.service.AnimeService;
import bg.senpai.common.dtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/anime")
@RequiredArgsConstructor
public class AnimeController {

    private final AnimeService animeService;


    @GetMapping("/m3u8Link")
    public AnimeM3U8LinkDto getM3u8Link(@RequestParam("url") String animeUrl) {
        System.out.println(animeUrl);
        return animeService.getM3U8Link(animeUrl);
    }

    @PostMapping("/video")
    public VideoCreationResponseDto createVideo(@RequestBody VideoCreationRequestDto videoCreationRequestDto){
        return animeService.createVideo(videoCreationRequestDto);
   }


   @PostMapping("/subtitles")
    public SubtitlesDownloadedResponseDto subtitlesDownloadedResponseDto(@RequestBody SubtitlesDownloadRequestDto subtitlesDownloadRequestDto){
        return animeService.downloadSubtitles(subtitlesDownloadRequestDto);
   }
}

