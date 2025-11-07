package bg.senpai.anime.client;

import bg.senpai.anime.config.FeignConfig;
import bg.senpai.common.dtos.AnimeM3U8LinkDto;
import bg.senpai.common.dtos.VideoCreationRequestDto;
import bg.senpai.common.dtos.VideoCreationResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "node-server",
        url = "http://localhost:5000/api/v1/anime",
        configuration = FeignConfig.class
)
public interface NodeClient {

    @GetMapping(value = "/stream", produces = "application/octet-stream")
    ResponseEntity<Resource> streamVideo(@RequestParam("vidName") String vidName);

    @GetMapping("/episode-url")
    AnimeM3U8LinkDto m3u8Extracting(@RequestParam("url") String url);

    @PostMapping(value = "/video", produces = "application/json")
    VideoCreationResponseDto createVideo(@RequestBody VideoCreationRequestDto videoCreationRequestDto);


}

