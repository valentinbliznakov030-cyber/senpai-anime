package bg.senpai.anime.controller;

import bg.senpai.anime.service.SubtitleService;
import bg.senpai.common.dtos.SubtitlesDownloadRequestDTO;
import bg.senpai.common.dtos.SubtitlesDownloadedResponseDto;
import bg.senpai.common.dtos.TranslateSubtitleRequestDto;
import bg.senpai.common.dtos.TranslateSubtitleResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/subtitles")
@RequiredArgsConstructor
public class SubtitleController {
    private final SubtitleService subtitleService;

    @PostMapping("/{sessionId}")
    public ResponseEntity<SubtitlesDownloadedResponseDto> subtitlesDownloadedResponseDto(@RequestBody SubtitlesDownloadRequestDTO dto,
                                                                                         @PathVariable String sessionId) throws ExecutionException, InterruptedException {
        subtitleService.downloadSubtitles(dto, sessionId);

        return ResponseEntity.status(200).body(SubtitlesDownloadedResponseDto
                .builder()
                .success(true)
                .message("Subtitles download")
                .statusCode(200)
                .subtitleName(sessionId)
                .build());
    }

    @PostMapping("/translation")
    public ResponseEntity<TranslateSubtitleResponseDto> translateSubtitles(@RequestBody TranslateSubtitleRequestDto translateSubtitleRequestDto){
        String translatedSubtitleName = subtitleService.translateSubtitle(translateSubtitleRequestDto, translateSubtitleRequestDto.getSubtitleName());

        TranslateSubtitleResponseDto response = TranslateSubtitleResponseDto.builder()
                .success(true)
                .statusCode("200")
                .message("Subtitle translated successfully!")
                .subtitleName(translatedSubtitleName)
                .build();

        return ResponseEntity.ok(response);
    }
}
