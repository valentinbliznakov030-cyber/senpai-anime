package bg.senpai.anime.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AnimeCreateRequestDTO {
    @NotBlank
    private String animeTitle;
    @NotBlank
    private String hiAnimeId;
}
