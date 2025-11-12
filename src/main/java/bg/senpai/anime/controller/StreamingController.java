package bg.senpai.anime.controller;

import bg.senpai.anime.service.StreamingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/streaming")
@RequiredArgsConstructor
public class StreamingController {
    private final StreamingService streamingService;

    @GetMapping
    public void streamVideo(@RequestParam String vidName, HttpServletRequest request, HttpServletResponse response){
        streamingService.streamVideo(vidName, request, response);
    }
}
