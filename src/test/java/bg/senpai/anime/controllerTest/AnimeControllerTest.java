package bg.senpai.anime.controllerTest;

import bg.senpai.anime.service.AnimeService;
import bg.senpai.common.dtos.AnimeM3U8LinkDto;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AnimeControllerTest.class)
@AutoConfigureMockMvc(addFilters = false)
public class AnimeControllerTest {

    @MockBean
    public AnimeService animeService;


    @Test
    public void shouldGetSuccessfullyM3U8Link(){
        String animeUrl = "https://animepahe.si/play/3a949d66-f993-e47d-e101-f53cb73cb941/b0c352c95ff846f549dddcab375c46656e79f65548c31bb9cbc289c34e25b569";
        String expectedM3U8Link = "https://vault-14.owocdn.top/stream/14/15/895cd42ab0fa0e45062e1b8aa17a04b95dc967c5acee999e290b49fcd6d873c1/uwu.m3u8";

        AnimeM3U8LinkDto response = AnimeM3U8LinkDto
                .builder()
                .success(true)
                .statusCode("200")
                .m3u8Link(expectedM3U8Link)
                .build();

        when(animeService.getM3U8Link(any())).thenReturn(response);


    }
}
