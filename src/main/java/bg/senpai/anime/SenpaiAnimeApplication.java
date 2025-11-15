package bg.senpai.anime;

import com.microsoft.playwright.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableFeignClients
public class SenpaiAnimeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SenpaiAnimeApplication.class, args);
    }
}
