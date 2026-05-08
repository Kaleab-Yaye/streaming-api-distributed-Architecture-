package com.adnakiwoch.platform.streaming_api.web.controller.weberr;

import com.adnakiwoch.platform.streaming_api.exception.resource.ResourceNotFoundException;
import com.adnakiwoch.platform.streaming_api.exception.user.DuplicateUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
  //

  @ExceptionHandler
  public ResponseEntity<ProblemDetail> handelResourceNotFoundException(
      ResourceNotFoundException resourceNotFoundException, HttpStatus status) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(status);
    // I am not setting the value for tittle here but I must during prod
    problemDetail.setDetail(resourceNotFoundException.getMessage());
    return ResponseEntity.status(status).body(problemDetail);
  }

  @ExceptionHandler({BadCredentialsException.class, InternalAuthenticationServiceException.class})
  public ResponseEntity<ProblemDetail> handelCredentialException(Exception credentialException) {
    if (credentialException instanceof InternalAuthenticationServiceException) {
      ProblemDetail problemDetail =
          ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIAL");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, credentialException.getMessage());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
  }

  @ExceptionHandler(DuplicateUserException.class)
  public ResponseEntity<ProblemDetail> handelDuplicateEmail(
      DuplicateUserException duplicateUserException) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatusCode.valueOf(409), duplicateUserException.getMessage());
    return ResponseEntity.status(HttpStatusCode.valueOf(409)).body(problemDetail);
  }
}
