package bg.senpai.anime.exceptionHandlers;

import bg.senpai.anime.exception.M3U8LinkNotFoundException;
import bg.senpai.anime.exception.SubtitlesNotFoundException;
import bg.senpai.anime.exception.VideoNotCreatedException;
import bg.senpai.common.dtos.AnimeM3U8LinkDto;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.concurrent.TimeoutException;


@RestControllerAdvice
public class AnimeExceptionHandlers {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false,
                            "statusCode", 404,
                            "message", ex.getMessage()
                        ));
    }

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<Map<String, Object>> handleFeignNotFound(FeignException.NotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false,
                        "statusCode", 404,
                        "message", ex.getMessage(),
                        "m3u8Link", ""
                ));
    }


    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, Object>> handleFeignException(FeignException ex){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "success", false,
                        "statusCode", 500,
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(M3U8LinkNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFeignException(M3U8LinkNotFoundException ex){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "success", false,
                        "statusCode", 500,
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(SubtitlesNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleSubtitlesNotFoundException(SubtitlesNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "success", false,
                        "statusCode", 500,
                        "message", ex.getMessage()
                ));
    }


    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<Map<String, Object>> handleTimeoutException(TimeoutException te){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "success", false,
                        "statusCode", 404,
                        "message", te.getMessage()
                ));
    }

    @ExceptionHandler(VideoNotCreatedException.class)
    public ResponseEntity<Map<String, Object>> handleVideoNotCreatedException(VideoNotCreatedException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "success", false,
                        "statusCode", 500,
                        "message", "interrupted or server error"
                ));
    }
}
