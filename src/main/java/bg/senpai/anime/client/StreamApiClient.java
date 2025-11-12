package bg.senpai.anime.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "streamApiClient", url = "http://localhost:3030/api/v1")
public interface StreamApiClient {
    // 1. Търсене на аниме по заглавие
    @GetMapping("/search")
    Map<String, Object> searchAnime(@RequestParam("keyword") String animeTitle,
                                    @RequestParam("page") int page);

    // 2. Взимаме епизоди по animeId
    @GetMapping("/episodes/{animeId}")
    Map<String, Object> getEpisodes(@PathVariable("animeId") String animeId);

    // 3. Взимаме линкове за субтитри
    @GetMapping("/stream")
    Map<String, Object> getStreamData(@RequestParam("id") String episodeId,
                                      @RequestParam("type") String type,
                                      @RequestParam("server") String server);
}
