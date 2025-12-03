package bg.senpai.anime.controllerTest;

import bg.senpai.anime.controller.SubtitleController;
import bg.senpai.anime.service.SubtitleService;
import bg.senpai.common.dtos.SubtitlesDownloadRequestDto;
import bg.senpai.common.dtos.TranslateSubtitleRequestDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubtitleController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SubtitleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubtitleService subtitleService;

    @Test
    void shouldDownloadSubtitlesSuccessfully() throws Exception {
        String subtitleUrl = "https://example.com/subtitle.vtt";
        String subtitleName = "demon-slayer-episode-1";

        SubtitlesDownloadRequestDto request = SubtitlesDownloadRequestDto.builder()
                .subtitleUrl(subtitleUrl)
                .subtitleName(subtitleName)
                .build();

        Mockito.doNothing().when(subtitleService).downloadSubtitles(any(SubtitlesDownloadRequestDto.class), anyString());

        mockMvc.perform(post("/api/v1/subtitles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Subtitles download"))
                .andExpect(jsonPath("$.subtitleName").value(subtitleName));

        Mockito.verify(subtitleService).downloadSubtitles(any(SubtitlesDownloadRequestDto.class), eq(subtitleName));
    }

    @Test
    void shouldTranslateSubtitlesSuccessfully() throws Exception {
        String subtitleName = "demon-slayer-episode-1";
        String translatedSubtitleName = "demon-slayer-episode-1-bg.vtt";

        TranslateSubtitleRequestDto request = TranslateSubtitleRequestDto.builder()
                .subtitleName(subtitleName)
                .build();

        Mockito.when(subtitleService.translateSubtitle(any(TranslateSubtitleRequestDto.class), anyString()))
                .thenReturn(translatedSubtitleName);

        mockMvc.perform(post("/api/v1/subtitles/translation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.statusCode").value("200"))
                .andExpect(jsonPath("$.message").value("Subtitle translated successfully!"))
                .andExpect(jsonPath("$.subtitleName").value(translatedSubtitleName));

        Mockito.verify(subtitleService).translateSubtitle(any(TranslateSubtitleRequestDto.class), eq(subtitleName));
    }
}

