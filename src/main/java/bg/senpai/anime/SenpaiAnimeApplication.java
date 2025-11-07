package bg.senpai.anime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SenpaiAnimeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SenpaiAnimeApplication.class, args);
	}

}
