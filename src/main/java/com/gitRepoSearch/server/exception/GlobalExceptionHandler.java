package com.gitRepoSearch.server.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ApiError> handleGitHubError(RestClientResponseException e) {
        int upstream = e.getStatusCode().value();
        HttpHeaders headers = e.getResponseHeaders();
        log.warn("GitHub returned {}", upstream, e);

        boolean rateLimited = upstream == 429
                || (upstream == 403 && headers != null
                && "0".equals(headers.getFirst("X-RateLimit-Remaining")));
        if (rateLimited) {
            ResponseEntity.BodyBuilder response = ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS);
            String retryAfter = headers == null ? null : headers.getFirst("Retry-After");
            if (retryAfter != null) {
                response.header("Retry-After", retryAfter);
            }
            return response.body(new ApiError("GitHub rate limit reached, try again later"));
        }
        if (upstream == 422) {
            return ResponseEntity.badRequest().body(new ApiError("GitHub rejected the search query"));
        }
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ApiError("GitHub request failed"));
    }

   @ExceptionHandler(ResourceAccessException.class)
   public ResponseEntity<ApiError> handleGithubUnreachable(ResourceAccessException e){
        log.warn("GitHub unreachable",e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiError("Github is currently unreachable"));

   }

   @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDatabaseError(DataAccessException e){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("Could not save the repositories"));
   }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ApiError> handleInvalidGithubResponse(RestClientException e) {
        log.warn("Invalid response from GitHub", e);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiError("GitHub returned an invalid response"));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> handleValidation(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(new ApiError(message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(new ApiError("Malformed request body or invalid value"));
    }

}
