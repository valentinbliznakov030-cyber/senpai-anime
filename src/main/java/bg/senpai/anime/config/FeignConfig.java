package bg.senpai.anime.config;

import feign.Request;
import feign.codec.Decoder;
import feign.optionals.OptionalDecoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class FeignConfig {
    @Bean
    public Decoder feignDecoder() {
        return new OptionalDecoder(
                new ResponseEntityDecoder(
                        new SpringDecoder(() -> new HttpMessageConverters(new RestTemplate().getMessageConverters()))
                )
        );
    }

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                Duration.ofMinutes(15),   // колко да чака за свързване
                Duration.ofMinutes(30),  // колко да чака за четене (изтегляне)
                true                     // следвай redirect-и
        );
    }
}
