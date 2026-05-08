package com.adnakiwoch.platform.streaming_api.exception.user;

public class DuplicateUserException extends RuntimeException {
  public DuplicateUserException(String message) {
    super(message, null, false, false);
  }
}
