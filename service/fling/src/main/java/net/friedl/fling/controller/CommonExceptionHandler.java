package net.friedl.fling.controller;

import java.io.IOException;
import java.io.UncheckedIOException;
import javax.persistence.EntityNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class CommonExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler({EntityNotFoundException.class, IOException.class, UncheckedIOException.class})
  public ResponseEntity<Object> handleNotFound(Exception ex, WebRequest request)
      throws Exception {

    HttpHeaders headers = new HttpHeaders();

    if (ex instanceof IOException) {
      log.error("IO Error", ex);
      HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
      return handleExceptionInternal(ex, null, headers, status, request);
    }

    if (ex instanceof UncheckedIOException) {
      log.error("IO Error", ex);
      HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
      return handleExceptionInternal(ex, null, headers, status, request);
    }

    if (ex instanceof EntityNotFoundException) {
      log.error("Entity not found", ex);
      HttpStatus status = HttpStatus.NOT_FOUND;
      return handleExceptionInternal(ex, null, headers, status, request);
    }

    return super.handleException(ex, request);
  }
}
