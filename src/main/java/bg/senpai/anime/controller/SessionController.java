package bg.senpai.anime.controller;

import bg.senpai.anime.service.SessionProcessManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/session")
@RequiredArgsConstructor
public class SessionController {
    private static final Logger logger = LoggerFactory.getLogger(SessionController.class);
    private final SessionProcessManager sessionProcessManager;

    @PostMapping("/{sessionId}")
    public ResponseEntity<?> postSessionDelete(@PathVariable String sessionId) {
        logger.info("Killing session: {}", sessionId);

        sessionProcessManager.killSession(sessionId);

        return ResponseEntity.ok(
                "Session " + sessionId + " terminated and cleaned up."
        );
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
