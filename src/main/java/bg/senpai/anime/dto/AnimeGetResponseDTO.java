package bg.senpai.anime.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class AnimeGetResponseDTO {
    private UUID id;
    private String animeTitle;
    private String hiAnimeId;
}
