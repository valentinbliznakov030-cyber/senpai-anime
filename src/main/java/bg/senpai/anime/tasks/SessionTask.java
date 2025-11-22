package bg.senpai.anime.tasks;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class SessionTask {
    private final String sessionId;

    private final List<Process> processes = new ArrayList<>();
    private final List<Future<?>> futures = new ArrayList<>();

    public void addProcess(Process p) {
        processes.add(p);
    }

    public void addFuture(Future<?> f) {
        futures.add(f);
    }

    public void cancel() {
        for (Process p : processes) {
            try {
                if (p != null && p.isAlive()) {
                    p.destroyForcibly();
                }
            } catch (Exception ignored) {}
        }

        for (Future<?> f : futures) {
            try {
                if (f != null && !f.isDone()) {
                    f.cancel(true);
                }
            } catch (Exception ignored) {}
        }
    }
}
