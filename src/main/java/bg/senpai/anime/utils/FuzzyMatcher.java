package bg.senpai.anime.utils;

import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;

import java.util.List;
import java.util.Map;

public class FuzzyMatcher {
    private static final StringMetric metric = StringMetrics.cosineSimilarity();

    public static Map<String, Object> findBestMatch(List<Map<String, Object>> animeList, String animeTitle) {
        double bestScore = 0;
        Map<String, Object> bestMatch = null;

        for (Map<String, Object> anime : animeList) {
            String title = anime.get("title").toString();
            double score = metric.compare(title.toLowerCase(), animeTitle.toLowerCase());

            if (score > bestScore) {
                bestScore = score;
                bestMatch = anime;
            }
        }

        System.out.printf("🎯 Best match: %s (score: %.2f)%n",
                bestMatch != null ? bestMatch.get("title") : "none", bestScore);

        return bestMatch;
    }
}
