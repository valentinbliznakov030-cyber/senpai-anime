package bg.senpai.anime.controller;

import bg.senpai.anime.service.SessionProcessManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/session")
@RequiredArgsConstructor
public class SessionController {
    private static final Logger logger = LoggerFactory.getLogger(SessionController.class);
    private final SessionProcessManager sessionProcessManager;

    @PostMapping()
    public ResponseEntity<String> postSessionDelete(@RequestParam String animeId) {
        logger.info("Creating session for anime: {}", animeId);

        String session = UUID.randomUUID() + LocalDateTime.now().toString() + animeId;

        sessionProcessManager.createOrGetSession(session);

        return ResponseEntity.ok(session);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> killSession(@PathVariable String sessionId) {
        logger.info("Killing session: {}", sessionId);

        sessionProcessManager.killSession(sessionId);

        return ResponseEntity.ok(
                "Session " + sessionId + " terminated and cleaned up."
        );
    }
}
