package bg.senpai.anime.controller;

import bg.senpai.anime.service.SubtitleService;
import bg.senpai.common.dtos.SubtitlesDownloadRequestDto;
import bg.senpai.common.dtos.SubtitlesDownloadedResponseDto;
import bg.senpai.common.dtos.TranslateSubtitleRequestDto;
import bg.senpai.common.dtos.TranslateSubtitleResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subtitles")
@RequiredArgsConstructor
public class SubtitleController {
    private final SubtitleService subtitleService;

    @PostMapping
    public SubtitlesDownloadedResponseDto subtitlesDownloadedResponseDto(@RequestBody SubtitlesDownloadRequestDto subtitlesDownloadRequestDto){
        return subtitleService.downloadSubtitles(subtitlesDownloadRequestDto);
    }

    @PostMapping
    public ResponseEntity<TranslateSubtitleResponseDto> translateSubtitles(@RequestBody TranslateSubtitleRequestDto translateSubtitleRequestDto){
        String translatedSubtitleName = subtitleService.translateSubtitle(translateSubtitleRequestDto);

        TranslateSubtitleResponseDto response = TranslateSubtitleResponseDto.builder()
                .success(translatedSubtitleName != null)
                .statusCode(translatedSubtitleName != null ? "200" : "500")
                .message(translatedSubtitleName != null ?
                        "✅ Subtitle translated successfully!" :
                        "❌ Failed to translate subtitle.")
                .subtitleName(translatedSubtitleName)
                .build();

        return ResponseEntity.ok(response);
    }
}
