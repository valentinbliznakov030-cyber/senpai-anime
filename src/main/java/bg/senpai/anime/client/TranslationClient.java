package bg.senpai.anime.client;

import bg.senpai.common.dtos.TranslationResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class TranslationClient {

    private final WebClient webClient;

    public TranslationClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://localhost:5000").build();
    }

    public String translate(String text) {
        Map<String, Object> body = Map.of(
                "q", text,
                "source", "en",
                "target", "bg",
                "format", "text"
        );

        TranslationResponse response = webClient.post()
                .uri("/translate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(TranslationResponse.class)
                .block();

        return response != null ? response.getTranslatedText() : text;
    }
}