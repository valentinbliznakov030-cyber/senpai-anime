package bg.senpai.anime.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class FuzzyMatcherTest {

    @Test
    @DisplayName("Should find best match when exact match exists")
    void shouldFindBestMatchWhenExactMatchExists() {
        List<Map<String, Object>> animeList = createAnimeList(
                "Demon Slayer",
                "Naruto",
                "One Piece"
        );

        Map<String, Object> result = FuzzyMatcher.findBestMatch(animeList, "Demon Slayer");

        assertThat(result).isNotNull();
        assertThat(result.get("title")).isEqualTo("Demon Slayer");
    }

    @Test
    @DisplayName("Should find best match when similar title exists")
    void shouldFindBestMatchWhenSimilarTitleExists() {
        List<Map<String, Object>> animeList = createAnimeList(
                "Demon Slayer: Kimetsu no Yaiba",
                "Naruto Shippuden",
                "One Piece"
        );

        Map<String, Object> result = FuzzyMatcher.findBestMatch(animeList, "Demon Slayer");

        assertThat(result).isNotNull();
        assertThat(result.get("title")).isEqualTo("Demon Slayer: Kimetsu no Yaiba");
    }

    @Test
    @DisplayName("Should find best match when case differs")
    void shouldFindBestMatchWhenCaseDiffers() {
        List<Map<String, Object>> animeList = createAnimeList(
                "DEMON SLAYER",
                "naruto",
                "ONE PIECE"
        );

        Map<String, Object> result = FuzzyMatcher.findBestMatch(animeList, "demon slayer");

        assertThat(result).isNotNull();
        assertThat(result.get("title")).isEqualTo("DEMON SLAYER");
    }

    @Test
    @DisplayName("Should find best match from multiple similar titles")
    void shouldFindBestMatchFromMultipleSimilarTitles() {
        List<Map<String, Object>> animeList = createAnimeList(
                "Naruto",
                "Naruto Shippuden",
                "Boruto: Naruto Next Generations"
        );

        Map<String, Object> result = FuzzyMatcher.findBestMatch(animeList, "Naruto Shippuden");

        assertThat(result).isNotNull();
        assertThat(result.get("title")).isEqualTo("Naruto Shippuden");
    }

    @Test
    @DisplayName("Should return null when empty list is provided")
    void shouldReturnNullWhenEmptyListProvided() {
        List<Map<String, Object>> emptyList = new ArrayList<>();

        Map<String, Object> result = FuzzyMatcher.findBestMatch(emptyList, "Demon Slayer");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should find best match even with partial match")
    void shouldFindBestMatchWithPartialMatch() {
        List<Map<String, Object>> animeList = createAnimeList(
                "Attack on Titan",
                "Attack on Titan: Final Season",
                "My Hero Academia"
        );

        Map<String, Object> result = FuzzyMatcher.findBestMatch(animeList, "Attack");

        assertThat(result).isNotNull();
        // Should match one of the Attack on Titan titles
        assertThat(result.get("title").toString()).contains("Attack");
    }

    private List<Map<String, Object>> createAnimeList(String... titles) {
        List<Map<String, Object>> animeList = new ArrayList<>();
        for (String title : titles) {
            Map<String, Object> anime = new HashMap<>();
            anime.put("title", title);
            animeList.add(anime);
        }
        return animeList;
    }
}

