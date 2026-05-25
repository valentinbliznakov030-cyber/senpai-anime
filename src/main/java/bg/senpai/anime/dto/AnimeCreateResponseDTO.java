package bg.senpai.anime.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class AnimeCreateResponseDTO {
    private UUID animeId;
    private String animeTitle;
    private String hiAnimeId;
}
