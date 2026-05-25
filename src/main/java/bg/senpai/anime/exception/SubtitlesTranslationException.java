package bg.senpai.anime.exception;

public class SubtitlesTranslationException extends RuntimeException {
    public SubtitlesTranslationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubtitlesTranslationException(String m){
        super(m);
    }
}
