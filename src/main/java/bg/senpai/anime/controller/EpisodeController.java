package bg.senpai.anime.controller;

import bg.senpai.anime.dto.EpisodeCreateOrGetRequestDTO;
import bg.senpai.anime.dto.EpisodeCreateOrGetResponseDTO;
import bg.senpai.anime.entity.Episode;
import bg.senpai.anime.service.EpisodeService;
import bg.senpai.anime.service.SessionProcessManager;
import bg.senpai.common.dtos.VideoCreationRequestDto;
import bg.senpai.common.dtos.VideoCreationResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/episode")
public class EpisodeController {
    private static final Logger logger = LoggerFactory.getLogger(EpisodeController.class);
    private final EpisodeService episodeService;
    private final SessionProcessManager sessionProcessManager;

    @PostMapping("/video/{sessionId}")
    public ResponseEntity<VideoCreationResponseDto> createVideo(@RequestBody VideoCreationRequestDto videoCreationRequestDto,
                                                                @PathVariable String sessionId){

        episodeService.convertVideo(videoCreationRequestDto, sessionId);

        return ResponseEntity.status(201).body(VideoCreationResponseDto
                .builder()
                .success(true)
                .message("Video converted, downloaded and saved")
                .statusCode("201")
                .vidName(sessionId)
                .build());
    }

    @PostMapping("/{sessionId}")
    public ResponseEntity<EpisodeCreateOrGetResponseDTO> createOrGetEpisode(@Valid @RequestBody EpisodeCreateOrGetRequestDTO dto, @PathVariable String sessionId){
        logger.debug("Request for episode creation - episode_URL: {}, SessionId: {}", dto.getEpisodeUrl(), sessionId);

        Episode episode = episodeService.getEpisode(dto, sessionId);

        EpisodeCreateOrGetResponseDTO responseDTO = EpisodeCreateOrGetResponseDTO
                    .builder()
                    .animeId(episode.getAnime().getId())
                    .episodeNumber(episode.getEpisodeNumber())
                    .m3u8Link(episode.getM3u8Link())
                    .episodeId(episode.getId())
                .build();

        return ResponseEntity.ok(responseDTO);
    }
}
