package bg.senpai.anime.exception;

public class VideoNotCreatedException extends RuntimeException {
    public VideoNotCreatedException(String message, Throwable cause) {
        super(message, cause);
    }
    public VideoNotCreatedException(String message) {
        super(message);
    }

    public VideoNotCreatedException(){

    }
}
