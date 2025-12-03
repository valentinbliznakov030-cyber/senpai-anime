package bg.senpai.anime.controllerTest;

import bg.senpai.anime.controller.AnimeController;
import bg.senpai.anime.service.AnimeService;
import bg.senpai.anime.service.SessionProcessManager;
import bg.senpai.common.dtos.VideoCreationRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnimeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AnimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AnimeService animeService;

    @MockBean
    private SessionProcessManager sessionProcessManager;

    @Test
    void shouldGetM3U8LinkSuccessfully() throws Exception {
        String animeUrl = "https://hianime.to/watch/demon-slayer/episode-1";
        String sessionId = "test-session-123";
        String m3u8Link = "https://example.com/video.m3u8";

        Mockito.when(sessionProcessManager.createOrGetSession(anyString())).thenReturn(sessionId);
        Mockito.when(animeService.getM3U8Link(anyString(), anyString())).thenReturn(m3u8Link);

        mockMvc.perform(get("/api/v1/anime/m3u8Link")
                        .param("url", animeUrl)
                        .param("sessionId", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.statusCode").value("200"))
                .andExpect(jsonPath("$.message").value("M3U8 link found"))
                .andExpect(jsonPath("$.m3u8Link").value(m3u8Link));

        Mockito.verify(sessionProcessManager).createOrGetSession(sessionId);
        Mockito.verify(animeService).getM3U8Link(animeUrl, sessionId);
    }

    @Test
    void shouldCreateVideoSuccessfully() throws Exception {
        String vidName = "demon-slayer-episode-1";
        String m3u8Link = "https://example.com/video.m3u8";
        String sessionId = "test-session-123";

        VideoCreationRequestDto request = VideoCreationRequestDto.builder()
                .m3u8Link(m3u8Link)
                .vidName(vidName)
                .build();

        Mockito.when(sessionProcessManager.createOrGetSession(anyString())).thenReturn(sessionId);
        Mockito.doNothing().when(animeService).createVideo(any(VideoCreationRequestDto.class), anyString());

        mockMvc.perform(post("/api/v1/anime/video")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.statusCode").value("201"))
                .andExpect(jsonPath("$.message").value("Video converted, downloaded and saved"))
                .andExpect(jsonPath("$.vidName").value(vidName));

        Mockito.verify(sessionProcessManager).createOrGetSession(vidName);
        Mockito.verify(animeService).createVideo(any(VideoCreationRequestDto.class), eq(sessionId));
    }
}

