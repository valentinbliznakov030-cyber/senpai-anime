package bg.senpai.anime.controller;

import bg.senpai.anime.service.AnimeService;
import bg.senpai.common.dtos.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
        String link = animeService.getM3U8Link(animeUrl);

        return AnimeM3U8LinkDto
                .builder()
                .m3u8Link(link)
                .success(link != null)
                .message(link != null? "M3U8 link found" : "M3U8 link not found")
                .build();
    }

    @PostMapping("/video")
    public VideoCreationResponseDto createVideo(@RequestBody VideoCreationRequestDto videoCreationRequestDto){

        boolean result = animeService.createVideo(videoCreationRequestDto);


        return VideoCreationResponseDto
                .builder()
                .success(result)
                .message(result ? "Video converted, downloaded and saved" : "Video not converted")
                .statusCode( result ? "201" : "500")
                .vidName(videoCreationRequestDto.getVidName())
                .build();
   }





}

