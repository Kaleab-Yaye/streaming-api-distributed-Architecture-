/*

package com.adnakiwoch.platform.streaming_api.web.controller.weberr;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class TestException {

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    HttpMessageNotReadableException.class,
    MissingServletRequestParameterException.class,
    MethodArgumentTypeMismatchException.class,
    ConstraintViolationException.class
  })
  public ResponseEntity<String> handleBadRequestExceptions(Exception ex) {
    // 1. Log the exact exception here
    log.warn("400 Bad Request Caught: " + ex.getClass().getSimpleName());
    log.warn("Message: " + ex.getMessage());
    ex.printStackTrace();


    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body("Invalid request: " + ex.getMessage());
  }
}

 */
