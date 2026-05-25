package bg.senpai.anime.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class EpisodeCreateOrGetResponseDTO {
    private UUID animeId;
    private UUID episodeId;
    private String m3u8Link;
    private Integer episodeNumber;
}

/*
 private UUID animeId;
    private String animeTitle;
 */