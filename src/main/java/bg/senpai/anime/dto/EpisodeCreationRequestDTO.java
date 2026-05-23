package bg.senpai.anime.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class EpisodeCreationRequestDTO {
    private String episodeUrl;
    private int episodeNumber;
    private UUID animeId;
}
