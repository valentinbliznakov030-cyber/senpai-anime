package bg.senpai.anime.repository;

import bg.senpai.anime.entity.Anime;
import bg.senpai.anime.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, UUID> {
    Optional<Episode> findByAnime_Id(UUID animeId);
}
