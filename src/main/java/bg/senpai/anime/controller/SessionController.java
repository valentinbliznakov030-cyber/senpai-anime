package bg.senpai.anime.controller;

import bg.senpai.anime.service.SessionProcessManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/session")
@RequiredArgsConstructor
public class SessionController {
    private final SessionProcessManager sessionProcessManager;

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> killSession(@PathVariable String sessionId) {

        System.out.println("Killing session: " + sessionId);

        sessionProcessManager.killSession(sessionId);

        return ResponseEntity.ok(
                "Session " + sessionId + " terminated and cleaned up."
        );
    }
}
