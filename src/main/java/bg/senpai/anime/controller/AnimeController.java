package bg.senpai.anime.controller;

import bg.senpai.anime.service.AnimeService;
import bg.senpai.anime.service.SessionProcessManager;
import bg.senpai.common.dtos.*;
import feign.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/anime")
@RequiredArgsConstructor
public class AnimeController {
    private final AnimeService animeService;
    private final SessionProcessManager sessionProcessManager;

    @GetMapping("/m3u8Link")
    public ResponseEntity<AnimeM3U8LinkDto> getM3u8Link(@RequestParam("url") String animeUrl, @RequestParam("sessionId") String sessionId) {
        System.out.println(animeUrl);
        System.out.println(sessionId);

        sessionProcessManager.createOrGetSession(sessionId);

        String link = animeService.getM3U8Link(animeUrl, sessionId);

        return ResponseEntity.status(200).body(AnimeM3U8LinkDto
                .builder()
                .m3u8Link(link)
                .success(true)
                .message("M3U8 link found")
                .statusCode("200")
                .build());
    }

    @PostMapping("/video")
    public ResponseEntity<VideoCreationResponseDto> createVideo(@RequestBody VideoCreationRequestDto videoCreationRequestDto){
        String sessionId = sessionProcessManager.createOrGetSession(videoCreationRequestDto.getVidName()); //vidName  sessionId от фронт-енда,

        animeService.createVideo(videoCreationRequestDto, sessionId);


        return ResponseEntity.status(201).body(VideoCreationResponseDto
                .builder()
                .success(true)
                .message("Video converted, downloaded and saved")
                .statusCode("201")
                .vidName(videoCreationRequestDto.getVidName())
                .build());
   }
}

