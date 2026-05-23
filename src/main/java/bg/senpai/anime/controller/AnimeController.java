package bg.senpai.anime.controller;

import bg.senpai.anime.dto.CreatedOrExistingAnimeResponseDTO;
import bg.senpai.anime.entity.Anime;
import bg.senpai.anime.service.AnimeService;
import bg.senpai.anime.service.SessionProcessManager;
import bg.senpai.common.dtos.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/anime")
public class AnimeController {
    private final AnimeService animeService;

    @PostMapping
    public ResponseEntity<CreatedOrExistingAnimeResponseDTO> createOrGetAnime(@RequestBody AnimeInfoRequestDto dto) {
        Anime anime = animeService.getAnime(dto);

        CreatedOrExistingAnimeResponseDTO response = CreatedOrExistingAnimeResponseDTO
                .builder()
                .animeId(anime.getId())
                .animeTitle(anime.getTitle())
                .build();

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(anime.getId())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }
}
