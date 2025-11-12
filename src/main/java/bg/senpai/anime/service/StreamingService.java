package bg.senpai.anime.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface StreamingService {
    void streamVideo(String vidName, HttpServletRequest request, HttpServletResponse response);
}
