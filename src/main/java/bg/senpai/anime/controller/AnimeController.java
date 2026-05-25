package bg.senpai.anime.controller;

import bg.senpai.anime.dto.AnimeCreateRequestDTO;
import bg.senpai.anime.dto.AnimeCreateResponseDTO;
import bg.senpai.anime.dto.AnimeGetResponseDTO;
import bg.senpai.anime.entity.Anime;
import bg.senpai.anime.service.AnimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/anime")
public class AnimeController {
    private final AnimeService animeService;

    @PostMapping
    public ResponseEntity<AnimeCreateResponseDTO> create(@Valid @RequestBody AnimeCreateRequestDTO dto) {
        Anime anime = animeService.createAnime(dto);

        AnimeCreateResponseDTO response = AnimeCreateResponseDTO.builder()
                    .animeTitle(anime.getTitle())
                    .animeId(anime.getId())
                    .hiAnimeId(anime.getHiAnimeId())
                .build();

        URI uri = ServletUriComponentsBuilder
                    .fromCurrentRequestUri()
                    .path("/{id}")
                    .buildAndExpand(anime.getId())
                    .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimeGetResponseDTO> getAnime(@PathVariable UUID animeId){
        Anime anime = animeService.getAnime(animeId);

        AnimeGetResponseDTO responseDTO = AnimeGetResponseDTO
                    .builder()
                    .id(anime.getId())
                    .animeTitle(anime.getTitle())
                    .hiAnimeId(anime.getHiAnimeId())
                .build();

        return ResponseEntity.ok(responseDTO);
    }
}
