package bg.senpai.anime.utils;

import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class FuzzyMatcher {
    private static final Logger logger = LoggerFactory.getLogger(FuzzyMatcher.class);
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

        logger.info("Best match: {} (score: {})",
                bestMatch != null ? bestMatch.get("title") : "none", String.format("%.2f", bestScore));

        return bestMatch;
    }
}
