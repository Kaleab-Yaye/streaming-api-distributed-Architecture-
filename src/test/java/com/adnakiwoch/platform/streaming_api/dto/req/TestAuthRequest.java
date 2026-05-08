package com.adnakiwoch.platform.streaming_api.dto.req;

public class TestAuthRequest {
  public String email;

  public String password;

  public TestAuthRequest(String email, String password) {
    this.email = email;
    this.password = password;
  }
}
