package bg.senpai.anime.exception;

public class SubtitlesNotFoundException extends RuntimeException {
    public SubtitlesNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    public SubtitlesNotFoundException(String message){
        super(message);
    }
}
