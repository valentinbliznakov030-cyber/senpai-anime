package bg.senpai.anime.repository;

import bg.senpai.anime.entity.Anime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnimeRepository extends JpaRepository<Anime, UUID> {
    Optional<Anime> findByTitle(String animeName);
    Optional<Anime> findByHiAnimeId(String hiAnimeId);
    boolean existsByHiAnimeId(String hiAnimeId);
}
